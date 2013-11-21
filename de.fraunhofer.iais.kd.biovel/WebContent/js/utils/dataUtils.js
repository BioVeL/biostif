
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


/**
 * loads KML, DwC (xml) and json data from URL and converts it to GeoTemCo dataset 
 */
function getDatasetFromUrl(dataUrl, contentType, label) {
	var data;
		
	if (typeof (console) != "undefined") {
        console.info ("dataUtils: get Data from url " + dataUrl + " of type " + contentType);        
    }
	var fullscreenlog = document.getElementById("FullscreenWindowLog");
	if (typeof(fullscreenlog) != "undefined" && fullscreenlog) {
		fullscreenlog.innerHTML="Download data (" + dataUrl + ")";
	}
	try {		
		if (contentType.toLowerCase()=="kml") {
			var kmlData = GeoTemConfig.getKml(dataUrl);
			data = GeoTemConfig.loadKml(kmlData);				       
		} else { //use shim service to transform the Data
			var transformService = "";
			var jsonData;
			if (contentType.toLowerCase()=="json") {
				jsonData = GeoTemConfig.getJson	(dataUrl);						
			} else if (contentType.toLowerCase()=="dwc") {	
				transformService = SHIMSERVICEACTIONS.DWC2JSON;
			} else if (contentType.toLowerCase()=="csv"){
				transformService = SHIMSERVICEACTIONS.CSV2JSON;
			} else {
				throw new Error ("Transformer not implemented for ContentType: " + contentType);
			}
			
			if (transformService.length > 0) {
				var jsonString = getJsonFromSourceFile(dataUrl, transformService);
				jsonData = $.parseJSON(jsonString);
			}
			
			data = GeoTemConfig.loadJson(jsonData);						
		}
		
	} catch (e) {		
		throw new Error ("Error on loading data from " + dataUrl + ": " + e.message);
	}	
//    if (typeof (console) != "undefined") {
//        console.info("Data Array is " + (((!data) || data.length == 0)?"empty":data.join()));
//    }
    return new Dataset(data,label);		
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
	
    if (typeof (console) != "undefined") {    	
        console.info("Get JSON rest client result status: " + client.status); // + ": " + response);
    }	
	if (client.status == 415) {
		response = client.responseText;	
		throw new Error ("Error on converting data to JSON: The data sent was not in the right format: " + response);
	} else if (client.status != 200) {		
        throw new Error ("Error on converting data to JSON (service status: " + client.status + ")");        
	}
	
	return response;
	
};

function uploadData(data, dataUrl, contenttype, location, workspaceid, workflowrunid) {
	var dataManagerService = "../workflow/rest/data"; //../workflow/rest/data";
	var client;	
	var method = "POST";
	var headers = [];
	if (contenttype) {
		headers.push(["Content-Type","\"" + contenttype + "\""]);
	}
	if (!location && !dataUrl) {	
		client = restClient (dataManagerService, method, headers, (data==null?"":data));		
	} else if (dataUrl) {
		if (dataManagerService.indexOf("proxy?")==0) {
			location = dataManagerService + encodeURIComponent("?source="+dataUrl);
		} else {
			location = dataManagerService+"?source=" + encodeURIComponent(dataUrl);
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
 */
function restClient(url, method, headers, content) {
	
    if (typeof (console) != "undefined") {
        console.info ("dataUtils RestClient called with url= " + url + ", method = " + method + ", headers = " + headers + "content =" + content);
    }
    
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
		            console.info("Header requests sent: " + header[0] + ": " + header[1]);
		        }
			}		
		}
	
        if (typeof (console) != "undefined") {
            //console.info ("send content " + content + " over " + method);
        }
		if (typeof(content) != "undefined" && content && method != "GET" && method != "DELETE") {
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



