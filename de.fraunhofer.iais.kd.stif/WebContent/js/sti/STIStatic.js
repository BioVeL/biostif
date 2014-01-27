/**
 * @class STIStatic
 * Configuration File
 * @author Stefan Jänicke (stjaenicke@informatik.uni-leipzig.de)
 * @version 0.9
 */

var STIStatic = {

	// configuration of modules
	map:			true,		// show/hide map
	mapWidth:		'728px',	// false or desired width of the map
	mapHeight:		'580px',	// false or desired height of the map
	mapHeadline:		false,		// headline for map; false or a valid string
	timeplot:		true,		// show/hide timeplot
	timeplotWidth:		'728px',	// false or desired width of the timeplot
	timeplotHeight:		'100px',	// false or desired height of the timeplot
	timeHeadline:		false,		// headline for timeline; false or a valid string
	table:			true,		// show/hide timeplot
	tableWidth:		'728px',	// false or desired width of the table
	tableHeight:		false,		// false or desired height of the table
	incompleteData:		true,		// show/hide data with either temporal or spatial metadata
	inverseFilter:		false,		// if inverse filtering is offered
	filterDisplayStyle: 'block',
	filterBarDisplayOnce: false,

	// general settings
	maxDatasets:		4,		// maximum number of parallel datasets
	mouseWheelZoom:		true,		// enable/disable zoom with mouse wheel on map & timeplot
	popups:			true,		// enabled popups will show popup windows for selected data (instead of placename cloud on the map)

	// configuration of map settings
	historicMaps:	false,		// enable/disable custom maps, which can be defined in layers.xml
	googleMaps:		false,		// enable/disable Google maps (if enabled, a valid Google Maps API key must be included in the DOM)	
	bingMaps:		false,		// enable/disable Bing maps
	bingApiKey:		'none',		// bing maps api key, see informations at http://bingmapsportal.com/
	osmMaps:		true,		// enable/disable OSM maps
	baseLayer:		'Open Street Map',	// initial layer to show (e.g. 'Google Streets')
	fractionalZoom:		false,		// frctional zoom for vector maps
	resetMap:		true,		// show/hide map reset button
	countrySelect:		true,		// show/hide map country selection control button
	polygonSelect:		true,		// show/hide map polygon selection control button
	circleSelect:		true,		// show/hide map circle selection control button
	squareSelect:		true,		// show/hide map square selection control button
	editPolygon:		true,		// true, if polygons should be editable
	multiSelection:		true,		// true, if multiple polygons should be selectable
	individualSelection:	true,		// true, if individual circle should be add- and removable
	olNavigation:		false,		// show/hide OpenLayers navigation panel
	olLayerSwitcher:	false,		// show/hide OpenLayers layer switcher
	olMapOverview:		true,		// show/hide OpenLayers map overview
	olKeyboardDefaults:	false,		// (de)activate Openlayers keyboard defaults
	olMousePosition:	false,		//vhz (de) activate the OpenLayers mouse position info
	geoLocation:		true,		// show/hide GeoLocation feature
	connections:		false,		// show/hide connection control
	boundaries: 		{		// initial map boundaries
					minLon: -29,
					minLat: 35,
					maxLon: 44,
					maxLat: 67
				},

	labelGrid:		false,		// show label grid on hover
	maxPlaceLabels:		6,		// Integer value for fixed number of place labels: 0 --> unlimited, 1 --> 1 label (won't be shown in popup, 2 --> is not possible because of others & all labels --> 3 labels, [3,...,N] --> [3,...,N] place labels)
	selectDefault:		true,		// true, if strongest label should be selected as default
	maxLabelIncrease:	0,		// maximum increase (in em) for the font size of a label
	labelHover: 		false,		// true, to update on label hover
	cloudOpacity:       0.85,               // opacity of the placename cloud
	circleGap:		0,      // gap between the circles on the map (>=0)
	minimumRadius:		4,              // minimum radius of a circle with mimimal weight (>0)
	classBase:		1.5,    // controls the diameter growth of circles with #items (>=1); the lower the faster the diameters grow
	circleOutline:		true,				// true if circles should have a default outline
	circleTransparency:	0.6,				// transparency of the circles
	binning:		false,	// a binning procedure for the map, possible values are: 'generic', 'square', 'hexagonal', 'triangular' or false for 'no binning'
	noBinningRadii:		'static',	// for 'no binning': 'static' for only minimum radii, 'dynamic' for increasing radii for increasing weights
	circlePackings:		false,		// if circles of multiple result sets should be displayed in circle packs, if a binning is performed
	binCount:		10,		// number of bins for x and y dimension for lowest zoom level
	showDescriptions:	true,           // true to show descriptions of data items (must be provided by kml/json), false if not
	
	showBBox:			false,				//vhz if true shows a bbox given as parameter as a layer and offers a toolbar button for activate/deactivate it
	overlayVisibility:	false,				//vhz sets the default visibility of overlays on the map
	showOverlaySelector:	false,			//vhz shows the dropdown for overlays	
	showInverseFilter: false,
	proxyHost:			'',//vhz proxy for selectCountry
	showMapTypeSelector: true, //vaue to show or hide the map type selection box

	// configuration of timeplot settings
	timeZoom:			false,              // show/hide timeplot zoom
	animation:			'embedded',	           // show/hide animation control button
	timeUnit:		-1,  // minimum temporal unit (SimileAjax.DateTime or -1 if none) of the data
	featherSlider:		false,              // show/hide feather slider
	defaultMinDate:		new Date(2012,0,1),  // required, when empty timelines are possible
	defaultMaxDate:		new Date(),  		// required, when empty timelines are possible
	timeDragAndDrop:	true,				// use drag&drop for timerange modification or not (click&click)
	toolbarStyle:		'alternative',		// styling for the timerange icons (possible: default, alternative)

	// configuration of table settings
	validResultsPerPage:	[ 10, 20, 50, 100 ],     // valid number of elements per page
	initialResultsPerPage:	10,     		// initial number of elements per page
	tableSorting:		true,		// true, if sorting of columns should be possible
	tableContentOffset: 	250,		// maximum display number of characters in a table cell
	tableSelectPage:	true,		// selection of complete table pages
	tableSelectAll:		false,		// selection of complete tables
	tableShowSelected:	true,		// show selected objects only option

	// configuration of style settings
	designURL:			"images/", // URL to the images to use, default is "images/"
	lightScheme:		false,                // use e4D light scheme
	toolbarLeft:		false,               // place toolbar on the left or right
	toolbarWidth:		41,                  // width of the toolbar
	toolbarColor:		"#DDDDDD",           // color1 of the toolbar (also used for timeplot background)
	toolbarColor2:		"#878380",           // color2 of the toolbar (also used for timeplot background)
	toolbarBorder:		"1px solid #878380", // toolbar border
    rangeBoxColor:	    "white",           // fill color for time range box
    frameColor:		    "#eebccd",           // fill color of frames (placename cloud, map info, time range)
	frameColor2:		"#cb063a",           // border & text color of frames
    frameBorder:		"1px solid #cb063a", // border of frames

    placeLabelStyles:	"custom",           // style of place labels (possible: shadows, frames, custom)
    // shadows (define shadows for selected, unselected and hovered labels); COLOR{1,0} are placeholders for objects' assigned colors
    shadowSelected:	    "0 0 0.1em white, 0 0 0.1em white, 0 0 0.1em white, 0 0 0.1em COLOR1",
    shadowUnselected:	"0 0 0.2em white, 0 0 0.2em white, 0 0 0.2em white, 0 0 0.2em COLOR0",
    shadowHovered:      "0 -1px COLOR1, 1px 0 COLOR1, 0 1px COLOR1, -1px 0 COLOR1",
    // custom (define complete css strings for selected, unselected and hovered labels); COLOR{1,0} can be used as placeholders for objects' assigned colors
    customSelected:	    "color: COLOR1; font-weight: normal;",
    customUnselected:	"color: COLOR1; font-weight: normal;",
    customHovered:      "color: COLOR1; font-weight: bold;",

	selectedPlaceStyle:	"bold",	             // define style of a selected place name tag (possible: underline, bold, custom)
	// custom (define complete css strings for the style of a selected and unselected placename tag); COLOR{1,0} can be used as placeholders for objects' assigned colors
    customSelectedPlace: "",
    customUnselectedPlace: "",
	language: "en", //vhz language of the application. can be change on init
	defaultZoomPanel: true,
	
	//colors for several datasets; rgb1 will be used for selected objects, rgb0 for unselected

	colors:		[{
			    r1: 221,
			    g1: 8,
			    b1: 60,
			    r0: 179,
			    g0: 142,
			    b0: 150
/*			},{
			    r1: 255,
			    g1: 101,
			    b1: 0,
			    r0: 253,
			    g0: 229,
			    b0: 205,*/
			},{
			    r1: 144,
			    g1: 26,
			    b1: 255,
			    r0: 230,
			    g0: 225,
			    b0: 255,
			}, {
			    r1: 0,
			    g1: 217,
			    b1: 0,
			    r0: 213,
			    g0: 255,
			    b0: 213,
			},{
			    r1: 240,
			    g1: 220,
			    b1: 0,
			    r0: 247,
			    g0: 244,
			    b0: 197
			}]

}

STIStatic.ie8 = false;

if( SimileAjax.Platform.browser.isIE ){
	if( STIStatic.placeLabelStyles == "shadows" ){
    		STIStatic.placeLabelStyles = "frames";
		STIStatic.placeTagStyle = "bold";
	}
	if( SimileAjax.Platform.browser.majorVersion < 9 ){
		STIStatic.ie8 = true;
	}
}

STIStatic.configure = function(urlPrefix){
	STIStatic.urlPrefix = urlPrefix;
	STIStatic.path = STIStatic.urlPrefix + "" + STIStatic.designURL;
	var textsJson = STIStatic.getJsonFile(urlPrefix+"config/texts.json");
	STIStatic.texts = eval('(' + textsJson + ')');
}

STIStatic.applySettings = function(settings){
	$.extend(this, settings);
};

STIStatic.getString = function(language,field){
    if (typeof language == 'undefined' || language == null || typeof this.texts[language] == 'undefined'){
        language = 'de';
    }
	return this.texts[language][field];
}

/**
 * creates a toolbar of an array of tools as a dl list
 * @return the dl element
*/
STIStatic.createToolbar = function(tools){
	var dl = document.createElement("dl");
	dl.style.margin = "0px";
	for( i in tools ){
		tools[i].style.cursor = "pointer";
		tools[i].style.marginLeft = "auto";
		tools[i].style.marginRight = "auto";
		var dt = document.createElement("dt");
		dt.style.textAlign = "center";
		dt.appendChild(tools[i]);
		dl.appendChild(dt);
	}
	return dl;
}

/**
 * returns the actual mouse position
 * @param {Event} e the mouseevent
 * @return the top and left position on the screen
*/
STIStatic.getMousePosition = function(e){
	if(!e){
    	e = window.event;
	}
    var body = (window.document.compatMode && window.document.compatMode == "CSS1Compat") ? window.document.documentElement : window.document.body;
    return {
    	top: e.pageY ? e.pageY : e.clientY,
        left: e.pageX ? e.pageX : e.clientX
    };
}

/**
 * returns the radius for a circle that contains n elements
 * @param {int} n the number of assigned elements
 * @return the calculated radius for the corresponding cirlce
*/
STIStatic.getRadius = function(n){
	if( n == 0 ){
		return 0;
	}
	if( n == 1 ){
		return STIStatic.minimumRadius;
	}
	var aMax = Math.PI * STIStatic.maximumRadius * STIStatic.maximumRadius;
  	var aMin = Math.PI * STIStatic.minimumRadius * STIStatic.minimumRadius;
    return Math.sqrt( ( aMin + (aMax-aMin)/(STIStatic.maximumPoints-1)*(n-1) * Math.sqrt(Math.log(n)) )/Math.PI );
}

STIStatic.getJsonFile = function(url){
	var json = $.ajax({
		  url: url,
		  async: false,
		  dataType: 'json'
	});
	return json.responseText;
}

STIStatic.getFile = function(url){
    var xmlhttp = false;
    if(!xmlhttp){
        try {
            xmlhttp = new XMLHttpRequest();
        }
        catch (e) {
            xmlhttp = false;
        }
    }
    if (typeof ActiveXObject != "undefined") {
        if(!xmlhttp){
            try {
                xmlhttp = new ActiveXObject("MSXML2.XMLHTTP");
            }
            catch (e) {
                xmlhttp = false;
            }
        }
        if(!xmlhttp){
            try {
                xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
            }
            catch (e) {
                xmlhttp = false;
            }
        }
    }
    xmlhttp.open('GET', url, false);
    xmlhttp.send("");
    return xmlhttp;
}

/**
 * returns the xml dom object of the file of the given url
 * @param {String} url the url of the file to parse
 * @return xml dom object of the given file
*/
STIStatic.getXmlDoc = function(url){
	var xmlhttp = STIStatic.getFile(url);
	var xmlDoc;
    if (window.DOMParser) {
        parser = new DOMParser();
        xmlDoc = parser.parseFromString(xmlhttp.responseText, "text/xml");
    }
    else {
        xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
        xmlDoc.async = "false";
        xmlDoc.loadXML(xmlhttp.responseText);
    }
    return xmlDoc;
}

/**
 * returns a Date and a SimileAjax.DateTime granularity value for a given XML time
 * @param {String} xmlTime the XML time as String
 * @return JSON object with a Date d and a SimileAjax.DateTime granularity g
*/
STIStatic.getTimeData = function( xmlTime ){
	if (!xmlTime) return;
	var dateData;
	try {
		var bc = false;
		if( xmlTime.startsWith("-") ){
			bc = true;
			xmlTime = xmlTime.substring( 1, xmlTime.length-1 );
		}
		var timeSplit = xmlTime.split("T");
		var timeData = timeSplit[0].split("-");
		for (var i = 0; i < timeData.length; i++) {
			parseInt(timeData[i]);
		}
		if( bc ){
			timeData[0] = "-"+timeData[0];
		}
		if (timeSplit.length == 1) {
			dateData = timeData;
		}
		else {
			var dayData;
			if (timeSplit[1].indexOf("Z") != -1) {
				dayData = timeSplit[1].substring(0, timeSplit[1].indexOf("Z") - 1).split(":");
			}
			else {
				dayData = timeSplit[1].substring(0, timeSplit[1].indexOf("+") - 1).split(":");
			}
			for (var i = 0; i < timeData.length; i++) {
				parseInt(dayData[i]);
			}
			dateData = timeData.concat(dayData);
		}
	}
	catch (exception) {
		return null;
	}
	var date, granularity;
	if (dateData.length == 6) {
		granularity = SimileAjax.DateTime.SECOND;
    	date = new Date(Date.UTC(dateData[0], dateData[1]-1, dateData[2], dateData[3], dateData[4], dateData[5]));
	}
	else if (dateData.length == 3) {
		granularity = SimileAjax.DateTime.DAY;
    	date = new Date(Date.UTC(dateData[0], dateData[1]-1, dateData[2]));
	}
	else if (dateData.length == 2) {
		granularity = SimileAjax.DateTime.MONTH;
       	date = new Date(Date.UTC(dateData[0], dateData[1]-1, 1));
  	}
	else if (dateData.length == 1) {
		granularity = SimileAjax.DateTime.YEAR;
       	date = new Date(Date.UTC(dateData[0], 0, 1));
  	}
	if( timeData[0] && timeData[0] < 100 ){
		date.setFullYear(timeData[0]);
	}
	return { d: date, g: granularity };
}

/**
 * converts a JSON array into an array of map objects
 * @param {String} JSON a JSON array of spatial objects
 * @return an array of map objects
*/
STIStatic.loadSpatialJSONData = function(JSON){
	var mapObjects = [];
	var runningIndex = 0;
	for( var i in JSON ){
		try {
			var item = JSON[i];			
			var index = item.index || item.id || runningIndex++;
			var name = item.name || "";
			var description = item.description || "";
			var place = item.place || "unknown";
			var lon = parseFloat(item.lon); //vhz parseFloat
			var lat = parseFloat(item.lat); //vhz parseFloat
        	if( (isNaN(lon) || isNaN(lat)) && !STIStatic.incompleteData ){
        		throw "e";
        	}
        	var tableContent = item.tableContent || [];
        	var weight = item.weight || 1;
			var mapObject = new MapObject(name,description,place,lon,lat,weight,tableContent);
			mapObject.setIndex(index);
			mapObjects.push(mapObject);
		}
		catch(e){
			continue;
		}
	}
	return mapObjects;
}

/**
 * converts a JSON array into an array of time objects
 * @param {String} JSON a JSON array of temporal objects
 * @return an array of time objects
*/
STIStatic.loadTemporalJSONData = function(JSON){
	var timeObjects = [];
	for( var i in JSON ){
		try {
			var item = JSON[i];			
			var index = item.index || item.id || runningIndex++;
			var name = item.name || "";
			var description = item.description || "";
			var tableContent = item.tableContent || [];
			var timeStart, timeEnd, granularity;
			var time1 = STIStatic.getTimeData(item.time);
			if( time1 == null ){
				continue;
			}
			timeStart = time1.d;
			granularity = time1.g;
			var time2 = STIStatic.getTimeData(item.time2);
			if( time2 == null ){
				time2 = time1;
			}
			timeEnd = time2.d;
			if( time2.g > granularity ){
				granularity = time2.g;
			}
			var weight = item.weight || "1";
			var timeObject = new TimeObject(name,description,timeStart,timeEnd,granularity,weight, tableContent);
			timeObject.setIndex(index);
			timeObjects.push(timeObject);
		}
		catch(e){
			continue;
		}
	}
	return timeObjects;
};

/**
 * converts a JSON array into an array of map time objects
 * @param {String} JSON a JSON array of spatio-temporal objects
 * @return an array of map time objects
*/
STIStatic.loadSpatioTemporalJSONData = function(JSON){
	var mapTimeObjects = [];
	var runningIndex = 0;
	
	var indexprop = "";
	if (JSON.length == 0) {
		return mapTimeObjects;
	}
	if ('index' in JSON[0]) {
		indexprop = "index";
	} else if ('id' in JSON[0]) {
		indexprop = "id";		
	}
	var index;
	for( var i in JSON ){
		try {
			//vhz index control
			var item = JSON[i];
			if (indexprop) {
				index = item[indexprop] || "";
						
				if (index.length == 0){
					if (typeof (console) != "undefined") {
				        console.info ("Reading JSON with index " + i + ": no index value, record will not be read");
				    }
					continue;
				}
			} else {
				index = runningIndex++;
			}
			//var index = item.index || item.id || runningIndex++;			 			
			var name = item.name || "";
			var description = item.description || "";
			var place = item.place || "unknown";
			var lon = parseFloat(item.lon); //vhz parseFloat
			var lat = parseFloat(item.lat); //vhz parseFloat
        	if( (isNaN(lon) || isNaN(lat)) && !STIStatic.incompleteData){
        		throw new RuntimeException ("Error reading JSON: latitude and/or longitude values missing ");
        	}
			var tableContent = item.tableContent || [];
			var timeStart, timeEnd, granularity;
			var time1 = STIStatic.getTimeData(item.time);
			if( time1 == null && !STIStatic.incompleteData ){
				throw new RuntimeException ("Error reading JSON: time value missing ");
			}
			if( time1 == null ){
				timeStart = null;
				timeEnd = null;
				granularity = null;
			}
			else {
				timeStart = time1.d;
				granularity = time1.g;
				var time2 = STIStatic.getTimeData(item.time2);
				if( time2 == null ){
					time2 = time1;
				}
				timeEnd = time2.d;
				if( time2.g > granularity ){
					granularity = time2.g;
				}
			}
			var weight = item.weight || 1;
			var mapTimeObject = new MapTimeObject(name,description,place,lon,lat,timeStart,timeEnd,granularity,weight,tableContent);
			mapTimeObject.setIndex(index);
			mapTimeObjects.push(mapTimeObject);
		}
		catch(e){
			continue;
		}
	}

	return mapTimeObjects;
}

/**
 * TODO
*/
STIStatic.loadSpatioTemporalKMLData = function(url){
	var mapObjects = [];

	var xmlDoc = STIStatic.getXmlDoc(url);
    var elements = xmlDoc.getElementsByTagName("Placemark");
    if( elements.length == 0 ){
    	return null;
    }
    var index = 0;
    for (var i = 0; i < elements.length; i++) {
        var placemark = elements[i];
		var name, description, place, timeStart, timeEnd, granularity, lon, lat, tableContent = [];
		var weight = 1;
		var timeData = false, mapData = false;
		try {
			name = placemark.getElementsByTagName("name")[0].childNodes[0].nodeValue;
			tableContent["name"] = name;
		}
		catch(e){
			name = "";
		}

		try {
			description = placemark.getElementsByTagName("description")[0].childNodes[0].nodeValue;			
		}
		catch(e){
			description = "";
		}

		try {
			place = placemark.getElementsByTagName("address")[0].childNodes[0].nodeValue;
			tableContent["place"] = place;
		}
		catch(e){
			place = "";
		}

		try {
			var coordinates = placemark.getElementsByTagName("Point")[0].getElementsByTagName("coordinates")[0].childNodes[0].nodeValue;
        	var lonlat = coordinates.split(",");
        	lon = lonlat[0];
        	lat = lonlat[1];
        	if( lon == "" || lat == "" || isNaN(lon) || isNaN(lat) ){
        		throw "e";
        	}
			mapData = true;
		}
		catch(e){
			if( !STIStatic.incompleteData ){
				continue;
			}
		}

		try {
			var tuple = STIStatic.getTimeData(placemark.getElementsByTagName("TimeStamp")[0].getElementsByTagName("when")[0].childNodes[0].nodeValue);
			if( tuple != null ){
				timeStart = tuple.d;
				timeEnd = timeStart;
				granularity = tuple.g;
				timeData = true;
			}
			if( timeStart == undefined && !STIStatic.incompleteData ){
				continue;
			}
		}
		catch(e){
			try {
				var timeSpanTag = placemark.getElementsByTagName("TimeSpan")[0];
				var tuple1 = STIStatic.getTimeData(timeSpanTag.getElementsByTagName("begin")[0].childNodes[0].nodeValue);
				timeStart = tuple1.d;
				granularity = tuple1.g;
				var tuple2 = STIStatic.getTimeData(timeSpanTag.getElementsByTagName("end")[0].childNodes[0].nodeValue );
				timeEnd = tuple2.d;
				if( tuple2.g > granularity ){
					granularity = tuple2.g;
				}
				timeData = true;
			}
			catch(e){
				if( !STIStatic.incompleteData ){
					continue;
				}
			}
		}
		var object;
		if( timeData && mapData ){
			object = new MapTimeObject(name, description, place, lon, lat, timeStart, timeEnd, granularity, 1, tableContent);
		}
		else if( mapData ){
			object = new MapObject(name, description, place, lon, lat, 1, tableContent);
		}
		else if( timeData ){
			object = new TimeObject(name, description, timeStart, timeEnd, granularity, 1, tableContent);
		}
		object.setIndex(index);
		index++;
		mapObjects.push(object);
    }
	return mapObjects;
}

/**
 * parses a KML-File and returns a Dataset object containing the KML-Placemarks as data objects
 * @param {URL} url URL of some KML-File
 */
STIStatic.loadKML = function(url){
	var xmlDoc = STIStatic.getXmlDoc(url);
    var elements = xmlDoc.getElementsByTagName("Placemark");
    if( elements.length == 0 ){
    	return null;
    }
    var index = 0;
    var dataset = new Dataset();
    for (var i = 0; i < elements.length; i++) {
        var placemark = elements[i];
		var name, description, place, timeStart, timeEnd, granularity, lon, lat;
		var weight = 1;
		var timeData = false, mapData = false;
		try {
			name = placemark.getElementsByTagName("name")[0].childNodes[0].nodeValue;
		}
		catch(e){
			name = "";
		}
		try {
			description = placemark.getElementsByTagName("description")[0].childNodes[0].nodeValue;
		}
		catch(e){
			description = "";
		}
		try {
			place = placemark.getElementsByTagName("address")[0].childNodes[0].nodeValue;
		}
		catch(e){
			place = "";
		}
		try {
			var coordinates = placemark.getElementsByTagName("Point")[0].getElementsByTagName("coordinates")[0].childNodes[0].nodeValue;
        	var lonlat = coordinates.split(",");
        	lon = lonlat[0];
        	lat = lonlat[1];
        	if( lon == "" || lat == "" || isNaN(lon) || isNaN(lat) ){
        		throw "e";
        	}
			mapData = true;
		}
		catch(e){
			if( !STIStatic.incompleteData ){
				continue;
			}
		}
		try {
			var tuple = STIStatic.getTimeData(placemark.getElementsByTagName("TimeStamp")[0].getElementsByTagName("when")[0].childNodes[0].nodeValue);
			if( tuple != null ){
				timeStart = tuple.d;
				timeEnd = timeStart;
				granularity = tuple.g;
				timeData = true;
			}
			if( timeStart == undefined && !STIStatic.incompleteData ){
				continue;
			}
		}
		catch(e){
			try {
				var timeSpanTag = placemark.getElementsByTagName("TimeSpan")[0];
				var tuple1 = STIStatic.getTimeData(timeSpanTag.getElementsByTagName("begin")[0].childNodes[0].nodeValue);
				timeStart = tuple1.d;
				granularity = tuple1.g;
				var tuple2 = STIStatic.getTimeData(timeSpanTag.getElementsByTagName("end")[0].childNodes[0].nodeValue );
				timeEnd = tuple2.d;
				if( tuple2.g > granularity ){
					granularity = tuple2.g;
				}
				timeData = true;
			}
			catch(e){
				if( !STIStatic.incompleteData ){
					continue;
				}
			}
		}
		var object;
		if( timeData && mapData ){
			object = new MapTimeObject(name, description, place, lon, lat, timeStart, timeEnd, granularity, 1);
		}
		else if( mapData ){
			object = new MapObject(name, description, place, lon, lat, 1);
		}
		else if( timeData ){
			object = new TimeObject(name, description, timeStart, timeEnd, granularity, 1);
		}
		object.setIndex(index);
		index++;
		dataset.addObject(object);
    }
    if( dataset.getObjects().length == 0 ){
    	return null;
    }
    return dataset;
}

STIStatic.getAbsoluteLeft = function(div,finalDiv){
	var left = 0;
	while( div ) {
		left += div.offsetLeft;
		div = div.offsetParent;
		if( finalDiv && finalDiv == div ){
			break;
		}
	}
	return left;
}

STIStatic.getAbsoluteTop = function(div,finalDiv){
	var top = 0;
	while( div ) {
		top += div.offsetTop;
		div = div.offsetParent;
		if( finalDiv && finalDiv == div ){
			break;
		}
	}
	return top;
}
