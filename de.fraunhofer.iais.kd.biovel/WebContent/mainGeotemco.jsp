<%@page import="java.util.Locale"%>
<%@page import="de.fraunhofer.iais.kd.biovel.i18n.I18nUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>

<%! 
//instance variables, parameters for the view
String loc = "";	//locale for the language
//STIF can read up to 4 datasources 
String url = "";	//1 data source url. Accept kml, DarwinCore. csv. 
String feedEntryUrl = ""; //feedentry with informations as JSON
String contentType = ""; //contenttype of DataSource	
String label = ""; //label of DataSource
String layers = ""; //user layers to be added to the map 
String bbox = ""; //viewport coordinates to start analysing the map
String logoURL = "http://www.biovel.eu";
//String helpPageURL = "https://wiki.biovel.eu/display/biovelfriends/About+BioSTIF";
String helpPageURL = "https://wiki.biovel.eu/display/doc/BioSTIF+User+Manual";
String aboutPageURL = "http://biovel.iais.fraunhofer.de/biostif/about.jsp";
String version = "Test V. 2012/08/31";
%>

<%
String debug = request.getParameter("debug");
loc = request.getParameter("lang");
if( loc == null || loc.length() == 0 ){
    loc = "en";
}
%>
<fmt:setLocale value="<%= loc %>" scope="session" />

<fmt:setBundle
	basename="de.fraunhofer.iais.kd.biovel.i18n.Messages"
	scope="session" />

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="<%=loc %>">
<head>
<title>BioVeL Map Client Test -with GeoTempCo <%= version %></title>
<meta name="description" content="BioSTIF test: Spatial Temporal Interactive interFace for Biodiversity data">
<meta name="keywords" content="spatio-temporal, map viewer, time viewer, interactive spatio-temporal viewer, spatial temporal interface, STIF, biodiversity data, occurrences viewer">
<meta http-equiv="content-type" content="text/html;charset=UTF-8" />

<link rel="shortcut icon" href="img/favicon_small.ico" type="image/x-icon" />

<link rel="stylesheet" type="text/css" href="css/biovel.css" />
<!-- <link class="jsbin" href="jslib/jquery/jquery-ui.css" rel="stylesheet" type="text/css" /> -->
<link type="text/css" href="jslib/jquery/css/flick/jquery-ui-1.8.21.custom.css" rel="Stylesheet" />	


 <script type="text/javascript">
 
    var application;    
        
    </script>
 
<%

url = request.getParameter("url");
if( url == null || url.length() == 0 ){
		url = "";	
}
debug = request.getParameter("debug");
if( debug == null || debug.length() == 0 ){
	debug = "true"; //TODO change to false
}

feedEntryUrl = request.getParameter("entryurl");
if( feedEntryUrl == null || feedEntryUrl.length() == 0 ){
	feedEntryUrl = ""; 
}

feedEntryUrl = request.getParameter("entryurl");
if( feedEntryUrl == null || feedEntryUrl.length() == 0 ){
	feedEntryUrl = ""; 
}

contentType = request.getParameter("contenttype");
if( contentType == null || contentType.length() == 0 ){
	contentType = ""; 
}

label = request.getParameter("label");
if( label == null || label.length() == 0 ){
	label = ""; 
}

layers = request.getParameter("layers");
if( layers == null || layers.length() == 0 ){
	layers = ""; 
}

bbox = request.getParameter("bbox");
if( bbox == null || bbox.length() == 0 ){
	bbox = ""; 
}
%>

<script type="text/javascript">
var pars = {
		lang : '<%=loc %>',			
		debug: '<%=debug %>',
		url: '<%=url %>',
		contentType: '<%=contentType %>',
		label: '<%=label %>',
		feedEntryUrl: '<%=feedEntryUrl %>',
		layers: '<%=layers %>',
		bbox: '<%=bbox %>'
	};
</script>

<!--  jsLibraries -->
<script type="text/javascript" src="jslib/jquery/jquery.min.1.4.1.js"></script>
<script type="text/javascript" src="jslib/jquery/jquery-ui-1.8.21.min.js"></script>
<script type="text/javascript" src="jslib/jquery/splitter.js"></script>
<script type="text/javascript" src="jslib/jquery/urldecoder.js"></script>

<!--  BioVeL js files -->
<script type="text/javascript" src="js/testscriptloader.js"></script>
<!--  GeoTemCo Files -->
<script type="text/javascript" src="../geotemco/js/Util/Publisher.js"></script>
<script type="text/javascript" src="../geotemco/js/Build/Loader/DynaJsLoader.js"></script>
<script type="text/javascript" src="../geotemco/js/Build/Loader/Loader.js"></script>


<script type="text/javascript">
 
 var loadTimer;
 var subscriptionId;
  
 
function checkLoad(){	
	if (typeof (console) != "undefined") {
		console.info ("Load timer interval " + loadTimer);
	}
	if( typeof(Publisher) != "undefined" && (Publisher != null)) { 
		if (typeof (console) != "undefined") {
	        console.info("Subscribing for STIF ready event ");
	    }
		subscriptionId = Publisher.Subscribe("GeoTemCoReady", this, 
		        function(result) {			
					if (loadTimer) {
						window.clearInterval(loadTimer);
						loadTimer = null;
			 			delete loadTimer;
					}
					Publisher.Unsubscribe("GeoTemCoReady", subscriptionId);
					if (result != null) {						
						if (typeof (GeoTemConfig) == "undefined" || (!GeoTemConfig)) {
							throw new Error ("BioSTIF could not be loaded. GeoTemConfig is not loaded");
						}
						
						var fullscreenlog = document.getElementById("FullscreenWindowLog");
						if (typeof(fullscreenlog) != "undefined" && fullscreenlog) {
							fullscreenlog.innerHTML="Initializing application";
						}
						initBioVeL(pars, "mapContainerDiv","plotContainerDiv","tableContainerDiv","layerselectorDiv");
					} else {
						alert ("BioSTIF could not be loaded. Application can not start. Please try reloading again.")
						throw new Error ("BioSTIF could not be loaded. Application can not start");
					}
				}
		        
		    );
				 
		 }
	 }
		
   $(document).ready(function() {
		   
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
	   	  
	   
	var borderOffset = 18; //empiricall because of IE 
	var borderHeight = $('#mainToolbar').offset().bottom;  
			
	var mapwidth = $(window).width() -40 -210;
    var mapheight = $(window).height(); //80;
  
    //on ready, make sure the widget is resized
	$("#map").height(mapheight); //.trigger("resize");
	$("#map").width(mapwidth); //.trigger("resize");	
		
	$("#splitPaneMap").height( mapheight); //.trigger("resize");
	$("#splitPaneMap").width(mapwidth); //.trigger("resize");	
	var halfsize = (mapheight)/2;
	
	$("#splitPaneMap").trigger("resize", [halfsize]);	
		
		
	
	//loadTimer = window.setInterval( checkLoad, 500);
	checkLoad();
				
	
	$("#splitPaneMap").resize( function(e) {		
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
});

</script>
</head>

<body  bgcolor="white">
<noscript>
      <div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
        Your web browser must have JavaScript enabled in order for this application to display correctly.
      </div>
</noscript>

<!-- invisible message for accessibility-->
<h1 style="position: absolute;top: -500px"><fmt:message key="main_overview_text" /></h1>

 <!--  Toobar --> 
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
  </div>
<%-- <%@ include file="WEB-INF/jsp/main_toolbar.jsp"%> --%> 
 

	<!--  STIF interface -->	
	<!-- <div id="biovel_ls" style="position: absolute; top: 300px; left:900px;"></div> -->
	
		<div id="layerselectorDiv" style="position: absolute; top: 45px; left:10px;width:250px;height:250px;display:inline-block;overflow-y:auto;"></div>
		<div id="map" style="position: absolute; top: 45px; left:263px; right:10px;" > 
	        <div id="mapTimeDiv" style="position:relative;z-index:0;">
	            <!-- <div id="mapContainerDiv" style="position:relative;"></div>            
	            <br>
	            <div id="tableContainerDiv" style="position:relative;width:0px;height:0px; overflow-y:scroll;overflow-x:scroll;">
	            </div> -->
	            <div id="splitPaneMap">  
	   
					<div id="mapContainerDiv" style="position:relative;"></div> 
					<div id="plot&table">
						<!-- <div id="splitPanePlot">  -->
							<div id="plotContainerDiv" style="position:relative;width:0px;height:0px;"></div> 
							<div id="tableContainerDiv" style="position:relative;width:0px;height:0px; top:5px;"></div>
						<!-- </div> -->						
					</div>
				</div> 
	        </div>
	     </div>	
	

</body>


</html>