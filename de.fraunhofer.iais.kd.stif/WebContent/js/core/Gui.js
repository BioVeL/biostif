var gui = new function(){

	this.mapCount;
	this.plotCount;

	var gui = this;

    this.initializeMapGUI = function(id){

	this.mapContainer = document.getElementById(id);
	if( STIStatic.mapWidth ){
		this.mapContainer.style.width = STIStatic.mapWidth;
        }
        if( STIStatic.mapHeight ){
        	this.mapContainer.style.height = STIStatic.mapHeight;
        }
        SpaceWrapper.map = new STIMap(SpaceWrapper,this.mapContainer);
        this.map = SpaceWrapper.map;

		var toolbarTable = document.createElement("table");
		toolbarTable.setAttribute('class','absoluteToolbar ddbToolbar');
		this.mapContainer.appendChild(toolbarTable);
		this.mapToolbar = toolbarTable;

		var titles = document.createElement("tr");
		toolbarTable.appendChild(titles);
		var tools = document.createElement("tr");
		toolbarTable.appendChild(tools);

		if (typeof(STIStatic.showMapTypeSelector) == "undefined" || STIStatic.showMapTypeSelector) {
			this.mapTypeTitle = document.createElement("td");
			titles.appendChild(this.mapTypeTitle);
			this.mapTypeSelector = document.createElement("td");
			tools.appendChild(this.mapTypeSelector);
		}

		//vhz overlay selector for (the selected overlay in the combobox is the one used for the geographic selection
		if (STIStatic.showOverlaySelector) {
			this.overlaySelectorTitle = document.createElement("td");
			titles.appendChild(this.overlaySelectorTitle);
			this.overlaySelector = document.createElement("td");
			tools.appendChild(this.overlaySelector);
		}
		//~vhz
			
		//vhz habe reihenfolge geaendert, weil das Binning weiter hinter kommt
		this.mapSelectorTitle = document.createElement("td");
		titles.appendChild(this.mapSelectorTitle);
		var mapSelectorTools = document.createElement("td");
		var selectorTools = this.map.initSelectorTools();
		for( var i in selectorTools ){
			mapSelectorTools.appendChild(selectorTools[i].button);
		}
		tools.appendChild(mapSelectorTools);
			
		this.binningTitle = document.createElement("td");
		titles.appendChild(this.binningTitle);
		this.binningSelector = document.createElement("td");
		tools.appendChild(this.binningSelector);
		
		var gui = this;
		//vhz
		this.showBbox(STIStatic.showBBox);
		//~vhz
		//vhz for lat lon if defined in StiStatic
		var mapMousePosition = document.createElement("td");
		this.mapMousePositionVisibilityChooser = document.createElement("div");
		$(this.mapMousePositionVisibilityChooser).addClass('mapPositionControl');
		this.mousePositionVisibilityOption = STIStatic.olMousePosition;
		this.showMousePosition (this.mousePositionVisibilityOption);
		this.mapMousePositionVisibilityChooser.onclick = function(){								
			gui.showMousePosition(!gui.mousePositionVisibilityOption);
		};
		mapMousePosition.appendChild(this.mapMousePositionVisibilityChooser);
		
		this.mapMousePositionDiv = document.createElement("div");
		//this.mapMousePositionDiv.setAttribute('class','mapPositionText');
		$(this.mapMousePositionDiv).addClass('mapPositionText');
		mapMousePosition.appendChild(this.mapMousePositionDiv);
		
						
		titles.appendChild(mapMousePosition);
		//~vhz
		
		var mapSum = document.createElement("td");
		this.mapElements = document.createElement("div");
		this.mapElements.setAttribute('class','ddbElementsCount');
		mapSum.appendChild(this.mapElements);
		tools.appendChild(mapSum);

		
		
		var top;
		if( navigator.geolocation && STIStatic.geoLocation ){
			this.geoActive = false;
			this.geoLocation = document.createElement("div");
			this.geoLocation.setAttribute('class','geoLocationOff');
			this.map.mapWindow.appendChild(this.geoLocation);
			this.geoLocation.style.left = "20px";
			this.geoLocation.onclick = function(){
				var changeStyle = function(){
					if( gui.geoActive ){
						gui.geoLocation.setAttribute('class','geoLocationOn');
		                gui.geoLocation.title = STIStatic.getString(STIStatic.language,'deactivateGeoLocation');
					}
					else {
						gui.geoLocation.setAttribute('class','geoLocationOff');
		                gui.geoLocation.title = STIStatic.getString(STIStatic.language,'activateGeoLocation');
					}
				}
				if( !gui.geoActive ){
					if( typeof gui.longitude == 'undefined' ){
						navigator.geolocation.getCurrentPosition(function(position){
						  	gui.longitude = position.coords.longitude;
							gui.latitude = position.coords.latitude;
							gui.map.setMarker(gui.longitude,gui.latitude);
							gui.geoActive = true;
							changeStyle();
						}, function(msg){
							console.log(typeof msg == 'string' ? msg : "error");
						});
					}
					else {
						gui.map.setMarker(gui.longitude,gui.latitude);
						gui.geoActive = true;
						changeStyle();
					}
				}
				else {
					gui.map.removeMarker();
					gui.geoActive = false;
					changeStyle();
				}
			};
		}

		if( !STIStatic.olNavigation ){
			this.map.zoomSlider = new MapZoomSlider(this.map,"vertical");
			this.map.mapWindow.appendChild(this.map.zoomSlider.div);
			this.map.zoomSlider.div.style.left = "20px";
		}

		if( STIStatic.resetMap ){
			this.homeButton = document.createElement("div");
			this.homeButton.setAttribute('class','mapHome');
			this.map.mapWindow.appendChild(this.homeButton);
			this.homeButton.style.left = "20px";
			this.homeButton.onclick = function(){
				gui.map.drawObjectLayer(true);
			};
		}

//		var tooltip = document.createElement("div");
//		tooltip.setAttribute('class','ddbTooltip');
//		toolbarTable.appendChild(tooltip);

//		var tooltip = document.createElement("div");
//		tooltip.setAttribute('class','ddbTooltip');
//		toolbarTable.appendChild(tooltip);
//
//		tooltip.onmouseover = function(){
//			/*
//		    Publisher.Publish('TooltipContent', {
//						content: STIStatic.getString(STIStatic.language,'timeHelp'),
//						target: $(tooltip)
//					    });
//			*/
//		}
//		tooltip.onmouseout = function(){
//		 //   Publisher.Publish('TooltipContent');
//		}
//		//vhz tooltip on click should open a help file if defined in STIStatic
//		if(STIStatic.helpURL) {
//			tooltip.onclick = function () {
//				
//			}
//		}
			
//		}
//		tooltip.onmouseout = function(){
//   			Publisher.Publish('TooltipContent');
//		}

		this.map.setLanguage(STIStatic.language);
		this.setLanguageForMap(STIStatic.language);

		var top = toolbarTable.offsetHeight + 20;
		if( typeof this.geoLocation != "undefined" ){
			this.geoLocation.style.top = top+"px";
			top += this.geoLocation.offsetHeight+4;
		}
		if( STIStatic.olNavigation ){
			var panBar = $(".olControlPanPanel")[0];
			var zoomBar = $(".olControlZoomPanel")[0];
			$(panBar).css('top',top+'px');
			var panSouth = $(".olControlPanSouthItemInactive")[0];
			top = $(panSouth).height() + $(panSouth).offset().top;
			$(zoomBar).css('top',top+'px');
			var zoomOut = $(".olControlZoomOutItemInactive")[0];
			top = $(zoomOut).height() + $(zoomOut).offset().top;
		}
		else {
			this.map.zoomSlider.div.style.top = top+"px";
			top += this.map.zoomSlider.div.offsetHeight+2;
		}
		if( STIStatic.resetMap ){
			this.homeButton.style.top = top+"px";
		}

		this.headerHeight = toolbarTable.offsetHeight;

		//vhz		
		Publisher.Publish ("MapGuiInitialized", SpaceWrapper);
		//~vhz

    };

    this.initializePlotGUI = function(id){

    	this.plotContainer = document.getElementById(id);
	if( STIStatic.timeplotWidth ){
		this.plotContainer.style.width = STIStatic.timeplotWidth;
        }
        if( STIStatic.timeplotHeight ){
        	this.plotContainer.style.height = STIStatic.timeplotHeight;
        }

	var toolbarTable = document.createElement("table");
	toolbarTable.setAttribute('class','ddbToolbar');
	this.plotContainer.appendChild(toolbarTable);

        TimeWrapper.plot = new STITimeplot(TimeWrapper,this.plotContainer);
        this.plot = TimeWrapper.plot;

		var titles = document.createElement("tr");
		toolbarTable.appendChild(titles);
		var tools = document.createElement("tr");
		toolbarTable.appendChild(tools);

		
		this.timeUnitTitle = document.createElement("td");
		titles.appendChild(this.timeUnitTitle);
		this.timeUnitSelector = document.createElement("td");
		tools.appendChild(this.timeUnitSelector);

		/*
		this.timeAnimation = document.createElement("td");
		var timeAnimationTools = document.createElement("td");
		this.plot.animation = document.createElement("div");
		this.plot.animation.setAttribute('class','animation');
		timeAnimationTools.appendChild(this.plot.animation);

		titles.appendChild(this.timeAnimation);
		tools.appendChild(timeAnimationTools);
		*/

		var timeSum = document.createElement("td");
		this.timeElements = document.createElement("div");
		this.timeElements.setAttribute('class','ddbElementsCount');
		timeSum.appendChild(this.timeElements);
		tools.appendChild(timeSum);
/*
		var tooltip = document.createElement("div");
		tooltip.setAttribute('class','ddbTooltip');
		toolbarTable.appendChild(tooltip);

		tooltip.onmouseover = function(){
			/*
		    getPublisher().Publish('TooltipContent', {
						content: STIStatic.getString(STIStatic.language,'timeHelp'),
						target: $(tooltip)
					    });
			
		}
		tooltip.onmouseout = function(){
		    //getPublisher().Publish('TooltipContent');
		}

/*
		var sumElementsFailed = document.createElement("td");
		sumElementsFailed.innerHTML = "19 Results without time information";
		tools.appendChild(sumElementsFailed);
*/

		this.plot.setLanguage(STIStatic.language);
		this.setLanguageForPlot(STIStatic.language);

//		toolbarTable.style.height = (toolbarTable.offsetHeight+2*this.verticalMargin)+"px";
		this.plotContainer.style.height = (this.plot.plotWindow.offsetHeight+toolbarTable.offsetHeight)+"px";
//		this.plot.plotWindow.style.top = (plotToolbar.offsetHeight-1)+"px";
		Publisher.Publish ("TimePlotGuiInitialized", TimeWrapper);

    };


    this.initializeTableGUI = function(id){

    	this.tableContainer = document.getElementById(id);
	if( STIStatic.tableWidth ){
		this.tableContainer.style.width = STIStatic.tableWidth;
        }
        if( STIStatic.tableHeight ){
        	this.tableContainer.style.height = STIStatic.tableHeight;
        }
        TableWrapper.table = new TableWidget(TableWrapper,this.tableContainer);
        this.table = TableWrapper.table;
        Publisher.Publish ("TableGuiInitialized", TableWrapper);


    };



    this.updateSpaceQuantity = function(count){
    	var lang = STIStatic.language;
    	this.mapCount = count;
    	if( count != 1 ){
    		this.mapElements.innerHTML = this.beautifyCount(count)+" "+STIStatic.getString(lang,'results')+" "+STIStatic.getString(lang,'resultsLocation');
    	}
    	else {
    		this.mapElements.innerHTML = this.beautifyCount(count)+" "+STIStatic.getString(lang,'result')+" "+STIStatic.getString(lang,'resultsLocation');
    	}
    }

    this.updateTimeQuantity = function(count){
    	var lang = STIStatic.language;
    	this.plotCount = count;
    	if( count != 1 ){
    		this.timeElements.innerHTML = this.beautifyCount(count)+" "+STIStatic.getString(lang,'results')+" "+STIStatic.getString(lang,'resultsTime');
    	}
    	else {
    		this.timeElements.innerHTML = this.beautifyCount(count)+" "+STIStatic.getString(lang,'result')+" "+STIStatic.getString(lang,'resultsTime');
    	}
    }

    this.setSpaceFacetDropdown = function(){
    return;
    	$(this.locationTypeSelector).empty();
		var locationFacets = getSpaceFacets();
		var facets = [];
		var addFacet = function(name,index){
			var setFacet = function(){
			    SpaceWrapper.setSpaceFacet(name);
			}
			facets.push({
				name: name,
				onclick: setFacet
			});
		}
		if( this.spaceFacet ){
			facets.push({
				name: this.spaceFacet,
				onclick: null
			});
		}
		else {
			for( var i=0; i<locationFacets.length; i++ ){
				addFacet(locationFacets[i],i);
			}
		}
		this.locationFacetDropdown = new Dropdown(this.locationTypeSelector,this.mapContainer,facets,'selectLocationType');
		this.locationFacetDropdown.setEntry(0);
		this.locationFacetDropdown.setLanguage(STIStatic.language);
    }

    this.setTimeUnitDropdown = function(){
    	$(this.timeUnitSelector).empty();
		var gui = this;
		var timeUnits = [ "day", "month", "year" ];
		var units = [];
		var addUnit = function(name,index){
			var setUnit = function(){
			    gui.plot.setTimeUnit(name);
			}
			units.push({
				name: name,
				onclick: setUnit
			});
		}
		for( var i=0; i<timeUnits.length-2; i++ ){
			addUnit(timeUnits[i],i);
		}
		this.timeUnitDropdown = new Dropdown(this.timeUnitSelector,this.plotContainer,units,'selectTimeUnit');
		this.timeUnitDropdown.setEntry(0);
		this.timeUnitDropdown.setLanguage(STIStatic.language);
    }

    this.setMapsDropdown = function(){
    	$(this.mapTypeSelector).empty();
		var maps = [];
		var gui = this;
		var addMap = function(name,index){
			var setMap = function(){
				gui.map.setMap(index);
			}
			if( name == 'latest' ){
				name = STIStatic.getString(STIStatic.language,'contemporaryMap');
			}
			else if( name.indexOf('mapOf:') != -1 ){
				name = STIStatic.getString(STIStatic.language,'mapOf') + ' ' + name.substring(name.indexOf(':')+1);
			}
			maps.push({
				name: name,
				onclick: setMap
			});
		}
		for( var i=0; i<this.map.baseLayers.length; i++ ){
			addMap(this.map.baseLayers[i].name,i);
		}
		this.mapTypeDropdown = new Dropdown(this.mapTypeSelector,this.mapContainer,maps,'selectMapType');
		this.mapTypeDropdown.setEntry(this.map.baselayerIndex);
		this.mapTypeDropdown.setLanguage(STIStatic.language);
    };

    this.setBinningDropdown = function(){
    	$(this.binningSelector).empty();
		var binnings = [];
		var gui = this;
		var index = 0;
		var entry = 0;
		var addBinning = function(name,id){
			if( STIStatic.binning == id ){
				entry = index;	
			}
			else {
				index++;
			}
			var setBinning = function(){
				STIStatic.binning = id;
				gui.map.initMap(gui.map.mapObjects,false);
				gui.map.riseLayer(gui.table.activeTable);
			};
			binnings.push({
				name: name,
				onclick: setBinning
			});
		};
		addBinning(STIStatic.getString(STIStatic.language,'genericBinning'),'generic');
		addBinning(STIStatic.getString(STIStatic.language,'squareBinning'),'square');
		addBinning(STIStatic.getString(STIStatic.language,'hexagonalBinning'),'hexagonal');
		addBinning(STIStatic.getString(STIStatic.language,'triangularBinning'),'triangular');
		addBinning(STIStatic.getString(STIStatic.language,'noBinning'),false);
		var dd = new Dropdown(this.binningSelector,this.mapContainer,binnings,'selectBinningType');
		dd.setEntry(entry);
		dd.setLanguage(STIStatic.language);
    };


    //vhz
    //configuration of the overlaySelector
    this.setOverlayDropdown = function(){
    	if (!STIStatic.showOverlaySelector)  {
    		return;
    	}
    	$(this.overlaySelector).empty();
		var overlays = [];
		var gui = this;
		var visibleOverlayIndex = -1;
		var addOverlay = function(name,index){
			var setOverlay = function(){
				
//				logg("setO: " + name + " " + index);
				
				if(name == "no overlay"){
					for(var j = 0; j < gui.map.wmsOverlays.length; j++){
						gui.map.wmsOverlays[j].setVisibility(false);
					}
				}
				
				if (visibleOverlayIndex >= 0) {
					gui.map.wmsOverlays[visibleOverlayIndex].setVisibility(false);
				}
				if (index < 0) {
					gui.map.deactivateCountrySelector();
				} else {
					gui.map.wmsOverlays[index].setVisibility(true);					
					gui.map.activateCountrySelector(gui.map.wmsOverlays[index]);
				}
				visibleOverlayIndex = index;
					
			};
			
			overlays.push({
				name: name,
				onclick: setOverlay
			});
		}

//		for( var i=0; i<this.map.wmsOverlays.length; i++ ){
//			addOverlay(this.map.wmsOverlays[i].name,i);
//		}
		for( var i=0; i<gui.map.wmsOverlays.length; i++ ){
			addOverlay(gui.map.wmsOverlays[i].name,i);
		}
		//XXX rk add dummy del overlay
		addOverlay("no overlay",-1);
		this.overlayDropdown = new Dropdown(this.overlaySelector,this.mapContainer,overlays,'selectOverlay');
		//this.overlayDropdown.setEntry(this.map.baselayerIndex);
		this.overlayDropdown.setLanguage(STIStatic.language);
    };
    //~vhz
    
    this.setLanguageForMap = function(){
    	var language =  STIStatic.language;
    	if (this.homeButton) {
    		this.homeButton.title = STIStatic.getString(language,'home');
    	}
        if( typeof this.geoLocation != "undefined" ){
        	if( this.geoActive ){
                this.geoLocation.title = STIStatic.getString(language,'deactivateGeoLocation');
        	}
        	else {
                this.geoLocation.title = STIStatic.getString(language,'activateGeoLocation');
        	}
        }
		this.mapSelectorTitle.innerHTML = STIStatic.getString(language,'mapSelectorTools');
		if (typeof(STIStatic.showMapTypeSelector) == "undefined" || STIStatic.showMapTypeSelector) {
			this.mapTypeTitle.innerHTML = STIStatic.getString(language,'mapType');
		}
		this.binningTitle.innerHTML = STIStatic.getString(language,'binningType');
		this.binningTitle.title = STIStatic.getString(language,'binningTooltip');
		
    	if( this.mapCount ){
        	if( this.mapCount != 1 ){
        		this.mapElements.innerHTML = this.beautifyCount(this.mapCount)+" "+STIStatic.getString(language,'results')+" "+STIStatic.getString(language,'resultsLocation');
        	}
        	else {
        		this.mapElements.innerHTML = this.beautifyCount(this.mapCount)+" "+STIStatic.getString(language,'result')+" "+STIStatic.getString(language,'resultsLocation');
        	}
    	}
    	if (STIStatic.showOverlaySelector) {
			this.overlaySelectorTitle.innerHTML = STIStatic.getString(language,'overlaySelector');
			this.setOverlayDropdown();
    	}
//    	this.setSpaceFacetDropdown();
    	if (typeof(STIStatic.showMapTypeSelector) == "undefined" || STIStatic.showMapTypeSelector) {
    		this.setMapsDropdown();
    	}
    	this.setBinningDropdown();


//    	this.map.popup.setLanguage(language);
    }

    this.beautifyCount = function(count){
   		var c = count+'';
  		var p = 0;
   		var l = c.length;
   		while( l-p > 3 ){
   			p += 3;
   			c = c.substring(0,l-p) + "." + c.substring(l-p);
   			p++;
   			l++;
   		}
    	return c;
    };

    this.setLanguageForPlot = function(){
    	var language = STIStatic.language;
//		this.timeAnimation.innerHTML = STIStatic.getString(language,'timeAnimation');
//		this.timeUnitTitle.innerHTML = STIStatic.getString(language,'timeUnit');
		if( this.plotCount ){
	    	if( this.plotCount != 1 ){
	    		this.timeElements.innerHTML = this.beautifyCount(this.plotCount)+" "+STIStatic.getString(language,'results')+" "+STIStatic.getString(language,'resultsTime');
	    	}
	    	else {
	    		this.timeElements.innerHTML = this.beautifyCount(this.plotCount)+" "+STIStatic.getString(language,'result')+" "+STIStatic.getString(language,'resultsTime');
	    	}
		}
    	this.setTimeUnitDropdown();
    }

    //vhz
    this.showBboxStatus = function(show){		
		if( show ){
			this.showBboxButton.setAttribute('class','hideBbox');
			this.showBboxButton.title = STIStatic.getString(STIStatic.language,'hideBbox');
		}
		else {
			this.showBboxButton.setAttribute('class','showBbox');
			this.showBboxButton.title = STIStatic.getString(STIStatic.language,'showBbox');					
		}
	};
	
	//vhz
	this.showBbox = function (show) {
		if (show) {
			this.showBboxOption = show;
			this.showBboxTitle = document.createElement("td");
			this.showBboxTitle.style.textAlign = "center";
			
			this.showBboxTitle.innerHTML = STIStatic.getString(STIStatic.language,'showBoxTitle');
			this.mapToolbar.firstChild.appendChild(this.showBboxTitle);
			this.showBboxTool = document.createElement("td");
			this.showBboxTool.style.textAlign = "center";
			//insert the tools before the mapElement with the number of objects
			if (this.mapElements) {
				var node = this.mapToolbar.childNodes.item(1).childNodes.item(0);
				while (node != null) {
					if (node.hasChildNodes() && node.firstChild === this.mapElements) {
				    	this.mapToolbar.childNodes.item(1).insertBefore(this.showBboxTool, node);
				    	node = null;					  
					} else {
					  node = node.nextSibling;
					}
				}
				
			} else {
				this.mapToolbar.childNodes.item(1).appendChild(this.showBboxTool);
			}
			this.showBboxButton = document.createElement("div");
			this.showBboxTool.appendChild(this.showBboxButton);
						
			this.showBboxButton.onclick = function(){
				gui.showBboxOption = !gui.showBboxOption;
				gui.showBboxStatus(gui.showBboxOption);
				gui.map.showBBox(gui.showBboxOption);
			};
			this.showBboxStatus(this.showBboxOption);
		} else { //delete the entries of the document
			if (this.showBboxTitle &&  this.mapToolbar.hasChildNodes() &&(this.mapToolbar.firstChild.childNodes.length > 0)) {
				var node = this.mapToolbar.firstChild.childNodes.item(0);
				while (node != null) {
				  if (node === this.showBboxTitle) {
					  this.mapToolbar.firstChild.removeChild(node);
					  node = null;
					  this.showBboxTitle = null;
				  } else {
					  node = node.nextSibling;
				  }
				}
			}
			if (this.showBboxTool &&  this.mapToolbar.hasChildNodes() &&(this.mapToolbar.childNodes.item(1).childNodes.length > 0)) {
				var node = this.mapToolbar.childNodes.item(1).childNodes.item(0);
				while (node != null) {
				  if (node === this.showBboxTool) {
					  this.mapToolbar.childNodes.item(1).removeChild(node);
					  node = null;
					  this.showBboxTool = null;
				  } else {
					  node = node.nextSibling;
				  }
				}
			}
		}
	};
	
	this.showMousePosition = function (show) {
		this.mousePositionVisibilityOption = show;
		
		if (show) {			
			//setup button for hiding
			this.mapMousePositionVisibilityChooser.innerHTML = STIStatic.getString(STIStatic.language,'deactivateMousePosition');
			this.mapMousePositionVisibilityChooser.title = STIStatic.getString(STIStatic.language,'deactivateMousePositionHelp');			
		} else {
			//setup Button for showing
			this.mapMousePositionVisibilityChooser.innerHTML = STIStatic.getString(STIStatic.language,'activateMousePosition');
			this.mapMousePositionVisibilityChooser.title = STIStatic.getString(STIStatic.language,'activateMousePositionHelp');
			this.mapMousePositionDiv.innerHTML = "<span></span>";           
		}
		this.map.showMousePosition(show);
	};
	//~vhz
};
