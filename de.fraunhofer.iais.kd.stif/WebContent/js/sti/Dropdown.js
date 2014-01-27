/**
 * @class Dropdown
 * Implementation for Dropdown box
 * @author Stefan Jänicke (stjaenicke@informatik.uni-leipzig.de)
 * @version 0.9
 */

function Dropdown(ddParent,listParent,elements,dropperTitle){
	
	var dropdown = this;
	this.visibility = false;
	this.div = document.createElement("div");
	this.div.setAttribute('class','dropdown');

	this.selection = document.createElement("div");
	this.selection.setAttribute('class','dropdownSelection');
	ddParent.appendChild(this.div);

	var leftBorder = document.createElement("div");
	leftBorder.setAttribute('class','dropdownLeft');
	this.div.appendChild(leftBorder);

	this.div.appendChild(this.selection);

	var dropdownButton = document.createElement("div");
	this.div.appendChild(dropdownButton);
	if( elements.length > 1 ){
		dropdownButton.setAttribute('class','dropdownButtonEnabled');
	}
	else {
		dropdownButton.setAttribute('class','dropdownButtonDisabled');
	}
	dropdownButton.onclick = function(){
		if( elements.length > 1 ){
			dropdown.changeVisibility();
		}
	}

	var entryMenu = document.createElement("div");
	entryMenu.setAttribute('class','ddbMapsMenu');
	this.div.appendChild(entryMenu);

	var entries = document.createElement("dl");
	var addEntry = function(e){
		var entry = document.createElement("dt");
		entry.innerHTML = e.name;
		entry.onclick = function(){
			e.onclick();
			dropdown.changeVisibility();
			dropdown.changeEntries(e);
		}
		entries.appendChild(entry);
		e.entry = entry;
	}
	for( var i=0; i<elements.length; i++ ){
		addEntry(elements[i]);
	}
	entryMenu.appendChild(entries);
	this.selection.style.width = entryMenu.offsetWidth+"px";
	entryMenu.style.width = (entryMenu.offsetWidth+leftBorder.offsetWidth+dropdownButton.offsetWidth-2)+"px";

	entryMenu.style.display = 'none';

	this.setEntry = function(index){
		//vhz
		if (typeof(index) == "undefined") {
		  if ((elements) && elements.length > 0) {
			  this.changeEntries(elements[0]);
		  }
		 //original vhz?? diable 
		 //else {
		 //	this.changeEntries(elements[index]);
		 //}
		  	
			this.changeEntries(elements[index]);
		}
	}

	this.changeEntries = function(element){
		if( this.selectedEntry ){
			this.selectedEntry.setAttribute('class','dbbUnselectedMapEntry');
		}
		this.selectedEntry = element.entry;
		this.selectedEntry.setAttribute('class','dbbSelectedMapEntry');
		this.selection.innerHTML = element.name;
	}

	this.changeVisibility = function(){
		this.visibility = !this.visibility;
		if( this.visibility ){
			entryMenu.style.display = "inline-block";
		}
		else {
			entryMenu.style.display = "none";
		}
	}

    this.setLanguage = function(language){
    	if( elements.length > 1 ){
    		dropdownButton.title = STIStatic.getString(language,dropperTitle);
    	}
    	else {
    		dropdownButton.title = STIStatic.getString(language,'singleEntry');
    	}
    }

}
