/**
 * Model holding the information of a BoundingBox
 */
var BoundingBox = function() {
	this.xmin;
	this.xmax;
	this.ymin;
	this.ymax;
	this.history = [];

	this.checkParameterValidity = function (par, name, isLongitude) {
		try {  
	        par;        
	  	} catch(e) {  
	       throw new Error ("Error creating or updating BoundingBox: " + name + " is not defined: ");    
	  	}  
	  	if (!isValidNumber (par)) {
	  		throw new Error("Error creating or updating BoundingBox: " + name + " must be a valid number");  
	  	}
	  	if (isLongitude && ((par < -180) || (par > 180))) {
	  		throw new Error("Error creating or updating BoundingBox: " + name + " must be a valid number between -180 and 180");  			
		}
	  	if (!isLongitude && ((par < -90) || (par > 90))) {
	  		throw new Error("Error creating or updating BoundingBox: " + name + " must be a valid number between -90 and 90");  			
		}
	          
	};

	this.checkMinMax = function (min, max, name) {
	  	if (min >= max) {
	  		throw new Error("Error creating or updating BoundingBox: the " + name + "min value must be lower than the " + name + "max value");  
	  	}

	};
	
	this.update = function (xmin, ymin, xmax, ymax) {
		this.checkParameterValidity(xmin, "xmin", true);
		this.checkParameterValidity(xmax, "xmax", true);
		this.checkParameterValidity(ymin, "ymin", false);
		this.checkParameterValidity(ymax, "ymax", false);
		this.checkMinMax(xmin, xmax, "x");
		this.checkMinMax(ymin,ymax, "y");
		
		if (this.xmin && this.xmax && this.ymin && this.ymax) {
			var bbox = {"xmin":this.xmin, "xmax":this.xmax, "ymin": this.ymin, "ymax": this.ymax};
			this.history.push(bbox);
		}
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;	
	};
	

	this.toArray = function() {
		return ([this.xmin, this.ymin, this.xmax, this.ymax]);
	};


};