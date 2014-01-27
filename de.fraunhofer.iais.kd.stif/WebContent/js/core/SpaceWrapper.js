/**
   data should not be retrieved through the space wrapper
   perform the data processing step in the top-level environment
   then, f.i., the table or timeplot widget can be filled with the same objects
*/
var SpaceWrapper = new function() {

    this.id = 'map';

    var wrapper = this;

    Publisher.Subscribe('highlight',function(data,id){
        if( typeof wrapper.map != 'undefined' && id != wrapper.id ){
             wrapper.highlight(data);
        }
    });

    Publisher.Subscribe('selection',function(data,id){
        if( typeof wrapper.map != 'undefined' && id != wrapper.id ){
             wrapper.select(data);
        }	
    });

    Publisher.Subscribe('filter',function(data){
//    	logg("SW subscribe filter");
	if( typeof wrapper.map != 'undefined' ){
		wrapper.filter(data);
	}
    });

    Publisher.Subscribe('rise',function(id){
        if( typeof wrapper.map != 'undefined' ){
             wrapper.rise(id);
        }
    });
    
    Publisher.Subscribe('deselect',function(){
        if( typeof wrapper.map != 'undefined'){
             wrapper.map.deselection();
        }
    });

    this.display = function(data,zoom) {
    	
    	if ( data instanceof Array ) {
    		
    		logg("SW display: " + data[0].length);
    		
	    	this.map.initMap(data,zoom);
//	    	this.filterBar.setData(data);
	    	Publisher.Publish('filterbardata',data);
//	    	logg("this.map.count: " + this.map.count);
	    	gui.updateSpaceQuantity(this.map.count);
    	}

    };

    this.triggerRefining = function(mapObjects) {
//    	logg("SW space");
        if (mapObjects && mapObjects.length > 0) {
	    Publisher.Publish('filter',mapObjects);
        }
    };

    this.triggerSelection = function(mapObjects) {
	Publisher.Publish('selection',mapObjects,this.id);
    };

    this.triggerHighlight = function(mapObjects) {
	Publisher.Publish('highlight',mapObjects,this.id);
    };

    this.highlight = function(data) {
        if (data == undefined) {
            return;
        }
        if (data.length > 0) {
            this.map.highlightChanged(data);
        }
        else {
            this.map.highlightChanged([]);
        }
    };

    this.select = function(data){
//    	logg("select data in mapp");
        if (data == undefined) {
            return;
        }
        if (data.length > 0) {
            this.map.selectionChanged(data);
        }
        else {
            this.map.selectionChanged([]);
        }
    };

    this.filter = function(data){
//    	logg("SW function filter data");
        this.display(data);
    };

    this.rise = function(id){
        this.map.riseLayer(id);
    };
    
    //added by vhz
    /**
     * Add wms overlays to the map
	 * @param layers: Array of JSON object describing the layers, 
	 * an empty object will remove all the layers
	 * example of datastructure:
	 *
	 *	var layers = [{
	 *					name: "Neighborhoods",
	 *					url: "http://demo.cubewerx.com/demo/cubeserv/cubeserv.cgi?",
	 *					layer: "Foundation.GTOPO30"
	 *				},{
	 *					name: "Nexrad",
	 *					url: "http://mesonet.agron.iastate.edu/cgi-bin/wms/nexrad/n0r-t.cgi?",
	 *					layer: "nexrad-n0r-wmst"
	 *		}];
     */
    this.loadOverlays = function(layers) {
    	this.map.setOverlays(layers);
    	gui.setOverlayDropdown();
    };
    /**
     * adds a boundingbox overlay to the map and a control for modifying it. 
     * If the array is empty than the control is removed
     */
    this.showBboxControl = function (dataSourceId, extentArray) {
    	if (extentArray) {    		
    		gui.showBbox(true);
    		this.map.showBBox(true, dataSourceId, extentArray);
    	} else {    		
    		gui.showBbox(false);
    		this.map.deleteBBox();
    	}
    }; 
    
     
    //~vhz

};
