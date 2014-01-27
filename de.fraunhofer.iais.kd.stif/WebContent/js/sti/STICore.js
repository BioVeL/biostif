/**
 * defines the core component of the Spatio Temporal Interface
 *
 * @constructor
 */
function STICore(){

    this.map;
    this.timeplot;
    this.tables;
    
    this.refining;
    
    this.history;
    this.historyIndex;
    
    this.initialize();
    
};

STICore.prototype = {

    /**
     * initializes the core component for the Spatio Temporal Interface.
     * here, the handling of the search interface is defined (including undo, refine and clear selection button).
     * furthermore, the elements (map, timeplot, tables) are instanciated.
     */
    initialize: function(){
	    var context = this;
	    this.history = [];    
	    this.history.push( new HistoryEntry([]) );
	    this.historyIndex = 0;    
		this.gui = new STIGui(this);
		this.gui.initialize();
	},
		
	triggerRefining: function(objects){
		var newDataSets = [];
		var oldDataSets = this.history[this.historyIndex].datasets;
    	for (var i = 0; i < oldDataSets.length; i++) {
			var dataSet = new Dataset();
			dataSet.setID(oldDataSets[i].getID());
			newDataSets.push(dataSet);            			
    	}
    	var valid = false;
    	for(var i = 0; i < objects.length; i++){
    		if( objects[i].value == 1 ){
            	for (var j = 0; j < objects[i].objects.length; j++) {
                	for (var k = 0; k < objects[i].objects[j].length; k++) {
    					newDataSets[j].addObject( objects[i].objects[j][k] );
    					valid = true;
                	}
            	}
    		}
    	}
    	if (valid) {
    		this.addHistoryEntry( new HistoryEntry(newDataSets) );
            this.initElements();
        }
        else {
            alert("Your Selection contains no elements!");
        }
    	return valid;
    },
	    
	/**
 	* adds selected elements of a specific dataset as a new dataset
 	* @param {int} id the id of the dataset
 	*/
	storeSelected: function(id){
		var datasets = this.history[this.historyIndex].datasets;
		if( datasets.length == STIStatic.maxDatasets ){
			alert( "The maximum number of "+ STIStatic.maxDatasets +" parallel datasets is reached!" );
		}
		else {
			var dataSet = datasets[id].copy();
       		var contains = false;
           	for (var j = 0; j < datasets[id].objects.length; j++) {
    	       	if ( datasets[id].objects[j].percentage == 1 ){
					dataSet.addObject( datasets[id].objects[j] );
                   	contains = true;
				}
           	}
       		if (contains) {
				this.addDataSet(dataSet);
			}
        	else {
        		alert("Your Selection contains no elements!");
        	}
      	}
	},

	/**
     * Adds a dataset to the actual history entry
 	*/
	addDataSet: function( dataSet ){
		var oldDataSets = this.history[this.historyIndex].datasets;
		var newDataSets = oldDataSets.concat(dataSet);
		this.addHistoryEntry(new HistoryEntry(newDataSets));
		this.initElements();
	},
        
    /**
     * constructs an url to a dynamic datasource with the specific user input as attribute
     * @param {int} ds the datasource index
     * @param {String} input the user input
     */
    retrieveKml: function(ds,input){
    	var url = this.sources[ds].url;
		url += input;
    	var dataset = STIStatic.loadKML(url);
        dataset.setID(input);
        this.addDataSet(dataset);
    },
    
    loadKml: function(url,term){
    	var dataset = STIStatic.loadKML(url);
    	if ( !dataset ) {
    		console.error('KML is empty or malformed');
    		this.kmlLoaded = false;
    		return;
    	}
        this.kmlLoaded = true;
    	if( term != undefined ){
            dataset.setID(term);
    	}
        this.addDataSet(dataset);
    },
    
    /**
     * called if a highlight has been done in a widget
     * @param objects objects within the highlight
     */
    triggerHighlight: function(objects){
		if( STIStatic.timeplot ){
	        this.timeplot.highlightChanged(objects);
		}
		if( STIStatic.map ){
	        this.map.highlightChanged(objects);
		}
    },
    
    /**
     * called if a selection done in a widget
     * @param objects objects within the selection
     */
    triggerSelection: function(objects){
		if( STIStatic.timeplot ){
	        this.timeplot.selectionChanged(objects);
		}
		if( STIStatic.map ){
	        this.map.selectionChanged(objects);
		}
    },
    
    /**
     * initializes the sti components (map, timeplot, table) depending on the top masks of the data sets.
     * its called after a new search was performed, refining or undo button had been clicked
     */
    initElements: function(){
    	var datasets = this.history[this.historyIndex].datasets;
    	var timeObjects = [];
    	var mapObjects = [];
    	for( var i=0; i<datasets.length; i++ ){
    		mapObjects.push(datasets[i].getMapObjects());
    		timeObjects.push(datasets[i].getTimeObjects());
    	}
    	if( STIStatic.tables ){
			this.tables.init(datasets);
		}
		if( STIStatic.timeplot ){
    		this.timeplot.initTimeplot(timeObjects);
        }
		if( STIStatic.map ){
        	this.map.initMap(mapObjects);
		}
		if( STIStatic.history ){
	        this.gui.updateHistory();
		}
    },
    
    /**
     * deletes a data set with specific index
     * @param {int} index the index of the data set to delete
     */
    deleteDataSet: function(index){
    	var color = colors[index];
        colors.splice(index, 1);
    	var oldDataSets = this.history[this.historyIndex].datasets;
    	var newDataSets = [];
    	for( var i=0; i<oldDataSets.length; i++ ){
    		if( i != index ){
    			newDataSets.push( oldDataSets[i] );
    		}
    	}
		colors.splice( newDataSets.length, 0, color );
		this.addHistoryEntry(new HistoryEntry(newDataSets));
		this.initElements();
   	},

    /**
     * Switches to another history entry with the given index
     * @param {int} index the index of the history entry to load
    */
	switchThroughHistory: function( index ){
		this.historyIndex = index;
		this.initElements();
	},
	
    /**
     * Adds a new history entry containing actual datasets
     * @param {HistoryEntry} historyEntry the history entry to add
    */
	addHistoryEntry: function( historyEntry ){	
		this.history = this.history.slice(0,this.historyIndex+1);
		this.history.push(historyEntry);
		this.historyIndex = this.history.length - 1;
	}
	    
};

/**
 * defines a history entry
 * @param {DataSet[]} datasets the datasets of this history entry
 * 
 * @constructor
 */
function HistoryEntry( datasets ){
	this.datasets = datasets;
};