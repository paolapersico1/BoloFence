var size = 0;
var placement = 'point';

var style_kmeans_bike_clusters = function(feature, resolution){

		var view = map.getView();
		var coords = view.getCenter();
		var resolution2 = view.getResolution();    
		var projection = view.getProjection();
		var resolutionAtCoords = ol.proj.getPointResolution(projection,resolution2, coords);

    // convert cid from int to letters
		var letters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K'];
    var cid = feature.get('cid');
    if (cid === (cid | 0)) {
      feature.set('cid', letters[cid]);
    }
		
		var labelText = feature.get("points");
		var labelFont = "12px, sans-serif";
		var labelFill = "#000000";
		var radius_mt = feature.get("radius");
    var style = [ new ol.style.Style({
        image: new ol.style.Circle({radius: ((radius_mt/resolutionAtCoords) + 5),
						fill: new ol.style.Fill({color: 'rgb(44,164,69,0.4)'})})
				//text: createTextStyle(feature, resolution, labelText, labelFont,
				//											labelFill, 'point', "", 0)
    })];

    return style;
};
