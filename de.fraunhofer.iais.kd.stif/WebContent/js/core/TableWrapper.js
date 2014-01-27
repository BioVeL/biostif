var TableWrapper = new function() {

    this.table;
    this.id = 'table';
    this.isInitialized = false;

    var wrapper = this;

    Publisher.Subscribe('highlight',function(data,id){
        if( typeof wrapper.table != 'undefined' && id != wrapper.id ){
             wrapper.highlight(data);
        }
    });

    Publisher.Subscribe('selection',function(data,id){
        if( typeof wrapper.table != 'undefined' && id != wrapper.id ){
             wrapper.select(data);
        }
    });

    Publisher.Subscribe('filter',function(data){
	if( typeof wrapper.table != 'undefined' ){
		wrapper.filter(data);
	}
    });
    
    Publisher.Subscribe('deselect',function(){
        if( typeof wrapper.table != 'undefined'){
             wrapper.table.deselection();
        }
    });
    
    this.display = function(data,names) {

	if( typeof names != 'undefined' ){
		this.names = names;
	}
    	if ( data instanceof Array ) {
	    	this.table.initTable(data,this.names);
	}

    };

    this.triggerRefining = function(tableObjects) {
        if (tableObjects && tableObjects.length > 0) {
	    Publisher.Publish('filter',tableObjects);
        }
    };

    this.triggerSelection = function(tableObjects) {
	Publisher.Publish('selection',tableObjects,this.id);
    };

    this.triggerHighlight = function(tableObjects) {
	Publisher.Publish('highlight',tableObjects,this.id);
    };

    this.rise = function(id){
	Publisher.Publish('rise',id);
    };

    this.highlight = function(data) {
        if (data == undefined) {
            return;
        }
        if (data.length > 0) {
            this.table.highlightChanged(data);
        }
        else {
            this.table.highlightChanged([]);
        }
    };

    this.select = function(data){
        if (data == undefined) {
            return;
        }
        if (data.length > 0) {
            this.table.selectionChanged(data);
        }
        else {
            this.table.selectionChanged([]);
        }
    };

    this.filter = function(data){
        this.display(data);
    };

};
