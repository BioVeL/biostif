
String.prototype.fulltrim=function(){
	return this.replace(/(?:(?:^|\n)\s+|\s+(?:$|\n))/g,'').replace(/\s+/g,' ');
};

RegExp.quote = function(str) {
    return (str+'').replace(/([.?*+^$[\]\\(){}|-])/g, "\\$1");
};

function logg (str) {
	if (typeof (console) != "undefined") {
        console.info(str);
    }
};

function writeToFullscreen (str) {
	var fullscreenlog = document.getElementById(FULLSCREEN_DIV_ID);
	var animation = null;

	if (typeof(fullscreenlog) == "undefined" || (!fullscreenlog)) {
		var animation = fullscreen;
		animation.addFullscreen(animation.loaderContent());
		fullscreenlog = document.getElementById(FULLSCREEN_DIV_ID);
	}
	
	if (typeof(fullscreenlog) != "undefined" && fullscreenlog) {
		fullscreenlog.innerHTML=str;
	}
};

function writeToFullscreenHeader (str) {
	var fullscreenheader  = $(FULLSCREEN_HEADER_DIV_ID);
	var animation = null;

	if (typeof(fullscreenheader) == "undefined") {
		var animation = fullscreen;
		animation.addFullscreen(animation.loaderContent());
		fullscreenheader  = $(FULLSCREEN_HEADER_DIV_ID);
	}
	
	if (typeof(fullscreenheader) != "undefined" && fullscreenheader) {
		fullscreenheader.html(str);
	}
};    
	
function stopFullscreen () {
	try {
		if (typeof(fullscreen) != "undefined" && fullscreen && fullscreen.fullscreens.length > 0) {
			fullscreen.removeFullscreen();
		}
	} catch (error) {
		logg("removeFullscreen can not be performed, probably no fullscreen to remove: " + error.message);
	}
};

//initialize text variables for messages and errors
function loadBundles(lang) {
	$.i18n.properties({
	    name:'messages', 
	    path:'bundle/', 
	    mode:'map',
	    language:lang, 
	    callback: function() {
	        ; //nothing to do
	        logg ("Bundles read for language: " + lang + ": " + $.i18n.prop('msg_hello'));			        
	    }
	});
};

/**
 * The function allows to parse a json string to get a record with particular keys or all keys, if keyList is omitted
 * precondition jQuery must be available 
 * @param jsonstring: string containing json to be parsed
 * @param keyList
 */
function parseJSON (jsonstring, keyList) {
	
	if (!$) {				
		throw new Error ($.i18n.prop('msg_error_no_library','JQuery'));
	}
	var item;
	try {
		item = $.parseJSON(jsonstring);
	} catch (e) {
		logg("Error parsing json: " + jsonstring + "\n" + e.message);
		throw new Error ($.i18n.prop('msg_error_application_start_failed',$.i18n.prop('msg_error_config_wrong_format','Data', 'JSON')));
	}
	
	if (typeof (keyList) == "undefined" || keyList.length == 0) {
		return item;
	}
	var result = [];
	for (var i= 0; i < keyList.length; i++) {
		result[keyList[i]] = item[keyList[i]];
	}
	
//	muß so abgefragt werden
//	for(var l in result){
//		alert(" i : " + l + " value:" + result[l]);
//	}
	
	return result;
};

/**
 * this function looks of all elements of the array to see if at list one of the array elements
 * coincides with the beginning of the string, to be used e.g. to test if a url (string) is located at 
 * one of the server adresses contained in the array
 * @param array: Array of string, e.g. server adresses
 * @param string. String in wich one of the elements of the array should appear at the beginning
 * @returns else: if one of the elements of the array coincides with the start of the string, else false
 */
function isStartInArray (array, string) {
	for (var i in array) {
		if (string.indexOf(array[i]) == 0) {
			return true;
		};		
	}
	return false;
};

function resizeViews(width, topHeight, bottomHeight) {
	
	// Configure Stif settings
//    var containerWidth = (Math.floor(width / 10)) * 10 - 10;    
//    var containerHeight = (Math.floor(height / 10)) * 10 - 10;
//    containerHeight = containerHeight / 2 - 10;
    
   // document.getElementsByClassName("hsplitbar")[0].style.top = containerHeight;
	
	
    STIStatic.applySettings({
        mapWidth : width + "px", // false or desired width of the map
        mapHeight : topHeight + "px", // '580px',
        tableWidth : width + "px", // false or desired width of the table
        tableHeight : bottomHeight + "px",//bottomHeight + "px", // '150px of plot container - 50 px for header'        
		timeplotWidth:	width + "px"//,	// false or desired width of the timeplot		
	});
    document.getElementsByClassName("hsplitbar")[0].style.width = STIStatic.mapWidth;
    
    if (typeof(gui) != "undefined" && gui) {
    	gui.mapContainer.style.width = STIStatic.mapWidth;
    	gui.mapContainer.style.height = STIStatic.mapHeight;
    	
    	if (typeof(gui.map) != "undefined" && gui.map) {
    		gui.map.mapWindow.style.width = STIStatic.mapWidth;
    		gui.map.mapWindow.style.height = STIStatic.mapHeight;
    		
    		var canvas = gui.map.mapWindow.getElementsByClassName("mapCanvas")[0];
        	gui.map.configureCanvas (canvas, gui.map.mapWindow.clientWidth, gui.map.mapWindow.clientHeight);
        	
        	gui.map.openlayersMap.div.style.width = STIStatic.mapWidth;
    		gui.map.openlayersMap.div.style.height = STIStatic.mapHeight;
        	gui.map.openlayersMap.updateSize();
        	        	
    	}
    	
    	if (typeof(gui.tableContainer) != "undefined" && gui.tableContainer) {
	    	gui.tableContainer.style.width = STIStatic.tableWidth;
	    	gui.tableContainer.style.height = STIStatic.tableHeight;
	    	var scrollheight = bottomHeight - 130 + "px";	
	    	
//	    	if (typeof (console) != "undefined") {
//		        console.info ("scrollheight 2: " + scrollheight);
//		    }  
	    	$(".scrollableTable").attr('style',"width: " + STIStatic.tableWidth+ "; height:" + scrollheight+";");
	    	
    	}
    	
    	if (typeof(gui.plotContainer) != "undefined" && gui.plotContainer) {
    		
    		//$("#plotContainer").attr('style',"width: " + STIStatic.tableWidth+ ";");
    		gui.plotContainer.style.width = STIStatic.tableWidth;
	    	gui.plot.plotWindow.style.width = STIStatic.tableWidth;
	    	gui.plot.timeplotDiv.style.width = (width-32)+"px";
	    	var plotcanvas = gui.plot.plotWindow.getElementsByClassName("plotCanvas")[0];
        	gui.plot.configureCanvas (plotcanvas, gui.plot.plotWindow.clientWidth, gui.plot.plotWindow.clientHeight);
        	gui.plot.redrawPlot();
        	
	    	//$("#plotWindow").attr('style',"width: " + STIStatic.tableWidth+ ";");
	    	//$("#plotContainer").attr('style',"width: " + STIStatic.tableWidth+ ";");
//	    	$(".plotCanvas").attr('style',"width: " + STIStatic.tableWidth+ ";");
	    	  	
    	}    		      	
    	    	
    }
	
}; 

function resizePlotTableViews(width, topHeight, bottomHeight) {
	// Configure Stif settings
//    var containerWidth = (Math.floor(width / 10)) * 10 - 10;    
//    var containerHeight = (Math.floor(height / 10)) * 10 - 10;
//    containerHeight = containerHeight / 2 - 10;
    
   // document.getElementsByClassName("hsplitbar")[0].style.top = containerHeight;
	
    STIStatic.applySettings({        
        tableWidth : width + "px", // false or desired width of the table
        tableHeight : bottomHeight - 100 + "px", // '580px'        
		timeplotWidth:	width + "px",	// false or desired width of the timeplot
		timeplotHeight:	topHeight + 'px'	// false or desired height of the timeplot
	});
    
    if (typeof(gui) != "undefined" && gui) {	
    	if (typeof(gui.tableContainer) != "undefined" && gui.tableContainer) {
	    	gui.tableContainer.style.width = STIStatic.tableWidth;
	    	gui.tableContainer.style.height = STIStatic.tableHeight;
	    	var scrollheight = bottomHeight - 130 + "px";	    	
	    	$(".scrollableTable").attr('style',"width: " + STIStatic.tableWidth+ "; height:" + scrollheight+";");
	    	
	    		    	
    	}
    	
    	if (typeof(gui.plotContainer) != "undefined" && gui.plotContainer) {
	    	gui.plotContainer.style.width = STIStatic.timeplotWidth;	
	    	gui.plotContainer.style.height = STIStatic.timeplotHeight;	
	    	
	    	var canvas = gui.plot.plotWindow.getElementsByClassName("plotCanvas")[0];
        	gui.plot.configureCanvas (canvas,gui.plot.plotWindow.clientWidth, gui.plot.plotWindow.clientHeight);
        		    	
    	}    		
    	    	  	
    	    	
    }
	
}; 



function getMessages(lang){
	
	var req;
	if (window.XMLHttpRequest){// code for IE7+, Firefox, Chrome, Opera, Safari
		req=new XMLHttpRequest();
	}
	else {// code for IE6, IE5
		req=new ActiveXObject("Microsoft.XMLHTTP");
	}
	
	req.open("GET","servlet?i18n=2&lang=" + lang,false);
	req.send("");
	var res = req.responseText;
	return eval("(" + res + ")" );
};


function i18n(key){
	
	var val = application.i18n[key];
	if( val == null ){
		val = '!_' + key + '_!'; 
	}
	return val;
};


// IE does not have indexOf for arrays :-( ,so we append 
if(!Array.indexOf){
   Array.prototype.indexOf = function(obj){
	   	for(var i=0; i<this.length; i++){
	   		if(this[i]===obj){
	   			return i;
	   		}
	      }
	      return -1;
	   };
};

function wordwrap( str, width, brk, cut ) {
    brk = brk || '\n';
    width = width || 75;
    cut = cut || false;
 
    if (!str) { return str; }
 
    var regex = '.{1,' +width+ '}(\\s|$)' + (cut ? '|.{' +width+ '}|.+$' : '|\\S+?(\\s|$)');
 
    return str.match( RegExp(regex, 'g') ).join( brk );
 
};

function isValidNumber(num){
	  // regular expression to test if field contains alpha
	  // expand accordingly to include any other non numeric character
	  isAlpha = /[(A-Z)|(a-z)]/ ;	
	  if( isAlpha.test(num)) {
	    // not a number, return 0
	    return 0;
	  }
	  // num is a number, return 1
	  return 1;
};

/**
 * loads KML, DwC (xml) and json data from URL and converts it to STIF data array 
 */
function getDataFromUrl(dataUrl, contentType, label) {
	var data;
		
	if (typeof (console) != "undefined") {
        console.info ("utils: get Data from url " + dataUrl + " mit type " + contentType);        
    }
	var fullscreenlog = document.getElementById("FullscreenWindowLog");
	if (typeof(fullscreenlog) != "undefined" && fullscreenlog) {
		fullscreenlog.innerHTML="Download data (" + dataUrl + ")";
	}
	try {		
		if (((contentType) && (contentType=="kml")) ||(dataUrl.indexOf(".kml") > 0 || (dataUrl.indexOf("format=kml") > 0))) {
			if ((dataUrl.indexOf("http://")>=0 || dataUrl.indexOf("https://") >= 0) &&
				(dataUrl.indexOf("biovel.iais.fraunhofer.de")<0 && dataUrl.indexOf("localhost") < 0)) {
				dataUrl = "proxy?url=" + dataUrl;
			}
	        if (typeof (console) != "undefined") {
	            console.info ("STIStatic.loadSpatioTemporalKMLData: " + dataUrl);
	        }
			data = STIStatic.loadSpatioTemporalKMLData(dataUrl);
		} else if (((contentType) && (contentType=="dwc")) ||(dataUrl.indexOf(".xml") == (dataUrl.length-4) ||//it is assumed that it is DwC
				(dataUrl.indexOf("format=darwin")>0))) { 
			
	        if (typeof (console) != "undefined") {
	            console.info ("getJsonFromDwCFile: " + dataUrl);
	        }
			var jsonString = getJsonFromSourceFile(dataUrl, "Dwc2Json");
			var jsonData = $.parseJSON(jsonString);
			data = STIStatic.loadSpatialJSONData(jsonData);	
		} else if (((contentType) && (contentType=="csv")) ||(dataUrl.indexOf(".txt") > (0) )) { 
			
			logg("utils.js contentType = CSV");

			var jsonString = getJsonFromSourceFile(dataUrl, "Csv2Json?popuplabel=nameComplete");
			var jsonData = $.parseJSON(jsonString);
			data = STIStatic.loadSpatioTemporalJSONData(jsonData);	
			if (jsonData.length != data.length) {
				var source = label || dataUrl;
				alert ("Only " + data.length + " from " + jsonData.length + " records with valid information were loaded from the source " + source);
			}
		} else if (((contentType) && (contentType=="json")) ||(dataUrl.indexOf(".json") == (dataUrl.length-5))) {
	        if (typeof (console) != "undefined") {
	            console.info ("getJsonFile: " + dataUrl);			
	        }
			var jsonData = STIStatic.getJsonFile(dataUrl);
			data = STIStatic.loadSpatioTemporalJSONData(jsonData);
		}
		
	} catch (e) {		
		throw new Error ("Error on loading data from " + dataUrl + ": " + e.message);
	}	
    if (typeof (console) != "undefined") {
        //console.info("Data Array is " + (((!data) || data.length == 0)?"empty":data.join()));
    }
	return data;	
};

/**
 *  converts DwC data into json
 * @param dataUrl: Url to the DwC data
 */
function getJsonFromSourceFile(dataUrl, transformServiceUrl) {
	var paramName = "source";
	var concatenator = (transformServiceUrl.indexOf("?")>0?"&":"?");
	var requestUrl = transformServiceUrl+ concatenator + paramName + "=" + dataUrl;
	
	var client;
	var response;
	
	var fullscreenlog = document.getElementById("FullscreenWindowLog");
	if (typeof(fullscreenlog) != "undefined" && fullscreenlog) {
		fullscreenlog.innerHTML="Trsansforming data to JSON";
	}
	
	client = restClient (requestUrl, "POST");
	response = client.responseText;
	
//    logg("Get JSON rest client result status: " + client.status + ": "); // + response);
    
	if (client.status == 415) {
		response = client.responseText;	
		throw new Error ("Error on converting data to JSON: The data sent was not in the right format: " + response);
	} else if (client.status != 200) {		
        throw new Error ("Error on converting data to JSON (service status: " + client.status + ")");        
	}
	
	return response;
	
};

function uploadData(data, dataUrl, contenttype, location, username, workflowrunid) {
	
	logg(data + " - "+ dataUrl + " - "+ contenttype + " - "+ location + " - "+ username + " - "+ workflowrunid);
	
	var workspaceStoreParameter = "?username="+username+"&workflowid="+workflowrunid+"&suffix="+contenttype; 
	
	var dataManagerService = "../workflow/rest/data"; //../workflow/rest/data";
	var client;	
	var method = "POST";
	var headers = [];
//	if (contenttype) {
//		headers.push(["Content-Type","\"" + contenttype + "\""]);
//	}
	
//	logg("dataManagerService: " + dataManagerService);
//	logg("method: " + method);
	
	
	
	if (!location && !dataUrl) {
		logg("no loc and dataurl");
		client = restClient (dataManagerService+workspaceStoreParameter, method, headers, (data==null?"":data));		
	} else if (dataUrl) {
		if (dataManagerService.indexOf("proxy?")==0) {
			location = dataManagerService + workspaceStoreParameter + encodeURIComponent("&source="+dataUrl);
		} else {
			location = dataManagerService+ workspaceStoreParameter +"&source=" + encodeURIComponent(dataUrl);
		}
		
		client = restClient(location, method, headers, (data==null?"":data));
	}
	if (!client) {
		throw new Error ("Error uploading data: Missing parameters");
	}
	
	if (client.status == 201) {
		location = client.getResponseHeader("Location");
	} else if (client.status == 200) {
		location = client.getResponseHeader("Location");
	} else {
		throw new Error ("Rest Request failed for sending data to location with status " + client.status);		
	}
		
	return location;
}
/**
 * 
 * @param url: the url to the REST API
 * @param method: the Rest Method as String use "GET", "PUT", "DELETE", "POST"
 * @param headers: a list containing header elements in form of a 2 element list [0]: headertype, [1]: headercontent 
 * @param content
 * @return rest client object, from which the result code and the response can be obtained
 */
function restClient(url, method, headers, content) {
//    if (typeof (console) != "undefined") {
//        console.info ("RestClient called with url= " + url + ", method = " + method + ", headers = " + headers + "content =" + content);
//    }
	if (!url) {
		throw new Error ("restClient needs a url parameter");
	}
	
	if (!method) {
        if (typeof (console) != "undefined") {
            console.info ("restClient has no method set, get will be used");
        }
		method = "GET";
	}
	var client = new XMLHttpRequest();	
	//encodeURI(url) needed?
	try {
		client.open(method, url, false);
	
		if (typeof(headers) != "undefined" && headers) {
			for (i in headers) {
				header = headers[i];				
				client.setRequestHeader(header[0], header[1]);	
		        if (typeof (console) != "undefined") {
//		            console.info("Header requests sent: " + header[0] + ": " + header[1]);
		        }
			}		
		}
	
        if (typeof (console) != "undefined") {
            //console.info ("send content " + content + " over " + method);
        }
		if (typeof(content) != "undefined" && content && method != "GET" && method != "DELETE") {
//			logg("content: " + content);
			
			client.send(content);
		} else {
			client.send("");
		}
	} catch (err) {
        if (typeof (console) != "undefined") {
            console.info ("restClient error: " + err.message);
        }
	}
    if (typeof (console) != "undefined") {
        //console.info("restClient sent " + method + " request to " + url + " and got response status " + client.status + ": " + client.statusText);
    }
	return client;

};

/**
 * searches for parameters in the url having a bbox information
 * parameters searched are bbox (OGC standard for wms, wfs) and minlatitude, maxlatitude, minlongitude, maxlongitude (GBIF Occurrences Rest interface)
 * @param url
 * @return a bbox as string (minx, miny, maxx, maxy) or an empty string of no bbox info found
 */
function parseBBoxFromUrl(url) {
	
	var requestPars = (url.indexOf("?")>0?url.substring(url.indexOf("?")+1):"");
	var parArray = requestPars.split("&");
	if (!parArray || parArray.length <= 1) {
		return "";
	}
	var bbox = new Array(4);
	for (var i in parArray) {
		var parval = parArray[i].split("=");
		if (!parval[0]) {
			continue;
		}
		if (parval[0].toLowerCase() =="bbox") {
			bbox = parval[1].split(",");
			return bbox;
		}
		if (parval[0].toLowerCase()=="minlongitude") {
			bbox[0] = parseFloat(parval[1]);
		} else if (parval[0].toLowerCase()=="minlatitude") {
			bbox[1] = parseFloat(parval[1]);
		} else if (parval[0].toLowerCase() =="maxlongitude") {
			bbox[2] = parseFloat(parval[1]);
		} else if (parval[0].toLowerCase() =="maxlatitude") {
			bbox[3] = parseFloat(parval[1]);
		}
	}
	if (!bbox[0]) {
		return [];
	}
	return bbox;
};

/**
 * changes parameters in the url having a bbox information
 * parameters searched are bbox (OGC standard for wms, wfs) and minlatitude, maxlatitude, minlongitude, maxlongitude (GBIF Occurrences Rest interface)
 * @param url as string (not encoded)
 * @param a bbox as string (minx, miny, maxx, maxy) or an empty string of no bbox info found
 * @return url as string
 */
function changeBBoxInUrl(url, bbox) {
	
	var index = url.indexOf("?") + 1;
	var requestPars = (index>0?url.substring(index):"");
	
	var parArray = requestPars.split("&");
	if (!parArray || parArray.length <= 1) {
		return url;
	}
	var newUrl = url.substr(index);
	var bboxArray = bbox.split(",");
	for (var i in parArray) {
		var parval = parArray[i].split("=");
		if (parval[0].toLowerCase().equals("bbox")) {
			parval[1] = bbox;			
		}
		if (parval[0].toLowerCase().equals("minlongitude")) {
			parval[1] = bbox[0];
		} else if (parval[0].toLowerCase().equals("minlatitude")) {
			parval[1] = bbox[1];
		} else if (parval[0].toLowerCase().equals("maxlongitude")) {
			parval[1] = bbox[2];
		} else if (parval[0].toLowerCase().equals("maxlatitude")) {
			parval[1] = bbox[3];
		}
		newUrl+=parval[0]+"="+parval[1]+"&";
	}
    if (typeof (console) != "undefined") {
        console.info ("Url changed: old = " + url + ", new =" + newUrl);
    }
	return newUrl.substr(0,newUrl.length-1);
};

function userAction(actiontype) {
	//if (actiontype == this.actionType.ok || actiontype == this.actionType.cancel) {
    if (typeof (console) != "undefined") {
        console.info("ButtonClicked " + actiontype);
    }
	if (application) {
	    if (actiontype == actionType.OK) {
	        application.exitApplicationOK();
	    } else if (actiontype == actionType.CANCEL) {
	        application.exitApplicationCancel();
	    }
	}
};



