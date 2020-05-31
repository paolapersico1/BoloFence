// Walk point style
var style_kmeans_walk_points = function(feature, resolution){
		var letters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K'];
    var cid = feature.get('cid');
    var value = ""
    var clusteredFeatures = feature.get("features");

    var style = [ new ol.style.Style({
      image: new ol.style.Circle({radius: 6.0,
        stroke: new ol.style.Stroke({
          color: 'rgba(50,87,128,1.0)',
          lineDash: null, lineCap: 'butt',
          lineJoin: 'miter',
          width: 1
        }),
        fill: new ol.style.Fill({color: 'rgba(72,123,182,1.0)'})
      })
    }),
		new ol.style.Style({
      text: new ol.style.Text({
        text: letters[cid],
        scale: 1,
        fill: new ol.style.Fill({color: 'rgba(255,255,255,1.0)'})})
    })];

    return style;
};



// Bike point style
var style_kmeans_bike_points = function(feature, resolution){
		var letters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K'];
    var cid = feature.get('cid');

    var style = [ new ol.style.Style({
      image: new ol.style.Circle({radius: 6.0,
        stroke: new ol.style.Stroke({
          color: 'rgba(50,87,128,1.0)',
          lineDash: null, lineCap: 'butt',
          lineJoin: 'miter',
          width: 1
        }),
        fill: new ol.style.Fill({color: 'rgba(61,128,53,1.0)'})
      })
    }),
		new ol.style.Style({
      text: new ol.style.Text({
        text: letters[cid],
        scale: 1,
        fill: new ol.style.Fill({color: 'rgba(255,255,255,1.0)'})})
    })];

    return style;
};



// Car point style
var style_kmeans_car_points = function(feature, resolution){
		var letters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K'];
    var cid = feature.get('cid');

    var style = [ new ol.style.Style({
      image: new ol.style.Circle({radius: 6.0,
        stroke: new ol.style.Stroke({
          color: 'rgba(50,87,128,1.0)',
          lineDash: null, lineCap: 'butt',
          lineJoin: 'miter',
          width: 1
        }),
        fill: new ol.style.Fill({color: 'rgba(219,30,42,1.0)'})
      })
    }),
		new ol.style.Style({
      text: new ol.style.Text({
        text: letters[cid],
        scale: 1,
        fill: new ol.style.Fill({color: 'rgba(255,255,255,1.0)'})})
    })];

    return style;
};
