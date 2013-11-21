<%
/** The request params are stored in the main.jsp in global? variables
  * these are: loc, urls, layers         
  * They will be used for further links
  */

String logoURL = "http://www.biovel.eu";
String helpPageURL = "https://wiki.biovel.eu/display/biovelfriends/About+BioSTIF";
String aboutPageURL = "http://biovel.iais.fraunhofer.de/biostif/about.jsp";
String version = "V. 2012/07/17";

    
%>
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