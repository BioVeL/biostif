/**
 * widget to list all available datasources
 * @param container: a DIV id on the html place
 * @param layers: an array of layer record elements having at least name
 *
 *TODO: 
 * - list all kinds of data (basis layers. overlays datasource)
 * - separate the mapWrapper call: use publisher events
 * - put an expander on it
 * - add more information to the layers (e.g. metadata or the hole url
 */

function LayerWidget(container, layers, mapWrapper){

	this.container = container;

	this.header = document.createElement("div");
	this.header.setAttribute('class','header');
	this.header.innerHTML = "Layers";
	this.container.appendChild(this.header);

	this.content = document.createElement("div");
	this.content.setAttribute('class','content');
	this.container.appendChild(this.content);

	var sortlist = $("<ul id='sortlist'/>").appendTo(this.content);
	$(sortlist).appendTo(this.content);
	for( var i in layers ){
		var checkDiv = $("<li class='ui-state-default'/>").appendTo(sortlist);
		$('<span class="ui-icon ui-icon-arrowthick-2-n-s"></span>').appendTo(checkDiv);
		var check = $("<input class='overlayCheckbox' type='checkbox' overlayId='"+i+"'>"+layers[i].name+"</input>").appendTo(checkDiv);
	}
	$(sortlist).sortable();
	$(this.content).css('min-width',$(this.content).width());
	
	var widget = this;
	var buttonDiv = $("<div/>").appendTo(this.content);
	var setButton = $("<input type='submit' value='Set overlays'/>").appendTo(buttonDiv);
	setButton.click(function(){
		var overlays = [];
		$('.overlayCheckbox').each(function(index,cb){
			if( $(cb).attr('checked') ){
				overlays.push(layers[parseInt($(cb).attr('overlayId'))]);
			}
		});		
		mapWrapper.setOverlays(overlays.reverse());
	});

}
