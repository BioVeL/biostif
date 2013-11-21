/**
 * Copyright (c) 2008-2009 The Open Source Geospatial Foundation
 * 
 * Published under the BSD license.
 * See http://svn.geoext.org/core/trunk/geoext/license.txt for the full text
 * of the license.
 */

/*
 * The code in this file is based on code taken from GeoExt Sandbox which in turn from OpenLayers.
 *
 * Copyright (c) 2006-2007 MetaCarta, Inc., published under the Clear BSD
 * license.  See http://svn.openlayers.org/trunk/openlayers/license.txt for the
 * full text of the license.
 */
 
(function() {

    /**
     * The relative path of this script.
     */
    var scriptName = "scriptloader.js";

    /**
     * Function returning the path of this script.
     */
    var getScriptLocation = function() {
        var scriptLocation = "";
        // If we load other scripts right before GeoExt using the same
        // mechanism to add script resources dynamically (e.g. OpenLayers), 
        // document.getElementsByTagName will not find the GeoExt script tag
        // in FF2. Using document.documentElement.getElementsByTagName instead
        // works around this issue.
        var scripts = document.documentElement.getElementsByTagName('script');
        for(var i=0, len=scripts.length; i<len; i++) {
            var src = scripts[i].getAttribute('src');
            if(src) {
                var index = src.lastIndexOf(scriptName); 
                // set path length for src up to a query string
                var pathLength = src.lastIndexOf('?');
                if(pathLength < 0) {
                    pathLength = src.length;
                }
                // is it found, at the end of the URL?
                if((index > -1) && (index + scriptName.length == pathLength)) {
                    scriptLocation = src.slice(0, pathLength - scriptName.length);
                    break;
                }
            }
        }
        return scriptLocation;
    };

    var jsfiles = new Array(  
    		"./utils/utils.js",
    		"./biovel.js",				//TODO eliminate all resize and data functions from this 
    		"./model/BiostifModel.js",
    		"./control/StifControl.js",
    		"./control/Application.js",    		
    		//"./utils/resizeUtils.js", //TODO
    		//"./utils/dataUtils.js",	//TODO
            //"../data/overlays.js",                     
           // "./control/Publisher.js",
            "./control/OverlayWidget.js" //TODO separate the view and controller
            
    );

    var agent = navigator.userAgent;
    var docWrite = (agent.match("MSIE") || agent.match("Safari"));
    if(docWrite) {
        var allScriptTags = new Array(jsfiles.length);
    }
    //var host = getScriptLocation() + "ux/";
    var host = getScriptLocation();
    for (var i=0, len=jsfiles.length; i<len; i++) {
        if (docWrite) {
            allScriptTags[i] = "<script src='" + host + jsfiles[i] +
                               "'></script>"; 
        } else {
            var s = document.createElement("script");
            s.src = host + jsfiles[i];
            var h = document.getElementsByTagName("head").length ? 
                       document.getElementsByTagName("head")[0] : 
                       document.body;
            h.appendChild(s);
        }
    }
    if (docWrite) {
    	
        document.write(allScriptTags.join(""));
    }
})();
