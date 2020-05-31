var size = 0;
var placement = 'point';

var style_kmeans_car_points = function(feature, resolution){
    var context = {
        feature: feature,
        variables: {}
    };
		var letters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K'];
		var cid = feature.get("cid");
    var value = ""
    var labelText = "";
    var clusteredFeatures = feature.get("features");
    var labelFont = "10px, sans-serif";
    var labelFill = "#000000";
    var bufferColor = "";
    var bufferWidth = 0;
    var textAlign = "center";
    var offsetX = 0;
    var offsetY = 0;
		var placement = 'point';
    if ("" !== null) {
        labelText = String("");
    }

    var style = [ new ol.style.Style({
        image: new ol.style.Circle({radius:6.0 + size,
						stroke: new ol.style.Stroke({color: 'rgba(128,17,25,1.0)', lineDash: null, lineCap: 'butt', lineJoin: 'miter', width: 1}), fill: new ol.style.Fill({color: 'rgba(219,30,42,1.0)'})})
    }),
		new ol.style.Style({
						text: new ol.style.Text({
								text: letters[cid],
								scale: 1,
								fill: new ol.style.Fill({color: 'rgba(255,255,255,1.0)'})})
				})];

    return style;
};
