function ZoomSlider(id,parent,orientation){

	this.div;
	this.slider;
	this.parent = parent;
	this.initialize(id,orientation);
    
};

ZoomSlider.prototype = {

    /**
     * initializes the GUI of the Spatio Temporal Interface
     */
    initialize: function(id,orientation){
    	var zs = this;
    	this.div = document.createElement("div");
    	this.div.setAttribute('class','sliderStyle');
    	this.imagepath = STIStatic.urlPrefix+'images/';
    	
    	var sliderContainer = document.createElement("div");
    	sliderContainer.setAttribute('class','zoomSliderContainer');
		var sliderDiv = document.createElement("div");
		sliderDiv.id = id;
		sliderDiv.tabIndex = 1;
		var sliderInputDiv = document.createElement("div");
		sliderInputDiv.id = id+"-input";
		sliderDiv.appendChild(sliderInputDiv);		
		sliderContainer.appendChild(sliderDiv);		
		this.slider = new Slider( sliderDiv, sliderInputDiv, orientation );
		this.div.appendChild(sliderContainer);
		
		var zoomIn = document.createElement("img");
		zoomIn.src = this.imagepath+"zoom-in.png";
		zoomIn.title = "Zoom in";
		zoomIn.setAttribute('class','zoomSliderIn');
		zoomIn.onclick = function(){
	        zs.parent.zoom(1);
		}
		this.div.appendChild(zoomIn);
		
		var zoomOut = document.createElement("img");
		zoomOut.src = this.imagepath+"zoom-out.png";
		zoomOut.title = "Zoom out";
		zoomOut.setAttribute('class','zoomSliderOut');
		zoomOut.onclick = function(){
	        zs.parent.zoom(-1);
		}
		this.div.appendChild(zoomOut);
		
		this.slider.handle.onmousedown = function(){
			var oldValue = zs.slider.getValue();
			document.onmouseup = function(){
				if( !zs.parent.zoom( (zs.slider.getValue()-oldValue) / zs.max*zs.levels ) ){
					zs.setValue(oldValue);
				}
				document.onmouseup = null;
			}
		}
		
    },
    
    setValue: function(value){
    	this.slider.setValue(value/this.levels*this.max);
    },
    
    setMaxAndLevels: function(max,levels){
    	this.max = max;
    	this.levels = levels;
    	this.slider.setMaximum(max);
    }

}