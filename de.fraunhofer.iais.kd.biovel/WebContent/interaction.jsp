<%@page import="java.util.Locale"%>
<%@page import="de.fraunhofer.iais.kd.biovel.i18n.I18nUtils"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.FileReader"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt"%>

<%! 
//instance variables, parameters for the view
String logoURL = "http://www.biovel.eu";
//String helpPageURL = "help/en/BioSTIF_manual.html";
String helpPageURL = "https://wiki.biovel.eu/display/doc/BioSTIF+User+Manual";
String version = "V. 2012/08/27";
%>

<%
String applicationPath = application.getRealPath("/"); //session.getServletContext().getRealPath("/biostif");
String txtFilePath = applicationPath+ "/version.txt";
BufferedReader reader = new BufferedReader(new FileReader(txtFilePath));
StringBuilder sb = new StringBuilder();
String line;

if ((line = reader.readLine()) != null) {
   version = "V. " + line;
}
reader.close();
//helpPageURL = applicationPath + "/" + helpPageURL;
%>

<fmt:setLocale value="en" scope="session" />

<!-- this bundle is still for application toolbar (help, ok, cancel) 
//TODO:change it with the local bundle, 
//TODO also change the stif/geotempco texts through the i18 jquery bundles -->
<fmt:setBundle
	basename="de.fraunhofer.iais.kd.biovel.i18n.Messages"
	scope="session" />

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
<title>Spatio-Temporal Interactive interFace for Biodiversity Data <%= version %></title>
<meta name="description" content="BioSTIF: Spatial Temporal Interactive interFace for Biodiversity data">
<meta name="keywords" content="spatio-temporal, map viewer, time viewer, interactive spatio-temporal viewer, spatial temporal interface, STIF, biodiversity data, occurrences viewer">
<meta http-equiv="content-type" content="text/html;charset=UTF-8" />
<!-- cache corrector -->
<meta http-equiv="cache-control" content="no-cache, no-store, max-age=0, must-revalidate" />
<meta http-equiv="pragma" content="no-cache" />
<meta http-equiv="expires" content="Fri, 01 Jan 1990 00:00:00 GMT" />

<link rel="shortcut icon" href="img/favicon.ico" type="image/x-icon" />

<link rel="stylesheet" type="text/css" href="css/biovel.css" />


<script type="text/javascript" src="jslib/jquery/jquery.min.js"></script>
<script type="text/javascript" src="jslib/jquery/jquery.i18n.properties-1.0.9.js"></script>
<script type="text/javascript" src="jslib/jquery/urldecoder.js"></script>
<script type="text/javascript" src="jslib/jquery/splitter.js"></script>
<script type="text/javascript" src="js/scriptloader.js"></script> 
<script type="text/javascript" src="jslib/pmrpc/pmrpc.js"></script>
<script type="text/javascript" src="js/utils/FullscreenWindow.js"></script>
<script src="interaction.js"></script>

		
<script src="../stif/js/build/loader/DynaJsLoader.js"></script> 	
 <!-- <script src="../stif/minify/stif-complete-min.js"></script> --> 	 

<script type="text/javascript">

	var loadTimer;
     
	var stifMinLoader = {
	 	stifReady: function () {

			if (typeof (SpaceWrapper) != "undefined") {
				if (typeof (console) != "undefined") {
			        console.info("STIF Minimized was loaded " + loadTimer);
			    }
				Publisher.Publish('StifReady','BioSTIF');			
			}		

		}  
	};
		
	function runApplication() {
		$("#splitPane").splitter({
			type: "h",
			outline: true,
			title: "<fmt:message key="splitBarHelpText" />",
			//minLeft: 500, sizeLeft: 400, minRight: 325,
			sizeTop: true, 
			dock: true,
			outline: true,
			//resizeToWidth: true,
			accessKey: 'I'
		});
	   		
		var borderOffset = 18; //empiricall because of IE 
		var borderHeight = $('#mainToolbar').offset().bottom;  
	    
		//on ready, make sure the widget is resized
		$("#map").height( $(window).height()-120).trigger("resize");
		$("#map").width($(window).width()-40).trigger("resize");	
			
		$("#splitPane").height( $(window).height()-120).trigger("resize");
		$("#splitPane").width($(window).width()-40).trigger("resize");	
		var halfsize = ($(window).height()-120)/2;
		
		$("#splitPane").trigger("resize", [halfsize]);
		
		//var appFullscreenAnimation = fullscreen;
		//appFullscreenAnimation.addFullscreen(appFullscreenAnimation.loaderContent());
		var urlPrefix = document.documentURI;
		var biostif = this;
		var index = urlPrefix.indexOf('/biostif/');
		urlPrefix = urlPrefix.substring(0,index);
				
	  	var dynaloader = new DynaJsLoader();  	
	    var scripts = [{ url: urlPrefix + "/stif/minify/stif-complete-min.js", test: "stifMinLoader.stifReady"}];
	    
	    dynaloader.loadScripts(scripts,stifMinLoader.stifReady);
	    loadTimer = window.setInterval( loadModulesAndStartApplication, 500);	
	    
	    
		$("#splitPane").resize( function() {
	   		resizeSplitPane();
					
		});
		
		$(window).resize( function() {
			resizeWindow();			
			
		});
	}
    window.onload = runApplication();
         
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
 
<%-- <%@ include file="WEB-INF/jsp/main_toolbar.jsp"%> --%>
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
		<br>
	<div id="tavernaToolbar">
		<span class="message"><fmt:message key="user_text" />
		</span>			    
		<button id="user_ok" class="toolbarButton ok" title="<fmt:message key="ok_tooltip"/>"
			onClick="userAction(actionType.OK);"><span class="icon"><fmt:message key="ok"/></span>
		</button>
		
			<!-- 
			<button id="user_retry" class="toolbarButton retry" title="<fmt:message key="retry_tooltip"/>"
 			onClick="userAction(actionType.RETRY);"><span class="icon"><fmt:message key="retry"/></span></button>
			-->

 		<button id="user_cancel" class="toolbarButton cancel" title="<fmt:message key="cancel_tooltip"/>"
 			onClick="userAction(actionType.CANCEL);"><span class="icon"><fmt:message key="cancel"/></span>
 		</button>
		
	</div>		  	    
  </div>

<tr>
	<!--  STIF interface -->	
	<div id="biovel_ls" style="position: absolute; top: 300px; left:900px;"></div>
	<div id="map" style="position: absolute; top: 120px; left:43px;"> 
        <div id="mapTimeDiv" style="position:relative;z-index:0;">
            <div id="splitPane">  
   
				<div id="mapContainerDiv" style="position:relative;"></div> 
				<div id="tableContainerDiv" style="position:relative;width:0px;height:0px; overflow-y:scroll;overflow-x:scroll;">
				</div>
			</div> 
        </div>
	</div>	
	
</tr>


</table>
</body>
</html>

