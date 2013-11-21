/**
 * Business model for the BioSTIF application. It holds all the data referenced by the application*
 */


function BiostifModel(config) {
    
	/***************************************************************************
     * All FIELDS
     **************************************************************************/

	/**
	 * general
	 */
	var model = this;
	
	/** config info
	 * 
	 */
	this.config = config;
	this.map = null; //This field holds the map application, if exist to access to openLayers and other data
    /**
     * Models
     */
	this.exportServer = null;   
    this.originalDataSourceUrls = null;
    this.dataSourceUrls = null;
    this.modifiedUrls = null;
    this.olFormats = null;
    this.overlays = null;
    this.currentLang ="en";
    
    this.contentTypes = null;
    
    this.userLayers = null;
    this.workflowRunId = null;
    this.workspaces = null;
    
    this.username = null;
    this.credential = null;
    
    this.maskLayerName = null;
    this.maskUrl = null;
    this.maskId = null;
    
    this.task = null;

    this.history = []; //for undo/redo actions
    this.historyIndex = -1;
    this.selectionHistory; //selectionissues to protocol user actions
    this.maskLayers = null;
    this.lastSelectionWidget = null;
    
    this.pointsSelected = false;

    //vhz temporarly until the new StifDataSource works
    this.activeData = {
    		"index": null,
    		"data": null,
    		"label": null,
    		"datasets": null  //for GeoTemCo
    };
    
    Publisher.Subscribe('points_selected', function(){
		model.pointsSelected = true;
		});

	Publisher.Subscribe('deselect', function(){
		model.pointsSelected = false;
		});
    

};

BiostifModel.prototype.setOverlays = function (jsonArray) {
	this.overlays = jsonArray;
};

/**
 * reaction on selection on widgets, the selection is stored 
 * @param data: STIF dataset array containing selected items
 * @param widget: widget where selection occurred
 * @param object: list of selectionObjects to e stored or further used, e.g. for mask operations 
 */
BiostifModel.prototype.select = function (data, widget, selectionObjects) {
	
	
	this.lastSelectionWidget = widget;
		
	var idList = this.extractFeatureIds(data);
	
	if (idList.length == 0) return;

	if (this.history[this.historyIndex].selectedIds == null ||
			this.history[this.historyIndex].selectedIds.length != this.history[0].data.length) {

		this.history[this.historyIndex].selectedIds = new Array (this.history[0].data.length);
	}
	for (var i = 0; i < this.activeData.data.length; i++) {
		var index = this.activeData.index[i];
		this.history[this.historyIndex].selectedIds[index] = idList[i];
	}

	if (typeof(selectionObjects) != "undefined" && selectionObjects != null && selectionObjects.length > 0) {
		this.selectionHistory = selectionObjects;
		Publisher.Publish(BIOSTIF_EVENT_POLYGON_SELECTION, this.selectionHistory );
	} else {
		this.selectionHistory = null;
	}	
};

/**
 * Filter action on data, action in history
 * @param data
 */
BiostifModel.prototype.filter = function (data) {
	
	var idList = this.extractFeatureIds(data);
    if (idList.length == 0) return; //nothing to filter

	this.history[this.historyIndex].filteredIds = idList;

    this.history = this.history.slice(0,this.historyIndex+1);

    var newData = {
    	action: BIOSTIF_ACTION_FILTER,
    	data: data,
    	label: this.activeData.label,
    	filteredIds: null,
    	selectedIds: null,
    	index: this.activeData.index
    };

    this.history.push(newData);
    this.historyIndex++;
    Publisher.Publish(BIOSTIF_EVENT_DATA_CHANGED, null );	
};

/**
 * 
 * @returns the last action type
 */
BiostifModel.prototype.getLastAction = function() {
	return (this.historyIndex <= 0)?"":this.history[this.historyIndex].action;	
	
};

/**
 * @returns the next possible action type 
 */
BiostifModel.prototype.getNextAction = function() {
	return (this.historyIndex >= this.history.length - 1)?"":this.history[this.historyIndex+1].action;
};

/**
 * @param data: dataset array (STIF)
 * @return a list of lists (for each data source of object ids (which were previously been selected))
*
*/
BiostifModel.prototype.extractFeatureIds = function(data) {
	//go through a list of data objects, which are lists (one entry for each data source) and extract the ids
	
	var idList = new Array(data.length);

	//i is one of multiple selection (special for time selections)
	for (var i=0; i < data.length; i++) {
		if (!data[i].objects) continue;

		//j os one of the data sources
		for (var j=0; j<data[i].objects.length; j++) {
			var ids = new Array();
			for (var k=0; k<data[i].objects[j].length; k++) {
				ids.push(data[i].objects[j][k].index);
			}
			idList[j] = ids;
		};
	}
	return idList;
};

/**
 * creates GeoTemCo Datasets from the given parameters
   if the contentType is kml, the kml data will be loaded, else it is expected that the data is kml
 * @param labels: array with names for the datasets
 * @param contentTypes: array with data contenttypes
 * @param urls: array with data urls
 */
BiostifModel.prototype.setDatasets = function(labels, contentTypes, urls) {

	this.originalDataSourceUrls = new Array();
    this.dataSourceUrls = new Array();
    this.modifiedUrls = new Array();
    this.contentTypes = new Array();

    var applicationData = {
        	action: BIOSTIF_ACTION_INITIAL,
        	datasets: new Array(),
        	clusters: new Array(),
        	filteredIds: null,
        	selectedIds: null,
        	index: new Array()
        };
    var count = 0;
	for (var i = 0; i< urls.length; i++) {
		try {
			var dataset = getDatasetFromUrl (urls[i], contentTypes[i], labels[i]);

			//store the original urls and create the entries for modifications
			this.modifiedUrls.push(""); //for each
			this.originalDataSourceUrls.push(urls[i]);
			this.contentTypes.push(contentTypes[i]);

			// eventually put dataSource on local server this should be part of the workflow, not of BioVel !
			applicationData.datasets.push(dataset);
			applicationData.index.push(count);
            count++;

		} catch (e) {
			alert ($.i18n.prop('msg_alert_error_loading_dataset', labels[i], contentTypes[i], urls[i], e.message));
		}
	}
	 this.history.push(applicationData);
     this.historyIndex++;
     this.setActiveData();

};

BiostifModel.prototype.setWorkspaces = function(workspaces) {
	this.workspaces = workspaces;
};

BiostifModel.prototype.setWorkflowRunId = function(workflowRunId) {
	this.workflowRunId = workflowRunId;
};

BiostifModel.prototype.setUsername = function(username) {
	this.username = username;
};

BiostifModel.prototype.setMaskLayerName = function(maskLayerName) {
	this.maskLayerName = maskLayerName;
};

BiostifModel.prototype.setCredential = function(credential) {
	this.credential = credential;
};

BiostifModel.prototype.setActiveData = function() {
	this.activeData =  this.history[this.historyIndex];
};

BiostifModel.prototype.setTask = function(task) {
	this.task =  task;
};

/*
 * sets the layers for the overview window defined by the user and given as parameter layers
 * @layers should be a string array with the form layer_label|layer_name|visibility (true or false)
 */
BiostifModel.prototype.setUserLayers = function (layers) {
	if (layers && layers.length > 0 && layers[0] != null && layers[0].length > 0) {
		
		this.userLayers = new Array(layers.length);
		
		for (var i = 0; i < layers.length;i++) {
			
			//TODO rk change user layer sepatator to wsID
			var layer = layers[i];//.replace(":", "|");
			var layerprops = layer.split("|");
				if(layerprops.length < 2){
					layerprops = layer.split("@");
				}
			
			var layerWS = layerprops[1].split(":");
			if(this.workspaces.indexOf(layerWS[0].replace(" ","")) > -1){	
				var layerdef = {
						name: layerprops[0],
						url: this.config.USER_WMS, //"https://biovel.iais.fraunhofer.de/geoserver/wms?",
						layer: layerprops[1],
						transparency: true,
//					visibility: typeof(layerprops[2]) == "undefined"?false:eval(layerprops[2]) //XXX RK layervis
						visibility: typeof(layerprops[2]) == "undefined"?true:eval(layerprops[2])
				};
				this.userLayers[i] = layerdef;
			} else{
				alert("layer '"+ layerprops[1] + "' cannot be display - no permissions to access workspace " + layerWS[0]);
			}
		}
	}
};


//TODO DELETE when transfered to GeoTemCo, for STIF
/**
 * sets the data sources of the application
 * @param urls a list of urls
 */
BiostifModel.prototype.setDataUrls = function(labels, urls) {
    // TODO check if urls are well formated
    // if the data is external get it over tpp
	
    if (typeof (urls) != undefined && urls != null && urls.length > 0) {
    	
    	var newData = {
            	action: "BIOSTIF_ACTION_INITIAL",
            	data: new Array(),
            	label: new Array(),
            	filteredIds: null,
            	selectedIds: null,
            	index: new Array()
            };
    	
        this.originalDataSourceUrls = urls;
        
        this.initialBbox = new Array();
        this.dataSourceUrls = new Array();
        this.modifiedUrls = new Array();

        var label = "";
        
        for ( var i = 0; i <this.originalDataSourceUrls.length; i++) {
            var dataUrl = this.originalDataSourceUrls[i];
//            logg("dataUrl: " + dataUrl.length); // originalURL of csv or s.l.t.
            
            var data = null;

            if (!dataUrl || dataUrl.length == 0) {
//            	continue;
            	var data = "[]";
            } else {
	            dataUrl = dataUrl.fulltrim();
	            label = (labels[i]?labels[i]:"Datasource " + (i+1));
	            
	            var bbox = parseBBoxFromUrl(dataUrl);
	            this.initialBbox.push(bbox);
	            var data = null;
	            
	            if (!this.isUrlOnBioVeLServer(dataUrl)) {
	//            	logg("isUrlOnBioVeLServer(dataUrl): " + isUrlOnBioVeLServer(dataUrl));
	                // put dataSource on local server
	            	writeToFullscreen ($.i18n.prop('msg_fullscreen_post_dataset_to_server', i+1));
//	            	logg("this.username: " + this.username);
//	            	logg("this.workflowRunId: " + this.workflowRunId);
	            	var localUrl = uploadData("", dataUrl,null,null,this.username,this.workflowRunId);
	            	if (localUrl) {
	            		data = getDataFromUrl(localUrl, this.contentTypes[i], label);
	            		this.dataSourceUrls.push(localUrl);
	            	}
	            }
	            if (!data || data.length == 0) {
	                data = getDataFromUrl(dataUrl, this.contentTypes[i], label);
	                this.dataSourceUrls.push(dataUrl);
	            }
//	            logg("new data source with length : " + data.length);
            
            }
            
            if (this.dataSourceUrls.length < (i+1)) {
            	this.dataSourceUrls.push("");
            }
            
            //rk test out
            newData.data.push(data);
            newData.label.push(label);
            newData.index.push(i);
            //this.data.visibility.push((i<4?true:false));
            this.modifiedUrls.push("");
        }
        this.history.push(newData);
        this.historyIndex++;
        this.setActiveData();

    } else {
    	
    	var newData = {
            	action: "BIOSTIF_ACTION_INITIAL",
            	data: new Array(),
            	label: new Array(),
            	filteredIds: null,
            	selectedIds: null,
            	index: new Array()
            };

        this.initialBbox = new Array();
        this.dataSourceUrls = new Array();
        this.modifiedUrls = new Array();
        
        var data = "[]";
        
        logg(data);
                //rk test out
        newData.data.push(data);
//        newData.label.push(label);
        newData.index.push("");
        //this.data.visibility.push((i<4?true:false));
        this.modifiedUrls.push("");
        this.history.push(newData);
        this.historyIndex++;
        this.setActiveData();
    	
    }
};

BiostifModel.prototype.isUrlOnBioVeLServer = function (url) {
	var array = this.config.DATA_SERVERS.split(',');
	return isStartInArray (array, url);
};

BiostifModel.prototype.getCurrentDataset = function () {
	return this.history[this.historyIndex]||null;
};

BiostifModel.prototype.isCurrentDatasetEmpty = function () {
	return (this.getCurrentDataset().data.length == 0);
};

BiostifModel.prototype.undo = function() {
	if( this.historyIndex > 0 ){
		this.historyIndex--;
		Publisher.Publish(BIOSTIF_EVENT_HISTORY_CHANGED, null );			
	}
};

BiostifModel.prototype.redo = function() {
	if( this.historyIndex < this.history.length - 1 ){
		this.historyIndex++;
		Publisher.Publish(BIOSTIF_EVENT_HISTORY_CHANGED, null );
	}
};

BiostifModel.prototype.getLastHistoryStepForDatasource = function (index) {

	for (var i= this.historyIndex; i >= 0; i--) {
		var data = this.history[i];
		var position = $.inArray(index, data.index);
		if (position >= 0) {
			return {historyIndex: i, position: position};
		};
	}
	return {historyIndex:-1, position:-1};
};

BiostifModel.prototype.changeVisibility = function (visibleIndex) {
	var initialData = this.history[0];
//	var currentData = this.getCurrentDataset();

	var newData = {
	    	action: BIOSTIF_ACTION_VISIBILITY_CHANGED,
	    	data: new Array(),
	    	label: new Array(),
	    	filteredIds: new Array(),
	    	selectedIds: new Array(),
	    	index: new Array()
	    };

	var filteredIdsFound = false;
	var selectedIdsFound = false;
	for (var i = 0; i < initialData.data.length; i++) {
		if ($.inArray(i, visibleIndex) < 0) {
			continue;
		}
		var stepIndex = this.getLastHistoryStepForDatasource(i);
		if (stepIndex.historyIndex < 0 || stepIndex.position < 0) {
			continue;
		}
		newData.data.push( this.history[stepIndex.historyIndex].data[stepIndex.position]);
		newData.label.push( this.history[stepIndex.historyIndex].label[stepIndex.position]);
		newData.index.push( this.history[stepIndex.historyIndex].index[stepIndex.position]);

		if (this.history[stepIndex.historyIndex].filteredIds) {
			newData.filteredIds.push(this.history[stepIndex.historyIndex].filteredIds[stepIndex.position]);
			filteredIdsFound = true;
		} else {
			newData.filteredIds.push(this.getSelectedFeatureIds([this.history[stepIndex.historyIndex].data[stepIndex.position]])[0]);
		}
		if (this.history[stepIndex.historyIndex].selectedIds) {
			newData.selectedIds.push(this.history[stepIndex.historyIndex].selectedIds[stepIndex.position]);
	 		fselectedIdsFound = true;
		} else {
			newData.selectedIds.push(this.getSelectedFeatureIds([this.history[stepIndex.historyIndex].data[stepIndex.position]])[0]);
		}
	}
	
	log("!!!! ######  check select" + filteredIdsFound	+" "+	fselectedIdsFound);

	if (!selectedIdsFound) {
		newData.selectedIds = null;
	}
	if (!filteredIdsFound) {
		newData.filteredIds = null;
	}
	this.history = this.history.slice(0,this.historyIndex+1);

    this.history.push(newData);
    this.historyIndex++;
    
    Publisher.Publish(BIOSTIF_EVENT_DATA_CHANGED, null );    
};

/*
 * searches for the last selected or filtered or all ids
 */
BiostifModel.prototype.getLastActiveIds = function (index) {
	//search for the last step where the data was used
	var stepIndizes = this.getLastHistoryStepForDatasource(index);
	if (stepIndizes.historyIndex < 0) {
//		throw new Error ($.i18n.prop('msg_error_historydata_not_found',index));
		return null;
	}
	if (stepIndizes.position < 0) {
		return null;
	}
	var ids = null;

	var data = this.history[this.historyIndex];

	if (data.selectedIds != null) { //if selected take this ones
		ids = data.selectedIds[stepIndizes.position];
	} else if (data.filteredIds != null) {
		ids = data.filteredIds[stepIndizes.position];
	} else {
		ids = this.getFilteredFeatureIds ([data.data[stepIndizes.position]]);
	}
	return ids;
};

BiostifModel.prototype.createModifiedUrls = function() {
	
	var initdata = this.history[0];
	//var featureIdList = this.getLastActiveIds(i);
	var featureIdList = new Array(initdata.data.length);
	
	//XXX RK change index read
	for (var i = 0; i < featureIdList.length; i++) {
		featureIdList[i] = this.getLastActiveIds(i);
	}
	
	if (featureIdList.length != initdata.data.length) {
		throw new Error ($.i18n.prop('msg_error_uploading_modified_data_dataset', ". Current modified dataset list is not as long as the initial datasets"));
	}

    for ( var i = 0; i < this.originalDataSourceUrls.length; i++) {
    	
    	var suffix = "";
    	var featurelistLength = featureIdList[i].toString().split(",").length;
    	
    	logg("this.pointsSelected: " + this.pointsSelected);
    	logg("featurelistLength: " + featurelistLength);
    	logg("initdata.data[i].length: " + initdata.data[i].length);
    	
    	logg("originalDataSourceUrls: " + this.originalDataSourceUrls[i]);
    	logg("dataSourceUrls: " + this.dataSourceUrls[i]);
    	
    	
    	//XXX RK, point return check
    	if(!this.pointsSelected ){
    		this.modifiedUrls[i] = this.originalDataSourceUrls[i];
    		continue;
    	}
    	//mist: datasource == originaldatasource
//    	else if ( (featurelistLength == initdata.data[i].length) ){// && (featurelistLength == this.originalDataSourceUrls[i].length) ) {
//    		this.modifiedUrls[i] = this.dataSourceUrls[i];
//    		continue;
//    	}
    	
        var filterService = this.config.FILTER_DWC_SERVICE; //"\FilterDwC?source=";
        var sourceUrl = ((this.dataSourceUrls[i].length > 0) ? this.dataSourceUrls[i]
                    : this.originalDataSourceUrls[i]);
      
        //if (initdata.contenttype && this.contentTypes[i] && this.contentTypes[i].length > 0) {
        if ( this.contentTypes[i] && this.contentTypes[i].length > 0) {
            	if (this.contentTypes[i] == contentTypes.CSV) {
            		suffix = "csv";
            		filterService = this.config.FILTER_CSV_SERVICE; //"\FilterCsv?source=";
            	} else if (this.contentTypes[i] == contentTypes.DWC) {
            		filterService = this.config.FILTER_DWC_SERVICE; //"\FilterDwC?source=";
            	} else {
            		//filter function not supported
            		logg("Filter function for contenttype " + this.contentTypes[i] + " is not supported");            	    
            		continue;
            	};
         } else if (sourceUrl.indexOf(".txt")>0 || sourceUrl.indexOf(".csv")>0) {
        	 
        	 if (sourceUrl.indexOf(".csv")>0){
        		 suffix = "csv";
        	 } else {
        		 suffix = "txt";
        	 }
        	 
             filterService = this.config.FILTER_CSV_SERVICE; //"\FilterCsv?source=";
         }

         filterService += encodeURIComponent(sourceUrl);
         // filterService+= "&target="+encodeURI(newUrl);
         //alert ("Call Filterservice " + filterService);
         var content = featureIdList[i].join(",");
         logg("BioSTI send suffix: " + suffix);
         
         try {
             var client = restClient(filterService, "POST", [], content);
             if (client.status == 200) {
                 var result = client.responseText;
                 var newLocation = uploadData(result, "",suffix,null,this.username,this.workflowRunId);
                 if (!newLocation) {
                     alert($.i18n.prop('msg_error_uploading_modified_data_dataset', "\nService didn't provide a new location"));
                 }
                 this.modifiedUrls[i] = newLocation;
             } else {
                 throw new Error($.i18n.prop('msg_error_rest_failed', filterService,client.status));
             };
         } catch (e) {
             throw new Error ($.i18n.prop('msg_error_uploading_modified_data_dataset', e.message));
         } ;
         logg("modified URLS created " + this.modifiedUrls[i]);           
    };
};

BiostifModel.prototype.createMaskInformations = function () {
	
	
		if(document.getElementById("MaskChkBox") && document.getElementById("MaskChkBox").checked){
			this.maskUrl = this.maskLayers[0].wmsurl.substring(0, this.maskLayers[0].wmsurl.indexOf("?", 0)+1);
			this.maskId = this.maskLayers[0].layername;
			return true;
		}else if(document.getElementById("MaskChkBox") && !document.getElementById("MaskChkBox").checked){
			logg("The Maskcheckbox was not set or created");
			this.maskUrl = "mask is not available or created";
			this.maskId = "mask is not available or created";
			return false;
		} else{
			return true;
		};
	
};

BiostifModel.prototype.addMaskLayer = function (layerdef) {
	if (this.maskLayers == null) {
		this.maskLayers = [];
	}
	this.maskLayers.push(layerdef);
	
};

BiostifModel.prototype.checkMaskCreation = function () {
	
	if(this.task == "createmask" && (this.maskLayers == null || this.maskLayers.length == 0)){
	
		var dialogDiv = document.createElement('div');
		var divIdName = 'dialogDiv';
		dialogDiv.style.zIndex = "998";
		dialogDiv.setAttribute('id',divIdName);
		dialogDiv.style.width = "100%";
		dialogDiv.style.height = "100%";
	//	dialogDiv.style.left = "40%";
	//	dialogDiv.style.top = "40%";
		dialogDiv.style.opacity = "0.3";
		dialogDiv.style.position = "absolute";
		dialogDiv.style.background = "grey";
	//	dialogDiv.style.border = "4px solid #000";
	//	dialogDiv.innerHTML = 'Test Div';
		document.body.appendChild(dialogDiv);
	
		var innerDiv = document.createElement('div');
		innerDiv.setAttribute('id','innerDiv');
		innerDiv.style.zIndex = "999";
		innerDiv.style.width = "400px";
		innerDiv.style.height = "160px";
		innerDiv.style.left = "40%";
		innerDiv.style.top = "42%";
		innerDiv.style.position = "absolute";
		innerDiv.style.border = "4px solid #000";
		innerDiv.style.background = "#5C85AD";
	//	innerDiv.style.opacity = "0.6";
		
	    innerDiv.appendChild(document.createElement('br'));
	    
		var label1 = document.createElement('label');
		//  this.labelSelect.setAttribute("style", "position:absolute; top: 5px");
		label1.style.color = "#D9ECFF";
		label1.style.fontSize = "15px";
		label1.style.marginLeft = "20px";
		label1.innerHTML = "No mask was created. You need to either create a mask and ";
		innerDiv.appendChild(label1);
		
		innerDiv.appendChild(document.createElement('br'));
		
		var label2 = document.createElement('label');
		label2.style.color = "#D9ECFF";
		label2.style.fontSize = "15px";
		label2.style.marginLeft = "20px";
		label2.innerHTML = "continue the workflow or abort the workflow execution.";
		innerDiv.appendChild(label2);
	    
	    innerDiv.appendChild(document.createElement('br'));
		innerDiv.appendChild(document.createElement('br'));
	
		var label3 = document.createElement('label');
		label3.style.color = "#D9ECFF";
		label3.style.fontSize = "15px";
		label3.style.marginLeft = "20px";
		label3.innerHTML = "For more information about mask creation click <a href='https://wiki.biovel.eu/display/doc/Map+Widget#MapWidget-CreatingRastermask' target='_blank'>here</a>";
		innerDiv.appendChild(label3);
		
		innerDiv.appendChild(document.createElement('br'));
		
		var label4 = document.createElement('label');
		label4.style.color = "#D9ECFF";
		label4.style.fontSize = "15px";
		label4.style.marginLeft = "20px";
		label4.innerHTML = "or use the 'Help' link on the top right corner.";
		innerDiv.appendChild(label4);
		
	    innerDiv.appendChild(document.createElement('br'));
	    innerDiv.appendChild(document.createElement('br'));
	    
		var buttonOK = document.createElement('button');
		buttonOK.appendChild(document.createTextNode('OK'));
		buttonOK.style.marginLeft = "20px";
	//	buttonOK.style.marginTop = "200";

		buttonOK.onclick = function() {
			var closediv = document.getElementById("dialogDiv"); 
			document.body.removeChild(closediv);
			
			var inDiv = document.getElementById("innerDiv");
			document.body.removeChild(inDiv);
	    };
	    
		innerDiv.appendChild(buttonOK);
		document.body.appendChild(innerDiv);
		
		return false;
	} else {
		return true;
	}
	
};

/**
 * serializes features into a particular format
 * @type the formattype into which to serialize the features. Shuld be an element from formatTypes
 * @features a feature collection to be serialized
 * @return string a serialized string
 */
BiostifModel.prototype.serializeSelectedFeatures = function (formatType) {

	var features =  this.selectionHistory;
	logg("features: " + features);
    var str = this.olFormats[formatType].write(features);
    // not a good idea in general, just for this demo
    //str = str.replace(/,/g, ', ');
    return str;
};

BiostifModel.prototype.getApplicationLink = function() {
	if (this.exportServer == null) {
		throw new Error ($.i18n.prop('msg_error_map_link_server_missing'));
	};
	var url = this.exportServer + STATICFILENAME;
//	logg("is leer?: " + this.exportServer==""); 
//	logg("this.exportServer:" + this.exportServer + "- model app url: " + url);
	var separator = (url.indexOf("?") > 0)?"&":"?";
	url+=separator;

	var sourceUrls = (this.dataSourceUrls.length > 0)?this.dataSourceUrls.toString():this.originalDataSourceUrls.toString();
	var contentTypes = this.contentTypes.toString();
	var layers = "";
	if ((this.userLayers != null && this.userLayers.length > 0)||(this.maskLayers != null && this.maskLayers.length > 0)) {
		var layerArray = [];
		
		if (this.userLayers != null && this.userLayers.length > 0){		
			for (var i = 0; i < this.userLayers.length; i++) {
				var layerdef = this.userLayers[i];
				var layerstring = layerdef.name + "|" + layerdef.layer + "|" + layerdef.visibility;
				layerArray.push(layerstring);
			}
		}
		if (this.maskLayers != null && this.maskLayers.length > 0){		
//			for (var i = 0; i < this.model.maskLayers.length; i++) {
				var layerdef = this.maskLayers[0].layername;//this.userLayers[i];
				var layerstring = layerdef + "|" + layerdef + "|" + true;
				layerArray.push(layerstring);
//			}
		}
		
		layers = layerArray.toString();
	}
	var dataLabels = this.history[0].label.toString();

	url = url + "debug=true&url="+sourceUrls+"&contenttype="+contentTypes + "&label=" + dataLabels + "&layers="+layers;
	
	//add the current  viewport for the map if available
	if (this.map != null) {
		var bounds = this.map.openlayersMap.getExtent();
		var projectionBounds = bounds.transform(this.map.openlayersMap.projection, this.map.openlayersMap.displayProjection);
		var bbox = bounds.toArray();
		url= url + "&bbox=" + bbox;
	}

	return url;
};

// sends the map object a relocation according to the given bbox
BiostifModel.prototype.zoomToBBOX = function (bbox) {
	
	if (this.map != null) {
		var bboxArray = bbox.split(",");
		if (bboxArray.length < 4) {
//			throw new Error ($.i18n.prop('msg_error_wrong_bbox',bbox));
		}
		try {
			var bounds = new OpenLayers.Bounds(bboxArray[0], bboxArray[1], bboxArray[2], bboxArray[3]);
			var projectionBounds = bounds.transform(application.spaceWidget.map.openlayersMap.displayProjection, application.spaceWidget.map.openlayersMap.projection);
			this.map.openlayersMap.zoomToExtent(projectionBounds, true);
			this.map.drawObjectLayer(false);
		} catch (bboxerror) {
//			throw new Error ($.i18n.prop('msg_error_wrong_bbox',bbox));
		};		
	};
};

BiostifModel.prototype.getMap = function () {
	return this.map;
};

BiostifModel.prototype.getConfig = function () {
	return this.config;
};

BiostifModel.prototype.setMap = function (mapObject) {
	this.map = mapObject;
	logg ("Map Object set at BiostifModel");
};
/**
 * returns a list of lists (for each data source of object ids (which were previously been selected
*
*/

BiostifModel.prototype.getSelectedFeatureIds = function(data) {
	//go thorugh a list of data objects, which are lists (one entry for each data source) and extract the ids
	var idList = new Array(data.length);

	//i is one of multiple selection (special for time selections)
	for (var i=0; i < data.length; i++) {
		if (!data[i].objects) continue;

		//j os one of the data sources
		for (var j=0; j<data[i].objects.length; j++) {
			var ids = new Array();
			for (var k=0; k<data[i].objects[j].length; k++) {
				ids.push(data[i].objects[j][k].index);
			}
			idList[j] = ids;
		};
	}
	return idList;
};

/**
 * returns a list of lists (for each data source of object ids) which were previously been filtered
 *
 */

BiostifModel.prototype.getFilteredFeatureIds = function(data) {

	//go thorugh a list of data objects, which are lists (one entry for each data source) and extract the ids
	var idList = new Array(data.length);
	

	//j is one of the data sources
	for (var j=0; j<data.length; j++) {
		var ids = new Array();
		//get the id of each data object
		if(typeof(data[j]) != "undefined"){
			for (var k=0; k<data[j].length; k++) {
				ids.push(data[j][k].index);
			}
			//get the real index of the data source
			idList[j] = ids;
		} else {
			idList[j] = "";
		}
	}

	return idList;
};

