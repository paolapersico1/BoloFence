/**
 * AJAX Request: 
 * asks a k-means evaluation for a given k and activity of path arrival points.
 * input:
 *   - int k: number of cluster
 *   - str activity: activity type
 * output:
 *   - geojson obj: includes centroid of each cluster and its radius
 *
 * The returned objects are passed to the relative kmeans render function.
 */

// WALK
$(document).ready(function(){
  $('#submit_generate_walk').click(function(event){
    var size = json_path_walk_geom.features.length;
    // loading screen if too many points
    if (size > 49) {
      $(".loader-wrapper").fadeIn("slow");
    }

    var percentile = [0, 0.05, 0.1, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50];
    var i = $('#precision_generate_walk').val();
    var precision = percentile[i];
    var activity = 'walk';
    
    $.ajax({
      url: baseURL + 'index.php/web/new_geofence',
      type: 'post',
      data: {'precision': precision,
            'activity': activity},
      dataType: 'json',
      success: function(result){    
        new_geofence_walk_render(result);
        while (!$('#new_geofence_walk_visibility').is(":checked")){
          $('#new_geofence_walk_visibility').click();
        }
        $(".loader-wrapper").fadeOut("slow");
      },
      error: function(xhr, textStatus, error){
        console.log(xhr.statusText);
        console.log(textStatus);
        console.log(error);
      }
    });
  });
});



// BIKE
$(document).ready(function(){
  $('#submit_generate_bike').click(function(event){
    var size = json_path_bike_geom.features.length;
    // loading screen if too many points
    if (size > 49) {
      $(".loader-wrapper").fadeIn("slow");
    }

    var percentile = [0, 0.05, 0.1, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50];
    var i = $('#precision_generate_bike').val();
    var precision = percentile[i];
    var activity = 'bike';
    
    $.ajax({
      url: baseURL + 'index.php/web/new_geofence',
      type: 'post',
      data: {'precision': precision,
            'activity': activity},
      dataType: 'json',
      success: function(result){    
        new_geofence_bike_render(result);
        while (!$('#new_geofence_bike_visibility').is(":checked")){
          $('#new_geofence_bike_visibility').click();
        }
        $(".loader-wrapper").fadeOut("slow");
      },
      error: function(xhr, textStatus, error){
        console.log(xhr.statusText);
        console.log(textStatus);
        console.log(error);
      }
    });
  });
});



// CAR
$(document).ready(function(){
  $('#submit_generate_car').click(function(event){
    var size = json_path_car_geom.features.length;
    // loading screen if too many points
    if (size > 49) {
      $(".loader-wrapper").fadeIn("slow");
    }

    var percentile = [0, 0.05, 0.1, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50];
    var i = $('#precision_generate_car').val();
    var precision = percentile[i];
    var activity = 'car';
    
    $.ajax({
      url: baseURL + 'index.php/web/new_geofence',
      type: 'post',
      data: {'precision': precision,
            'activity': activity},
      dataType: 'json',
      success: function(result){    
        new_geofence_car_render(result);
        while (!$('#new_geofence_car_visibility').is(":checked")){
          $('#new_geofence_car_visibility').click();
        }
        $(".loader-wrapper").fadeOut("slow");
      },
      error: function(xhr, textStatus, error){
        console.log(xhr.statusText);
        console.log(textStatus);
        console.log(error);
      }
    });
  });
});



/**
 * New geofence layer generator functions
 * input:
 *    - geojson obj of the pseudo-optimal new geofence
 * output:
 *    - polygon layer representing the generated geofence
 */

// WALK
function new_geofence_walk_render(json_new_geofence_walk) { 
  // Remove new walk geofence layer if already exists
  map.getLayers().getArray()
    .filter(layer => layer.get('name') === 'new_geofence_walk')
    .forEach(layer => map.removeLayer(layer));
    
  // Create new geofence layer
  var format_new_geofence_walk = new ol.format.GeoJSON();
  var features_new_geofence_walk = format_new_geofence_walk.readFeatures(json_new_geofence_walk, 
    {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});    
  var jsonSource_new_geofence_walk = new ol.source.Vector();
  jsonSource_new_geofence_walk.addFeatures(features_new_geofence_walk);
  var lyr_new_geofence_walk = new ol.layer.Vector({
    source: jsonSource_new_geofence_walk, 
    style: style_new_geofence_walk,
    title: 'New geofence walk'
  });

  // Visualize new geofence layer
  lyr_new_geofence_walk.set('name', 'new_geofence_walk');
  map.addLayer(lyr_new_geofence_walk);
  lyr_new_geofence_walk.setVisible(true);

  lyr_new_geofence_walk.set('fieldAliases', {'intensity': '<i class="fas fa-map-marker-alt" style="padding-right: 5px"></i>Intensity'});
  lyr_new_geofence_walk.set('fieldImages', {'intensity': '' });
  lyr_new_geofence_walk.set('fieldLabels', {'intensity': 'header label'});
}



// BIKE
function new_geofence_bike_render(json_new_geofence_bike) { 
  // Remove new bike geofence layer if already exists
  map.getLayers().getArray()
    .filter(layer => layer.get('name') === 'new_geofence_bike')
    .forEach(layer => map.removeLayer(layer));
    
  // Create new geofence layer
  var format_new_geofence_bike = new ol.format.GeoJSON();
  var features_new_geofence_bike = format_new_geofence_bike.readFeatures(json_new_geofence_bike, 
    {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});    
  var jsonSource_new_geofence_bike = new ol.source.Vector();
  jsonSource_new_geofence_bike.addFeatures(features_new_geofence_bike);
  var lyr_new_geofence_bike = new ol.layer.Vector({
    source: jsonSource_new_geofence_bike, 
    style: style_new_geofence_bike,
    title: 'New geofence bike'
  });

  // Visualize new geofence layer
  lyr_new_geofence_bike.set('name', 'new_geofence_bike');
  map.addLayer(lyr_new_geofence_bike);
  lyr_new_geofence_bike.setVisible(true);

  lyr_new_geofence_bike.set('fieldAliases', {'intensity': '<i class="fas fa-map-marker-alt" style="padding-right: 5px"></i>Intensity'});
  lyr_new_geofence_bike.set('fieldImages', {'intensity': '' });
  lyr_new_geofence_bike.set('fieldLabels', {'intensity': 'header label'});
}



// CAR
function new_geofence_car_render(json_new_geofence_car) { 
  // Remove new car geofence layer if already exists
  map.getLayers().getArray()
    .filter(layer => layer.get('name') === 'new_geofence_car')
    .forEach(layer => map.removeLayer(layer));
    
  // Create new geofence layer
  var format_new_geofence_car = new ol.format.GeoJSON();
  var features_new_geofence_car = format_new_geofence_car.readFeatures(json_new_geofence_car, 
    {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});    
  var jsonSource_new_geofence_car = new ol.source.Vector();
  jsonSource_new_geofence_car.addFeatures(features_new_geofence_car);
  var lyr_new_geofence_car = new ol.layer.Vector({
    source: jsonSource_new_geofence_car, 
    style: style_new_geofence_car,
    title: 'New geofence car'
  });

  // Visualize new geofence layer
  lyr_new_geofence_car.set('name', 'new_geofence_car');
  map.addLayer(lyr_new_geofence_car);
  lyr_new_geofence_car.setVisible(true);

  lyr_new_geofence_car.set('fieldAliases', {'intensity': '<i class="fas fa-map-marker-alt" style="padding-right: 5px"></i>Intensity'});
  lyr_new_geofence_car.set('fieldImages', {'intensity': '' });
  lyr_new_geofence_car.set('fieldLabels', {'intensity': 'header label'});
}



/**
 * Layer visibility handlers
 * on generated geofence layer checkbox trigger
 * changes the visibility
 */

// WALK
$(document).ready(function(){
  $('#new_geofence_walk_visibility').change(function(event){
    var visible = $(this).is(":checked");
    map.getLayers().forEach(function(lyr) {
      if (lyr.get('name') === 'new_geofence_walk') {
        lyr.setVisible(visible);
      }
    });
  });
});



// BIKE
$(document).ready(function(){
  $('#new_geofence_bike_visibility').change(function(event){
    var visible = $(this).is(":checked");
    map.getLayers().forEach(function(lyr) {
      if (lyr.get('name') === 'new_geofence_bike') {
        lyr.setVisible(visible);
      }
    });
  });
});



// CAR
$(document).ready(function(){
  $('#new_geofence_car_visibility').change(function(event){
    var visible = $(this).is(":checked");
    map.getLayers().forEach(function(lyr) {
      if (lyr.get('name') === 'new_geofence_car') {
        lyr.setVisible(visible);
      }
    });
  });
});
