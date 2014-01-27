var arrayIndex = function( array, obj ){
	if( Array.indexOf ){
		return array.indexOf(obj);
	}	
	for (var i = 0; i < array.length; i++ ){
      	if (array[i] == obj) {
			return i;
		}
     	}
     	return -1;
}

STILoader = {
		
	resolveUrlPrefix: function(file){
		var sources = document.getElementsByTagName("script");
		for( var i=0; i<sources.length; i++ ){
			var index = sources[i].src.indexOf(file);
			if( index != -1 ){
				return sources[i].src.substring(0,index);
			}
		}
	},
	
	load: function(){
		STILoader.startTime = new Date();
		STILoader.urlPrefix = STILoader.resolveUrlPrefix('js/build/loader/STILoader.js');
		(new DynaJsLoader()).loadScripts([{ url: STILoader.urlPrefix + 'js/build/loader/TimeplotLoader.js'}],STILoader.loadTimeplot);
	},
	
	loadTimeplot: function(){
		TimeplotLoader.load( STILoader.urlPrefix+'lib/', STILoader.loadScripts );
	},
	
	loadScripts : function() {

		SimileAjax.includeCssFile(document,STILoader.urlPrefix+'css/Sti.css');
		SimileAjax.includeCssFile(document,STILoader.urlPrefix+'css/customDesign.css');
		SimileAjax.includeCssFile(document,STILoader.urlPrefix+'lib/openlayers/theme/default/style.css');

		(new DynaJsLoader()).loadScripts([{ url: STILoader.urlPrefix+'lib/slider/js/range.js' }]);
		(new DynaJsLoader()).loadScripts([{ url: STILoader.urlPrefix+'lib/slider/js/slider.js' }]);
		(new DynaJsLoader()).loadScripts([{ url: STILoader.urlPrefix+'lib/slider/js/timer.js' }]);
		(new DynaJsLoader()).loadScripts([{ url: STILoader.urlPrefix+'js/sti/' + 'timeplot-modify.js' }]);
		(new DynaJsLoader()).loadScripts([{ url: STILoader.urlPrefix+'js/sti/' + 'ExtendedSimileTimeDate.js' }]);
		
		var olFiles = [
                       { 
                    	   url: STILoader.urlPrefix+'lib/' + 'openlayers/' + 'OpenLayers.js'
                       },
                       { 
                    	   url: STILoader.urlPrefix+'js/sti/' + 'ModifiedZoomPanel.js'
                       }//
	        ];
		(new DynaJsLoader()).loadScripts(olFiles);

		var stiFiles = [
		                { 
	                       url: STILoader.urlPrefix+'js/sti/' + 'STIStatic.js',
	                       test: "STIStatic.getAbsoluteTop"
		                },
		                { 
		                   url: STILoader.urlPrefix+'js/sti/' + 'MapControl.js'
		                },//
		                { 
		                   url: STILoader.urlPrefix+'js/sti/' + 'FilterBar.js'
		                },
		                { 
		                   url: STILoader.urlPrefix+'js/sti/' + 'PlacenameTags.js'
		                },
		                { 
		                   url: STILoader.urlPrefix+'js/sti/' + 'STIMap.js'
		                },
		                { 
		                   url: STILoader.urlPrefix+'js/sti/' + 'STITimeplot.js'
		                },
		                { 
		                   url: STILoader.urlPrefix+'js/sti/' + 'TableWidget.js'
		                },
		                { 
		                   url: STILoader.urlPrefix+'js/sti/' + 'DataObject.js'
		                },
		                { 
		                   url: STILoader.urlPrefix+'js/sti/' + 'DataSet.js'
		                },
		                { 
		                   url: STILoader.urlPrefix+'js/sti/' + 'TimeDataSource.js'
		                },
		                { 
		                   url: STILoader.urlPrefix+'js/sti/' + 'Binning.js'
		                },
		                { 
		                   url: STILoader.urlPrefix+'js/sti/' + 'MapDataSource.js'
		                },
		                { 
		                   url: STILoader.urlPrefix+'js/sti/' + 'Clustering.js'
		                },
		                { 
		                   url: STILoader.urlPrefix+'js/sti/' + 'kruskal.js'
		                },
		                { 
		                   url: STILoader.urlPrefix+'js/sti/' + 'FullscreenWindow.js'
		                },
		                { 
		                   url: STILoader.urlPrefix+'js/sti/' + 'Dropdown.js'
		                },
		                { 
		                   url: STILoader.urlPrefix+'js/sti/' + 'MapZoomSlider.js'
		                },
		                { 
		                   url: STILoader.urlPrefix+'js/sti/' + 'Popup.js'
		                },
		                {
		                   url: STILoader.urlPrefix+'js/core/Publisher.js'
		                },
		                {
		                   url: STILoader.urlPrefix+'js/core/SpaceWrapper.js'
		                },
		                {
		                   url: STILoader.urlPrefix+'js/core/TimeWrapper.js'
		                },
		                {
		                   url: STILoader.urlPrefix+'js/core/TableWrapper.js'
		                },
		                {
		                   url: STILoader.urlPrefix+'js/core/Gui.js'
		                }
		];
		(new DynaJsLoader()).loadScripts(stiFiles,STILoader.initSTI);
        
    },

    initSTI : function() {

       	STIStatic.configure(STILoader.urlPrefix);
	Publisher.Publish('StifReady','yeah');

    }   
}

STILoader.load();
