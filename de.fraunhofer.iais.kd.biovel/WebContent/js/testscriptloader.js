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
    var scriptName = "testscriptloader.js";

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
    		"./utils/resizeUtils.js",
    		"./utils/dataUtils.js",
    		"../biovel.js",    		
    		"./utils/FullscreenWindow.js",
    		"./control/Application.js",            
            "../data/overlays.js",
            "./model/BoundingBox.js",         
           // "./control/Publisher.js",
            "./control/BoundingBoxControl.js",
            "./control/OverlayWidget.js"
            
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
