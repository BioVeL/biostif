/**
 * Main controller for the BioSTIF application. It holds the data references of the application*
 */


function Application(model) {
    /***************************************************************************
     * All FIELDS
     **************************************************************************/    
    this.isClosed = false;
    this.model = model;    

    
    /**
     * View Controler
     */
    this.stifControl = null;
    this.toolbarWidget = null;
    this.layerWidget = null;   
    this.layerContainer = null;
   
    this.init();

};

Application.prototype.init = function() {
    /***************************************************************************
     * /** User Interaction MODEL ** /
     **************************************************************************/

    var application = this;
       
    this.stifControl = new StifControl (this.model);
    
//    Publisher.Subscribe(BIOSTIF_EVENT_STIF_READY, function() {    	
//    	if (typeof (application.model.overlays) != "undefined" && application.model.overlays) {
//    		writeToFullscreen( $.i18n.prop('msg_fullscreen_uploading_overlays'));
//    		application.stifControl.setOverlays(application.model.overlays);    		
//        }
//         application.configureLayerWidget();
//    });
    
    Publisher.Subscribe(BIOSTIF_EVENT_DATA_CHANGED , function() {
    	application.reloadAction();
    });
    Publisher.Subscribe(BIOSTIF_EVENT_HISTORY_CHANGED, function() {
    	application.updateStatus();
    });
    $("#user_retry").attr("disabled", "true");
};

Application.prototype.display = function () {	
	this.stifControl.display();	 
};

Application.prototype.getModel = function () {	
	return this.model;	 
};

Application.prototype.resizeWidgets = function () {
	this.stifControl.resizeWidgets();
};


Application.prototype.configureLayerWidget = function() {
	
//	if (this.model.userLayers || (! this.model.isCurrentDatasetEmpty())) {
    	//this.layerWidget = new OverlayWidget(document.getElementById(this.layerContainer), this.spaceWidget.map, this.history[this.historyIndex], this.changeVisibility, this.undo, this.redo, this.userLayers, this.exportMapControl, this.getApplicationLink, this.createMask);
		this.layerWidget = new OverlayWidget(this.model, document.getElementById(this.layerContainer), this.createMask);
//    }
};

/** 
 * for GeoTemco //TODO
 * @param container
 */
Application.prototype.configureToolbar = function(container) {
	this.toolbarWidget = new ToolbarWidget(container, this);

};

/**
 * called after a undo/redo action to actualize the toolbar and the visibility of datasets
 */
Application.prototype.showHistoryStep = function(){	
	this.reloadDataInWidgets();
	logg("Show application step: " + this.model.historyIndex + " of " + this.model.history.length);
	
	var indizes = this.model.getCurrentDataset().index;
	this.layerWidget.changeVisibility(indizes);
};

Application.prototype.reloadDataInWidgets = function() {

	application.model.setActiveData();		
	
	application.stifControl.displaySTIF();		

};


Application.prototype.exitApplicationOK = function() {
	
	var close = true;
	
   	var maskOk = this.model.checkMaskCreation();
	
	if(!maskOk){   
		close = false;
		return;
	}
	
	if(this.model.originalDataSourceUrls == null){
		
		try {
		    var maskEnabled = this.model.createMaskInformations();
		    
			var openMaskDialog = function(){
			
				var r=confirm('The Mask checkbox is diabled. Mask data can not be sent \n\n' +
						'\"OK\" for continue without the data (the Workflow run will be canceled)\n' +
						'\"Cancel\" to return to the Application');
				
				if (r==false){
					close = false;
				}			
			};
			
			if (maskEnabled == false){
				openMaskDialog();
				this.model.createModifiedUrls();
			};
			
			if(close == false){
			    this.isClosed = close;
			}else{
				this.isClosed = close;
			    if (typeof(reply) != "undefined") {
				    	reply("No originalDataSourceUrls", " No dataSourceUrls", " No modifiedUrls", "wcs>"+this.model.maskUrl+">"+this.model.maskId);
			    };
			}
		} catch (error) {
			alert ($.i18n.prop('msg_alert_error_closing', error.message));
		};
		
		
	} else {
	
		try {
		    this.model.createModifiedUrls();
		    var maskEnabled = this.model.createMaskInformations();
		    
			var openMaskDialog = function(){
			
				var r=confirm('The Mask checkbox is diabled. Mask data can not be sent \n\n' +
						'\"OK\" for continue without the data (the Workflow run will be canceled)\n' +
						'\"Cancel\" to return to the Application');
				
				if (r==false){
					close = false;
				}			
			};
			
			if (maskEnabled == false){
				openMaskDialog();
				this.model.createModifiedUrls();
			};
			
			if(close == false){
			    this.isClosed = close;
			}else{
				this.isClosed = close;
			    if (typeof(reply) != "undefined") {
	
			    	if (this.model.originalDataSourceUrls != null){
	//					logg("reply: "+this.model.originalDataSourceUrls.join(","), this.model.dataSourceUrls.join(","), this.model.modifiedUrls.join(","), this.model.maskUrl+"|"+this.model.maskId);
				    	reply(this.model.originalDataSourceUrls.join(","), this.model.dataSourceUrls.join(","), this.model.modifiedUrls.join(","), "wcs>"+this.model.maskUrl+">"+this.model.maskId);
						
					} else {
	//					logg("reply: "+"No originalDataSourceUrls " + this.model.maskUrl+"|"+this.model.maskId);
				    	reply("No originalDataSourceUrls", " No dataSourceUrls", " No modifiedUrls", "wcs>"+this.model.maskUrl+">"+this.model.maskId);
					};
			    };
			}
		} catch (error) {
			alert ($.i18n.prop('msg_alert_error_closing', error.message));
		};
	}
};

Application.prototype.exitApplicationCancel = function() {
	try {
	    this.isClosed = true;
	    if (typeof(cancel) != "undefined") {
	    	cancel();
	    }
	} catch (error) {
		alert ($.i18n.prop('msg_alert_error_closing', error.message));
	};
};

Application.prototype.exitApplication = function(actiontype) {
	
	logg("exitApplication");
	
    this.status = actiontype;
    var content;
    var result;
    
    logg("actiontype: " + actiontype);
    
    if (actiontype == actionType.OK) {
    	
    	
   		this.model.createModifiedUrls();
   		this.model.createMaskInformations();

    }
    reply();    
    this.isClosed = true;

    document.writeln("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
    document.writeln("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
    document.writeln("<title>BioSTIF results</title>");
    document.writeln("<link rel=\"shortcut icon\" href=\"img/favicon.ico\" type=\"image/x-icon\" />");
    document.writeln("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/biovel.css\" />");
    document.writeln("</head><body>");
    document.writeln("<p><b>BioSTIF was closed with action " + actiontype + "<\/b></p>");
    document.writeln("<p>The following content was sent to Taverna:<br>" + content + "</p>");
    document.writeln("</body></html>");

};

//Application.prototype.configureSTIF = function(stifGui, mapContainerDiv, plotContainerDiv, tableContainerDiv, mapWidth,
 //       mapHeight) {


 Application.prototype.configureWidgets = function(stifGui, mapContainerDiv, plotContainerDiv, tableContainerDiv, mapWidth, mapHeight) {  
	 
	 this.stifControl.configureWidgets (stifGui, mapContainerDiv, plotContainerDiv, tableContainerDiv, mapWidth, mapHeight);
	 this.configureLayerWidget();
    
};

Application.prototype.createMask = function () {
	
    var executeMaskRequest = function(){
    	
        dialogDiv.style.display = "none";
        innerDiv.style.display = "none";
        
        application.model.maskLayers = [];
        var serializedFeatures = application.model.serializeSelectedFeatures(formatTypes.WKT);
        
        logg("serializedFeatures: " + serializedFeatures);
        
        var workspaceid = "";//application.model.username;
        if(application.model.username || application.model.username.length > 0){
        	workspaceid = application.model.username;
        } else {
        	workspaceid = "biovel_temp";
        }
        
        
        var workflowRunId = "";
        if(application.model.workflowRunId || application.model.workflowRunId.length > 0){
        	workflowRunId = application.model.workflowRunId;
        } else {
        	workflowRunId = "maskLayers";
        }
        
//        logg("mask workspaceID/username: " + workspaceid);
//        logg("mast workflowRunId: " + workflowRunId);
        
//        hier create auth infos!!!!
          
        var maskService = application.model.config.MASK_SERVICE+"&workspaceid="+workspaceid+"&workflowid="+workflowRunId+"&cellsize="+cellsize+"&layername="+maskLayerName; //"http://localhost:8080/shim/rest/computation/raster/mask?format=wkt&cellsize=180&workspaceid=biovel_temp";
        
    	var response = null;
    	try {
    		writeToFullscreenHeader($.i18n.prop('msg_fullscreenheader_access_service',"Create Mask"));
    		writeToFullscreen($.i18n.prop('msg_fullscreen_mask_service'));
    		
    		var client = restClient(maskService, "POST", "", serializedFeatures);
    		response = client.responseText;
    		
    		if (client.status != 200) {
    			stopFullscreen();
    			alert($.i18n.prop('msg_alert_error_mask', client.status));
    			logg ($.i18n.prop('msg_alert_error_mask', response));
    		} else {
    			stopFullscreen();
    			//alert ($.i18n.prop('msg_alert_mask',''));
    			logg($.i18n.prop('msg_alert_mask',response));
    		};
    	} catch (error) {
    		stopFullscreen();
    		alert ($.i18n.prop('msg_alert_error_mask',error.message));
    	}
    		var layerRecord = parseJSON (response, ['wcsgridurl', 'wmsurl', 'layername']);
    		application.model.addMaskLayer(layerRecord);
    		application.layerWidget.updateMaskLayerInOverlay();
    };
	
	
    var cellsize = "";
	
	var maskLayerName = "";
	if(application.model.maskLayerName == null){
		application.model.maskLayerName = "";
	}
    if(application.model.maskLayerName.length > 0){
    	maskLayerName = application.model.username;
    } else {
    	maskLayerName = "";
    }
    
	
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
	innerDiv.style.zIndex = "999";
	innerDiv.style.width = "350px";
	innerDiv.style.height = "200px";
	innerDiv.style.left = "40%";
	innerDiv.style.top = "40%";
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
	label1.innerHTML = "Enter your layername ";
	innerDiv.appendChild(label1);
	
	innerDiv.appendChild(document.createElement('br'));
    
	var inputLayerName = document.createElement("input");
	inputLayerName.style.marginLeft = "20px";
	inputLayerName.setAttribute("id","labelField");
	inputLayerName.setAttribute("type","text");
	inputLayerName.setAttribute("name","labelField");
	inputLayerName.setAttribute("size","40px");
//    labelField.style.opacity = "1.0";
    innerDiv.appendChild(inputLayerName);
    
    innerDiv.appendChild(document.createElement('br'));
	innerDiv.appendChild(document.createElement('br'));

	var label2 = document.createElement('label');
	// label2belSelect.setAttribute("style", "position:absollabel2: 5px");
	label2.style.color = "#D9ECFF";
	label2.style.fontSize = "15px";
	label2.style.marginLeft = "20px";
	label2.innerHTML = "Enter the raster resolution in arcseconds";
	innerDiv.appendChild(label2);
	
	innerDiv.appendChild(document.createElement('br'));
	
	var label3 = document.createElement('label');
	// label2belSelect.setAttribute("style", "position:absollabel2: 5px");
	label3.style.color = "#D9ECFF";
	label3.style.fontSize = "15px";
	label3.style.marginLeft = "20px";
	label3.innerHTML = "(default value is 180 arcseconds) ";
	innerDiv.appendChild(label3);
	
	innerDiv.appendChild(document.createElement('br'));
    
	var inputResolution = document.createElement("input");
	inputResolution.style.marginLeft = "20px";
	inputResolution.setAttribute("id","labelField");
	inputResolution.setAttribute("type","text");
	inputResolution.setAttribute("name","labelField");
	inputResolution.setAttribute("size","40px");
//    labelField.style.opacity = "1.0";
	innerDiv.appendChild(inputResolution);
    
    innerDiv.appendChild(document.createElement('br'));
    innerDiv.appendChild(document.createElement('br'));
    
	var buttonOK = document.createElement('button');
	buttonOK.appendChild(document.createTextNode('OK'));
	buttonOK.style.marginLeft = "20px";
//	buttonOK.style.marginTop = "200";
	buttonOK.onclick = function() {
        
		
        if(inputLayerName.value.length > 0){
        	
        	var userLayerName = inputLayerName.value.replace(/ /g, '_'); 
        	
            //var strReg = "^([a-zA-Z0-9_\-])+$";
            //#var regex = new RegExp(strReg);
            
            var regex = new RegExp("^[a-zA-Z0-9\-_]*$");
            
            logg("maskLayerName: " + userLayerName + " - match: " + regex.test(userLayerName));

            if (!(regex.test(userLayerName))){
            	alert("Please use only letters, numbers, hyphen, spaces or underline on labels!");
            	userLayerName = "";
            	return;
            };
        	
        	
        	
        	if(maskLayerName.length > 0){
//        	maskLayerName = inputLayerName.value.replace(/ /g, '_').replace(/[`~!@#$%^&*()|+\=?;:'",.<>\{\}\[\]\\\/]/gi, '')+"_"+maskLayerName;
        	maskLayerName = userLayerName+"_"+maskLayerName;

        	} else {
//        		maskLayerName = inputLayerName.value.replace(/ /g, '_').replace(/[`~!@#$%^&*()|+\=?;:'",.<>\{\}\[\]\\\/]/gi, '');
        		maskLayerName = userLayerName;
        	}
		}
       
        
//        var replaceUmlaute = function(curText) {
//        	 if (typeof curText != "number" && typeof curText != "string" ) {
////        	  alert("Sie versuchen Text auszugeben, der kein Text ist sondern: "+typeof curText);
//        	  return "";
//        	 }
//        	 if (curText == "") return "";
//        	 if (typeof curText == "number") curText = String(curText);
//        	 var result = curText;
//        	 result=result.replace(/\u00fc/g, "ue;");
//        	 result=result.replace(/\u00f6/g, "oe");
//        	 result=result.replace(/\u00e4/g, "ae");
//        	 result=result.replace(/\u00dc/g, "UE");
//        	 result=result.replace(/\u00d6/g, "OE");
//        	 result=result.replace(/\u00c4/g, "AE");
//        	 result=result.replace(/\u00df/g, "ss");
////        	 result=result.replace(/ü/g, "&uuml;");
////        	 result=result.replace(/ö/g, "&ouml;");
////        	 result=result.replace(/ä/g, "&auml;");
////        	 result=result.replace(/Ü/g, "&Uuml;");
////        	 result=result.replace(/Ö/g, "&Ouml;");
////        	 result=result.replace(/Ä/g, "&Auml;");
////        	 result=result.replace(/ß/g, "&szlig;");
//        	 return result;
//        	};
//
//        	maskLayerName = replaceUmlaute(maskLayerName);
        
        if(inputResolution.value.length == 0){
        	cellsize = 180;
        	executeMaskRequest();
        } else {
        	
        	cellsize = parseInt(inputResolution.value);
        	
        	if(isNaN(cellsize)){
        		alert("Raster resolution ist not a Number");
        	} else {
        		executeMaskRequest();
        	}
        }
    };
    
	innerDiv.appendChild(buttonOK);
	document.body.appendChild(innerDiv);

};

//for geotemco, if application is the parent of the filter
Application.prototype.filtering = function () {
	if (application.model.lastSelectionWidget != null) {
		application.model.lastSelectionWidget.widget.filtering();
	}
};

//for geotemco, if application is the parent of the filter
Application.prototype.inverseFiltering = function () {
	if (application.model.lastSelectionWidget != null) {
		application.model.lastSelectionWidget.widget.inverseFiltering();
	}
};

//for geotemco, if application is the parent of the filter
Application.prototype.deselection = function () {
	if (application.model.lastSelectionWidget != null) {
		application.model.lastSelectionWidget.widget.deselection();
	}
};

Application.prototype.reloadAction = function () { 
	this.layerWidget.updateHistoryButtons();
	this.reloadDataInWidgets();
};

/**
 * called after a history change event happened
 */
Application.prototype.updateStatus = function () {
	this.layerWidget.updateHistoryButtons();
	application.showHistoryStep();
};

