/**
 * * Global constants 
 */
/** configuration infos **/
var READ_CONFIG_SERVICE_URL = "BioSTIFServlet?config";
var VERSION_URL = "version.txt";


/** Global enummeration objects **/	
var actionType = {CANCEL:"CANCEL",OK:"OK",RETRY:"RETRY",STARTED:"STARTED",DATAREAD:"DATAREAD"};
var formatTypes = {WKT:"wkt",GEOJSON:"geojson",GEORSS:"georss",GML2:"gml2",GML3:"gml3",KML:"kml",
		ATOM:"atom",GPX:"gpx"}; 
var contentTypes = {CSV:"csv",DWC:"dwc",KML:"kml"};

var SHIMSERVICEACTIONS = {DWC2JSON:"Dwc2Json", CSV2JSON:"Csv2Json?popuplabel=nameComplete"};
/** configuration keys **/
var configKey = {OVERLAY_WMS_URL:"OVERLAY_WMS_URL", SHIM_SERVICE:"SHIM_SERVICE", RASTER_LEGEND_URL:"RASTER_LEGEND_URL"};

/** Application constants **/
var OVERLAYS_DEFINITON = "overlays.json";
var OVERLAYS_SERVER_VAR = "<WMSSERVER>";
var OSM_BASE_LAYER = "OSM";	
var CONFIG_VAR_BASE_LAYER ="BASE_LAYER";

var STATICFILENAME = "main.jsp";

var MAPWIDGETTITLE = "Map View";
var PLOTWIDGETTITLE = "Temporal View";
var FULLSCREEN_DIV_ID = "FullscreenWindowLog";
var FULLSCREEN_HEADER_DIV_ID = "#FullscreenWindowHeader";

/**
 * BioSTIF_EVENTS_
 */
//Fired when the STIF components are ready
var BIOSTIF_EVENT_STIF_READY = "StifReady"; 
var BIOSTIF_EVENT_POLYGON_SELECTION = "BIOSTIF_POLYGONE_SELECTION";
var BIOSTIF_EVENT_DATA_CHANGED = "BIOSTIF_DATA_CHANGED";
var BIOSTIF_EVENT_RESET_SELECTION = "Reset";
var BIOSTIF_EVENT_HISTORY_CHANGED ="BIOVEL_HISTORY_STEP";

/**
 * BioSTIF relevant actions: to be used for provenance
 */
var BIOSTIF_ACTION_FILTER = "FILTER"; //after filter action
var BIOSTIF_ACTION_INITIAL = "INITIAL"; //initial mode
var BIOSTIF_ACTION_VISIBILITY_CHANGED = "VISIBILITY CHANGED"; //after switching on/of the visibility of datasets 

/**
 * the function reads a file located at VERSION_URL containing the formatted version number
 * @return the version info as string     
 * 
 */
//function getVersion () {
//	var client = restClient (VERSION_URL, "GET");
//	var version;
//	if (client.status == 200) {
//		version = client.responseText;
//	} else {
//		logg ("Error on acccessing version: " + client.responseText);        		
//		throw new Error ($.i18n.prop('msg_error_application_start_failed', $.i18n.prop('msg_error_read_info_failed', 'Version', VERSION_URL)));				
//	}
//	return version;	
//}

/**
 * the function calls a service on the same location, which reads the config data from the VM parameters
 * the config file location must be specified either as system variable using the VM argument: 
 * e.g. -Dbiostif.client.conf="C:\workspace\BioVeLConfiguration\vhdez\biostif.client.conf" or as
 * init-param of the BioSTIFServlet sevlet description on the web.xml: 
 * e.g. <param-name>biostif.client.conf</param-name> <param-value>/home/biovel/conf/biostif.client.conf</param-value>
 * the config file should be a properties-file (key=value on each line)
 * @return the  property objects specified in the config as json      
 * 
 */
function getClientConfigData() {
	var client = restClient(READ_CONFIG_SERVICE_URL, "GET");
	var jsonResult;
	if (client.status == 200) {
		jsonResult = client.responseText;
	} else {
		logg ("Error on acccessing configuration info: " + client.responseText);        	    
		throw new Error ($.i18n.prop('msg_error_application_start_failed', $.i18n.prop('msg_error_read_info_failed', 'Config', READ_CONFIG_SERVICE_URL)));						
	}
	try {
		jsonResult = $.parseJSON(jsonResult);	
	} catch (error) {
		logg ("Error on reading config: " + error.message);
		throw new Error ($.i18n.prop('msg_error_application_start_failed',$.i18n.prop('msg_error_config_wrong_format','Config', 'JSON')));				
	}
	return jsonResult;
		
}

/**
 * reads the name of the layers of a particular wms to be used as selection layers for BioSTIF
 * the server url of the layer definition will be replaced by the wms url base defined in the client config file
 * precondition: application object must exist, config data should have been set on the application object and
 * the overlay description file must be available 
 * @returns
 */
function getMapOverlays( config, parameter) {
	
	var wmsurl;
	try {
		wmsurl = config[parameter];
	} catch (error) {
		throw new Error ($.i18n.prop('msg_error_application_start_failed',$.i18n.prop('msg_error_config_param_missing', parameter)));
	}
	
	var client = restClient(OVERLAYS_DEFINITON, "GET");
	var result;
	if (client.status == 200) {
		result = client.responseText;
	} else {
		logg ("Error on acccessing overlay definition at "  + OVERLAYS_DEFINITON + ": "+ client.responseText);        	    
		throw new Error ($.i18n.prop('msg_error_application_start_failed',$.i18n.prop('msg_error_rest_failed',OVERLAYS_DEFINITON, client.status)));				
	}
	//substitute WMSSERVER variable
	if (result.indexOf(OVERLAYS_SERVER_VAR) >= 0) {
		var regexp = new RegExp(RegExp.quote(OVERLAYS_SERVER_VAR), 'g');
		result = result.replace(regexp, wmsurl);
	}
	/** convert the string into a json object and put it into an array as required by STIF **/
	var overlays;
	try {
		overlays = $.parseJSON(result);	
	} catch (error) {
		logg ("Error on reading overlays " + error.message);
		throw new Error ($.i18n.prop('msg_error_application_start_failed',$.i18n.prop('msg_error_config_wrong_format',OVERLAYS_DEFINITON, 'JSON')));				
	}
	return overlays;	
}

//for biostif with main
function initialize_application(pars, stifGui, mapContainerDiv, plotContainerDiv, tableContainerDiv, layerContainerDiv, mapWidth, mapHeight) {
	
	try {
//		var fullscreenAnimation = fullscreen;
//	    fullscreenAnimation.addFullscreen(fullscreenAnimation.loaderContent());
	    
	    var params = {};
		params.urls = "";
		params.item = "";		
		params.contentTypes ="";
		params.labels = "";
		params.username = "",
		params.credential = "",
		params.layers = "";
		params.bbox;
		params.lang;
		params.auth_header = "";
		params.oauth_provider_url = "";
		params.workflowRunId = "";
		params.task = "";
			    
		document.body.style.cursor = "wait";
		
		//take the data from the parameters
		params.urls = (pars.url?decodeURIComponent(pars.url):""); 	
		params.contentTypes = (pars.contentType?pars.contentType:"");
		params.labels = (pars.label?pars.label:"");
		params.layers = (pars.layers?pars.layers:"");
		params.bbox = (pars.bbox?pars.bbox:"");	
		params.lang = (pars.lang?pars.lang:"en");
		
//		logg("params.urls: " + params.urls);
		
		params.task = (pars.task?pars.task:"");
		params.workflowRunId = (pars.workflowRunId?pars.workflowRunId:"");
		params.username = (pars.username?pars.username:"");
		//params.credential = (pars.credential?pars.credential:"");
		params.auth_header = (pars.auth_header?pars.auth_header:"");
		params.oauth_provider_url = (pars.oauth_provider_url?pars.oauth_provider_url:"");
		
//		logg("auth: "+ params.auth_header.length + " - " + params.auth_header);
//		logg("auth: "+ params.oauth_provider_url.length + " - " + params.oauth_provider_url);
//		
//		if(params.auth_header.length > 1 && params.oauth_provider_url.length > 1){
//			
//			alert("checkauth");
//			
//			var headers = [];
//				headers.push(["X-Auth-Service-Provider","\"" + params.oauth_provider_url + "\""]);
//				headers.push(["X-Verify-Credentials-Authorization","\"" + params.auth_header + "\""]);
//				
//			var authCheckShim = application.model.config.SHIM_SERVER+"/raster/authcheck";
//			var client = restClient (authCheckShim, "GET", headers, "");
//			var username = client.responseText;
//			logg("username: " + username);
//		}
		
//		logg("params.contentTypes: " + params.contentTypes.length);
		
		if(params.urls.length > 1){
			params.urls = params.urls.split(",");
		}
		if(params.contentTypes.length > 1){
			params.contentTypes = params.contentTypes.split(",");
		}
//		else{
//			params.contentTypes = params.contentTypes.split(",");
//		}
		if(params.labels.length > 1){
			params.labels = params.labels.split(",");
		}
		if(params.layers.length > 1){
			params.layers = params.layers.split(",");
		}
		
//		logg("params: " + params.urls.length + " - " + params.contentTypes.length);
		//logg(params.urls.length != params.contentTypes.length);
		
		if (params.urls.length != params.contentTypes.length) {
//	    	throw new Error ($.i18n.prop('msg_error_wrong_starting_pars', $.i18n.prop('msg_error_size_no_match', "url", "contentType")));	    	
	    	logg($.i18n.prop('msg_error_wrong_starting_pars', $.i18n.prop('msg_error_size_no_match', "url", "contentType")));	    	
		}
		
	    if (!params.labels || params.labels.length < params.urls.length) {
	    	params.labels = new Array(params.urls.length);
	    }
	    
	    var applicationUrl = document.documentURI;
	    if (! applicationUrl) {
	    	//try IE method
	    	applicationUrl = window.location.href;
	    }
	    logg("applicationUrl: " + applicationUrl);

	    applicationUrl = applicationUrl.substring(0, applicationUrl.indexOf("main.jsp"));
	    
	    startBioSTIF(params, applicationUrl, stifGui, mapContainerDiv, plotContainerDiv, tableContainerDiv, layerContainerDiv, mapWidth, mapHeight);
		
	} catch (error) {
		//logg("error: " + error.message);
		alert ($.i18n.prop('msg_error_application_start_failed',error.message));
//		if (fullscreenAnimation) {
//			fullscreenAnimation.removeFullscreen();
//		}
		document.body.style.cursor = "default";
		throw new Error ("Error initializing BioSTIF: " + error.message);
	}
};

function startBioSTIF (params, applicationUrl, stifGui, mapContainerDiv, plotContainerDiv, tableContainerDiv, layerContainerDiv, mapWidth, mapHeight) {

	try {
		writeToFullscreen($.i18n.prop('msg_fullscreen_read_config'));
		var config = getClientConfigData();
		logg("config.SHIM_SERVER: " + config.SHIM_SERVER);

// XXX
	    if(applicationUrl.length == 0){
	    	applicationUrl = config.SHIM_SERVER
	    }
		
		
//		logg("auth: "+ params.auth_header.length + " - " + params.auth_header);
//		logg("auth: "+ params.oauth_provider_url.length + " - " + params.oauth_provider_url);
		
		var username = "";
		
		if(params.auth_header.length > 1 && params.oauth_provider_url.length > 1){
			
//			model.setOauthProviderUrl(params.oauth_provider_url);
//			model.setAuthHeader(params.auth_header);
//			
//			setzen!!!!!
			
			//TODO: XXX rob oauth für folgedienste
//			ich brauche hier die secrets!!!!!
			
//			logg("checkauth");
			
			var headers = [];
				headers.push(["X-Auth-Service-Provider",params.oauth_provider_url]);
				headers.push(["X-Verify-Credentials-Authorization",params.auth_header]);
				
			var authCheckShim = config.SHIM_SERVER+"shim/rest/raster/authcheck";
			var client = restClient (authCheckShim, "GET", headers, "");
			

			
			
			if(client.status != 200){
				
				var resp = client.responseText.replace("Authentification error - credential not veryfied: ","");
//				logg("response TXT: "+resp);
				var jsonData = $.parseJSON(resp);
				var splitMessage = jsonData.message.split("\n");
				var msg = "Error '"+client.status+"' while check authetification: \n" + splitMessage[0];
				throw new Error("\n"+msg);
			} else {
				username = client.responseText;
			}
			
		}  else {
			username = "biovel_temp";
		}
		
		var workspaces = [];
		workspaces.push("biovel_temp");
		workspaces.push("biovel_projections");
		if(username.length > 0){
			workspaces.push(username);
		}
		
		//create Model
		var model = new BiostifModel(config);
		model.setTask(params.task);
		model.setUsername(username);
		model.setWorkflowRunId(params.workflowRunId);
		model.setWorkspaces(workspaces);
		model.currentLang=params.lang;
		model.exportServer = applicationUrl;								
		model.contentTypes = params.contentTypes;
		model.setUserLayers(params.layers);
		model.setOverlays(getMapOverlays(config, 'OVERLAY_WMS_URL'));

		model.setDataUrls(params.labels,params.urls);
		//model.setCredential(params.credential);
		
		writeToFullscreen($.i18n.prop('msg_fullscreen_reading_params'));
		//create application		
		application = new Application(model);		
		application.layerContainer = layerContainerDiv;
					
		writeToFullscreen($.i18n.prop('msg_fullscreen_configuring'));
		application.configureWidgets(stifGui, mapContainerDiv, plotContainerDiv, tableContainerDiv, mapWidth, mapHeight);
		
		//load data
		// ? application.reloadDataInWidgets();
		
		document.body.style.cursor = "default";
		
		//zoom to bbox if any		
		if (params.bbox.length > 0 ) {
			try {
				model.zoomToBBOX(params.bbox);
			} catch (error) {
				alert($.i18n.prop('msg_error_wrong_starting_pars',error.message)); //"Error reading configuration: Parameter \"bbox\" (" + bbox + ")  doesn't contain appropriated values.\nPlease give a comma separated string as \"minLongitude,minLatitude,maxLongitude,maxLatitude\" to allow a zoom to this viewport ");
			}						
		}
		
		stopFullscreen ();
	
	} catch (error) {
		stopFullscreen ();
		throw new Error (error.message);		
	}
	stopFullscreen ();
};

/****************************************************************************************************************************
 * from here for GeoTemCo
 * 
 *****************************************************************************************************************************/

/**
 * Initializes the application object for GeoTemCo //TODO check
 * @param pars
 * @param mapContainerDiv
 * @param plotContainerDiv
 * @param tableContainerDiv
 * @param layerContainerDiv
 */
function initBioVeL(pars, mapContainerDiv, plotContainerDiv, tableContainerDiv, layerContainerDiv) {	
	try {
		
		
		var fullscreenAnimation = fullscreen;
	    fullscreenAnimation.addFullscreen(fullscreenAnimation.loaderContent());
	   
	    document.body.style.cursor = "wait";
		
	    var urls = "";		//url to the datasources (comma separated list of URIs)				
		var contentTypes ="";	//contenttypes of the datasources: (comma separated list of Strings. Allowed values are [kml, json, dwc, csv])	
		var labels = "";	//labels of the datasources (comma separated list of Strings)	
		var layers;	//user layers: WMS layer names to be added to map (comma separated list of Layernames [workspace:layername])	
		var bbox;	//boundingBox to ZoomTo [minx,miny,maxx,maxy] in WGS84
		var lang; //language to start the application
		
		urls = (pars.url?decodeURIComponent(pars.url):""); 	
		urls = urls.split(",");
		
		contentTypes = (pars.contentType?pars.contentType:"");
		contentTypes = contentTypes.split(",");
		
		labels = (pars.label?pars.label:"");
		labels = labels.split(",");
		
		layers = (pars.layers?pars.layers:"");
		layers = layers.split(",");
				
		bbox = (pars.bbox?pars.bbox:"");
		if (bbox.length > 0) {
			bbox = bbox.split(",");
		}
		if (urls.length != contentTypes.length) {
	    	throw new Error ("the contentTypes array must be the same size like the url array");	    	
	    }
	    if (!labels || labels.length < urls.length) {
	    	labels = new Array(urls.length);
	    }
	    //internationalization settings
	    lang = (pars.lang?pars.lang:'en');
	    loadBundles(lang);
	    
	    var applicationUrl = document.documentURI;
	    applicationUrl = applicationUrl.substring(0, applicationUrl.indexOf(STATICFILENAME));
	    
	    //create the model
	    var model = new BiostifModel();
	    model.setOverlays(getMapOverlays());
	    
		application = new Application(model);
		
		application.currentLang=pars.lang;
		application.exportServer = applicationUrl;				
				
		if (layerContainerDiv) {
			application.layerContainer = layerContainerDiv;
			if (layers) {
				application.setUserLayers(layers);
			}
		}
	    
		var fullscreenlog = document.getElementById("FullscreenWindowLog");
		if (typeof(fullscreenlog) != "undefined" && fullscreenlog) {
			fullscreenlog.innerHTML="Configuring data";
		}		
		//load data into application		
		application.setDatasets(labels, contentTypes, urls);
				
		if (typeof(fullscreenlog) != "undefined" && fullscreenlog) {
			fullscreenlog.innerHTML="Configuring views";
		}
			
		var widgetWidth = $("#map").width();
		var widgetHeight = $("#map").height();
		widgetWidth = (Math.floor(widgetWidth / 10)) * 10 - 20;
		widgetHeight = (Math.floor(widgetHeight / 10)) * 10;
				
		widgetHeight = widgetHeight / 2; //splitt
	    
	    
		if (mapContainerDiv) {
			application.spaceWidget = configureMapWidget(mapContainerDiv, widgetWidth + "px", widgetHeight + "px", bbox);
						
		}
		
		if (plotContainerDiv) {
			application.timeWidget = configurePlotWidget(plotContainerDiv, widgetWidth + "px", '40px');				
		}
		if (tableContainerDiv) {
			var tableHeight = widgetHeight - 6; //for splitpane
			if (plotContainerDiv && application.timeWidget) {
				tableHeight-=100; //for plotcontainer
			}
			application.tableWidget = configureTableWidget(tableContainerDiv, widgetWidth + "px", tableHeight + "px" );				
		}
		//display the data in the corresponding widgets
		application.display();
					
		document.body.style.cursor = "default";
		
		//zoom to bbox if any
		//application.zoomToExtent(bbox);					
		
		fullscreenAnimation.removeFullscreen();
	} catch (error) {
		alert ("Error initializing BioSTIF: " + error.message);
		if (fullscreenAnimation) {
			fullscreenAnimation.removeFullscreen();
		}
		document.body.style.cursor = "default";
		throw new Error ("Error initializing BioSTIF: " + error.message);
	}
};

/***
 * for geotemco
 * @param mapWidgetDIV
 * @param width
 * @param height
 * @param bbox
 * @returns
 */
function configureMapWidget (mapWidgetDIV, width, height, bbox) {
	var div = document.getElementById(mapWidgetDIV);
	var wrapper = new WidgetWrapper();
	var boundaries = false;
	if (bbox && bbox.length == 4) {
		boundaries = {minLon: bbox[0], minLat: bbox[1], maxLon: bbox[2], maxLat: bbox[3]};
	}
	var options = {
			mapTitle: MAPWIDGETTITLE,
			mapWidth : width, // false or desired width css definition for the map
			mapHeight : height, // false or desired height css definition for the map			
			mapIndex : 0, // index = position in location array; for multiple locations the 2nd map refers to index 1
			alternativeMap : false, // alternative map definition for a web mapping service or 'false' for no alternative map			
			googleMaps : false, // enable/disable Google maps (actually, no Google Maps API key is required)
			bingMaps : false, // enable/disable Bing maps (you need to set the Bing Maps API key below)
			bingApiKey : 'none', // bing maps api key, see informations at http://bingmapsportal.com/
			osmMaps : true, // enable/disable OSM maps
			baseLayer : 'Open Street Map', // initial layer to show (e.g. 'Google Streets')
			resetMap : true, // show/hide map reset button
			countrySelect : true, // show/hide map country selection control button
			polygonSelect : true, // show/hide map polygon selection control button
			circleSelect : true, // show/hide map circle selection control button
			squareSelect : true, // show/hide map square selection control button
			multiSelection : true, // true, if multiple polygons or multiple circles should be selectable
			popups : true, // enabled popups will show popup windows for circles on the map
			olNavigation : false, // show/hide OpenLayers navigation panel
			olLayerSwitcher : false, // show/hide OpenLayers layer switcher
			olMapOverview : true, // show/hide OpenLayers map overview
			olKeyboardDefaults : true, // (de)activate Openlayers keyboard defaults
			olScaleLine : true, // (de)activate Openlayers keyboard defaults
			geoLocation : true, // show/hide GeoLocation feature
			boundaries : boundaries, // initial map boundaries or 'false' for no boundaries
			mapCanvasFrom : '#9db9d8', // map widget background gradient color top
			mapCanvasTo : '#5783b5', // map widget background gradient color bottom
			labelGrid : true, // show label grid on hover
			maxPlaceLabels : 6, // Integer value for fixed number of place labels: 0 --> unlimited, 1 --> 1 label (won't be shown in popup, 2 --> is not possible because of others & all labels --> 3 labels, [3,...,N] --> [3,...,N] place labels)
			selectDefault : true, // true, if strongest label should be selected as default
			maxLabelIncrease : 2, // maximum increase (in em) for the font size of a label
			labelHover : false, // true, to update on label hover
			ieHighlightLabel : "color: COLOR1; background-color: COLOR0; filter:'progid:DXImageTransform.Microsoft.Alpha(Opacity=80)';-ms-filter:'progid:DXImageTransform.Microsoft.Alpha(Opacity=80)';", // css code for a highlighted place label in IE
			highlightLabel : "color: COLOR0; text-shadow: 0 0 0.4em black, 0 0 0.4em black, 0 0 0.4em black, 0 0 0.4em COLOR0;", // css code for a highlighted place label
			ieSelectedLabel : "color: COLOR1; font-weight: bold;", // css code for a selected place label in IE
			selectedLabel : "color: COLOR1; font-weight: bold;", // css code for a selected place label
			ieUnselectedLabel : "color: COLOR1; font-weight: normal;", // css code for an unselected place label in IE
			unselectedLabel : "color: COLOR1; font-weight: normal;", // css code for an unselected place label
			ieHoveredLabel : "color: COLOR1; font-weight: bold;", // css code for a hovered place label in IE
			hoveredLabel : "color: COLOR1; font-weight: bold;", // css code for a hovered place label
			circleGap : 0, // gap between the circles on the map (>=0)
			minimumRadius : 4, // minimum radius of a circle with mimimal weight (>0)
			circleOutline : true, // true if circles should have a default outline
			circleTransparency : true, // transparency of the circles
			minTransparency : 0.4, // maximum transparency of a circle
			maxTransparency : 0.8, // minimum transparency of a circle
			binning : 'false', // binning algorithm for the map, possible values are: 'generic', 'square', 'hexagonal', 'triangular' or false for 'no binning'
			noBinningRadii : 'dynamic', // for 'no binning': 'static' for only minimum radii, 'dynamic' for increasing radii for increasing weights
			circlePackings : true, // if circles of multiple result sets should be displayed in circle packs, if a binning is performed
			binCount : 10, // number of bins for x and y dimension for lowest zoom level
			showDescriptions : true, // true to show descriptions of data items (must be provided by kml/json), false if not
			mapSelection : true, // show/hide select map dropdown
			binningSelection : true, // show/hide binning algorithms dropdown
			mapSelectionTools : true, // show/hide map selector tools
			dataInformation : true, // show/hide data information
			overlayVisibility : false, // initial visibility of additional overlays
			proxyHost : ''	//required for selectCountry feature, if the requested GeoServer and GeoTemCo are NOT on the same server			
	};
	var widget = new MapWidget(wrapper,div,options);
	return wrapper;
};

/**
 * for geotemco
 * @param plotWidgetDIV
 * @param width
 * @param height
 * @returns
 */
function configurePlotWidget (plotWidgetDIV, width, height) {
	var div = document.getElementById(plotWidgetDIV);
	var wrapper = new WidgetWrapper();
	var options = {
			timeTitle : PLOTWIDGETTITLE, // title will be shown in timeplot header
			timeIndex : 0, // index = position in date array; for multiple dates the 2nd timeplot refers to index 1
			timeWidth : width, // false or desired width css definition for the timeplot
			timeHeight : height, // false or desired height css definition for the timeplot
			defaultMinDate : new Date(2012, 0, 1), // required, when empty timelines are possible
			defaultMaxDate : new Date(), // required, when empty timelines are possible
			timeCanvasFrom : '#EEE', // time widget background gradient color top
			timeCanvasTo : '#EEE', // time widget background gradient color bottom
			rangeBoxColor : "white", // fill color for time range box
			rangeBorder : "1px solid #de7708", // border of frames
			dataInformation : true, // show/hide data information
			rangeAnimation : true, // show/hide animation buttons
			scaleSelection : true, // show/hide scale selection buttons
			linearScale : true, // true for linear value scaling, false for logarithmic
			unitSelection : true, // show/hide time unit selection dropdown
			timeUnit : -1 // minimum temporal unit (SimileAjax.DateTime or -1 if none) of the data
		};
	var widget = new TimeWidget(wrapper, div, options);
	return wrapper;
};
/**
 * for geotemco
 * @param tabWidgetDIV
 * @param width
 * @param height
 * @returns
 */
function configureTableWidget (tabWidgetDIV, width, height) {
	var div = document.getElementById(tabWidgetDIV);
	var wrapper = new WidgetWrapper();
	var options = {
			tableWidth : width, // false or desired width css definition for the table
			tableHeight : height, // false or desired height css definition for the table
			validResultsPerPage : [10, 20, 50, 100, 1000, 10000], // valid number of elements per page
			initialResultsPerPage : 1000, // initial number of elements per page
			tableSorting : true, // true, if sorting of columns should be possible
			tableContentOffset : 250, // maximum display number of characters in a table cell
			tableSelectPage : true, // selection of complete table pages
			tableSelectAll : true, // selection of complete tables
			tableShowSelected : true, // show selected objects only option
			unselectedCellColor : '#EEE' // color for an unselected row/tab
		};
	var widget = new TableWidget(wrapper,div, options);
	return wrapper;
};

/**
 * gets output format object for a particular layer 
 * @param layer: OpenLayers Layer class: see http://dev.openlayers.org/releases/OpenLayers-2.12/doc/apidocs/files/OpenLayers/Layer-js.html
 * @param format: should be a key from the variable formatTypes
 * @return 
 */
function getFeatureExportFormat (layer, format) {
	var out_options = {
            'internalProjection': layer.map.projection,
            'externalProjection': layer.map.displayProjection 
        };
    var gmlOptions = {
            featureType: "feature",
            featureNS: "http://example.com/feature"
        };            
    var gmlOptionsOut = OpenLayers.Util.extend(
            OpenLayers.Util.extend({}, gmlOptions),
            out_options
    );                
        
    var olFormats = {	wkt: new OpenLayers.Format.WKT(out_options),
      		geojson: new OpenLayers.Format.GeoJSON(out_options),
      		georss: new OpenLayers.Format.GeoRSS(out_options),
      		gml2: new OpenLayers.Format.GML.v2(gmlOptionsOut),
      		gml3: new OpenLayers.Format.GML.v3(gmlOptionsOut),
      		kml: new OpenLayers.Format.KML(out_options),
      		atom: new OpenLayers.Format.Atom(out_options),
      		gpx: new OpenLayers.Format.GPX(out_options)
    };
    return olFormats[format];
};

