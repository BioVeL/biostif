/**
 * widget to list all available datasources
 */
function DatasourceWidget(container,core){

	this.container = container;

	this.header = document.createElement("div");
	this.header.setAttribute('class','header');
	this.header.innerHTML = "Facets";
	this.container.appendChild(this.header);

	this.content = document.createElement("div");
	this.content.setAttribute('class','content');
	this.container.appendChild(this.content);

	this.checkboxes = [];
	for( var i in RvProps.sources ){
		var checkDiv = $("<div/>").appendTo(this.content);
		var check = $("<input type='checkbox'>"+RvProps.sources[i].label+"</input>").appendTo(checkDiv);
		this.checkboxes.push(check);
	}
	
	var widget = this;
	var buttonDiv = $("<div/>").appendTo(this.content);
	var setButton = $("<input type='submit' value='Set facets'/>").appendTo(buttonDiv);
	setButton.click(function(){
		var data = [];
		var labels = [];
		for( var i in widget.checkboxes ){
			if( widget.checkboxes[i].attr('checked') ){
				$.ajax({
					url: RvProps.sources[i].url,
					async: false,
					dataType: 'json',
					success: function(json){
						data.push(STIStatic.loadSpatioTemporalJSONData(json));
					}
				});
				labels.push(RvProps.sources[i].label);
			}
		}
		core.display(data,labels);
	});

}
