var TimeWrapper = new function() {

    this.plot;
    this.id = 'plot';
    this.isInitialized = false;

    var wrapper = this;

    Publisher.Subscribe('highlight',function(data,id){
        if( typeof wrapper.plot != 'undefined' && id != wrapper.id ){
             wrapper.highlight(data);
        }
    });

    Publisher.Subscribe('selection',function(data,id){
        if( typeof wrapper.plot != 'undefined' && id != wrapper.id ){
             wrapper.select(data);
        }	
    });

    Publisher.Subscribe('filter',function(data){
	if( typeof wrapper.plot != 'undefined' ){
		wrapper.filter(data);
	}
    });
    
    Publisher.Subscribe('deselect',function(){
    	if( typeof wrapper.plot != 'undefined' ){
    		wrapper.plot.deselection();
    	}
    });

    this.display = function(data) {

    	if ( data instanceof Array ) {

       		this.plot.setStyle('graph');
    		this.plot.initTimeplot(data);
    		gui.updateTimeQuantity(this.plot.count);

        }
    	
    };
   
    this.parseTimeString = function(timeString){
        var time = null;
        if (timeString.match(/^\d{3,4}$/g) ){
            // is a specific year
            time = timeString;
            this.timePoints = true;
        }
        else if ( timeString.match(/^\d{3,4}\s*-\s*\d{3,4}$/g) ){
           // is fromYear-ToYear
           // we use fromYear
           time = timeString.match(/^\d{3,4}/g)[0];
           this.timeIntervals = true;
        }
        else if (timeString.match(/^\d{3,4}.\d{1,2}.\d{1,2}$/g) ){
           // format is (y)yyy.xx.xx
           time = timeString.match(/^\d{3,4}/g)[0];
           this.timePoints = true;
        }
        else{
            // unknown format :-(
        }
        return time;
    }

    this.getTimes = function(timeObjects) {
        var times = [];
        for ( var i = 0; i < timeObjects.length; ++i) {
            if (timeObjects[i].value == 1) {
                // Don't include the right border of selection
                var length = timeObjects[i].objects[0].length;
                var lastIndex = (length > 1)?length-1:length;

                for ( var j = 0; j < lastIndex; ++j) {
                    times.push(timeObjects[i].objects[0][j].index);
                }
            }
        }
        return times;
    };

    this.triggerRefining = function(timeObjects) {
        if (timeObjects && timeObjects.length > 0) {
	    Publisher.Publish('filter',timeObjects);
        }
    };

    this.triggerSelection = function(timeObjects) {
	Publisher.Publish('selection',timeObjects,this.id);
    };

    this.triggerHighlight = function(timeObjects) {
	Publisher.Publish('highlight',timeObjects,this.id);
    };

    this.highlight = function(data) {
        if (data == undefined) {
            return;
        }
        if (data.length > 0) {
            this.plot.highlightChanged(data);
        }
        else {
        	if (typeof(this.plot) != "undefined") {
        		this.plot.highlightChanged([]);
        	}
        }
    };

    this.select = function(data){
        if (data == undefined) {
            return;
        }
        if (data.length > 0) {
        	if (typeof(this.plot) != "undefined") {
        		this.plot.selectionChanged(data);
        	}
        }
        else {
        	if (typeof(this.plot) != "undefined") {
        		this.plot.selectionChanged([]);
        	}
        }
    };

    this.filter = function(data){
        this.display(data);
    };

};
