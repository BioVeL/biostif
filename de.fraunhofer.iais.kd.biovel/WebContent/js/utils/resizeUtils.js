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
	
	var halfsize = height/2;	
	
	$("#splitPaneMap").trigger("resize", [halfsize]);
};

function resizeViews(width, topHeight, bottomHeight) {		
		
	//settings for the map divs
	if (typeof ($("#mapContainerDiv")) != "undefined") {
		$("#mapContainerDiv").height(topHeight + "px");
		$("#mapContainerDiv").width(width+ "px");
		
	}
		
	//settings for the time divs
	if (typeof ($("#plotContainerDiv")) != "undefined") {
		$("#plotContainerDiv").width(width+ "px");
		
	}
	
	//settings for the table divs
	if (typeof ($("#tableContainerDiv")) != "undefined") {
		$("#tableContainerDiv").height(bottomHeight - 100 + "px");
		$("#tableContainerDiv").width(width + "px");
		
    	var scrollheight = bottomHeight - 130 + "px";	
    	
    	if (typeof ($(".scrollableTable")) != "undefined") 
    		$(".scrollableTable").attr('style',"width: " + width + "px; height:" + scrollheight+";");
    	
	}
	
	//settings for the splitbars
	if (typeof ($(".hsplitbar")) != "undefined") {
		document.getElementsByClassName("hsplitbar")[0].style.width = width + "px";
	}
	
	//application.resizeWidgets();
	Publisher.Publish('resize', "", null);
    
	
}; 