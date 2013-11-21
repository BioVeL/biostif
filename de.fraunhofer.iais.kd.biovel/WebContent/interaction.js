
var subscriptionId;
var application;
var fullscreenAnimation = null;

function reply(originalDataURIs, biovelDataURIs, resultURIs, maskID) {
	
    pmrpc.call({
        destination : "publish",
        publicProcedureName : "reply",
        params : [ "OK", {
            "sourceURIs" : originalDataURIs,
            "sourceCopyURIs" : biovelDataURIs,
            "resultURIs" : resultURIs,
            "maskID" : maskID
        } ],
        onSuccess : function() {
//            document.getElementsByTagName('body')[0].innerHTML = '<h1>BioSTIF Data submitted</h1>';

            document.getElementById('tavernaToolbar').innerHTML = '';
            var div = document.createElement('div');
            div.setAttribute("style", "position:absolute;right:0px;width:250px;");
            var label = document.createElement('label');
            label.setAttribute("class", "message");
            label.setAttribute("style", "height:50px;position:absolute;margin-top:20px");
            label.innerHTML = '<h1>BioSTIF Data submitted</h1>';
            div.appendChild(label);
            document.getElementsByTagName('body')[0].appendChild(div);
        
        },
        onFailure : function() {
//            document.getElementsByTagName('body')[0].innerHTML = '<h1>BioSTIF Data submission failed</h1>';
        	
            document.getElementById('tavernaToolbar').innerHTML = '';
            var div = document.createElement('div');
            div.setAttribute("style", "position:absolute;right:0px;width:250px;");
            var label = document.createElement('label');
            label.setAttribute("class", "message");
            label.setAttribute("style", "height:50px;position:absolute;margin-top:20px");
            label.innerHTML = '<h1>BioSTIF Data submission failed</h1>';
            div.appendChild(label);
            document.getElementsByTagName('body')[0].appendChild(div);
        }
    });
    return true;
}

function cancel() {
    pmrpc.call({
        destination : "publish",
        publicProcedureName : "reply",
        params : [ "Cancelled", {} ],
        onSuccess : function() {
//            document.getElementsByTagName('body')[0].innerHTML = '<h1>BioSTIF cancelled</h1>';

        	document.getElementById('tavernaToolbar').innerHTML = '';
            var div = document.createElement('div');
            div.setAttribute("style", "position:absolute;right:0px;width:250px;");
            var label = document.createElement('label');
            label.setAttribute("class", "message");
            label.setAttribute("style", "height:50px;position:absolute;margin-top:20px");
            label.innerHTML = '<h1>BioSTIF cancelled</h1>';
            div.appendChild(label);
            document.getElementsByTagName('body')[0].appendChild(div);
        
        },
        onFailure : function() {
//            document.getElementsByTagName('body')[0].innerHTML = '<h1>BioSTIF Cancellation failed</h1>';
        	
            document.getElementById('tavernaToolbar').innerHTML = '';
            var div = document.createElement('div');
            div.setAttribute("style", "position:absolute;right:0px;width:250px;");
            var label = document.createElement('label');
            label.setAttribute("class", "message");
            label.setAttribute("style", "height:50px;position:absolute;margin-top:20px");
            label.innerHTML = '<h1>BioSTIF cancellation failed</h1>';
            div.appendChild(label);
            document.getElementsByTagName('body')[0].appendChild(div);
        }
    });
    return true;
}

function readInputValues() {
	if (typeof(pmrpc) != "undefined") {
	    pmrpc.call({
	        destination : "publish",
	        publicProcedureName : "getInputData",
	        params : [],
	        onSuccess : function(retVal) {
	            //send BioSTIF the entry url 	        	
	            startBioStif(retVal);
	        }
	    });
	} else {
	  alert ("BioSTIF can't connect with communication frame. No data will be loaded.");	
	}
    return true;
};

function resizeSplitPane() {
	
    var width = $("#map").width();
	var height = $("#map").height();
	var topHeight = parseInt(document.getElementsByClassName("hsplitbar")[0].style.top);		
	
		    
	var width = (Math.floor(width / 10)) * 10 - 10;    
    var topHeight = (Math.floor(topHeight / 10)) * 10;
    var bottomHeight =height - topHeight;      
    
    resizeViews(width, topHeight, bottomHeight);
};

function resizeWindow() {	
		
	$("#map").height( $(window).height()-100); //.trigger("resize");
	var height = $("#map").height();
	//alert ("Resize Window: " + $(window).height() + ", map = " + height);
	$("#map").width($(window).width()-260); //.trigger("resize");
	var width = $("#map").width();
	//resizeSplitPane();
//	$("#splitPaneMap").height(height); //.trigger("resize");
//	$("#splitPaneMap").width(width); //.trigger("resize");	
//	
//	
	var halfsize = height/2;	
//	
	$("#splitPaneMap").trigger("resize", [halfsize]);
};

function startBioStif(retVal) {
	
	//todo change it to populate a pars variable and use the same 
	//method like initialize_application(pars,gui,
//	"mapContainerDiv","plotContainerDiv","tableContainerDiv","layerselectorDiv",
//	$("#map").width(),$("#map").height());
    
	try {
		
		var pars = {
				lang : 'en',			
				debug: 'true',
				url: retVal.returnValue.dataURI,
				contentType: retVal.returnValue.contentType,
				label: (retVal.returnValue.label?retVal.returnValue.label:""),				
				layers: (retVal.returnValue.layer?retVal.returnValue.layer:""),
				username: (retVal.returnValue.username?retVal.returnValue.username:""),
				//credential: (retVal.returnValue.credential?retVal.returnValue.credential:""),
				workflowRunId: (retVal.returnValue.workflowRunId?retVal.returnValue.workflowRunId:""),
				oauth_provider_url: (retVal.returnValue.oauth_provider_url?retVal.returnValue.oauth_provider_url:""),
				auth_header: (retVal.returnValue.auth_header?retVal.returnValue.auth_header:""),
				task:(retVal.returnValue.task?retVal.returnValue.task:"")
				// XXX rk hier input parameter
			};
		//alert ("init data: " + pars.url);		
		initialize_application(pars,gui,"mapContainerDiv","plotContainerDiv","tableContainerDiv","layerselectorDiv",
				$("#map").width(),$("#map").height());
		
//		
//		document.body.style.cursor = "wait";
//		fullscreenAnimation = fullscreen;
//		fullscreenAnimation.addFullscreen(fullscreenAnimation.loaderContent());
//    
//		var applicationUrl = document.documentURI;		
//		applicationUrl = applicationUrl.substring(0, applicationUrl.indexOf("interaction"));
//		
//		var dataURI = retVal.returnValue.dataURI;
//    
//	    var urls = (dataURI instanceof Array)?dataURI:dataURI.split(",");
//			
//		var width = $("#map").width();
//		var height = $("#map").height();
//		
//		$("#splitPaneMap").height(height);
//		$("#splitPaneMap").width(width);	
//	
//	                       
//	    application.currentLang = "en";
//	    application.exportServer = applicationUrl;
//	    var contentTypes = retVal.returnValue.contentType;
//	    application.contentTypes = (contentTypes instanceof Array)?contentTypes:contentTypes.split(",");
//	    
//	    var layers = (retVal.returnValue.layer?retVal.returnValue.layer:"");
//	    layers = (layers instanceof Array?layers:layers.split(","));
//	    
//	    application.layerContainer = "layerselectorDiv";
//		application.setUserLayers(layers);
//		
//	    var labels = (retVal.returnValue.label?retVal.returnValue.label:"");
//	    labels = (labels instanceof Array?labels:labels.split(","));
//	    
//	    if (urls.length != application.contentTypes.length) {
//	    	throw new Error ("the contentTypes array must be the same size like the url array");	    	
//	    }
//	    if (!labels || labels.length < urls.length) {
//	    	labels = new Array(urls.length);
//	    }
//	    application.setDataUrls(labels, urls);
//	    application.configureSTIF(gui, "mapContainerDiv", "plotContainerDiv", "tableContainerDiv", width, height);
//
//    
//	    if (fullscreenAnimation) {
//	    	fullscreenAnimation.removeFullscreen();
//	    }
//	    document.body.style.cursor = "default";
	} catch (e) {
		if (fullscreenAnimation) {
	    	fullscreenAnimation.removeFullscreen();
	    }
		alert ("BioSTiF could not start:  an error occurred: "  + e.message);		
		throw new Error ("BioSTiF could not start:  an error occurred: "  + e.message);
	}

};

//function startApplication() {
//	
//    application = new Application();
//    readInputValues();
//    //resizeSplitPane();
//};




function loadModulesAndStartApplication() {
//	fullscreenAnimation = fullscreen;
//	fullscreenAnimation.addFullscreen(this.fullscreenAnimation.loaderContent());
	
	
    if (typeof (console) != "undefined") {
        console.info("Load timer interval " + loadTimer);
    }
    if (typeof (Publisher) != "undefined" && (Publisher != null)) {
        subscriptionId = Publisher.Subscribe("StifReady", function(result) {
            if (loadTimer) {
                window.clearInterval(loadTimer);
                loadTimer = null;
                delete loadTimer;
                Publisher.Unsubscribe("StifReady", subscriptionId);
                if (result != null) {
                    if (typeof (console) != "undefined") {
                        console.info("Stif is ready: " + result);
                    }
                    if (typeof (STIStatic) == "undefined" || (!STIStatic)) {
                        throw new Error("STIF could not be loaded. STIStatic is not loaded");
                    }
                    if (typeof (gui) == "undefined" || (!gui)) {
                        throw new Error("STIF could not be loaded. The Application gui object is not instantiated");
                    }
                    startApplication();
                } else {
                    alert ("STIF could not be loaded. Application cannot start.\nPlease try to reload the application again.");
                    throw new Error ("STIF could not be loaded. Application cannot start");
                }
            }
        });

    }
}
