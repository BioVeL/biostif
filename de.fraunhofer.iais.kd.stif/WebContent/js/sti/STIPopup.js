STIPopupSettings = {
		
		itemsPerPage: 5,
		maxHeight: 300,
		maxWidth: 300
	
}

STIPopup = function(parentDiv){

	this.parentDiv = parentDiv;
    this.tWidth = 32;
    this.tHeight = 18;
    
};

STIPopup.prototype = {

	initialize: function(){
		var popup = this;
		this.popupDiv = document.createElement("div");
		this.popupDiv.setAttribute('class','popupDiv');
		this.parentDiv.appendChild(this.popupDiv);
        this.previous = document.createElement("div");
        this.previous.title = "previous";
        this.previous.setAttribute('class','previous');
        this.popupDiv.appendChild(this.previous);
        this.previous.onclick = function(){
        	popup.page--;
        	popup.update();
        }
        this.next = document.createElement("div");
        this.next.title = "next";
        this.next.setAttribute('class','next');
        this.popupDiv.appendChild(this.next);
        this.next.onclick = function(){
        	popup.page++;
        	popup.update();
        }
		this.input = document.createElement("div");
		this.input.style.maxWidth = STIPopupSettings.maxWidth+"px";
		this.input.style.maxHeight = STIPopupSettings.maxHeight+"px";
		this.popupDiv.appendChild(this.input);
		this.triangle = document.createElement("div");
        this.popupDiv.appendChild(this.triangle);
        this.cancel = document.createElement("div");
        this.cancel.title = "Close Popup";
        this.cancel.setAttribute('class','cancelRange');
        this.popupDiv.appendChild(this.cancel);
        this.back = document.createElement("div");
        this.back.title = "Back";
        this.back.setAttribute('class','back');
        this.popupDiv.appendChild(this.back);
        this.back.onclick = function(){
        	if( popup.showElements == 1 ){
    			popup.showElements = STIPopupSettings.itemsPerPage;
    			popup.pages = Math.floor((popup.elements.length+STIPopupSettings.itemsPerPage-1)/popup.showElements);
    			popup.page = Math.floor(popup.page/popup.showElements);
    			popup.update();
        	}
        	else {
        		popup.fillWithLabels();
        	}
        }
        this.zoom = document.createElement("div");
        this.zoom.title = "Zoom into selection";
        this.zoom.setAttribute('class','zoomRange');
        this.popupDiv.appendChild(this.zoom);
        this.resultsLabel = document.createElement("div");
        this.resultsLabel.setAttribute('class','popupResultsLabel');
        this.popupDiv.appendChild(this.resultsLabel);
	},

	update: function(){
		var popup = this;
		if( this.page+1 == this.pages ){
			this.next.style.visibility = "hidden";
		}
		else {
			this.next.style.visibility = "visible";
		}
		if( this.page == 0 ){
			this.previous.style.visibility = "hidden";
		}
		else {
			this.previous.style.visibility = "visible";
		}
		if( this.showElements == 1 ){
			this.input.innerHTML = "";
			var description = document.createElement("div");				
			description.setAttribute('class','popupDescription');
			description.innerHTML = this.elements[this.page].description;
			if( description.innerHTML == "" ){
				description.innerHTML = "<i>no description available</i>"
			}
	        this.input.appendChild(description);
			this.resultsLabel.innerHTML = (this.page+1)+" of "+this.pages+" Results";
		}
		else {
			var list = document.createElement("ol");
			var start = this.page * this.showElements;
			list.start = (start+1)+"";
			var appendClick = function(item,index){
				item.onclick = function(){
					popup.showElements = 1;
					popup.pages = popup.elements.length;
					popup.page = index;
					popup.update();
				}
			}
			for( var i=start; i<start+this.showElements; i++ ){
				if( i == this.elements.length ){
					break;
				}
				var item = document.createElement("li");
				item.innerHTML = this.elements[i].name;
				item.setAttribute('class','popupContent');
				list.appendChild(item);
				appendClick(item,i);
			}
			this.input.innerHTML = "";
			this.input.appendChild(list);
			var start = this.page * this.showElements + 1;
			var end = ((this.page+1)*this.showElements);
			if( end > this.elements.length ){
				end = this.elements.length;
			}
			this.resultsLabel.innerHTML = start+"-"+end+" of "+this.elements.length+" Results";
		}
		this.place();
	},
		
	showLabelContent: function(label){
		this.input.style.overflow = "auto";
		this.back.style.visibility = "visible";
		this.resultsLabel.style.visibility = "visible";
		this.label = label;
		this.elements = label.elements;
		this.page = 0;
		this.showElements = STIPopupSettings.itemsPerPage;
		this.pages = Math.floor((this.elements.length+STIPopupSettings.itemsPerPage-1)/this.showElements);
		this.update();
	},
		
	fillWithLabels: function(){
		this.input.style.overflow = "visible";
		var labelList = document.createElement("ul"); 
		for( i in this.labels ){
			this.labels[i].div.style.position = "relative";
			this.labels[i].div.style.top = "0px";
			var li = document.createElement("li");
			li.appendChild(this.labels[i].div);
			labelList.appendChild(li);
		}
		this.input.innerHTML = "";
		this.input.appendChild(labelList);
		this.hide();
		this.place();
	},
		
	createPopup: function(x,y,labels){
		this.labels = labels;
		this.x = x;
		this.y = y;
		this.fillWithLabels();
	},
		
	hide: function(){
		this.popupDiv.style.visibility = "hidden";
		this.next.style.visibility = "hidden";
		this.previous.style.visibility = "hidden";
		this.back.style.visibility = "hidden";
		this.resultsLabel.style.visibility = "hidden";
	},
		
	reset: function(){
		this.hide();
		this.page = 0;
		this.pages = 0;
	}		
    
};

STIMapPopup = function(map,parentDiv){

	this.base = STIPopup;
	this.base(parentDiv);
	this.initialize();
	
	this.zoom.onclick = function(){
		map.mapRefining();
	}
	
	this.cancel.onclick = function(){
		map.mapDeselection();
	}
    
};

STIMapPopup.prototype = new STIPopup;

STIMapPopup.prototype.shift = function(x,y){
	this.left = this.left-this.x+x;
	this.top = this.top-this.y+y;
	this.x = x;
	this.y = y;
	if( this.left + this.popupDiv.offsetWidth > this.parentDiv.offsetWidth ){
		this.popupDiv.style.left = 'auto';
		this.popupDiv.style.right = (this.parentDiv.offsetWidth-this.left-this.popupDiv.offsetWidth)+"px";
	}
	else {
		this.popupDiv.style.right = 'auto';
		this.popupDiv.style.left = this.left+"px";
	}
	this.popupDiv.style.top = this.top+"px";
};

STIMapPopup.prototype.place = function(){
	this.popupDiv.style.visibility = "visible";
	this.popupDiv.style.left = 'auto';
	this.popupDiv.style.right = 'auto';
	
	var left = this.x;
	var right = this.parentDiv.offsetWidth - left;
	var top = this.y;
	var bottom = this.parentDiv.offsetHeight - top;
	var width = this.popupDiv.offsetWidth;
	var height = this.popupDiv.offsetHeight;

	var vertical = true;
	if( right >= left && right >= top && right >=bottom ){
		this.left = left + this.tHeight;
		this.triangle.setAttribute('class','popupTriangleLeft');
		this.triangle.style.left = (-1*this.tHeight)+"px";
	}
	else if( left >= right && left >= top && left >=bottom ){
		this.left = left - width - this.tHeight + 4;
		this.triangle.setAttribute('class','popupTriangleRight');
		this.triangle.style.left = (width-4)+"px";
	}
	else if( top >= right && top >= left && top >=bottom ){
		this.top = top - height - this.tHeight + 4;
		this.triangle.setAttribute('class','popupTriangleDown');
		this.triangle.style.top = (height-4)+"px";
		vertical = false;
	}
	else {
		this.top = top + this.tHeight;
		this.triangle.setAttribute('class','popupTriangleUp');
		this.triangle.style.top = (-1*this.tHeight)+"px";
		vertical = false;
	}
	if( vertical ){
		if( this.y < height/2 ){
			this.top = 0;
			this.triangle.style.top = (this.y-this.tWidth/2-2)+"px";
		}
		else if( this.parentDiv.offsetHeight < this.y + height/2 ){
			this.top = this.parentDiv.offsetHeight - height;
			this.triangle.style.top = (height-bottom-this.tWidth/2-2)+"px";
		}
		else {
			this.top = top - height/2;
			this.triangle.style.top = (height/2-this.tWidth/2-2)+"px";
		}
	}
	else {
		this.left = left - width/2;
		this.triangle.style.left = (width/2-this.tWidth/2-2)+"px";
	}
	this.popupDiv.style.left = this.left+"px";
	this.popupDiv.style.top = this.top+"px";
	this.resultsLabel.style.left = ((width-this.resultsLabel.offsetWidth)/2)+"px";
};


STITimeplotPopup = function(plot,parentDiv){

	this.base = STIPopup;
	this.base(parentDiv);
	this.initialize();
	
	this.zoom.style.visibility = "hidden";
	
	var popup = this;
	this.cancel.onclick = function(){
		popup.reset();
	}
    
};

STITimeplotPopup.prototype = new STIPopup;

STITimeplotPopup.prototype.place = function(){
	this.popupDiv.style.visibility = "visible";
	this.popupDiv.style.left = 'auto';
	this.popupDiv.style.right = 'auto';
	
	var left = this.x;
	var right = this.parentDiv.offsetWidth - left;
	var top = this.parentDiv.offsetTop;
	var bottom = this.parentDiv.offsetHeight - top;
	var width = this.popupDiv.offsetWidth;
	var height = this.popupDiv.offsetHeight;

	this.top = top - height + 4;
	this.triangle.setAttribute('class','popupTriangleDown');
	this.triangle.style.top = (height-4)+"px";

	if( left - width/2 < 0 ){
		this.left = 0;
		this.triangle.style.left = (left-this.tWidth/2-2)+"px";
	}
	else if( left + width/2 > this.parentDiv.offsetWidth ){
		this.left = this.parentDiv.offsetWidth - width;
		this.triangle.style.left = (width-right-this.tWidth/2-2)+"px";
	}
	else {
		this.left = left - width/2;
		this.triangle.style.left = (width/2-this.tWidth/2-2)+"px";
	}				
	
	this.popupDiv.style.left = this.left+"px";
	this.popupDiv.style.top = (-1*height)+"px";
	this.resultsLabel.style.left = ((width-this.resultsLabel.offsetWidth)/2)+"px";
};