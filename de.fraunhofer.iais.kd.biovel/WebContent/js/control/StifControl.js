/**
Controller for all interactions with the spatio-temporal library
**/

/**
 * STIF_EVENTS:
 */
var STIF_EVENT_MAP_INITIALIZED = "MapGuiInitialized";
var STIF_EVENT_TABLE_INITIALIZED = "TableGuiInitialized";
var STIF_EVENT_TIME_INITIALIZED = "TimePlotGuiInitialized";
var STIF_EVENT_SELECTION = "selection";
var STIF_EVENT_FILTER = "filter";


function StifControl(model) {	
	
	this.model = model;
	/**
     * Views
     */
    this.spaceWidget = null;
    this.timeWidget = null;
    this.tableWidget = null;
    
    this.init();
};

StifControl.prototype.init = function () {
	var control = this;
	
	var subMVId;
    subMVId = Publisher.Subscribe(STIF_EVENT_MAP_INITIALIZED, function(mapWrapper) {
    	Publisher.Unsubscribe(STIF_EVENT_MAP_INITIALIZED, subMVId);
    	control.initMapView(mapWrapper);
    });

    var subTVId;
    subTVId = Publisher.Subscribe(STIF_EVENT_TABLE_INITIALIZED, function(tableWrapper) {
        logg("TableGuiInitialized " + tableWrapper);        

        Publisher.Unsubscribe(STIF_EVENT_TABLE_INITIALIZED, subTVId);
        if (tableWrapper != null) {
        	control.tableWidget = tableWrapper;
        	writeToFullscreen($.i18n.prop('msg_fullscreen_loading_tabular_data'));
            control.tableWidget.display(control.model.activeData.data, control.model.activeData.label);            
        } else {
            throw new Error($.i18n.prop('msg_error_stif_table_not_initialized'));
        }
    });

    var subTpVId;
    subTpVId = Publisher.Subscribe(STIF_EVENT_TIME_INITIALIZED, function(timeWrapper) {
        logg("TimePlotGuiInitialized " + timeWrapper);        

        Publisher.Unsubscribe(STIF_EVENT_TIME_INITIALIZED, subTpVId);
        if (timeWrapper != null) {
        	control.timeWidget = timeWrapper;
        	writeToFullscreen($.i18n.prop('msg_fullscreen_loading_temporal_data'));
            control.timeWidget.display(control.model.activeData.data);            
        } else {
            throw new Error($.i18n.prop('msg_error_stif_time_not_initialized'));
        }
    });
    
    Publisher.Subscribe(STIF_EVENT_SELECTION, function(data, widget) {
    	var selectionObjects = SpaceWrapper.map.polygons;    	
    	control.model.select (data, widget, selectionObjects);
    });

    Publisher.Subscribe(STIF_EVENT_FILTER, function(data) {
    	control.model.filter(data);    	

    });
};

StifControl.prototype.initMapView = function(mapWrapper) {
	logg("MapGuiInitialized " + mapWrapper);
		
	if (mapWrapper != null) {
		
		this.displayDataInMapView(mapWrapper);
		//Publisher.Publish(BIOSTIF_EVENT_STIF_READY, true);
	    
	    var out_options = {
	            'internalProjection': mapWrapper.map.objectLayer.map.projection,//displayProjection,
	            'externalProjection': mapWrapper.map.objectLayer.map.displayProjection //projection
	        };
	    var gmlOptions = {
	            featureType: "feature",
	            featureNS: "http://example.com/feature"
	        };
	    var gmlOptionsOut = OpenLayers.Util.extend(
	            OpenLayers.Util.extend({}, gmlOptions),
	            out_options
	    );
	
	
	    this.model.olFormats = {	wkt: new OpenLayers.Format.WKT(out_options),
	      		geojson: new OpenLayers.Format.GeoJSON(out_options),
	      		georss: new OpenLayers.Format.GeoRSS(out_options),
	      		gml2: new OpenLayers.Format.GML.v2(gmlOptionsOut),
	      		gml3: new OpenLayers.Format.GML.v3(gmlOptionsOut),
	      		kml: new OpenLayers.Format.KML(out_options),
	      		atom: new OpenLayers.Format.Atom(out_options),
	      		gpx: new OpenLayers.Format.GPX(out_options)
	  };
	    
	  //set the map field on the model
	  //TODO change according to GeoTemCo, try to avoid this method
	  this.model.map = (this.spaceWidget?this.spaceWidget.map:null);
	
	} else {
	    throw new Error($.i18n.prop('msg_error_stif_map_not_initialized'));
	};
};

StifControl.prototype.displayDataInMapView = function(mapWrapper) {
    //logg("Configuring mapView: initial visible data array size " + this.model.activeData.data.length);
    
    if (typeof (mapWrapper) != "undefined" && mapWrapper != null) {
        this.spaceWidget = mapWrapper;
    } else {
        throw new Error($.i18n.prop('msg_error_application_start_failed') + " " +$.i18n.prop('msg_error_stif_map_not_initialized'));
    }
    // send the data to the widget
    if (this.model.activeData.data && this.model.activeData.data.length > 0) {
    	writeToFullscreen($.i18n.prop('msg_fullscreen_clustering'));    	
    	this.spaceWidget.display(this.model.activeData.data);
    };
    //set the overlays
    this.setOverlays();
};

StifControl.prototype.setOverlays = function () {
	this.spaceWidget.loadOverlays(application.model.overlays);
};

/**
 * displays the active data from model on the widgets of geotemco
 */
StifControl.prototype.display = function () {
	
//	if (!this.model.activeData.datasets || this.model.activeData.datasets.length == 0) {
//		alert($.i18n.prop('msg_alert_no_data'));	    
//	}	
	// send the data to the map widget
	if (this.spaceWidget) {
		writeToFullscreen ($.i18n.prop('msg_fullscreen_clustering'));
    	
		//this should happen asynchronously!!
    	this.spaceWidget.display(this.model.activeData.datasets);
    	
    	if (this.model.overlays && this.model.overlays.length > 0) {
    		writeToFullscreen ($.i18n.prop('msg_fullscreen_uploading_overlays'));
        	this.spaceWidget.widget.addBaseLayers(this.model.overlays);
        }
    }
	if (this.timeWidget) {
		writeToFullscreen=$.i18n.prop('msg_fullscreen_loading_temporal_data');
    	this.timeWidget.display(this.model.activeData.datasets);
    }
	if (this.tableWidget) {
		writeToFullscreen($.i18n.prop('msg_fullscreen_loading_tabular_data'));
    	this.tableWidget.display(this.model.activeData.datasets);
    }
	stopFullscreen();
};

/**
 * displays the active data from model on the widgets of STIF
 */
StifControl.prototype.displaySTIF = function () {
	
	writeToFullscreen ($.i18n.prop('msg_fullscreen_reloading'));
	
	if (!this.model.activeData.data || this.model.activeData.data.length == 0) {
//		alert($.i18n.prop('msg_alert_no_data'));	    
//		return;
		logg("no this.model.activeData");
		
	} else {		
		if (this.spaceWidget) {
			writeToFullscreen ($.i18n.prop('msg_fullscreen_clustering'));    	
			//this should happen asynchronously!!
	    	this.spaceWidget.display(this.model.activeData.data);    	    	
	    }
		if (this.timeWidget) {
			writeToFullscreen($.i18n.prop('msg_fullscreen_loading_temporal_data'));
	    	this.timeWidget.display(this.model.activeData.data);
	    }
		if (this.tableWidget) {
			writeToFullscreen($.i18n.prop('msg_fullscreen_loading_tabular_data'));
	    	this.tableWidget.display(this.model.activeData.data, this.model.activeData.label);
	    }
	}
		
	stopFullscreen();
	
	
	
};

StifControl.prototype.resizeWidgets = function () {
	if (typeof (this.spaceWidget) != "undefined" && this.spaceWidget != null) {
		this.spaceWidget.widget.gui.resize();
	}
	if (typeof (this.timeWidget) != "undefined" && this.timeWidget != null) {
		this.timeWidget.widget.gui.resize();
	}
};


StifControl.prototype.configureWidgets = function (stifGui, mapContainerDiv, plotContainerDiv, tableContainerDiv, mapWidth, mapHeight) {// Configure Stif settings
	
	//calculate widgets size
	mapWidth = (Math.floor(mapWidth / 10)) * 10 - 20;
	mapHeight = (Math.floor(mapHeight / 10)) * 10;
	mapHeight = mapHeight / 2;
	var tableHeight = mapHeight - 6; //for splitpane    
	
	var timePlotSettings;
	
	if (plotContainerDiv) {
		tableHeight-=100;
	}
	
	//change base layer depending on the config
	var base_layer_config = this.model.config[CONFIG_VAR_BASE_LAYER];
	var historicMaps = false;
	var baseLayer = 'Open Street Map';
	if (base_layer_config != OSM_BASE_LAYER) {
		historicMaps = true;
		baseLayer = base_layer_config; 
	};
	
	STIStatic.applySettings({
	    mapWidth : mapWidth + "px", // false or desired width of the map
	    mapHeight : mapHeight + "px", // '580px',
	    tableWidth : mapWidth + "px", // false or desired width of the table
	    tableHeight : tableHeight + "px", // '580px',
	    timeplot:		true,		// show/hide timeplot
		timeplotWidth:	mapWidth + "px",	// false or desired width of the timeplot
		timeplotHeight:	'40px',	// false or desired height of the timeplot
	
	    popups : true, // enabled popups will show popup windows for selected
	                    // data (instead of placename cloud on the map)
	    // configuration of map settings
	    maxPlaceLabels:		0,
	    historicMaps : historicMaps, //true, //false, // enable/disable custom maps, which can be defined in layers.xml
	    googleMaps : false, // enable/disable Google maps (if enabled, a valid Google Maps API key must be included in the DOM)
	    bingMaps : false, // enable/disable Bing maps
	    osmMaps : true, // enable/disable OSM maps
	    baseLayer : 'Open Street Map',//baseLayer, //'World countries', // initial layer to show (e.g. 'Google Streets')
	    resetMap : true, // show/hide map reset button
	    countrySelect : true, // show/hide map country selection control button
	    polygonSelect : true, // show/hide map polygon selection control button
	    circleSelect : true, // show/hide map circle selection control button
	    squareSelect : true, // show/hide map square selection control button
	    editPolygon : true, // true, if polygons should be editable
	    olNavigation : false, // show/hide OpenLayers navigation panel
	    olLayerSwitcher : true, // show/hide OpenLayers layer switcher
	    olMapOverview : true, // show/hide OpenLayers map overview
	    olKeyboardDefaults : true, // (de)activate Openlayers keyboard defaults
	    olMousePosition:	true, // (de)activate Openlayers mousePosition control
	    connections : true, // show/hide connection control
	    showInverseFilter: true,
	    //showBBox:			showBBox,				//vhz if true shows a bbox given as parameter as a layer and offers a toolbar button for activate/deactivate it
	    overlayVisibility : false, //vhz sets the default visibility of overlays on the map
	    showOverlaySelector : true, //vhz shows the dropdown for overlays
	    //layerSwitcher:	true,		// show/hide OpenLayers layer switcher
		mapOverview:		true,		// show/hide OpenLayers map overview
		keyboardDefaults:	true,
		geoLocation: false,
		showMapTypeSelector: false,
		validResultsPerPage:	[ 10, 100, 500, 1000 ],     // valid number of elements per page
		initialResultsPerPage:	100,     		// initial number of elements per page
	    //proxyHost : '../biostif/proxy?url='
		inverseFilter:		true,
		filterDisplayStyle: '',
		filterBarDisplayOnce: true
	
	});
	if (mapContainerDiv) {
		stifGui.initializeMapGUI(mapContainerDiv);		
	}
	if (plotContainerDiv) {
		stifGui.initializePlotGUI(plotContainerDiv);
	}
	if (tableContainerDiv) {
		stifGui.initializeTableGUI(tableContainerDiv);
	};
};
