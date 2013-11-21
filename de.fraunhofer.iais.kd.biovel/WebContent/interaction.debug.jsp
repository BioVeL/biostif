<%@page import="java.util.Locale"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.FileReader"%>
<%@page import="de.fraunhofer.iais.kd.biovel.i18n.I18nUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>

<%!
//String helpPageURL = "help/en/BioSTIF_manual.html";//"https://wiki.biovel.eu/display/biovelfriends/About+BioSTIF";
String helpPageURL = "https://wiki.biovel.eu/display/doc/BioSTIF+User+Manual";
String version = "";
String logoURL = "http://www.biovel.eu";
%>

<fmt:setLocale value="en" scope="session" />

<%
String applicationPath = application.getRealPath("/"); //session.getServletContext().getRealPath("/biostif");
String txtFilePath = applicationPath+ "/version.txt";
BufferedReader reader = new BufferedReader(new FileReader(txtFilePath));
StringBuilder sb = new StringBuilder();
String line;

if ((line = reader.readLine()) != null) {
   version = line;
   version = version.substring(0,version.indexOf("_"));
}
reader.close();
//helpPageURL = applicationPath + "/" + helpPageURL;
%>

<!-- this bundle is still for application toolbar (help, ok, cancel) 
//TODO:change it with the local bundle, 
//TODO also change the stif/geotempco texts through the i18 jquery bundles -->

<fmt:setBundle
	basename="de.fraunhofer.iais.kd.biovel.i18n.Messages"
	scope="session" />

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
<title>BioVeL Map Client <%= version %></title>
<meta name="description" content="BioSTIF: Spatial Temporal Interactive interFace for Biodiversity data">
<meta name="keywords" content="spatio-temporal, map viewer, time viewer, interactive spatio-temporal viewer, spatial temporal interface, STIF, biodiversity data, occurrences viewer">
<meta charset=utf-8 />
<meta http-equiv="content-type" content="text/html;charset=UTF-8" />
<!-- cache corrector -->
<meta http-equiv="cache-control" content="no-cache, no-store, max-age=0, must-revalidate" />
<meta http-equiv="pragma" content="no-cache" />
<meta http-equiv="expires" content="Fri, 01 Jan 1990 00:00:00 GMT" />

<link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon" />
<link type="text/css" href="jslib/jquery/css/flick/jquery-ui-1.8.21.custom.css" rel="Stylesheet" />
<link rel="stylesheet" type="text/css" href="css/biovel.css" />


<script type="text/javascript" src="jslib/jquery/jquery.min.1.4.1.js"></script>
<script type="text/javascript" src="jslib/jquery/jquery-ui-1.8.21.min.js"></script>
<script type="text/javascript" src="jslib/jquery/jquery.i18n.properties-1.0.9.js"></script>
<script type="text/javascript" src="jslib/jquery/urldecoder.js"></script>
<script type="text/javascript" src="jslib/jquery/splitter.js"></script>
<!-- These files must be read here, because the application does not react from the script loader //TODO check -->
<script type="text/javascript" src="js/utils/utils.js"></script>
<script type="text/javascript" src="js/biovel.js"></script>
<script type="text/javascript" src="js/scriptloader.js"></script>
<script type="text/javascript" src="jslib/pmrpc/pmrpc.js"></script>
<script src="interaction.js"></script>

<script src="../stif/js/core/Publisher.js"></script>
<script src="../stif/js/build/loader/DynaJsLoader.js"></script>
<script src="../stif/js/build/loader/STILoader.js"></script>  
	
	 

<script type="text/javascript">
	var application;
    var loadTimer;

   
	
    function runApplication() {
    	$("#splitPaneMap").splitter({
			type: "h",
			outline: true,
			title: "<fmt:message key="splitBarHelpText" />",
			//minLeft: 500, sizeLeft: 400, minRight: 325,
			minTop: 80, 			
			outline: true,
			//resizeToWidth: true,
			accessKey: 'I'
		});
    	    	    	
    	var mapwidth = $(window).width()  -40 -210;
        var mapheight = $(window).height();
	    
		//on ready, make sure the widget is resized
		$("#map").height(mapheight); //.trigger("resize");
		$("#map").width(mapwidth); //.trigger("resize");	
			
		$("#splitPaneMap").height( mapheight); //.trigger("resize");
		$("#splitPaneMap").width(mapwidth); //.trigger("resize");	
		var halfsize = (mapheight)/2;
		
		$("#splitPaneMap").trigger("resize", [halfsize]);	 	
		
	   	//loadTimer = window.setInterval( loadModulesAndStartApplication, 500);
	   	if (typeof (Publisher) != "undefined" && (Publisher != null)) {
	   		subscriptionId = Publisher.Subscribe(BIOSTIF_EVENT_STIF_READY, 
			        function(result) {			
						if (loadTimer) {
							window.clearInterval(loadTimer);
							loadTimer = null;
				 			delete loadTimer;
						}
						Publisher.Unsubscribe(BIOSTIF_EVENT_STIF_READY, subscriptionId);
						if (result != null) {			
							if (typeof (STIStatic) == "undefined" || (!STIStatic)) {
								throw new Error (jQuery.i18n.prop('msg_error_application_start_failed','STIStatic was not loaded'));
							}
							if (typeof (gui) == "undefined" || (!gui)) {
								throw new Error (jQuery.i18n.prop('msg_error_application_start_failed', '. The Application gui object is not instantiated'));
							}
							writeToFullscreenHeader($.i18n.prop('msg_fullscreen_starting'));
							
						    
						    loadBundles('en');
						   
							readInputValues();
							/* initialize_application(pars,gui,
									"mapContainerDiv","plotContainerDiv","tableContainerDiv","layerselectorDiv",
									$("#map").width(),$("#map").height()); */
						} else {
							alert ($.i18n.prop('msg_error_application_start_failed', '. Please try reloading again.'));
							throw new Error ($.i18n.prop('msg_error_application_start_failed', '"BioSTIF could not be loaded'));
						}
					}
			        
			    );
/*         	subscriptionId = Publisher.Subscribe("StifReady", function(result) {
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
                    alert ("STIF could not be loaded. Application cannot start.\n Please try to reload the application again.");
                    throw new Error ("STIF could not be loaded. Application cannot start");
                }
            });
 */
    	}	
	   	
	   	$("#splitPaneMap").resize( function(e) {
	   		//e.preventDefault()
	   		e.stopImmediatePropagation();
	   		e.stopPropagation();
	   		resizeSplitPane();
					
		});
		
		$(window).resize( function(e) {
			e.preventDefault();
			e.stopImmediatePropagation();
			e.stopPropagation();
			resizeWindow();			
			
		});
    }
 
    //window.onload = runApplication;
    
    
         
</script>
</head>

<body  bgcolor="white">
  <h2></h2>
<noscript>
      <div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
        Your web browser must have JavaScript enabled in order for this application to display correctly.
      </div>
</noscript>

<!-- 
<div id="loadingBlocker" style="width:100%; height: 100%; position:fixed; background-color: gray; opacity: 0.75;z-index: 1000;filter: alpha(opacity=55); margin-left: -42px; ">
<div style="color:white; position: fixed; left:50%; top:50%;"> <img src="img/loading.gif" >&nbsp;&nbsp;<fmt:message key="loading" /></div></div>
-->

<!-- invisible message for accessibility-->
 
<h1 style="position: absolute;top: -500px"><fmt:message key="main_overview_text" /></h1>
<table width="100%" height="100%" border="0" cellspacing="0" cellpadding="0" >
 
<div id="mainToolbar">	
		<span id="logo">
			<a target="_blank" href="<%=logoURL%>"><img alt="BioVeL Logo" src="img/logo_without_name_small.jpg" align="bottom"/></a>
		</span>
		<span id="logosite">
			<span class="logomaj">B</span>
			io
			<span class="logomaj">STIF</span>	
			<span class="logopetit">Biodiversity Spatial Temporal Interactive interface</span>
			<span class="version">Version <%=version%> 
			<span>&nbsp;&nbsp;</span>
			<a target="_blank" title="<fmt:message key="openHelpLinkText_tooltip"/>" href="<%= helpPageURL %>"><fmt:message key="openHelpLinkText"/></a>
			</span>
		</span>			  	
    <br>
	<div id="tavernaToolbar">
		<span class="message"><fmt:message key="user_text" />
		</span>			    
		<button id="user_ok" class="toolbarButton ok" style="width:170px" title="<fmt:message key="ok_tooltip"/>"
			onClick="userAction(actionType.OK);"><span class="icon"><fmt:message key="ok"/></span>
		</button>
		
			<!-- 
			<button id="user_retry" class="toolbarButton retry" title="<fmt:message key="retry_tooltip"/>"
 			onClick="userAction(actionType.RETRY);"><span class="icon"><fmt:message key="retry"/></span></button>
			-->

 		<button id="user_cancel" class="toolbarButton cancel" style="width:150px" title="<fmt:message key="cancel_tooltip"/>"
 			onClick="userAction(actionType.CANCEL);"><span class="icon"><fmt:message key="cancel"/></span>
 		</button>
		
	</div>
</div>

<tr>
	<!--  STIF interface -->	
	<div id="layerselectorDiv" style="position: absolute; top: 78px; left:10px;width:250px;height:250px;display:inline-block;overflow-y:auto;"></div>
		<div id="map" style="position: absolute; top: 78px; left:263px; right:10px;" > 
	        <div id="mapTimeDiv" style="position:relative;z-index:0;">	            
	            <div id="splitPaneMap">  
	   
					<div id="mapContainerDiv" style="position:relative;"></div> 
					<div id="plot&table">						
							<div id="plotContainerDiv" style="position:relative;width:0px;height:0px;"></div> 
							<div id="tableContainerDiv" style="position:relative;width:0px;height:0px; top:5px;"></div>											
					</div>
				</div> 
	        </div>
	     </div>		
</tr>


</table>
 <script>
 	runApplication();
 </script>
</body>
</html>

