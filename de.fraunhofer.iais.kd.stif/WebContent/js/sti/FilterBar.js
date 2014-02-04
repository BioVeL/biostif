/**
 * @class FilterBar
 * Implementation for FilterBar Object
 * @author Stefan Jänicke (stjaenicke@informatik.uni-leipzig.de)
 * @version 0.9
 */

function FilterBar(core, data, parentDiv){

	var bar = this;
	this.core = core;
	this.data = data;
	this.selectedObjects = [];

	this.toolbar;
	if (typeof(STIStatic.filterBarDisplayOnce) == "undefined" || (!STIStatic.filterBarDisplayOnce)) {
		this.toolbar = document.createElement('div');
		this.toolbar.setAttribute('class','filterBar');
		this.toolbar.setAttribute('id','filterBar');
		parentDiv.appendChild(this.toolbar);
	} else {
		this.toolbar = parentDiv;
	}

	this.filter = document.createElement('div');
	this.filter.setAttribute('class','smallButton filter');
	this.filter.setAttribute('id','smallButton filter');
	$(this.filter).attr("title", STIStatic.getString(STIStatic.language,'filterSelectedItemsHelp'));
	this.toolbar.appendChild(this.filter);	
	this.filter.onclick = function(){
		bar.filtering();
	};

	this.filterInverse = document.createElement('div');
	this.filterInverse.setAttribute('class','smallButton filterInverse');
	this.filterInverse.setAttribute('id','smallButton filterInverse');
	$(this.filterInverse).attr("title", STIStatic.getString(STIStatic.language,'inverseFilterSelectedItemsHelp'));
	this.toolbar.appendChild(this.filterInverse);
	
	this.filterInverse.style.display = 'none';
	
	this.filterInverse.onclick = function(){
		bar.inverseFiltering(); //XXX RK disable filterinverse by uncommenting this line
	};
	if( !STIStatic.inverseFilter ){
		this.filterInverse.style.display = 'none';
	}

	
	
	this.cancelSelection = document.createElement('div');
	this.cancelSelection.setAttribute('class','smallButton cancelSelection');
	this.cancelSelection.setAttribute('id','smallButton cancelSelection');
	$(this.cancelSelection).attr("title", STIStatic.getString(STIStatic.language,'cancelSelection'));
	this.toolbar.appendChild(this.cancelSelection);
	
	this.cancelSelection.onclick = function(){
		//parent.deselection();
		Publisher.Publish('deselect'); //vhz
		document.getElementById("smallButton mask").style.display = "none";
		bar.showFilterBar(false);
	};
	
	this.showFilterBar =function(visible) {
    	var displaystyle = 'block';
    	if (typeof(STIStatic.filterDisplayStyle) != undefined) {
    		displaystyle = STIStatic.filterDisplayStyle;
    	}    
    	if (!visible) {
    		displaystyle = 'none';
    	}
    	if (typeof(STIStatic.filterBarDisplayOnce) != "undefined" || (!STIStatic.filterBarDisplayOnce)) {
    		this.filter.style.display = displaystyle;
    		if (STIStatic.inverseFilter){
    			this.filterInverse.style.display = displaystyle;
    		}
    		this.cancelSelection.style.display = displaystyle;
    	} else {
    		this.toolbar.style.display = displaystyle;
    		this.toolbar.style.left = parentDiv.offsetWidth+"px";
//    		this.toolbar.style.right = "0px";
    		this.toolbar.style.top = Math.floor(parentDiv.offsetHeight/2-this.toolbar.offsetHeight/2)+"px";
    	}
    };

	this.showFilterBar(false);
	//this.toolbar.style.display = "none";
	

	Publisher.Subscribe('selection',function(data,id){
		
		// das sind die selektierten objekte
		
  		logg("FBar subscribed selection -- by id == " + id);
		
        if( typeof bar != 'undefined' ){
        	if (data && data.length > 0) {
                if (id === 'map') {
                    bar.selectedObjects = data[0].objects;
                } else if (id === 'plot') {
                    var nbDatasets = data[0].objects.length;
                    var selectedSets = [];
                    for (var i=0; i<nbDatasets; i++) {
                        selectedSets.push([]);
                    }
                    for (var t=0; t<data.length; t++) {
                        var tsets = data[t].objects;
                        for (var d=0; d<nbDatasets; d++) {
                            var more = tsets[d];
                            var sset = selectedSets[d];
                            selectedSets[d] = sset.concat(more);
                        }
                    }
                    bar.selectedObjects = selectedSets;
                } else if (id === 'table') {
                    bar.selectedObjects = data[0].objects;
                } else {
                    logg("error: origin of selection, unknown id: " + id);
                }
        	}
           //  bar.selectedObjects = data;
             bar.showFilterBar(true);
             
//             bar.setData(data);
             
             if(data.length > 0){
            	 for(var i=0; i<data.length; i++ ){
//            		logg("FBar selection subscr data[i]: " + i);
//            		logg("FBar selection subscr data.len: " + data[i].objects.length);
            		for(var j=0; j<data[i].objects.length; j++ ){
            			logg("FBar selected objects: " + data[i].objects[j].length);
            			if(data[i].objects[j].length > 0){
            				Publisher.Publish("points_selected");
            				
            			}
            		}
            	 }
             } 
        }	
        
    });
	
	
	Publisher.Subscribe('filterbardata',function(data){
		
        if( typeof bar != 'undefined' ){
        	if (data && data.length > 0) {
        		bar.setData(data);
        	}
        	
        logg("FBar filterbardata data.objects.len: " + data[0].length);

        }	
       
    });

	
	Publisher.Subscribe('reset',function(widget){
        if( typeof bar != 'undefined' ){
             bar.reset(false);
        }	
    });
	
	this.setData = function (data) {
		this.data = data;
	};
	
	this.reset = function(show){
		this.showFilterBar(show);
//		if( show ){
//			this.toolbar.style.display = "block";
//		}
//		else {
//			this.toolbar.style.display = "none";
//		}
//		this.toolbar.style.left = parentDiv.offsetWidth+"px";
////		this.toolbar.style.right = "0px";
//		this.toolbar.style.top = Math.floor(parentDiv.offsetHeight/2-this.toolbar.offsetHeight/2)+"px";
	};
	
	this.filtering = function(){
//		logg("HOHO this filtering");
		logg("\n\n ******* new filtering");
		document.getElementById("smallButton mask").style.display = "none";
		this.core.triggerRefining(this.selectedObjects);
	};
	
	this.inverseFiltering = function(){
		
		logg("\n\n ******* new INV filter");
		
		if(this.data !=null){
			if (this.selectedObjects.length == 0) {
                this.selectedObjects = this.data;
				this.filtering();
				return;
			}
    	var new_selectedObjects = [];
    	for( var i=0; i<this.data.length; i++ ){
//    		logg("data i: " + i);
    		var new_datasource_selectedObjects = [];    		
    		if (this.selectedObjects[i].length == 0) {
    			logg("inverseFiltering new selectedobjects length NULL");
    			new_selectedObjects[i] = this.data[i];
    		} else {
    			logg("inverseFiltering this.data[i].length: " + this.data[i].length);
    			
    			var elg = 0;
    			var elsor = 0;
    			
	    		for( var j=0; j < this.data[i].length; j++ ){
	    			elg++;
	    			var element = this.data[i][j];
	    			if ($.inArray(element, this.selectedObjects[i]) <0) {
	    				elsor++;
	    				new_datasource_selectedObjects.push(element);
	    			}
	    		}
	    		logg("elemente gesamt: " + elg + " sortiert: " + elsor);
//	    		logg("after add: new_datasource_selectedObjects: " + new_datasource_selectedObjects.length);
	    		new_selectedObjects[i] = new_datasource_selectedObjects;
    		}
    	}
    	this.selectedObjects = new_selectedObjects;
//    	this.core.triggerSelection(this.selectedObjects);
		
        this.filtering();
        
	} else {
		logg("inverseFiltering this.data null??: " + this.data);
	}
    };
    
    

};
