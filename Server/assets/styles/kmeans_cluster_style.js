// Walk cluster style
var style_kmeans_walk_clusters = function(feature, resolution){
  var size = feature.get('points');
  // convert cid from int to letters
  var letters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K'];
  var cid = feature.get('cid');
  if (cid === (cid | 0)) {
    feature.set('cid', letters[cid]);
  }
  var opacity = 0.4;
  if (size == 1) {
    opacity = 0;
  }

  style = [ new ol.style.Style({
    fill: new ol.style.Fill({color: 'rgba(72,123,182,'+opacity+')'})
  })]

  return style;
}



// Bike cluster style
var style_kmeans_bike_clusters = function(feature, resolution){
  var size = feature.get('points');
  // convert cid from int to letters
  var letters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K'];
  var cid = feature.get('cid');
  if (cid === (cid | 0)) {
    feature.set('cid', letters[cid]);
  }
  var opacity = 0.4;
  if (size == 1) {
    opacity = 0;
  }

  style = [ new ol.style.Style({
    fill: new ol.style.Fill({color: 'rgba(44,164,69,'+opacity+')'})
  })]

  return style;
}



// Car cluster style
var style_kmeans_car_clusters = function(feature, resolution){
  var size = feature.get('points');
  // convert cid from int to letters
  var letters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K'];
  var cid = feature.get('cid');
  if (cid === (cid | 0)) {
    feature.set('cid', letters[cid]);
  }
  var opacity = 0.4;
  if (size == 1) {
    opacity = 0;
  }

  style = [ new ol.style.Style({
    fill: new ol.style.Fill({color: 'rgba(219,52,68,'+opacity+')'})
  })]
  
  return style;
}
