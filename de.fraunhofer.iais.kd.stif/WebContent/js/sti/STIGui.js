function STIGui(core){

	this.core = core;
    
};

STIGui.prototype = {

    /**
     * initializes the GUI of the Spatio Temporal Interface
     */
    initialize: function(){    	
        
		this.container = document.getElementById("e4DContainer");

		var toolbar = document.createElement("div");
		toolbar.style.position = "absolute";
		toolbar.style.top = "0px";
		this.container.appendChild(toolbar);

    	var orientation = "Right";
		if( STIStatic.toolbarLeft ){
			toolbar.style.left = "0px";
			orientation = "Left";
		}
		else {
			toolbar.style.left = (this.container.offsetWidth-STIStatic.toolbarWidth)+"px";
		}
    	
		var timeplotHeight = STIStatic.timeplotHeight + 12;
    	var width = this.container.offsetWidth;
		var height = width * 3 / 4;

		if( STIStatic.map ){
			var mapWindow = document.createElement("div");
			mapWindow.id = "map";
			mapWindow.style.position = "absolute";
			mapWindow.style.width = width+"px";
			mapWindow.style.overflow = "hidden";
			mapWindow.style.height = (height-timeplotHeight-1)+"px";
			mapWindow.style.top = "0px";
			mapWindow.style.left = "0px";
			if( STIStatic.timeplot ){
				if( STIStatic.lightScheme ){
					mapWindow.style.borderBottom = "1px solid white";
				}
				else {
					mapWindow.style.borderBottom = "1px solid #444444";
				}
			}
			else {
				mapWindow.style.height = height+"px";
			}
			this.container.appendChild(mapWindow);
//console.info("gui");
			this.core.map = new STIMap(this.core,"map",{h:orientation,v:"Top",s:"Light"});
		}
		
		if( STIStatic.timeplot ){
			var plotDiv = document.createElement("div");
			plotDiv.id = "plot";
			plotDiv.style.position = "absolute";
			plotDiv.style.width = width+"px";
			plotDiv.style.top = (height-timeplotHeight)+"px";
			plotDiv.style.left = "0px";
			this.container.appendChild(plotDiv);
			this.core.timeplot = new STITimeplot(this.core,"plot",{h:orientation,v:"Bottom",s:"Light"});
			if( !STIStatic.map ){
				this.container.style.height = timeplotHeight+"px";
				plotDiv.style.top = "0px";
				if( !STIStatic.popups && timeplotHeight < 200 ){
					this.container.style.top = (200-timeplotHeight)+"px";
				}
				else if( STIStatic.popups ){
					this.container.style.top = "240px";
				}
			}
		}

		toolbar.style.height = (this.container.offsetHeight+1)+"px";
		toolbar.style.width = (STIStatic.toolbarWidth-1)+"px";
		toolbar.setAttribute('class','toolbar'+orientation+''+'Light');
		
    }
       
}
