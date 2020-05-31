var size = 0;
var placement = 'point';

var style_kmeans_walk = function(feature, resolution){
    var context = {
        feature: feature,
        variables: {}
    };
    var labelText = "";
    size = 0;
    var labelFont = "10px, sans-serif";
    var labelFill = "#000000";
    var bufferColor = "";
    var bufferWidth = 0;
    var textAlign = "left";
    var offsetX = 8;
    var offsetY = 3;
    var placement = 'point';
    if ("" !== null) {
        labelText = String("");
    }
    style = [ new ol.style.Style({
			stroke: new ol.style.Stroke({color: 'rgba(62,142,196, 0.0)', lineDash: null, lineCap: 'butt', lineJoin: 'round', width: 10}),fill: new ol.style.Fill({color: 'rgba(62,142,196, 0.4)'}),
        text: createTextStyle(feature, resolution, labelText, labelFont,
                              labelFill, placement, bufferColor,
                              bufferWidth)
				})]

    return style;
};
