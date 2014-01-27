/**
 * @class STITimeplot
 * Implementation for STITimeplot Object
 * @author Stefan Jänicke (stjaenicke@informatik.uni-leipzig.de)
 * @version 0.9
 */

/**
 * defines the timeplot component of the Spatio Temporal Interface
 * it builds a timeplot context with the Simile Widget Timeplot JavaScript Framework
 * @param {STICore} core the sti core component, the timeplot component has to deal with
 * @param {String} container the div id for the container of the timeplot widget
 *
 * @constructor
 */
function STITimeplot(core,containerDiv){

    this.core = core;
    this.timeplotDiv;
    this.timeplot;
    this.dataSources;
    this.eventSources;
    this.tds;
    this.timeGeometry;
    this.valueGeometry;
    this.canvas;
    
    this.leftFlagPole;
    this.rightFlagPole;
    this.rangeBox;
    this.leftFeather;
    this.rightFeather;
    this.leftHandle;
    this.rightHandle;
    
    this.leftFlagPos = null;
    this.leftFlagTime = null;
    this.rightFlagPos = null;
    this.rightFlagTime = null;
    this.leftFeatherTime = null;
    this.rightFeatherTime = null;
    
    this.mouseDownTime;
    this.mouseUpTime;
    this.mouseTempTime;    
    this.mouseDownPos;
    this.mouseUpPos;
    this.mouseTempPos;    
    
    this.status;
    this.featherWidth;    
    this.slider;
    this.container = containerDiv;
    
    this.initialize();
    
}

STITimeplot.prototype = {

    /**
     * clears the timeplot canvas and the timeGeometry properties
     */
	clearTimeplot: function(){
        this.timeplot._clearCanvas();        
        this.timeGeometry._earliestDate = null;
        this.timeGeometry._latestDate = null;
        this.timeGeometry._clearLabels();
	},

    /**
     * initializes the timeplot elements with arrays of time objects
     * @param {TimeObject[][]} timeObjects an array of time objects from different (1-4) sets
     */
    initTimeplot: function(timeObjects){
		this.clearTimeplot();
    	this.reset();
        for (var i = 0; i < this.timeplot._plots.length; i++) {
            this.timeplot._plots[i].dispose();
        }
        this.timeplot._clearCanvas();        
        this.timeGeometry._earliestDate = null;
        this.timeGeometry._latestDate = null;
        this.valueGeometry._minValue = null;
        this.valueGeometry._maxValue = null;
        this.highlightedSlice = undefined;
        this.dataSources = new Array();
        this.plotInfos = new Array();
        this.eventSources = new Array();
        var granularity = 0;
        this.count = 0;
        for (var i = 0; i < timeObjects.length; i++) {
        	var eventSource = new Timeplot.DefaultEventSource();
        	var dataSource = new Timeplot.ColumnSource(eventSource, 1);
        	this.dataSources.push(dataSource);
        	this.eventSources.push(eventSource);
        	var c = STIStatic.colors[i];
        	var plotInfo = Timeplot.createPlotInfo({
            	id: "plot" + i,
            	dataSource: dataSource,
            	timeGeometry: this.timeGeometry,
            	valueGeometry: this.valueGeometry,
            	fillGradient: false,
            	lineColor: 'rgba(' + c.r1 + ',' + c.g1 + ',' + c.b1 + ', 1)',
            	fillColor: 'rgba(' + c.r0 + ',' + c.g0 + ',' + c.b0 + ', 0.3)',
            	showValues: true
        	});
        	this.plotInfos.push(plotInfo);
            for (var j = 0; j < timeObjects[i].length; j++){
		var o = timeObjects[i][j];
		if( o.granularity == null ){
			continue;
		}
		else if( o.granularity > granularity ){
            		granularity = o.granularity;
            	}
            	this.count += o.weight;
            }
        }
        this.timeGeometry._granularity = granularity;
        this.timeGeometry._clearLabels();
        this.timeplot.resetPlots(this.plotInfos);
    	if( this.plotInfos.length == 0 ){
    		this.initLabels( this.timeplot.regularGrid() );
    		return;
    	}
        this.timeGeometry.extendedDataSource = this.tds;
        this.tds.initialize(this.dataSources, this.eventSources, timeObjects, granularity, STIStatic.timeUnit);
        var plots = this.timeplot._plots;
        for( var i = 0; i < plots.length; i++ ){
        	plots[i].pins = [];
        	plots[i].style = this.style;
        	for( var j = 0; j < this.tds.getSliceNumber(); j++ ){
        		plots[i].pins.push({
        			height: 0,
        			count: 0
        		});
        	}
        }
        var levels = Math.round( (this.tds.timeSlices.length-3)/2 );
        if( STIStatic.timeZoom ){
            this.zoomSlider.setMaxAndLevels(levels,levels);
        }
       	this.timeplot.repaint();
       	this.timeplot._resizeCanvas();
        // set maximum number of slider steps
        var slices = this.tds.timeSlices.length;
        var numSlices = Math.floor(slices / this.canvas.width * this.canvas.height + 0.5);
        if( STIStatic.featherSlider ){
            this.slider.setMaximum(numSlices);
        }

//OLD        this.header.innerHTML = STIStatic.timeHeadline + " (" + count + ")";
      //OLD        this.header.style.left = Math.floor( this.plotWindow.offsetWidth/2 - this.header.offsetWidth/2 )+"px"; 
        this.initLabels([]);
        this.initOverview();
	},
    
    /**
     * initializes the timeplot for the Spatio Temporal Interface.
     * all elements (including their events) that are needed for user interaction are instantiated here, the slider element as well
     */
    initialize: function(){
    
        this.status = 0;
        this.selectedObjects = [];
    	this.paused = true;
        this.dataSources = new Array();
        this.plotInfos = new Array();
        this.eventSources = new Array();
        this.timeGeometry = new Timeplot.DefaultTimeGeometry({
            gridColor: "#000000",
            axisLabelsPlacement: "top"
        });
        this.style = 'graph';
        this.timeGeometry._hideLabels = true;
        this.timeGeometry._granularity = 0;
        this.valueGeometry = new Timeplot.DefaultValueGeometry({
            min: 0
        });

//OLD		var tools = this.initializeTimeplotTools();
        
        var w = this.container.offsetWidth;
        var h = this.container.offsetHeight;

        this.plotWindow = document.createElement("div");
		this.plotWindow.id = "plotWindow";
		this.plotWindow.style.width = w+"px";
/*OLD
		if( STIStatic.timeHeadline ){
    		this.plotWindow.style.height = (h+12+25)+"px";
    		container.style.height = (h+12+25)+"px";
        }
        else {
    		this.plotWindow.style.height = (h+12)+"px";
    		container.style.height = (h+12)+"px";
        }
*/

		this.plotWindow.style.height = (h+12)+"px";
		this.container.style.height = (h+12)+"px";
		
		this.plotWindow.onmousedown = function(){
		  return false;
		}

		var plotContainer = document.createElement("div");
		plotContainer.id = "plotContainer";
		plotContainer.style.width = w+"px";
		plotContainer.style.height = h+"px";
		plotContainer.style.position = "absolute";
		plotContainer.style.zIndex = 0;
/*OLD
		if( STIStatic.timeHeadline ){
			plotContainer.style.top = "37px";
		}
		else {
			plotContainer.style.top = "12px";
		}
*/
		plotContainer.style.top = "12px";
		this.plotWindow.appendChild(plotContainer);
		this.container.appendChild(this.plotWindow);

        this.timeplotDiv = document.createElement("div");
        this.timeplotDiv.style.left = "16px";
        this.timeplotDiv.style.width = (w-32)+"px";
        this.timeplotDiv.style.height = h+"px";
        plotContainer.appendChild(this.timeplotDiv);
        
        this.timeplot = Timeplot.create(this.timeplotDiv, this.plotInfos);
        this.tds = new TimeDataSource();
        
        this.canvas = this.timeplot.getCanvas();
        
        this.leftFlagPole = this.timeplot.putDiv("leftflagpole", "timeplot-dayflag-pole");
        this.leftFlagPole.style.borderLeft = STIStatic.frameBorder;
        this.rightFlagPole = this.timeplot.putDiv("rightflagpole", "timeplot-dayflag-pole");
        this.rightFlagPole.style.borderLeft = STIStatic.frameBorder;
        SimileAjax.Graphics.setOpacity(this.leftFlagPole, 50);
        SimileAjax.Graphics.setOpacity(this.rightFlagPole, 50);

        this.rangeBox = this.timeplot.putDiv("rangebox", "range-box");
        this.rangeBox.style.backgroundColor = STIStatic.rangeBoxColor;
        this.leftFeather = this.timeplot.putDiv("leftfeather", "left-feather");
        this.rightFeather = this.timeplot.putDiv("rightfeather", "right-feather");
        
        this.leftHandle = document.createElement("div");
       	this.rightHandle = document.createElement("div");
        this.plotWindow.appendChild(this.leftHandle);
        this.plotWindow.appendChild(this.rightHandle);
        if( STIStatic.toolbarStyle == 'default' ){
            this.leftHandle.style.backgroundColor = STIStatic.frameColor;
            this.leftHandle.style.border = STIStatic.frameBorder;
            this.leftHandle.setAttribute('class','plotHandle plotHandleLabel');
            this.rightHandle.style.backgroundColor = STIStatic.frameColor;
            this.rightHandle.style.border = STIStatic.frameBorder;
           	this.rightHandle.setAttribute('class','plotHandle plotHandleLabel');
        }       
        if( STIStatic.toolbarStyle == 'alternative' ){
            this.leftHandle.style.backgroundImage = "url("+STIStatic.path+"leftHandle.png"+")";
            this.leftHandle.setAttribute('class','plotHandle plotHandleIcon');
            this.rightHandle.style.backgroundImage = "url("+STIStatic.path+"rightHandle.png"+")";
           	this.rightHandle.setAttribute('class','plotHandle plotHandleIcon');
        }
        
		this.poles = this.timeplot.putDiv( "poles", "pole" );
        this.timeplot.placeDiv(this.poles, {
            left: 0,
            bottom: 0,
            width: this.canvas.width,
            height: this.canvas.height,
            display: "block"
        });
        this.poles.appendChild(document.createElement("canvas"));  

	this.filterBar; // = new FilterBar(this,this.plotWindow);

        var plot = this;
        
        this.dragButton = document.createElement("div");
        
	/*
        this.zoomButton = document.createElement("div");
        this.zoomButton.onclick = function(){
        	plot.timeRefining();
        }
        
        this.cancelButton = document.createElement("div");
        this.cancelButton.onclick = function(){
        	plot.deselection();
        }
	*/
        
        this.toolbar = document.createElement("div");
        this.toolbar.setAttribute('class','plotToolbar');
        this.toolbar.style.backgroundColor = STIStatic.frameColor;
        this.toolbar.style.borderLeft = STIStatic.frameBorder;
        this.toolbar.style.borderRight = STIStatic.frameBorder;
        this.toolbar.style.borderBottom = STIStatic.frameBorder;
        this.toolbar.style.textAlign = "center";
        this.plotWindow.appendChild(this.toolbar);	        
        this.toolbarAbsoluteDiv = document.createElement("div");
        this.toolbarAbsoluteDiv.setAttribute('class','absoluteToolbar');
        this.toolbar.appendChild(this.toolbarAbsoluteDiv);

        if( STIStatic.toolbarStyle == 'default' ){
	        if( STIStatic.featherSlider ){
	        	this.toolbarAbsoluteDiv.style.width = "124px";
	        }
	        else {
	        	this.toolbarAbsoluteDiv.style.width = "69px";
	        }
	        this.dragButton.setAttribute('class','dragTimeRange');
//	        this.zoomButton.setAttribute('class','zoomRange');
//	        this.cancelButton.setAttribute('class','cancelRange');
	        this.toolbarAbsoluteDiv.appendChild(this.dragButton);
//	        this.toolbarAbsoluteDiv.appendChild(this.zoomButton);        
//	        this.toolbarAbsoluteDiv.appendChild(this.cancelButton);
        }

        if( STIStatic.toolbarStyle == 'alternative' ){
        	this.dragButton.setAttribute('class','dragTimeRangeAlt');
        	this.dragButton.style.backgroundImage = "url("+STIStatic.path+"drag.png"+")";
//        	this.zoomButton.setAttribute('class','zoomRangeAlt');
//        	this.cancelButton.setAttribute('class','cancelRangeAlt');
	        this.toolbarAbsoluteDiv.appendChild(this.dragButton);
        	this.toolbarAbsoluteDiv.style.width = this.dragButton.offsetWidth+"px";
//	        this.plotWindow.appendChild(this.zoomButton);        
//	        this.plotWindow.appendChild(this.cancelButton);
        }

        if( STIStatic.animation == 'embedded' ){
           	this.animation = document.createElement("div");
        	this.animation.setAttribute('class','animation');
        	this.animation.style.backgroundImage = "url("+STIStatic.path+"play-enabled.png"+")";
            this.plotWindow.appendChild(this.animation);
        }
        
        /*
        if( STIStatic.popups ){
        	this.popup = new STITimeplotPopup(this,this.plotWindow);
        	this.popupClickDiv = this.timeplot.putDiv("popupClickDiv", "popup-click-div");
        	this.popupClickDiv.title = "Click to open popup";        	
        }
        */
        
		// displays the feather divs
        var displayFeather = function(){
        	if( !STIStatic.featherSlider ){
        		return;
        	}
            plot.leftFeather.style.visibility = "visible";
            plot.rightFeather.style.visibility = "visible";            
            plot.timeplot.placeDiv(plot.leftFeather, {
                left: plot.leftFlagPos - plot.featherWidth,
                width: plot.featherWidth,
                bottom: 0,
                height: plot.canvas.height,
                display: "block"
            });
            plot.timeplot.placeDiv(plot.rightFeather, {
                left: plot.rightFlagPos,
                width: plot.featherWidth,
                bottom: 0,
                height: plot.canvas.height,
                display: "block"
            });            
            var leftCv = document.getElementById("leftFeatherCanvas");
            if (leftCv == null) {
                leftCv = document.createElement("canvas");
                leftCv.id = "leftFeatherCanvas";
                plot.leftFeather.appendChild(leftCv);
            }
            leftCv.width = plot.featherWidth;
            leftCv.height = plot.canvas.height;
 
           if (!leftCv.getContext && G_vmlCanvasManager) 
                leftCv = G_vmlCanvasManager.initElement(leftCv);
            var leftCtx = leftCv.getContext('2d');
            var leftGradient = leftCtx.createLinearGradient(plot.featherWidth, 0, 0, 0);
            leftGradient.addColorStop(0, 'rgba(95,136,178,0.6)');
            leftGradient.addColorStop(1, 'rgba(171,171,171,0)');
            leftCtx.fillStyle = leftGradient;
            leftCtx.fillRect(0, 0, plot.featherWidth, plot.canvas.height);   

            var rightCv = document.getElementById("rightFeatherCanvas");
            if (rightCv == null) {
                rightCv = document.createElement("canvas");
                rightCv.id = "rightFeatherCanvas";
                plot.rightFeather.appendChild(rightCv);
            }
            rightCv.width = plot.featherWidth;
            rightCv.height = plot.canvas.height;
            if (!rightCv.getContext && G_vmlCanvasManager) 
                rightCv = G_vmlCanvasManager.initElement(rightCv);
            var rightCtx = rightCv.getContext('2d');
            var rightGradient = rightCtx.createLinearGradient(0, 0, plot.featherWidth, 0);
            rightGradient.addColorStop(0, 'rgba(95,136,178,0.6)');
            rightGradient.addColorStop(1, 'rgba(171,171,171,0)');
            rightCtx.fillStyle = rightGradient;
            rightCtx.fillRect(0, 0, plot.featherWidth, plot.canvas.height);
        }         
                
        if( STIStatic.featherSlider ){
        	var featherSliderDiv = document.createElement("div");
    		featherSliderDiv.setAttribute('class', 'featherSlider');
    		featherSliderDiv.title = STIStatic.getString('timeFeather');;
            var sliderInput = document.createElement("input");
            featherSliderDiv.appendChild(sliderInput);
            this.slider = new Slider(featherSliderDiv,sliderInput,"horizontal");
            this.slider.onchange = function(){
                if (plot.leftFlagPos != null) {
                    plot.setFeather();
                    var plots = plot.timeplot._plots;
                    for (i = 0; i < plots.length; i++) {
                        plots[i].fullOpacityPlot(plot.leftFlagTime, plot.rightFlagTime, plot.leftFlagPos, plot.rightFlagPos, plot.leftFeatherTime, plot.rightFeatherTime, plot.featherWidth, STIStatic.colors[i]);
                        plots[i].opacityPlot.style.visibility = "visible";
                    }
                    displayFeather();
                    plot.timeSelection();
                }
            }
            this.toolbarAbsoluteDiv.appendChild(featherSliderDiv);
        }

        this.overview = document.createElement("div");
        this.overview.setAttribute('class','timeOverview');
        this.plotWindow.appendChild(this.overview);
        
		var mousedown = false;
        this.shift = function(shift){
        	if( !mousedown ){
        		return;
        	}
			if( plot.tds.setShift(shift) ){
				plot.redrawPlot();
			}
			setTimeout( function(){ plot.shift(shift); }, 200 );
        }
		
		var shiftPressed = function(shift){
        	mousedown = true;
        	document.onmouseup = function(){
        		mousedown = false;
        		document.onmouseup = null;
        	}
        	plot.shift(shift);
		}
		
        this.shiftLeft = document.createElement("div");
        this.shiftLeft.setAttribute('class','shiftLeft');
        this.plotWindow.appendChild(this.shiftLeft);
        this.shiftLeft.onmousedown = function(){
        	shiftPressed(1);
        }

        this.shiftRight = document.createElement("div");
        this.shiftRight.setAttribute('class','shiftRight');
        this.plotWindow.appendChild(this.shiftRight);
        this.shiftRight.onmousedown = function(){
        	shiftPressed(-1);
        }

        this.plotLabels = document.createElement("div");
        this.plotLabels.setAttribute('class','plotLabels');
        this.plotWindow.appendChild(this.plotLabels);
        
        this.initLabels( this.timeplot.regularGrid() );

/* OLD        
        if( STIStatic.timeHeadline ){
        	this.overview.style.top = "25px";
        	this.plotLabels.style.top = "25px";
        	this.plotLabels.style.borderTop = STIStatic.toolbarBorder;
        	this.header = document.createElement("div");
            this.plotWindow.appendChild(this.header);
            this.header.setAttribute('class','widgetHeadline');
            this.header.style.color = STIStatic.frameColor2;
        }
*/        
        
        //Finds the time corresponding to the position x on the timeplot
        var getCorrelatedTime = function(x){
            if (x >= plot.canvas.width) 
                x = plot.canvas.width;
            if (isNaN(x) || x < 0) 
                x = 0;
            var t = plot.timeGeometry.fromScreen(x);
            if (t == 0) 
                return;
            return plot.dataSources[0].getClosestValidTime(t);
        }
        
        //Finds the position corresponding to the time t on the timeplot
        var getCorrelatedPosition = function(t){
			var x = plot.timeGeometry.toScreen(t);
            if (x >= plot.canvas.width) 
                x = plot.canvas.width;
            if (isNaN(x) || x < 0) 
                x = 0;
            return x;
        }

        //Maps the 2 positions in the right order to left and right bound of the chosen timeRange
        var mapPositions = function(pos1, pos2){
            if (pos1 > pos2) {
                plot.leftFlagPos = pos2;
                plot.rightFlagPos = pos1;
            }
            else {
                plot.leftFlagPos = pos1;
                plot.rightFlagPos = pos2;
            }
            plot.leftFlagTime = plot.dataSources[0].getClosestValidTime(plot.timeGeometry.fromScreen(plot.leftFlagPos));
            plot.rightFlagTime = plot.dataSources[0].getClosestValidTime(plot.timeGeometry.fromScreen(plot.rightFlagPos));
        }
        
        //Sets the divs corresponding to the actual chosen timeRange
        var setRangeDivs = function(){
            plot.leftFlagPole.style.visibility = "visible";
            plot.rightFlagPole.style.visibility = "visible";
            plot.rangeBox.style.visibility = "visible";
            plot.timeplot.placeDiv(plot.leftFlagPole, {
                left: plot.leftFlagPos,
                bottom: 0,
                height: plot.canvas.height,
                display: "block"
            });
            plot.timeplot.placeDiv(plot.rightFlagPole, {
                left: plot.rightFlagPos,
                bottom: 0,
                height: plot.canvas.height,
                display: "block"
            });
            var boxWidth = plot.rightFlagPos - plot.leftFlagPos;
            if( plot.popup ){
            	plot.popupClickDiv.style.visibility = "visible";
                plot.timeplot.placeDiv(plot.popupClickDiv, {
                    left: plot.leftFlagPos,
                    width: boxWidth + 1,
                    height: plot.canvas.height,
                    display: "block"
                });
            }
            plot.timeplot.placeDiv(plot.rangeBox, {
                left: plot.leftFlagPos,
                width: boxWidth + 1,
                height: plot.canvas.height,
                display: "block"
            });
          	plot.setFeather();
          	displayFeather();
           	var plots = plot.timeplot._plots;
           	for (i = 0; i < plots.length; i++) {
               	plots[i].fullOpacityPlot(plot.leftFlagTime, plot.rightFlagTime, plot.leftFlagPos, plot.rightFlagPos, plot.leftFeatherTime, plot.rightFeatherTime, plot.featherWidth, STIStatic.colors[i]);
               	plots[i].opacityPlot.style.visibility = "visible";
           	}
        	var unit = plot.tds.unit;
        	
        	var top = plotContainer.offsetTop;
        	var left = plotContainer.offsetLeft;
        	var leftPos = plot.leftFlagPole.offsetLeft + plot.timeplot.getElement().offsetLeft;
        	var rightPos = plot.rightFlagPole.offsetLeft + plot.timeplot.getElement().offsetLeft;
			var rW = rightPos-leftPos;
			var pW = plot.canvas.width;
			var pL = plot.timeplot.getElement().offsetLeft;
        	        	
        	if( STIStatic.toolbarStyle == 'default' ){
            	plot.leftHandle.style.visibility = "visible";
    			plot.rightHandle.style.visibility = "visible";
            	plot.leftHandle.style.left = (leftPos-plot.leftHandle.offsetWidth/2)+"px";
            	plot.rightHandle.style.left = (rightPos-plot.rightHandle.offsetWidth+1+plot.rightHandle.offsetWidth/2)+"px";
            	plot.leftHandle.style.top = top+"px";     	
            	
    			var tW = plot.toolbarAbsoluteDiv.offsetWidth;
            	if( rightPos == leftPos ){
        			plot.rightHandle.style.visibility = "hidden";
            	}        	
            	else if( rightPos-leftPos < plot.leftHandle.offsetWidth/2 + plot.rightHandle.offsetWidth/2 ){
    				plot.rightHandle.style.top = (top+plot.rightHandle.offsetHeight)+"px";
            	}
            	else {
    				plot.rightHandle.style.top = top+"px";
            	}

    			if( rW >= tW ){
    				plot.toolbar.style.left = leftPos+"px";
    				plot.toolbar.style.width = (rW-1)+"px";
    				plot.toolbarAbsoluteDiv.style.left = ((rW-tW)/2)+"px";
    			} 
    			else {
    				plot.toolbar.style.left = (pL+plot.leftFlagPos*(pW-tW)/(pW-rW))+"px";
    				plot.toolbar.style.width = tW+"px";
    				plot.toolbarAbsoluteDiv.style.left = "0px";
    			}
            	var topPos = top + plot.timeplot.getElement().offsetHeight;
    			plot.toolbar.style.top = topPos+"px";
    			plot.toolbar.style.visibility = "visible";
    			plot.toolbarAbsoluteDiv.style.visibility = "visible";          
        	}
        	if( STIStatic.toolbarStyle == 'alternative' ){
        		var handleTop = top + Math.floor(plot.timeplotDiv.offsetHeight/2 - plot.leftHandle.offsetHeight/2);
            	plot.leftHandle.style.visibility = "visible";
    			plot.rightHandle.style.visibility = "visible";
            	plot.leftHandle.style.left = (leftPos-plot.leftHandle.offsetWidth/2)+"px";
            	plot.rightHandle.style.left = (rightPos-plot.rightHandle.offsetWidth+1+plot.rightHandle.offsetWidth/2)+"px";
            	plot.leftHandle.style.top = handleTop+"px";     	
    			plot.rightHandle.style.top = handleTop+"px";
            	if( rightPos == leftPos ){
        			plot.rightHandle.style.visibility = "hidden";
        			plot.leftHandle.style.backgroundImage = "url("+STIStatic.path+"mergedHandle.png"+")";
            	}
            	else {
        			plot.leftHandle.style.backgroundImage = "url("+STIStatic.path+"leftHandle.png"+")";
            	}
//            	plot.zoomButton.style.visibility = "visible";
            	//plot.dragButton.style.visibility = "visible";
//            	plot.cancelButton.style.visibility = "visible";
//        		plot.cancelButton.style.top = top+"px";
  //      		plot.zoomButton.style.top = top+"px";
        		//plot.dragButton.style.bottom = (-plot.dragButton.offsetHeight+2)+"px";
/*
            	if( rW > plot.cancelButton.offsetWidth ){
            		plot.cancelButton.style.left = (left+rightPos-plot.cancelButton.offsetWidth)+"px";
                	plot.zoomButton.style.left = (left+leftPos+2)+"px";
            	}
            	else {
            		plot.cancelButton.style.left = (left+rightPos)+"px";
            		plot.zoomButton.style.left = (left+leftPos-plot.zoomButton.offsetWidth)+"px";
            	}
            	/*
    			var tW = plot.dragButton.offsetWidth;
    			if( rW >= tW ){
    				plot.dragButton.style.left = (Math.floor(leftPos+(rW-tW)/2))+"px";
    			} 
    			else {
    				plot.dragButton.style.left = (pL+plot.leftFlagPos*(pW-tW)/(pW-rW))+"px";
    			}
    			*/
    			var tW = plot.toolbarAbsoluteDiv.offsetWidth;
    			if( rW >= tW ){
    				plot.toolbar.style.left = leftPos+"px";
    				plot.toolbar.style.width = (rW-1)+"px";
    				plot.toolbarAbsoluteDiv.style.left = ((rW-tW)/2)+"px";
    			} 
    			else {
    				plot.toolbar.style.left = (pL+plot.leftFlagPos*(pW-tW)/(pW-rW))+"px";
    				plot.toolbar.style.width = tW+"px";
    				plot.toolbarAbsoluteDiv.style.left = "0px";
    			}
            	var topPos = top + plot.timeplot.getElement().offsetHeight + 1;
    			plot.toolbar.style.top = topPos+"px";
    			plot.toolbar.style.visibility = "visible";
    			plot.toolbarAbsoluteDiv.style.visibility = "visible";          
        	}
        	if( STIStatic.animation == 'embedded' ){
            	plot.animation.style.visibility = "visible";
        		plot.animation.style.bottom = "0px";
            	plot.animation.style.left = (left+leftPos)+"px";
        	}
        }
        
        var getAbsoluteLeft = function(div){
        	var left = 0;
        	while( div ) {
        		left += div.offsetLeft;
        		div = div.offsetParent;
        	}
        	return left;
        }
        var timeplotLeft = getAbsoluteLeft(plot.timeplot.getElement());

		var checkPolesForStyle = function(x){
            if( plot.style == 'bars' && plot.leftFlagTime == plot.rightFlagTime ){
            	var index = plot.tds.getSliceIndex(plot.leftFlagTime);
        		var time1 = plot.leftFlagTime;
                var pos1 = plot.leftFlagPos;
                var time2, pos2;
            	if( index == 0 ){
            		time2 = plot.tds.getSliceTime(index+1);
            	}
            	else if( index == plot.tds.getSliceNumber() - 1 ){
            		time2 = plot.tds.getSliceTime(index-1);
            	}
            	else {
            		if( x < plot.leftFlagPos ){
                		time2 = plot.tds.getSliceTime(index-1);
            		}
            		else {
                		time2 = plot.tds.getSliceTime(index+1);
            		}
            	}
                pos2 = plot.timeGeometry.toScreen(time2);
                mapPositions(pos1,pos2,time1,time2);
            }
		}        
        
		var startX, startY, multiplier;

		// mousemove function that causes moving selection of objects and toolbar divs		
		var moveToolbar = function(start,actual){
			var pixelShift = actual-start;
			if( plot.status == 2 ){
				var newTime = getCorrelatedTime(startX + pixelShift);
				if( newTime == plot.mouseTempTime ){
					return;
				}
				plot.mouseTempTime = newTime;
                plot.mouseTempPos = plot.timeGeometry.toScreen(plot.mouseTempTime);
                mapPositions(plot.mouseDownPos, plot.mouseTempPos);				
			}
			else if( plot.status == 3 ){
				pixelShift *= multiplier;
				var plotPos = actual - timeplotLeft;
				if( plotPos <= plot.canvas.width/2 ){
					var newTime = getCorrelatedTime(startX + pixelShift);
					if( newTime == plot.leftFlagTime ){
						return;
					}
					plot.leftFlagTime = newTime;
					var diff = plot.leftFlagPos; 
	                plot.leftFlagPos = plot.timeGeometry.toScreen(plot.leftFlagTime);
	                diff -= plot.leftFlagPos;
	                plot.rightFlagTime = getCorrelatedTime(plot.rightFlagPos - diff);
	                plot.rightFlagPos = plot.timeGeometry.toScreen(plot.rightFlagTime);
				}
				else {
					var newTime = getCorrelatedTime(startY + pixelShift);
					if( newTime == plot.rightFlagTime ){
						return;
					}
					plot.rightFlagTime = newTime;
					var diff = plot.rightFlagPos; 
	                plot.rightFlagPos = plot.timeGeometry.toScreen(plot.rightFlagTime);
	                diff -= plot.rightFlagPos;
	                plot.leftFlagTime = getCorrelatedTime(plot.leftFlagPos - diff);
	                plot.leftFlagPos = plot.timeGeometry.toScreen(plot.leftFlagTime);                
				}
			}
			checkPolesForStyle(actual-timeplotLeft);
			setRangeDivs();
           	plot.timeSelection();
		}

		// fakes user interaction mouse move
		var playIt = function(start,actual,reset){
			if( !plot.paused ){
				if( STIStatic.animationHooking && !plot.hooked ){
					setTimeout( function(){ playIt(start,actual,reset) }, 100 );
					return;
				}
				var pixel = plot.canvas.width / ( plot.tds.timeSlices.length - 1 ) / 5;
				var wait = 100;
				if( !STIStatic.animationHooking ){
					wait = 20 * pixel;
				}
				if( reset ){
					actual = 0;
				}
				moveToolbar(start,actual);
				if( plot.rightFlagPos >= plot.canvas.width ){
					reset = true;
					wait = 1000;
				}
				else {
					reset = false;
				}
				setTimeout( function(){ playIt(start,actual+pixel,reset) }, wait );
			}
		}

		var deactivate;		
		var setMultiplier = function(){
			var rangeWidth = plot.rightFlagPos - plot.leftFlagPos;
			var toolbarWidth = plot.toolbarAbsoluteDiv.offsetWidth;
			var plotWidth = plot.canvas.width;
			if( rangeWidth < toolbarWidth ){
				multiplier = (plotWidth-rangeWidth)/(plotWidth-toolbarWidth);
			}
			else {
				multiplier = 1;
			}
		}
		
        if( STIStatic.animation == 'embedded' ){
        	this.animation.onclick = function(){
       			plot.play();
        	}
        	document.onclick = function(){
    			if( plot.status > 1 ){
    				if( deactivate ){
    					plot.stop();
    				}
    				else {
    					deactivate = true;
    				}
    			}
    		}
        }

        /**
     	 * starts the animation
     	*/
		this.play = function(){
			if( this.leftFlagPos == null ){
				return;
			}
        	deactivate = false;
			plot.paused = false;
			plot.hooked = true;
			plot.updateAnimationButton(2);
			plot.status = 3;
			setMultiplier();
			startX = plot.leftFlagPos;
			startY = plot.rightFlagPos;
			var position = Math.round(plot.leftFlagPos);
			playIt(position,position+1,false);
		}
		
    	/**
     	 * stops the animation
     	*/
		this.stop = function(){
			plot.paused = true;
			plot.status = 0;
			if( STIStatic.toolbarStyle == 'default' ){
		        plot.dragButton.setAttribute('class','dragTimeRange');
			}
			plot.updateAnimationButton(1);
		}
		
		// triggers the mousemove function to move the range and toolbar
        var toolbarEvent = function(evt){


        	deactivate = false;
  			var left = STIStatic.getMousePosition(evt).left;
   			document.onmousemove = function(evt){
    			moveToolbar(left,STIStatic.getMousePosition(evt).left);
    			if( plot.popup ){
    				plot.popup.reset();
    			}
      		}
        }
        
        var initializeLeft = function(){
        	plot.mouseDownTime = plot.rightFlagTime;
        	plot.mouseTempTime = plot.leftFlagTime;
        	plot.mouseDownPos = plot.timeGeometry.toScreen(plot.mouseDownTime);
        	startX = plot.leftFlagPos; 
        }

        var initializeRight = function(){
        	plot.mouseDownTime = plot.leftFlagTime;
        	plot.mouseTempTime = plot.rightFlagTime;
        	plot.mouseDownPos = plot.timeGeometry.toScreen(plot.mouseDownTime);
        	startX = plot.rightFlagPos; 
        }
        
        var initializeDrag = function(){
    		startX = plot.leftFlagPos;
    		startY = plot.rightFlagPos;
			setMultiplier();
			if( STIStatic.toolbarStyle == 'default' ){
		        plot.dragButton.setAttribute('class','dragTimeRangeClick');
			}
        }
        
        if( STIStatic.timeDragAndDrop ){
        	var checkBorders = function(){
                if( plot.style == 'bars' && plot.mouseUpTime == plot.mouseDownTime ){
                	var index = plot.tds.getSliceIndex(plot.mouseUpTime);
                	if( index == 0 ){
                		plot.mouseUpTime = plot.tds.getSliceTime(index+1);
                	}
                	else if( index == plot.tds.getSliceNumber() - 1 ){
                		plot.mouseUpTime = plot.tds.getSliceTime(index-1);
                	}
                	else {
                		if( plot.x < plot.leftFlagPos ){
                    		plot.mouseUpTime = plot.tds.getSliceTime(index-1);
                		}
                		else {
                    		plot.mouseUpTime = plot.tds.getSliceTime(index+1);
                		}
                	}
                }
            }        	
    		// handles mousedown on left handle
            this.leftHandle.onmousedown = function(evt){
            	if( plot.status != 2 ){
            		initializeLeft();
            		plot.status = 2;
    				toolbarEvent(evt);
    				document.onmouseup = function(){
    					document.onmousemove = null;
    					document.onmouseup = null;
    					plot.stop();
    				}
            	}
            }
    		// handles mousedown on right handle
            this.rightHandle.onmousedown = function(evt){
            	if( plot.status != 2 ){
            		initializeRight();
            		plot.status = 2;
    				toolbarEvent(evt);
    				document.onmouseup = function(){
    					document.onmousemove = null;
    					document.onmouseup = null;
    					plot.stop();
    				}
            	}
            }
    		// handles mousedown on drag button
            this.dragButton.onmousedown = function(evt){
            	if( plot.status != 3 ){
            		plot.status = 3;
            		initializeDrag();
            		toolbarEvent(evt);
    				document.onmouseup = function(){
    					document.onmousemove = null;
    					document.onmouseup = null;
    					if( STIStatic.toolbarStyle == 'default' ){
    				        plot.dragButton.setAttribute('class','dragTimeRange');
    					}
    					plot.stop();
    				}
            	}
            }
        }
        else {
    		// handles click on left handle
            this.leftHandle.onclick = function(evt){
            	if( plot.status != 2 ){
            		initializeLeft();
            		plot.status = 2;
    				toolbarEvent(evt);
            	}
            }            
    		// handles click on right handle
            this.rightHandle.onclick = function(evt){
            	if( plot.status != 2 ){
            		initializeRight();
            		plot.status = 2;
    				toolbarEvent(evt);
            	}
            }
    		// handles click on drag button
            this.dragButton.onclick = function(evt){
            	if( plot.status != 3 ){
            		plot.status = 3;
            		initializeDrag();
            		toolbarEvent(evt);
            	}
            }
    		document.onclick = function(){
    			if( plot.status > 1 ){
    				if( deactivate ){
    					plot.stop();
    					document.onmousemove = null;
    				}
    				else {
    					deactivate = true;
    				}
    			}
    		}
        }
                        
        // handles mousedown-Event on timeplot
        var mouseDownHandler = function(elmt, evt, target){
        	if( plot.dataSources.length > 0 ){
            	plot.x = Math.round(SimileAjax.DOM.getEventRelativeCoordinates(evt, plot.canvas).x);
            	if (plot.status == 0 ){
            		var time = getCorrelatedTime(plot.x);
            		if( plot.leftFlagPos != null && plot.popup && time >= plot.leftFlagTime && time <= plot.rightFlagTime ){
                        var x = plot.leftFlagPos+(plot.rightFlagPos-plot.leftFlagPos)/2;
                        var elements = [];
                        for( var i=0; i<plot.dataSources.length; i++ ){
                        	elements.push([]);
                        }
                        for( var i=0; i<plot.selectedObjects.length; i++ ){
                        	if( plot.selectedObjects[i].value == 1 ){
                                for( var j=0; j<plot.selectedObjects[i].objects.length; j++ ){
                                	elements[j] = elements[j].concat(plot.selectedObjects[i].objects[j]);
                                }
                        	}
                        }
                        var labels = [];
                        for( var i=0; i<elements.length; i++ ){
            				if( elements[i].length == 0 ){
            					continue;
            				}
            				var c = STIStatic.colors[i];
            				var color = 'rgb('+c.r0+','+c.g0+','+c.b0+')';
            				var div = document.createElement("div");
            				div.setAttribute('class','tagCloudItem');
            				div.style.color = color;
            				var label = { div: div, elements: elements[i] };
            				var weight = 0;
            				for( j in elements[i] ){
            					weight += elements[i][j].weight;
            				}
            				var fs = 2*weight/1000;
            				if( fs > 2 ){
            					fs = 2;
            				}
            				div.style.fontSize = (1+fs)+"em";
            				div.style.textShadow = "0 0 0.4em black, 0 0 0.4em black, 0 0 0.4em black, 0 0 0.4em "+c.hex;
            				if( weight == 1 ){
                				div.innerHTML = weight + " object";
            				}
            				else {
            					div.innerHTML = weight + " objects";
            				}
            				var appendMouseFunctions = function(label,div,color){
                				div.onclick = function(){
                					plot.popup.showLabelContent(label);
           							div.style.textShadow = "0 0 0.4em black, 0 0 0.4em black, 0 0 0.4em black, 0 0 0.4em "+color;
                				}
                				div.onmouseover = function(){
               						div.style.textShadow = "0 -1px "+color+", 1px 0 "+color+", 0 1px "+color+", -1px 0 "+color;
                				}
                				div.onmouseout = function(){
           							div.style.textShadow = "0 0 0.4em black, 0 0 0.4em black, 0 0 0.4em black, 0 0 0.4em "+color;
                				}
            				}
            				appendMouseFunctions(label,div,c.hex);
            				labels.push(label);
                        }
                    	if( labels.length > 0 ){
                    		plot.popup.createPopup(x+20,0,labels);
                    	}
            		}
            		else {
               			plot.deselection();
                    	plot.status = 1;
                    	plot.mouseDownTime = time;
                    	plot.mouseTempTime = plot.mouseDownTime;
                    	plot.mouseDownPos = plot.timeGeometry.toScreen(plot.mouseDownTime);
                    	mapPositions(plot.mouseDownPos, plot.mouseDownPos, plot.mouseDownTime, plot.mouseDownTime);
                        // handles mouseup-Event on timeplot
                    	document.onmouseup = function(){
                            if (plot.status == 1) {
                                plot.mouseUpTime = plot.mouseTempTime;
                                plot.mouseUpPos = plot.timeGeometry.toScreen(plot.mouseUpTime);
                                mapPositions(plot.mouseDownPos, plot.mouseUpPos, plot.mouseDownTime, plot.mouseUpTime);
                            	checkPolesForStyle(plot.x);
                                setRangeDivs();
                                plot.timeSelection();
            					plot.updateAnimationButton(1);
                                displayFeather();
                                document.onmouseup = null;
                                plot.status = 0;
                            }
                    	}
            		}
            	}
            }
        }
        
        // handles mousemove-Event on timeplot
        var mouseMoveHandler = function(elmt, evt, target){        	
        	if( plot.dataSources.length > 0 ){
            	plot.x = Math.round(SimileAjax.DOM.getEventRelativeCoordinates(evt, plot.canvas).x);
            	if (plot.status == 1) {
                	plot.mouseTempTime = getCorrelatedTime(plot.x);
                	plot.mouseTempPos = plot.timeGeometry.toScreen(plot.mouseTempTime);
                	mapPositions(plot.mouseDownPos, plot.mouseTempPos, plot.mouseDownTime, plot.mouseTempTime);
                	checkPolesForStyle(plot.x);
                	setRangeDivs();
            	}
            }
        }
		
        // handles mouseout-Event on timeplot
		var mouseOutHandler = function(elmt, evt, target){
        	if( plot.dataSources.length > 0 ){		
        		var x = Math.round(SimileAjax.DOM.getEventRelativeCoordinates(evt, plot.canvas).x);
        		var y = Math.round(SimileAjax.DOM.getEventRelativeCoordinates(evt, plot.canvas).y);
            	if (x > plot.canvas.width-2 || isNaN(x) || x < 2 ){
            		plot.timeHighlight(true);
            		plot.highlightedSlice = undefined;
            	}
            	else if (y > plot.canvas.height-2 || isNaN(y) || y < 2 ){
            		plot.timeHighlight(true);
            		plot.highlightedSlice = undefined;
            	}
			}
		}

        // handles mouse(h)over-Event on timeplot
        var mouseHoverHandler = function(elmt, evt, target){
        	if( plot.dataSources.length > 0 ){
				var x = Math.round(SimileAjax.DOM.getEventRelativeCoordinates(evt, plot.canvas).x);
				var time = getCorrelatedTime(x);
				if( time == undefined ){
					return;
				}
				var highlightSlice;
        		var slices = plot.tds.timeSlices;
            	var index = plot.tds.getSliceIndex(time);
				if( plot.style == 'graph' ){
					highlightSlice = slices[index];
				}
				if( plot.style == 'bars' ){
	                var pos = plot.timeGeometry.toScreen(time);
					if( x < pos && index > 0 ){
						highlightSlice = slices[index-1];
					}
					else {
						highlightSlice = slices[index];
					}				
				}
				if( plot.highlightedSlice == undefined || plot.highlightedSlice != highlightSlice ){
					plot.highlightedSlice = highlightSlice;
					plot.timeHighlight(false);
				}
        	}
		}

		this.redrawPlot = function(){
			plot.clearTimeplot();				
			plot.tds.reset(this.timeGeometry);
	       	plot.timeplot.repaint();
			if( plot.leftFlagPos != null ){
				plot.leftFlagPos = getCorrelatedPosition(plot.leftFlagTime);
				plot.rightFlagPos = getCorrelatedPosition(plot.rightFlagTime);
				setRangeDivs();
			}
			else {
				plot.displayOverlay();
			}				
			plot.initLabels([]);
			plot.updateOverview();
		}

    	/**
     	 * handles zoom of the timeplot
     	 * @param {int} delta the change of zoom
     	 * @param {Date} time a time that corresponds to a slice, that was clicked
     	*/
		this.zoom = function(delta,time){
			if( this.eventSources.length == 0 ){
				if( STIStatic.timeZoom ){
					this.zoomSlider.setValue(0);
				}
				return false;
			}
			if( time == null ){
				time = getCorrelatedTime(this.canvas.width/2);
			}
			if( this.tds.setZoom(delta,time,this.leftFlagTime,this.rightFlagTime) ){
				this.redrawPlot();
			}
			if( STIStatic.timeZoom ){
				this.zoomSlider.setValue(this.tds.getZoom());
			}
			return true;
		}		

		// handles mousewheel event on the timeplot		
		var mouseWheelHandler = function(elmt, evt, target){
			if (evt.preventDefault){
				evt.preventDefault();
			}
			if( plot.dataSources.length == 0 ){
				return;
			}
			var delta = 0;
			if (!evt) evt = window.event;
			if (evt.wheelDelta) {
				delta = evt.wheelDelta/120; 
				if (window.opera) delta = -delta;
			}
			else if (evt.detail) {
				delta = -evt.detail/3;
			}
			if (delta){
				var x = Math.round(SimileAjax.DOM.getEventRelativeCoordinates(evt, plot.canvas).x);
				var time = getCorrelatedTime(x);
				plot.zoom(delta,time);
			}		
		}
				
        var timeplotElement = this.timeplot.getElement();
        SimileAjax.DOM.registerEvent(timeplotElement, "mousedown", mouseDownHandler);
        SimileAjax.DOM.registerEvent(timeplotElement, "mousemove", mouseMoveHandler);
        SimileAjax.DOM.registerEvent(timeplotElement, "mousemove", mouseHoverHandler);
        SimileAjax.DOM.registerEvent(timeplotElement, "mouseout", mouseOutHandler);
        if( STIStatic.mouseWheelZoom ){
            SimileAjax.DOM.registerEvent(timeplotElement, "mousewheel", mouseWheelHandler);
        }
        
        this.setCanvas();
        
        /*
        if( tools.length > 0 ){
    		var timeplotToolbar = document.createElement("div");
    		timeplotToolbar.style.position = "absolute";
    		timeplotToolbar.id = "timeplotToolbar";
    		if( STIStatic.lightScheme ){
        		timeplotToolbar.setAttribute('class','toolbar'+toolbarStyle.h+' toolbar'+toolbarStyle.v+' toolbar'+toolbarStyle.h+''+'Light');
    		}
    		else {
    			timeplotToolbar.setAttribute('class','toolbar'+toolbarStyle.h+' toolbar'+toolbarStyle.v);
    			timeplotToolbar.style.backgroundColor = STIStatic.toolbarColor;
    			if( toolbarStyle.h == "Left" ){
    				timeplotToolbar.style.borderRight = STIStatic.toolbarBorder;
    			}
    			else {
    				timeplotToolbar.style.borderLeft = STIStatic.toolbarBorder;
    			}    			
    		}
    		timeplotToolbar.style.position = "absolute";
    		timeplotToolbar.appendChild(STIStatic.createToolbar(tools));
    		container.appendChild(timeplotToolbar);
        }
        */
        
    },
    
    /**
     * sets the background canvas of the timeplot window (or resets it after resizing the browser window)
     */
    setCanvas: function(){
        var cv = document.createElement("canvas");
        cv.setAttribute('class','plotCanvas');
        this.plotWindow.appendChild(cv);
        this.configureCanvas(cv, this.plotWindow.clientWidth, this.plotWindow.clientHeight);
        
    },
    
    configureCanvas: function(canvas, width, height ){
        
        canvas.width = width;
        canvas.height = height;
        if (!canvas.getContext && G_vmlCanvasManager) 
            canvas = G_vmlCanvasManager.initElement(canvas);
        var ctx = canvas.getContext('2d');
        var gradient = ctx.createLinearGradient(0, 0, 0, height); 
        if( STIStatic.lightScheme ){
	        gradient.addColorStop(0, '#c9c9cb');
	        gradient.addColorStop(1, '#ededed ');
        }
        else {
	        gradient.addColorStop(0, STIStatic.toolbarColor);
	        gradient.addColorStop(1, STIStatic.toolbarColor);
        }
        ctx.fillStyle = gradient;
        ctx.fillRect(0, 0, width, height);
    },
    
    resetOverlay: function(){
        this.poles.style.visibility = "hidden";
        var plots = this.timeplot._plots;
        for (var i = 0; i < plots.length; i++){
        	for( var j = 0; j < plots[i].pins.length; j++ ){
        		plots[i].pins[j] = {
            		height: 0,
            		count: 0
            	};
        	}
        }
    },
    
	setFilterBar: function(filterBar){
		this.filterBar = filterBar;
	},

    /**
     * resets the timeplot to non selection status
     */
    reset: function(){
    
        this.leftFlagPole.style.visibility = "hidden";
        this.rightFlagPole.style.visibility = "hidden";
        this.rangeBox.style.visibility = "hidden";
        this.leftFeather.style.visibility = "hidden";
        this.rightFeather.style.visibility = "hidden";
        this.leftHandle.style.visibility = "hidden";
        this.rightHandle.style.visibility = "hidden";
        this.toolbar.style.visibility = "hidden";
        this.toolbarAbsoluteDiv.style.visibility = "hidden";
/*
		if( STIStatic.toolbarStyle == 'alternative' ){
			this.zoomButton.style.visibility = "hidden";
			this.cancelButton.style.visibility = "hidden";
		}
*/
		if( STIStatic.animation == 'embedded' ){
			this.animation.style.visibility = "hidden";
		}

        var plots = this.timeplot._plots;
        for (var i = 0; i < plots.length; i++){
            plots[i].opacityPlot.style.visibility = "hidden";
        }
        this.resetOverlay();
        if (this.filterBar != null) {
        	this.filterBar.reset(false);
        }
        Publisher.Publish('reset',this); //vhz

        var slices = this.tds.timeSlices;
        if( slices != undefined ){
        	for (var i = 0; i < slices.length; i++){
            	slices[i].reset();
            }
        }

        this.status = 0;
        this.stop();
		document.onmousemove = null;
		this.updateAnimationButton(0);
        
        this.leftFlagPos = null;
        this.leftFlagTime = null;
        this.rightFlagPos = null;
        this.rightFlagTime = null;
        
        this.mouseDownTime = null;
        this.mouseUpTime = null;
        this.mouseTempTime = null;
        
        this.mouseDownPos = null;
        this.mouseUpPos = null;
        this.mouseTempPos = null;
        
        if( this.popup ){
        	this.popup.reset();
        	this.popupClickDiv.style.visibility = "hidden";
        }

        this.selectedObjects = [];
        
    },
    
    /**
     * sets a pole on the timeplot
     * @param {Date} time the time of the specific timeslice
     * @param {int[]} the number of selected elements per dataset
     */
	displayOverlay: function(){	
        this.poles.style.visibility = "visible";
        var cv = this.poles.getElementsByTagName("canvas")[0];
        cv.width = this.canvas.width;
        cv.height = this.canvas.height;
        if (!cv.getContext && G_vmlCanvasManager){
            cv = G_vmlCanvasManager.initElement(cv);
        } 
        var ctx = cv.getContext('2d');
        ctx.clearRect(0,0,this.canvas.width,this.canvas.height);
		var plots = this.timeplot._plots;
        var slices = this.tds.timeSlices;
		for( var i=0; i<slices.length; i++ ){
			if( this.style == 'bars' && i+1 == slices.length ){
				return;
			}			
			if( slices[i].overlay() == 0 ){
				continue;
			}
			var stacks = slices[i].stacks;
			var time = slices[i].date;			
	        var pos;
	        if( this.style == 'graph' ){
		        pos = this.timeGeometry.toScreen(time);
	        }
	        else if( this.style == 'bars' ){
	        	var x1 = this.timeGeometry.toScreen(time);
	        	var x2 = this.timeGeometry.toScreen(slices[i+1].date);
	        	pos = ( x1 + x2 ) / 2;
	        }
			var heights = [];
			var h = 0;
			for ( var j = 0; j < stacks.length; j++) {
				var data = plots[j]._dataSource.getData();
				for ( var k = 0; k < data.times.length; k++){ 
					if (data.times[k].getTime() == time.getTime()) {
						var height = plots[j]._valueGeometry.toScreen(plots[j]._dataSource.getData().values[k]) * stacks[j].overlay / stacks[j].value;
						heights.push(height);
						plots[j].pins[i] = { height: height, count: stacks[j].overlay };
						if( height > h ){
							h = height;
						}
						break;
					}
				}
			}
			ctx.fillStyle = "rgb(102,102,102)";
        	ctx.beginPath();
			ctx.rect(pos-1,this.canvas.height-h,2,h);
      		ctx.fill();
			for( var j=0; j<heights.length; j++ ){
				if( heights[j] > 0 ){
					ctx.fillStyle = "rgba("+STIStatic.colors[j].r1+","+STIStatic.colors[j].g1+","+STIStatic.colors[j].b1+",0.6)";
					ctx.beginPath();
					ctx.arc(pos, this.canvas.height-heights[j], 2.5, 0, Math.PI*2, true);
					ctx.closePath();
					ctx.fill();
				}
			}
		}
	},
	
    /**
     * updates the timeplot by displaying place poles, after a selection had been executed in another widget
     */
    highlightChanged: function(timeObjects){
    	this.resetOverlay();
   		this.tds.setOverlay(timeObjects);
		this.displayOverlay();
    },
	
    /**
     * updates the timeplot by displaying place poles, after a selection had been executed in another widget
     */
    selectionChanged: function(timeObjects){
       	this.reset();
   		this.tds.setOverlay(timeObjects);       		
   		this.displayOverlay();
    },       

    /**
     * calculates the new feather bounds
     */
    setFeather: function(){
		this.leftFeatherTime = this.leftFlagTime; 
		this.rightFeatherTime = this.rightFlagTime; 
		if( STIStatic.featherSlider ){
	        this.featherWidth = Math.floor(this.canvas.width / this.tds.timeSlices.length * this.slider.getValue());
	        var slices = this.tds.timeSlices;
	        for (var i = 0; i < slices.length; i++) {
	            if (slices[i].date.getTime() == this.leftFlagTime.getTime()) {
	                if (i - this.slider.getValue() >= 0) 
	                    this.leftFeatherTime = slices[i - this.slider.getValue()].date;
	                else 
	                    this.leftFeatherTime = slices[0].date;
	            }
	            if (slices[i].date.getTime() == this.rightFlagTime.getTime()) {
	                if (i + this.slider.getValue() < slices.length) 
	                    this.rightFeatherTime = slices[i + this.slider.getValue()].date;
	                else 
	                    this.rightFeatherTime = slices[slices.length - 1].date;
	            }
	        }
		}
		else {
			this.featherWidth = 0;
		}
    },
	
    /**
     * returns the approximate left position of a slice inside the overview representation
     * @param {Date} time time of the slice
     */
	getOverviewLeft: function(time){
		var w = this.overview.offsetWidth;
		var s = this.tds.earliest().getTime();
		var e = this.tds.latest().getTime();
		var t = time.getTime();		
		return Math.round(w*(t-s)/(e-s));
	},
	
    /**
     * visualizes the overview div (shows viewable part of zoomed timeplot)
     */
	initOverview: function(){
        var labels = this.timeGeometry._grid;
        if( labels.length == 0 ){
        	var plot = this;
        	setTimeout( function(){ plot.initOverview(); }, 10 );
        	return;
        }

        this.overview.style.width = this.canvas.width+"px";
        var left = this.timeplotDiv.offsetLeft;
		this.overview.innerHTML = "";
        this.overview.style.left = left+"px";

        this.overviewRange = document.createElement("div");
        this.overviewRange.setAttribute('class','overviewRange');
        this.overview.appendChild(this.overviewRange);

        for( var i=0; i<labels.length; i++ ){
        	var label = document.createElement("div");
        	label.setAttribute('class','overviewLabel');
        	label.innerHTML = labels[i].label;
        	label.style.left = Math.floor(labels[i].x)+"px";
        	this.overview.appendChild(label);
        }

		this.updateOverview();
	},

    /**
     * visualizes the labels of the timeplot
     */
	initLabels: function(labels){
		if( labels.length == 0 ){
	        labels = this.timeGeometry._grid;
    	    if( labels.length == 0 ){
        		var plot = this;
        		setTimeout( function(){ plot.initLabels([]); }, 10 );
        		return;
        	}
        }
        this.plotLabels.style.width = this.canvas.width+"px";
        var left = this.timeplotDiv.offsetLeft;
        this.plotLabels.style.left = left+"px";
		this.plotLabels.innerHTML = "";
        for( var i=0; i<labels.length; i++ ){
        	var label = document.createElement("div");
        	label.setAttribute('class','plotLabel');
        	label.innerHTML = labels[i].label;
        	label.style.left = Math.floor(labels[i].x)+"px";
        	this.plotLabels.appendChild(label);
        }
	},	
	
    /**
     * updates the overview div
     */
	updateOverview: function(){
		if( this.tds.getZoom() > 0 ){
			this.plotLabels.style.visibility = "hidden";
			this.timeGeometry._hideLabels = false;
			this.overview.style.visibility = "visible";
			this.shiftLeft.style.visibility = "visible";
			this.shiftRight.style.visibility = "visible";
			var left = this.getOverviewLeft( this.tds.timeSlices[this.tds.leftSlice].date );	
			var right = this.getOverviewLeft( this.tds.timeSlices[this.tds.rightSlice].date );
			this.overviewRange.style.left = left+"px";	
			this.overviewRange.style.width = (right-left)+"px";
		}
		else {
			this.timeGeometry._hideLabels = true;
			this.plotLabels.style.visibility = "visible";
			this.overview.style.visibility = "hidden";
			this.shiftLeft.style.visibility = "hidden";
			this.shiftRight.style.visibility = "hidden";
		}
	},
	
    /**
     * returns the time slices which are created by the extended data source
     */
	getSlices: function(){
		return this.tds.timeSlices;
	},
	
	initializeTimeplotTools: function(){
		var timeplotTools = [];
		if( STIStatic.animation ){
	    	this.animation = document.createElement("img");
	    	timeplotTools.push(this.animation);
	    	this.updateAnimationButton(0);
		}
		if( STIStatic.timeZoom ){
			this.zoomSlider = new ZoomSlider('timeZoom',this);
			this.zoomSlider.setMaxAndLevels(100,100);
			this.zoomSlider.setValue(0);
			timeplotTools.push(this.zoomSlider.div);
		}
		if( STIStatic.timeZoom || STIStatic.animation ){
			var logo = document.createElement("img");
			logo.src = STIStatic.path+"time.png";
			timeplotTools.push(logo);
		}
    	return timeplotTools;
	},
	
	updateAnimationButton: function(status){
		if( STIStatic.animation == 'embedded' ){
			if( this.paused ){
				this.animation.style.backgroundImage = "url("+STIStatic.path+"play-enabled.png)";
	    		this.animation.title = STIStatic.getString(this.language,'animationPlay');
			}
			else {
				this.animation.style.backgroundImage = "url("+STIStatic.path+"pause.png)";
	    		this.animation.title = STIStatic.getString(this.language,'animationPause');
			}
		}
		else if( STIStatic.animation == 'external' ){
			this.animationStatus = status;
			if( status == 0 ){
				this.animation.style.backgroundImage = "url("+STIStatic.path+"play-disabled.png)";
	    		this.animation.title = STIStatic.getString(this.language,'animationDisabled');
	    	}
	    	else if( status == 1 ){
				this.animation.style.backgroundImage = "url("+STIStatic.path+"play-enabled.png)";
	    		this.animation.title = STIStatic.getString(this.language,'animationPlay');
	    	}
	    	else if( status == 2 ){
				this.animation.style.backgroundImage = "url("+STIStatic.path+"pause.png)";
	    		this.animation.title = STIStatic.getString(this.language,'animationPause');
	    	}
	    	var plot = this;
	    	this.animation.onclick = function(){
	    		if( status == 1 ){
	    			if( plot.popup ){
	    				plot.popup.reset();
	    			}
	    			plot.play();
	    		}
	    		else if( status == 2 ){
	    			plot.stop();
	    		}
	    	}    	
		}
	},
	
	timeSelection: function(){
		this.setFeather();
		var slices = this.tds.timeSlices;
		var lfs, ls, rs, rfs;
		for (var i = 0; i < slices.length; i++) {
		    if (slices[i].date.getTime() == this.leftFeatherTime.getTime()) 
		        lfs = i;
		    if (slices[i].date.getTime() == this.leftFlagTime.getTime()) 
		        ls = i;
		    if (slices[i].date.getTime() == this.rightFlagTime.getTime()){
		    	if( this.style == 'graph' ){
		            rs = i;
		    	}
		    	if( this.style == 'bars' ){
		            rs = i-1;
		    	}
		    } 
		    if (slices[i].date.getTime() == this.rightFeatherTime.getTime()) {
		    	if( this.style == 'graph' ){
		            rs = i;
		    	}
		    	if( this.style == 'bars' ){
		            rs = i-1;
		    	}
		    }
		}
		this.selectedObjects = [];
		var objects = [];
		for (var i = 0; i < slices.length; i++) {
			var p = 0;
		    if (i > lfs && i < ls){
		    	p = (i - lfs) / (ls - lfs);
		    }
		    else if (i >= ls && i <= rs){
		    	p = 1;
		    }
		    else if (i > rs && i < rfs){
		    	p = (rfs - i) / (rfs - rs);
		    }
		    if( p == 0 ){
		    	continue;
		    }
			var timeObjects = [];
			var valid = false;
			for( var j in slices[i].stacks ){
				timeObjects.push( slices[i].stacks[j].elements );
				if( slices[i].stacks[j].elements.length > 0 ){
					valid = true;
				}
			}
			if( p == 1 ){
				if( this.selectedObjects.length == 0 ){
					for( var j in timeObjects ){
					   this.selectedObjects.push([]);
					}
				}
				for( var j in this.selectedObjects ){
		           	this.selectedObjects[j] = this.selectedObjects[j].concat( timeObjects[j] );
				}
			}
			if( valid ){
				objects.push({ value: p, objects: timeObjects });
			}
		}
	    	this.core.triggerSelection(objects);
	    	if (this.filterBar != null) {
	    		this.filterBar.reset(true);
	    	}
	},
	
	deselection: function(){
		if (this.selectedObjects.length > 0) {
			this.reset();
			this.core.triggerSelection([]);
		}
	},

	filtering: function(){
		this.core.triggerRefining(this.selectedObjects);
	},

	inverseFiltering: function(){
		var slices = this.tds.timeSlices;
		var ls, rs;
		for (var i = 0; i < slices.length; i++) {
		    if (slices[i].date.getTime() == this.leftFlagTime.getTime()) 
		        ls = i;
		    if (slices[i].date.getTime() == this.rightFlagTime.getTime()){
		    	if( this.style == 'graph' ){
		            rs = i;
		    	}
		    	if( this.style == 'bars' ){
		            rs = i-1;
		    	}
		    } 
		}
		this.selectedObjects = [];
		for (var i = 0; i < slices.length; i++){
			if( i >= ls && i <= rs ){
				continue;
			}
			var timeObjects = [];
			var valid = false;
			for( var j in slices[i].stacks ){
				timeObjects.push( slices[i].stacks[j].elements );
				if( slices[i].stacks[j].elements.length > 0 ){
					valid = true;
				}
			}
			if( this.selectedObjects.length == 0 ){
				for( var j in timeObjects ){
				   this.selectedObjects.push([]);
				}
			}
			for( var j in this.selectedObjects ){
		           	this.selectedObjects[j] = this.selectedObjects[j].concat( timeObjects[j] );
			}
		}
		this.filtering();
	},
	
	timeHighlight: function(undo){
		if( this.status == 0 ){
			var s = this.highlightedSlice;
			if( this.leftFlagTime != null ){
				if( this.style == 'graph' && s.date >= this.leftFlagTime && s.date <= this.rightFlagTime ){
					return;
				}
				if( this.style == 'bars' && s.date >= this.leftFlagTime && s.date < this.rightFlagTime ){
					return;
				}
			}
			var timeObjects = [];
			var count = 0;
			if( !undo ){
				for( var i in s.stacks ){
					timeObjects.push( s.stacks[i].elements );
					count += s.stacks[i].elements.length;
				}
			}
			var objects = [];
			if( this.selectedObjects.length > 0 ){
				objects.push({ value: 1, objects: this.selectedObjects });
			}
			if( count > 0 ){
				objects.push({ value: 1, objects: timeObjects });
			}
			this.core.triggerHighlight(objects);
		}
	},
	
	timeRefining: function(){
		this.core.triggerRefining(this.selectedObjects);
	},
	
	setLanguage: function(language){
		this.language = language;
        this.leftHandle.title = STIStatic.getString(language,'leftHandle');
        this.rightHandle.title = STIStatic.getString(language,'rightHandle');
        this.dragButton.title = STIStatic.getString(language,'dragTimeRange');
//        this.zoomButton.title = STIStatic.getString(language,'zoomSelection');
//        this.cancelButton.title = STIStatic.getString(language,'clearSelection');
        if( STIStatic.animation == 'embedded'){
    		if( this.paused ){
        		this.animation.title = STIStatic.getString(this.language,'animationPause');
        	}
        	else {
        		this.animation.title = STIStatic.getString(this.language,'animationPlay');
        	}
        }
        else if( STIStatic.animation == 'external'){
    		if( this.animationStatus == 0 ){
        		this.animation.title = STIStatic.getString(this.language,'animationDisabled');
        	}
        	else if( this.animationStatus == 1 ){
        		this.animation.title = STIStatic.getString(this.language,'animationPlay');
        	}
        	else if( this.animationStatus == 2 ){
        		this.animation.title = STIStatic.getString(this.language,'animationPause');
        	}
        }
	},
	
	setStyle: function(style){
		this.style = style;
	}
	
};
