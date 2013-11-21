/**
 * widget to list all available user layers

 * @container: div to which the widget will be added
 * @map: stif map variable which has the control of the layers
 * @layers: layers to be added to the widget and , if visibility is on, to the map
 * the layer order in the map can be reconfigured, if the user changes the order via drag and drop
 */
    	////previous call: new OverlayWidget(document.getElementById(this.layerContainer), this.spaceWidget.map, this.history[this.historyIndex], this.changeVisibility, this.undo, this.redo, this.userLayers, this.exportMapControl, this.getApplicationLink, this.createMask);
//function                      OverlayWidget(container,                                   map,                   datasources, callbackOnDataVisibilityChange, undoCallback, redoCallback, userLayers, exportMapControl, initialLinkCallback, maskCallback){
function OverlayWidget(model, container, maskCallback){
	
	this.model = model;
	this.container = container;
	this.map = model.map;	
	this.legendurl = model.config.USER_WMS + model.config.RASTER_LEGEND_PARAMS;//"https://biovel.iais.fraunhofer.de/geoserver/wms?service=WMS&version=1.1.0&REQUEST=GetLegendGraphic&VERSION=1.0.0&FORMAT=image/png&WIDTH=25&height=25&LEGEND_OPTIONS=forceRule:True;borderColor:000000;border:true;fontColor:000000;fontSize:18";

	var widget = this;

	var toolbarTable = document.createElement("table");
	toolbarTable.setAttribute('class','absoluteToolbar ddbToolbar');
	toolbarTable.setAttribute('id','absoluteToolbar ddbToolbar');
	this.container.appendChild(toolbarTable);

	//for the undo/redo buttons
	var column = document.createElement("tr");
	toolbarTable.appendChild(column);

	var header = document.createElement("td");
	header.innerHTML = $.i18n.prop('msg_html_toolbar_title');
	column.appendChild(header);

	column = document.createElement("tr");
	toolbarTable.appendChild(column);
	var content = document.createElement("td");
	column.appendChild(content);

	this.undo = document.createElement('div');
	this.undo.setAttribute('class','historyButton undoDisabled');
	$(this.undo).attr("title", $.i18n.prop('msg_tooltip_undo_action',''));
	content.appendChild(this.undo);
	this.undo.onclick = function(){
		widget.model.undo();
	};


	this.redo = document.createElement('div');
	this.redo.setAttribute('class','historyButton redoDisabled');
	$(this.redo).attr("title", $.i18n.prop('msg_tooltip_redo_action',''));
	content.appendChild(this.redo);
	this.redo.onclick = function(){
		widget.model.redo();

	};

	//link/export Button
	this.linkButton = document.createElement('div');
	this.linkButton.setAttribute('class','historyButton linkButton');
	$(this.linkButton).attr("title", $.i18n.prop('msg_tooltip_save_link'));
	content.appendChild(this.linkButton);

	//textfield with url:
	var linkdialog_div = "<div id='linkdialog'></div>";

	var positiontop = $(this.linkButton).position().top;
	var positionleft = 263; //from main

	var $linkdialog = $(linkdialog_div)
				//.html(linkhtml)
				.dialog({
						autoOpen: false,
						title: $.i18n.prop('msg_tooltip_save_link'),
						position: [positiontop,positionleft]
					});


	this.linkButton.onclick = function(){
		//logg("overlay widget.model.getApplicationLink():" + widget.model.getApplicationLink());
		//var html = "<div><p>To get the link to the application, save the following url: <p><textarea name='link' cols='40' rows='2'>" + initialLinkCallback() + "</textarea></p></div>";
		var html = $.i18n.prop('msg_html_maplink_header',widget.model.getApplicationLink());
		logg("widget.model.getApplicationLink(): " + widget.model.getApplicationLink());
		$('#linkdialog').html(html);
		$('#linkdialog').dialog('open');		

	};
	
	//filterBar
	if(this.model.getCurrentDataset() != null){
		logg("filterbarAction 1 ??");
		this.filterBar = new FilterBar(this.map.core,this.model.getCurrentDataset().data, content);
	} else{
		logg("filterbarAction 2 ??");
		this.filterBar = new FilterBar(this.map.core,"[]", content);
	}

//vhz add the mask function to the application, when the filterbar is there

	this.mask = document.createElement('div');
	this.mask.setAttribute('class','smallButton mask');
	this.mask.setAttribute('id','smallButton mask');
	$(this.mask).attr("title", $.i18n.prop('msg_tooltip_create_mask'));
	this.filterBar.toolbar.appendChild(this.mask);
	this.showSelection(false);
	this.mask.onclick = function(){
		maskCallback();
	};
	Publisher.Subscribe(BIOSTIF_EVENT_POLYGON_SELECTION, function(polygones){
       widget.showSelection(true);
    });
	//Publisher.Subscribe('reset',function(element){
	Publisher.Subscribe(BIOSTIF_EVENT_RESET_SELECTION, function(element){
		widget.showSelection(false);
    });

	//for the data sources
	var datasources = this.model.getCurrentDataset();
	if (datasources && datasources.label && datasources.label.length > 0) {
		column = document.createElement("tr");
		toolbarTable.appendChild(column);

		header = document.createElement("td");
		header.innerHTML = $.i18n.prop('msg_html_datasource_title');
		column.appendChild(header);

		for( var i = 0; i <datasources.label.length; i++ ){
			var label = datasources.label[i];
			var elements = datasources.data[i].length;
//			logg("datasources: " + datasources.data[i]);
//			logg("OVerlay datasource :" + i + " label: " + label +" elements: " + elements);
			
			if(elements > 2){

				column = document.createElement("tr");
				toolbarTable.appendChild(column);
				content = document.createElement("td");
				column.appendChild(content);
	
				//TODO change for a function together with geotemco
				var c = STIStatic.colors[i];
	            var color = 'rgb(' + c.r0 + ',' + c.g0 + ',' + c.b0 + ')';
	            //XXX rk change span border to black
//	            var border = '2px solid rgb(' + c.r1 + ',' + c.g1 + ',' + c.b1 + ')';
	            var border = '2px solid rgb(0,0,0)';
	
				//var checkDiv = $("<li class='ui-state-default'/>").appendTo(this.content);
				//$('<span class="ui-icon ui-icon-arrowthick-2-n-s"></span>').appendTo(checkDiv);
				var checked = (i<4?" checked='checked'":"");
	
				var check = $("<input class='datasourceCheckbox' type='checkbox' datasourceId='" +
							+i+"' title='" + $.i18n.prop('msg_tooltip_datasource_checkbox') +"' " + checked + "'>"+label + "&nbsp;&nbsp;</input>").appendTo(content);
	
				content = document.createElement("td");
				column.appendChild(content);
				var symbol = $("<span id='span_datasourceId_" + i + "' style='background-color: " + color + "; border:" + border + ";' title = '" + elements + " elements'>&nbsp;&nbsp;&nbsp;&nbsp;</span>").appendTo(content);

			}

		}
		var visibleIndex = new Array();

		var _this = this;
		
		$(".datasourceCheckbox").click(function() {

			  var noColor = $(".absoluteToolbar").css("background-color");
			  var data = new Array();
			  var visible_index = 0;
			  visibleIndex = new Array();

			  //create the new data source array
			  $('.datasourceCheckbox').each(function(index,cb){
				  
				  var visible =$(cb).attr('checked');
				  var overlayId = $(cb).attr('datasourceId');
				  
				  var dataLayer = 0;
				  var countFind = 0;
					for ( var j = 0; j < _this.map.openlayersMap.getNumLayers(); j++) {

						if (_this.map.openlayersMap.layers[j].name == "Data Objects") {
							if(countFind == overlayId){
								dataLayer = j;
								break;
							}
							countFind++;
						}
					}
					
				  if (visible) {
					  visibleIndex.push(parseInt(overlayId));
					  _this.map.openlayersMap.layers[dataLayer].setVisibility(true);
				  } else{
//					  logg("disable this");
					  _this.map.openlayersMap.layers[dataLayer].setVisibility(false);
				  }

				  var color = noColor;
				  var border = "2px solid " + noColor;
				  if (visible) {
					  var c = STIStatic.colors[visible_index++];
			          color = 'rgb(' + c.r0 + ',' + c.g0 + ',' + c.b0 + ')';
//			          border = '2px solid rgb(' + c.r1 + ',' + c.g1 + ',' + c.b1 + ')';
			          border = '2px solid rgb(0,0,0)';
				  }
				  var spanid = "#span_datasourceId_" + index;
				  $(spanid).css("background-color", color);
				  $(spanid).css("border", border);
			  });

			  //setup the widgets to display the new data
			 widget.model.changeVisibility(visibleIndex);				  

			});
	}

	// for the additional Layers
	if (this.model.userLayers && this.model.userLayers.length > 0) {
		var column = document.createElement("tr");
		toolbarTable.appendChild(column);

		var header = document.createElement("td");
		header.innerHTML = "&nbsp;";
		column.appendChild(header);
		column = document.createElement("tr");
		toolbarTable.appendChild(column);

		header = document.createElement("td");
		header.innerHTML = $.i18n.prop('msg_html_usermaps_title');
		column.appendChild(header);

	//	this.content = document.createElement("div");
	//	this.content.setAttribute('class','content');
	//	toolbarTable.appendChild(this.content);

		//var sortlist = $("<ul id='sortlist'/>").appendTo(this.content);
		//$(sortlist).appendTo(this.content);
		for( var i = 0; i <this.model.userLayers.length; i++ ){
			var layer = this.model.userLayers[i];
			
			logg("overlay layer: " + layer);

			//add layer to mapView
			var olLayer = new OpenLayers.Layer.WMS(layer.name,
					layer.url,
				    	{
						projection: "EPSG:4326",
						layers: layer.layer,
						transparent: "true",
						format: "image/png"
					},
					{
//						singleTile: true,
//						ratio: 1,
						isBaseLayer: false,
						visibility: layer.visibility //vhz change true for a parameter of the stistatic

					}
				);
			this.map.wmsOverlays.push(olLayer);
			var wmsOverlayIndex = this.map.wmsOverlays.length - 1;
			this.map.openlayersMap.addLayer(olLayer);

			column = document.createElement("tr");
			toolbarTable.appendChild(column);
			var content = document.createElement("td");
			column.appendChild(content);
			
//			logg("wmsOverlayIndex: " + wmsOverlayIndex);

			var checked = (layer.visibility==true?" checked='checked'":"");

			var check = $("<input class='overlayCheckbox' type='checkbox' " +
						"layerIndex='"+ i +"' " +"overlayId='"+wmsOverlayIndex+"'" + checked + ">"+
						layer.name+"</input>").appendTo(content);

			content = document.createElement("td");
			column.appendChild(content);



			//var html = "<span><img src='" + legendurl + "'/></span>";// + legendurl + "'/></span>";
			var legendicon = "img/legend_icon.png";
			var legendtooltip = $.i18n.prop('msg_tooltip_legend_icon','');
			var legend_id = 'legend_' + i;
			var dialog_div = "<div id='dialog_" + legend_id + "'></div>";
			var legend = $("<span id='" + legend_id + "' style='border:2px; align: right;' title = '" + $.i18n.prop('msg_tooltip_legend_icon', layer.name) + "'><img src='" + legendicon + "'/></span>").appendTo(content);

			var positiontop = $('#' + legend_id).position().top;
			var positionleft = 263; //from main

			var $dialog = $(dialog_div)
						//.html(legendurl)
						.dialog({
									autoOpen: false,
									title: $.i18n.prop('msg_html_legend_title',layer.name),
									position: [positiontop,positionleft],
									resizeStop: function (event, ui) {
										//on resize change the height and with of the wms call

									}
								});
			var jquerylegendid = '#' + legend_id;
			var jquerydialogid = '#dialog_' + legend_id;

			//var legend = $("<span id='" + legend_id + "' style='border:2px; align: right;' title = 'Click to show legend'><a target='_blank' href='" + legendurl + "'></img src='" + legendicon + "'></a></span>").appendTo(content);

			$(jquerylegendid).click(function(e) {
				//e.preventDefault();

				var dialog_id = "'#dialog_" + this.id + "'";
				var index = dialog_id.substring (dialog_id.lastIndexOf("_")+1);
				var layername = widget.model.userLayers[parseInt(index)].layer;
				var height = parseInt(widget.map.mapWindow.clientHeight/10);
				var html = "<span><img src='" + widget.legendurl + "&LAYER="+ layername +"&HEIGHT=" + height + "'/></span>";
				$('#dialog_' + this.id).html(html);
				$('#dialog_' + this.id).dialog('open');
				// prevent the default action, e.g., following a link
				return false;
			});
		}
		//$(sortlist).sortable();
		//$(this.content).css('min-width',$(this.content).width());

		//var buttonDiv = $("<div/>").appendTo(this.content);
		//for the overlay data, on click
		$(".overlayCheckbox").click(function() {
			  var visible =$(this).attr('checked');
			  var overlayId = $(this).attr('overlayId');
			  widget.map.wmsOverlays[overlayId].setVisibility(visible);
			  var layerindex =  $(this).attr('layerIndex');
			  widget.model.userLayers[layerindex].visibility = visible;

			});

	}

};

OverlayWidget.prototype.addNewMaskLayer = function(){
	
    var layer = this.model.maskLayers[0];
    
//    logg(this.model.maskLayers[0].wmsurl.substring(0, this.model.maskLayers[0].wmsurl.indexOf("?", 0)+1));
    
    var wmsUrl = layer.wmsurl.replace("8080", "8085");
    //XXX change the port back!!!
    
	var maskLayer = new OpenLayers.Layer.WMS("Mask",
			wmsUrl,
		    	{
				projection: "EPSG:4326",
				layers: layer.layername,
				transparent: "true",
				format: "image/png"
			},
			{
//				singleTile: true,
//				ratio: 1,
				isBaseLayer: false,
				visibility: layer.visibility //vhz change true for a parameter of the stistatic
			}
		);
	this.map.openlayersMap.addLayer(maskLayer);
	
};


OverlayWidget.prototype.updateMaskLayerInOverlay = function(){
	
	if (this.model.maskLayers && this.model.maskLayers.length > 0) {
	
	var formerMaskLayerID = -1;
	
	for(var i=0; i<this.map.openlayersMap.getNumLayers(); i++ ){
		if(this.map.openlayersMap.layers[i].name == "Mask"){
			formerMaskLayerID = i;
		}
	}
	
	if(formerMaskLayerID > -1){
		this.map.openlayersMap.removeLayer(this.map.openlayersMap.layers[formerMaskLayerID]);
		this.addNewMaskLayer();
		Publisher.Publish('deselect');

		this.showSelection(false);
		document.getElementById("smallButton filter").style.display = "none";
		document.getElementById("smallButton filterInverse").style.display = "none";
		document.getElementById("smallButton cancelSelection").style.display = "none";		
		document.getElementById("smallButton mask").style.display = "none";
		
	} else {
					
		this.addNewMaskLayer();
		Publisher.Publish('deselect');
		
		this.showSelection(false);
//		$(".smallButton filter").style.display = "none";
//		$(".filterInverse").style.display = "none";
		
		document.getElementById("smallButton filter").style.display = "none";
		document.getElementById("smallButton filterInverse").style.display = "none";
		document.getElementById("smallButton cancelSelection").style.display = "none";
		document.getElementById("smallButton mask").style.display = "none";

			var toolbarTable = document
					.getElementById("absoluteToolbar ddbToolbar");

			var column = document.createElement("tr");
			toolbarTable.appendChild(column);

			var header = document.createElement("td");
			header.innerHTML = $.i18n.prop('msg_html_mask_title');
			column.appendChild(header);

			column = document.createElement("tr");
			toolbarTable.appendChild(column);

			content = document.createElement("td");

			var chkBox = document.createElement('input');
			chkBox.setAttribute("id", "MaskChkBox");
			chkBox.setAttribute("type", "checkbox");
			chkBox.checked = true;
			content.appendChild(chkBox);

			var span = document.createElement('span');
			span.innerHTML = '&nbsp;'+this.model.maskLayers[0].layername;
			span.setAttribute("style",
							"border: medium none;color: white; font-family: Calibri,Arial,sans-serif; font-size: 13px; font-weight: bold;");
			content.appendChild(span);

			column.appendChild(content);

			// for colorbox
			content = document.createElement("td");

			var span = document.createElement('span');

			span.innerHTML = '&nbsp;&nbsp;&nbsp;&nbsp;';
			span.setAttribute("style",
							"background-color: rgb(127,126,254); border:2px solid rgb(221,8,60);");
			content.appendChild(span);

			column.appendChild(content);

			var _this = this;
			chkBox.onchange = function() {

				var maskLayerId = 0;
				for ( var j = 0; j < _this.map.openlayersMap.getNumLayers(); j++) {
					// logg("wmsOverlays layername: " +
					// this.map.wmsOverlays[i].name + " - i:" +i);

					if (_this.map.openlayersMap.layers[j].name == "Mask") {
						maskLayerId = j;
					}
				}

				if (chkBox.checked) {
					_this.map.openlayersMap.layers[maskLayerId].setVisibility(true);
				} else {
					_this.map.openlayersMap.layers[maskLayerId].setVisibility(false);
				}
				;
			};

		};
		
		document.getElementById("MaskChkBox").checked = true;

}
	
};

//this.model.historyIndex, this.model.history.length, lastAction, nextAction
OverlayWidget.prototype.updateHistoryButtons = function(){

		if( this.model.historyIndex < this.model.history.length - 1 ){
			this.redo.setAttribute('class','historyButton redo');
		}
		else {
			this.redo.setAttribute('class','historyButton redoDisabled');
		}
		$(this.redo).attr("title", $.i18n.prop('msg_tooltip_redo_action', this.model.getNextAction()));
		if( this.model.historyIndex > 0 ){
			this.undo.setAttribute('class','historyButton undo');
		}
		else {
			this.undo.setAttribute('class','historyButton undoDisabled');
		}
		$(this.undo).attr("title", $.i18n.prop('msg_tooltip_undo_action',this.model.getLastAction()));
};

OverlayWidget.prototype.changeVisibility = function(indizes){

	var noColor = $(".absoluteToolbar").css("background-color");
	var visible_index = 0;

	 $('.datasourceCheckbox').each(function(index,cb){

		  var overlayId = $(cb).attr('datasourceId');
		  var visible = $.inArray(parseInt(overlayId), indizes);
		  var checked = (visible < 0?false:true);

		  $(cb).attr('checked', checked);

		  var color = noColor;
		  var border = "2px solid " + noColor;
		  if (checked) {
			  var c = STIStatic.colors[visible_index++];
			  color = 'rgb(' + c.r0 + ',' + c.g0 + ',' + c.b0 + ')';
			  border = '2px solid rgb(' + c.r1 + ',' + c.g1 + ',' + c.b1 + ')';
		  }
		  var spanid = "#span_datasourceId_" + index;
		  $(spanid).css("background-color", color);
		  $(spanid).css("border", border);
	 });
};

OverlayWidget.prototype.showSelection = function (visible) {
    	var displaystyle = '';
    	if (!visible) {
    		displaystyle = 'none';
    	}
    	this.mask.style.display = displaystyle;
} ;