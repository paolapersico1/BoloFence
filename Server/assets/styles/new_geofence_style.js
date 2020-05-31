// WALK
var style_new_geofence_walk = function(feature, resolution){
  style = [ new ol.style.Style({
    stroke: new ol.style.Stroke({
      color: 'rgba(38,89,128,1.0)',
      lineDash: null, lineCap: 'butt',
      lineJoin: 'miter', width: 3}),
    fill: new ol.style.Fill({
      color: 'rgba(254, 241, 96, 0.2)'}),
    })]
  return style;
}



// BIKE
var style_new_geofence_bike = function(feature, resolution){
  style = [ new ol.style.Style({
    stroke: new ol.style.Stroke({
      color: 'rgba(56,128,54,1.0)',
      lineDash: null, lineCap: 'butt',
      lineJoin: 'miter', width: 3}),
    fill: new ol.style.Fill({
      color: 'rgba(254, 241, 96, 0.2)'}),
    })]
  return style;
}



// CAR
var style_new_geofence_car = function(feature, resolution){
  style = [ new ol.style.Style({
    stroke: new ol.style.Stroke({
      color: 'rgba(128,14,16,1.0)',
      lineDash: null, lineCap: 'butt',
      lineJoin: 'miter', width: 3}),
    fill: new ol.style.Fill({
      color: 'rgba(254, 241, 96, 0.2)'}),
    })]
  return style;
}
