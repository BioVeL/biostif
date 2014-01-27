
var subscriptionId;
var application;
var fullscreenAnimation = null;

function reply(originalDataURIs, biovelDataURIs, resultURIs) {
    pmrpc.call({
        destination : "publish",
        publicProcedureName : "reply",
        params : [ "OK", {
            "sourceURIs" : originalDataURIs,
            "sourceCopyURIs" : biovelDataURIs,
            "resultURIs" : resultURIs
        } ],
        onSuccess : function() {
            document.getElementsByTagName('body')[0].innerHTML = '<h1>BioSTIF Data submitted</h1>';
        },
        onFailure : function() {
            document.getElementsByTagName('body')[0].innerHTML = '<h1>BioSTIF Data submission failed</h1>';
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
            document.getElementsByTagName('body')[0].innerHTML = '<h1>BioSTIF Cancelled</h1>';
        },
        onFailure : function() {
            document.getElementsByTagName('body')[0].innerHTML = '<h1>BioSTIF Cancellation failed</h1>';
        }
    });
    return true;
}

/**
 * @returns {Boolean}
 */
function requestAuthenticate() {
    if (typeof(pmrpc) != "undefined") {
        pmrpc.call({
            destination : "publish",
            publicProcedureName : "getInputData",
            params : [],
            onSuccess : function(retVal) {
                callAuthenticate(retVal);
            }
        });
    }
    return true;
}

function callAuthenticate(retVal) {
    
    fullscreenAnimation = fullscreen;
    fullscreenAnimation.addFullscreen(fullscreenAnimation.loaderContent());
    
    var dataURI = retVal.returnValue.dataURI;
    
    //   var result = getDataFromUrl(dataURI);

    //  var item = $.parseJSON(result);
    var urls = dataURI;//item.dataURI || "";
    //  var contentType = item.contentType || "";

    //on ready, make sure the widget is resized
    $("#map").height($(window).height() - 120).trigger("resize");
    $("#map").width($(window).width() - 40).trigger("resize");
    //application.i18n = getMessages(pars.lang);
    application.currentLang = "en";
    //application.interactionUrl = interactionUrl;
    application.contentTypes = retVal.returnValue.contentType.split(",");
    application.setDataUrls(urls);
    application.configureSTIF(gui, "mapContainerDiv", "plotContainerDiv", "tableContainerDiv", $("#map").width(), $(
    "#map").height());

    if (fullscreenAnimation) {
        fullscreenAnimation.removeFullscreen();
    }

};

function startApplication() {
    
    application = new Application();
    readInputValues();
};

