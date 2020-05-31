var wms_layers = [];
var lyr_CartoPositron_0 = new ol.layer.Tile({
  'title': 'Carto Positron',
  'type': 'base',
  'opacity': 1.000000,        
  source: new ol.source.XYZ({
    url: 'https://cartodb-basemaps-a.global.ssl.fastly.net/light_all/{z}/{x}/{y}.png'
  })
});



// Car Geofence
var max_intensity_car = maxIntensity(json_geofence_car.features);
var format_geofence_car = new ol.format.GeoJSON();
var features_geofence_car = format_geofence_car.readFeatures(json_geofence_car, 
            {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});
var jsonSource_geofence_car = new ol.source.Vector();
jsonSource_geofence_car.addFeatures(features_geofence_car);
var lyr_geofence_car = new ol.layer.Vector({
                source:jsonSource_geofence_car, 
                style: style_geofence_car,
                title: 'Geofence Car'
});



// Car Path
var format_path_car_geom = new ol.format.GeoJSON();
var features_path_car_geom = format_path_car_geom.readFeatures(json_path_car_geom, 
            {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});
var jsonSource_path_car_geom = new ol.source.Vector();
jsonSource_path_car_geom.addFeatures(features_path_car_geom);
var lyr_path_car_geom = new ol.layer.Vector({
                source:jsonSource_path_car_geom, 
                style: style_path_car_geom,
                title: 'Path Car'
});



// Car Arrival
var format_path_car_arrival = new ol.format.GeoJSON();
var features_path_car_arrival = format_path_car_arrival.readFeatures(json_path_car_arrival, 
            {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});
var jsonSource_path_car_arrival = new ol.source.Vector();
jsonSource_path_car_arrival.addFeatures(features_path_car_arrival);
cluster_path_car_arrival = new ol.source.Cluster({
  distance: 5,
  source: jsonSource_path_car_arrival
});
var lyr_path_car_arrival = new ol.layer.Vector({
                source:cluster_path_car_arrival, 
                style: style_path_car_arrival,
                title: 'Arrival Car'
});



// Bike Geofence
var max_intensity_bike = maxIntensity(json_geofence_bike.features);
var format_geofence_bike = new ol.format.GeoJSON();
var features_geofence_bike = format_geofence_bike.readFeatures(json_geofence_bike, 
            {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});
var jsonSource_geofence_bike = new ol.source.Vector();
jsonSource_geofence_bike.addFeatures(features_geofence_bike);
var lyr_geofence_bike = new ol.layer.Vector({
                source:jsonSource_geofence_bike, 
                style: style_geofence_bike,
                title: 'Geofence Bike'
});



// Bike Path
var format_path_bike_geom = new ol.format.GeoJSON();
var features_path_bike_geom = format_path_bike_geom.readFeatures(json_path_bike_geom, 
            {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});
var jsonSource_path_bike_geom = new ol.source.Vector();
jsonSource_path_bike_geom.addFeatures(features_path_bike_geom);
var lyr_path_bike_geom = new ol.layer.Vector({
                source:jsonSource_path_bike_geom, 
                style: style_path_bike_geom,
                title: 'Path Bike'
});



// Bike arrival points
var format_path_bike_arrival = new ol.format.GeoJSON();
var features_path_bike_arrival = format_path_bike_arrival.readFeatures(json_path_bike_arrival, 
            {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});
var jsonSource_path_bike_arrival = new ol.source.Vector();
jsonSource_path_bike_arrival.addFeatures(features_path_bike_arrival);
cluster_path_bike_arrival = new ol.source.Cluster({
  distance: 5,
  source: jsonSource_path_bike_arrival
});
var lyr_path_bike_arrival = new ol.layer.Vector({
                source:cluster_path_bike_arrival, 
                style: style_path_bike_arrival,
                title: 'Arrival Bike'
});



// Walk Geofence
var max_intensity_walk = maxIntensity(json_geofence_walk.features);
var format_geofence_walk = new ol.format.GeoJSON();
var features_geofence_walk = format_geofence_walk.readFeatures(json_geofence_walk, 
            {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});
var jsonSource_geofence_walk = new ol.source.Vector();
jsonSource_geofence_walk.addFeatures(features_geofence_walk);
var lyr_geofence_walk = new ol.layer.Vector({
                source:jsonSource_geofence_walk, 
                style: style_geofence_walk,
                title: 'Geofence Walk'
});



// Walk path
var format_path_walk_geom = new ol.format.GeoJSON();
var features_path_walk_geom = format_path_walk_geom.readFeatures(json_path_walk_geom, 
            {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});
var jsonSource_path_walk_geom = new ol.source.Vector();
jsonSource_path_walk_geom.addFeatures(features_path_walk_geom);
var lyr_path_walk_geom = new ol.layer.Vector({
                source:jsonSource_path_walk_geom, 
                style: style_path_walk_geom,
                title: 'Path Walk'
});



// Walk arrival points
var format_path_walk_arrival = new ol.format.GeoJSON();
var features_path_walk_arrival = format_path_walk_arrival.readFeatures(json_path_walk_arrival, 
            {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});
var jsonSource_path_walk_arrival = new ol.source.Vector();
jsonSource_path_walk_arrival.addFeatures(features_path_walk_arrival);
cluster_path_walk_arrival = new ol.source.Cluster({
  distance: 5,
  source: jsonSource_path_walk_arrival
});
var lyr_path_walk_arrival = new ol.layer.Vector({
                source:cluster_path_walk_arrival, 
                style: style_path_walk_arrival,
                title: 'Arrival Walk'
});



// Visibility
lyr_CartoPositron_0.setVisible(true);
lyr_geofence_car.setVisible(false);
lyr_path_car_geom.setVisible(false);
lyr_path_car_arrival.setVisible(false);
lyr_geofence_bike.setVisible(false);
lyr_path_bike_geom.setVisible(false);
lyr_path_bike_arrival.setVisible(false);
lyr_geofence_walk.setVisible(false);
lyr_path_walk_geom.setVisible(false);
lyr_path_walk_arrival.setVisible(false);

var layersList = [lyr_CartoPositron_0,lyr_geofence_car,lyr_path_car_geom,lyr_path_car_arrival,lyr_geofence_bike,lyr_path_bike_geom,lyr_path_bike_arrival,lyr_geofence_walk,lyr_path_walk_geom,lyr_path_walk_arrival];

// Aliases
lyr_geofence_car.set('fieldAliases', {'id': 'id', 'message': '<i class="fas fa-envelope" style="padding-right: 5px"></i> Message', 'intensity': '<i class="fas fa-map-marker-alt" style="padding-right: 5px"></i>Intensity', });
lyr_path_car_geom.set('fieldAliases', {'id': 'id', 'arrival': 'arrival', });
lyr_path_car_arrival.set('fieldAliases', {'id': 'id', 'geom': 'geom', });
lyr_geofence_bike.set('fieldAliases', {'id': 'id', 'message': '<i class="fas fa-envelope" style="padding-right: 5px"></i> Message', 'intensity': '<i class="fas fa-map-marker-alt" style="padding-right: 5px"></i>Intensity', });lyr_path_bike_geom.set('fieldAliases', {'id': 'id', 'arrival': 'arrival', });
lyr_path_bike_arrival.set('fieldAliases', {'id': 'id', 'geom': 'geom', });
lyr_geofence_walk.set('fieldAliases', {'id': 'id', 'message': '<i class="fas fa-envelope" style="padding-right: 5px"></i> Message', 'intensity': '<i class="fas fa-map-marker-alt" style="padding-right: 5px"></i>Intensity', });lyr_path_bike_geom.set('fieldAliases', {'id': 'id', 'arrival': 'arrival', });
lyr_path_walk_geom.set('fieldAliases', {'id': 'id', 'arrival': 'arrival', });
lyr_path_walk_arrival.set('fieldAliases', {'id': 'id', 'geom': 'geom', });
// Images
lyr_geofence_car.set('fieldImages', {'id': '', 'message': '', 'intensity': '', });
lyr_path_car_geom.set('fieldImages', {'id': 'Hidden', 'arrival': 'Hidden', });
lyr_path_car_arrival.set('fieldImages', {'id': 'Hidden', 'geom': 'Hidden', });
lyr_geofence_bike.set('fieldImages', {'id': '', 'message': '', 'intensity': '', });
lyr_path_bike_geom.set('fieldImages', {'id': 'Hidden', 'arrival': 'Hidden', });
lyr_path_bike_arrival.set('fieldImages', {'id': 'Hidden', 'geom': 'Hidden', });
lyr_geofence_walk.set('fieldImages', {'id': '', 'message': '', 'intensity': '', });
lyr_path_walk_geom.set('fieldImages', {'id': 'Hidden', 'arrival': 'Hidden', });
lyr_path_walk_arrival.set('fieldImages', {'id': 'Hidden', 'geom': 'Hidden', });
// Labels
lyr_geofence_car.set('fieldLabels', {'id': 'no label', 'message': 'header label', 'intensity': 'header label', });
lyr_path_car_geom.set('fieldLabels', {'id': 'no label', 'arrival': 'no label', });
lyr_path_car_arrival.set('fieldLabels', {'id': 'no label', 'geom': 'no label', });
lyr_geofence_bike.set('fieldLabels', {'id': 'no label', 'message': 'header label', 'intensity': 'header label', });
lyr_path_bike_geom.set('fieldLabels', {'id': 'no label', 'arrival': 'no label', });
lyr_path_bike_arrival.set('fieldLabels', {'id': 'header label', 'geom': 'no label', });
lyr_geofence_walk.set('fieldLabels', {'id': 'no label', 'message': 'header label', 'intensity': 'header label', });
lyr_path_walk_geom.set('fieldLabels', {'id': 'no label', 'arrival': 'no label', });
lyr_path_walk_arrival.set('fieldLabels', {'id': 'no label', 'geom': 'no label', });
//lyr_path_walk_arrival.on('precompose', function(evt) {
//    evt.context.globalCompositeOperation = 'normal';
//});
