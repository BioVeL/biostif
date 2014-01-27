if( typeof Publisher == 'undefined' ){

	Publisher = function() {

	    var topics = new Array();
	    var callbackId = 0;

	    this.Get = function(topic){
		var value = this[topic];
		if (!value) {
		    value = this[topic] = [];
		}
		return value;
	    };

	    this.Publish = function(topic, data, id) {
		var subscribers = this.Get(topic);
		for( var i in subscribers ){
		    subscribers[i].callback(data,id);
		}
	    };

	    this.Subscribe = function(topic, callback) {
		var subscribers = this.Get(topic);
		callbackId++;
		subscribers.push({
		    id: callbackId,
		    callback: callback
		});
		return callbackId;
	    };

	    this.Unsubscribe = function(topic, id) {
		var subscribers = this.Get(topic);
		for( var i in subscribers ){
		    if( subscribers[i].id == id ){
		        subscribers.slice(i,1);
		        return;
		    }
		}
	    };

	    return this;

	}();

}
