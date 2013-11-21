/**
 * widget to hold all the application controls commons to all GeoTemCo widgets, 
 * it will hold the toolbar
 * @container: div to which the widget will be added * 
 * @control the object holding the control 
 * the layer order in the map can be reconfigured, if the user changes the order via drag and drop
 */

function ToolbarWidget(container, control) { 

	this.container = container;
	
	var widget = this;
	
	
	var toolbarTable = document.createElement("table");	
	toolbarTable.setAttribute('class','absoluteToolbar ddbToolbar');
	this.container.appendChild(toolbarTable);
		
	//for the undo/redo buttons
	var column = document.createElement("tr");		
	toolbarTable.appendChild(column);
	
	var header = document.createElement("td");			
	header.innerHTML = "Toolbar";
	column.appendChild(header);
	
	column = document.createElement("tr");		
	toolbarTable.appendChild(column);
	var content = document.createElement("td");		
	column.appendChild(content);
			
	 
	this.undo = document.createElement('div');
	this.undo.setAttribute('class','historyButton undoDisabled');
	$(this.undo).attr("title", "Undo last action");
	content.appendChild(this.undo);
	this.undo.onclick = function(){
		control.undo();
	};
	
	
	this.redo = document.createElement('div');
	this.redo.setAttribute('class','historyButton redoDisabled');
	$(this.redo).attr("title", "Redo last action");
	content.appendChild(this.redo);
	this.redo.onclick = function(){
		control.redo();

	};
	
	//link/export Button
	this.linkButton = document.createElement('div');
	this.linkButton.setAttribute('class','historyButton linkButton');
	$(this.linkButton).attr("title", "Save link to map");
	content.appendChild(this.linkButton);
		
	//textfield with url:
	var linkdialog_div = "<div id='linkdialog'></div>";
	
	var positiontop = $(this.linkButton).position().top;
	var positionleft = 263; //from main
	
	var $linkdialog = $(linkdialog_div)	
				//.html(linkhtml)
				.dialog({
						autoOpen: false,
						title: 'Link to save the current map',
						position: [positiontop,positionleft]
					});	
		
	this.linkButton.onclick = function(){	
		var html = "<div><p>Exported link to the map, save the following url: <p><textarea name='link' cols='40' rows='2'>" + control.getApplicationLink() + "</textarea></p></div>";		
		$('#linkdialog').html(html);
		$('#linkdialog').dialog('open');
		//exportMap(exportMapControl, "exportedImage"); 

	};
	
	//filterBar
	this.filterBar = new FilterBar(control,this.container);		
	
};

ToolbarWidget.prototype.updateHistoryButtons = function(historyIndex, historyLength, lastAction, nextAction){
	
		if( historyIndex < historyLength - 1 ){
			this.redo.setAttribute('class','historyButton redo');
		}
		else {
			this.redo.setAttribute('class','historyButton redoDisabled');
		}
		$(this.redo).attr("title", "Redo last action: "+ nextAction);
		if( historyIndex > 0 ){
			this.undo.setAttribute('class','historyButton undo');
		}
		else {
			this.undo.setAttribute('class','historyButton undoDisabled');
		}
		$(this.undo).attr("title", "Undo last action: "+ lastAction);
};


