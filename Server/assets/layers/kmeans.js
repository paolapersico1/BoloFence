/**
 * AJAX Request: 
 * asks a k-means evaluation for a given k and activity of path arrival points.
 * input:
 *   - int k: number of cluster
 *   - str activity: activity type
 * output:
 *   - geojson obj clusters polygon
 *   - geojson obj of clusters points
 *
 * The returned objects are passed to the relative kmeans render function.
 */

// WALK
$(document).ready(function(){
  $('#submit_kmeans_walk').click(function(event){
    var size = json_path_walk_geom.features.length;
    // loading screen if too many points
    if (size > 99) {
      $(".loader-wrapper").fadeIn("slow");
    }
    var k = $('#clusters_num_walk').val();
    var activity = 'walk';
    
    // check if k > arrival points
    if (k > size){
      console.log("[ERROR] k cannot be bigger tha arrival points");
      return false;
    }
    
    $.ajax({
      url: baseURL + 'index.php/web/kmeans',
      type: 'post',
      data: {'k': k,
            'activity': activity},
      dataType: 'json',
      success: function(result){    
        kmeans_walk_render(result.km_walk_clusters, result.km_walk_points);
        while (!$('#kmeans_walk_visibility').is(":checked")){
          $('#kmeans_walk_visibility').click();
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
  $('#submit_kmeans_bike').click(function(event){
    var size = json_path_bike_geom.features.length;
    // loading screen if too many points
    if (size > 99) {
      $(".loader-wrapper").fadeIn("slow");
    }
    var k = $('#clusters_num_bike').val();
    var activity = 'bike';
    
    // check if k > arrival points
    if (k > size){
      console.log("[ERROR] k cannot be bigger tha arrival points");
      return false;
    }

    $.ajax({
      url: baseURL + 'index.php/web/kmeans',
      type: 'post',
      data: {'k': k,
            'activity': activity},
      dataType: 'json',
      success: function(result){
        kmeans_bike_render(result.km_bike_clusters, result.km_bike_points);
        // init toggle
        while (!$('#kmeans_bike_visibility').is(":checked")){
          $('#kmeans_bike_visibility').click();
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
  $('#submit_kmeans_car').click(function(event){
    var size = json_path_car_geom.features.length;
    // loading screen if too many points
    if (size > 99) {
      $(".loader-wrapper").fadeIn("slow");
    }
    var k = $('#clusters_num_car').val();
    var activity = 'car';
    var size = json_path_walk_geom.features.length;
    
    // check if k > arrival points
    if (k > size){
      console.log("[ERROR] k cannot be bigger tha arrival points");
      return false;
    }

    $.ajax({
      url: baseURL + 'index.php/web/kmeans',
      type: 'post',
      data: {'k': k,
            'activity': activity},
      dataType: 'json',
      success: function(result){
        kmeans_car_render(result.km_car_clusters, result.km_car_points);
        // init checkbox toggle
        while (!$('#kmeans_car_visibility').is(":checked")){
          $('#kmeans_car_visibility').click();
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
 * K-means layers generator functions
 * input:
 *    - geojson obj of clusters area polygon
 *    - geojson obj of points per cluster
 * output:
 *    - polygon layer covering the cluster area
 *    - distinct arrival points for each cluster 
 */

// WALK
function kmeans_walk_render(json_kmeans_walk_clusters, json_kmeans_walk_points) { 
  // Remove CLUSTERS kmeans layer if already exists
  map.getLayers().getArray()
    .filter(layer => layer.get('name') === 'kmeans_walk_clusters')
    .forEach(layer => map.removeLayer(layer));
  // Remove POINTS kmeans layer if already exists
  map.getLayers().getArray()
    .filter(layer => layer.get('name') === 'kmeans_walk_points')
    .forEach(layer => map.removeLayer(layer));
    
  // Create CLUSTERS layer
  var format_kmeans_walk_clusters = new ol.format.GeoJSON();
  var features_kmeans_walk_clusters = format_kmeans_walk_clusters.readFeatures(json_kmeans_walk_clusters, 
    {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});    
  var jsonSource_kmeans_walk_clusters = new ol.source.Vector();
  jsonSource_kmeans_walk_clusters.addFeatures(features_kmeans_walk_clusters);
  var lyr_kmeans_walk_clusters = new ol.layer.Vector({
    source: jsonSource_kmeans_walk_clusters, 
    style: style_kmeans_walk_clusters,
    title: 'K-means walk clusters'
  });
  // Create POINTS layer
  var format_kmeans_walk_points = new ol.format.GeoJSON();
  var features_kmeans_walk_points = format_kmeans_walk_points.readFeatures(json_kmeans_walk_points, 
    {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});
  var jsonSource_kmeans_walk_points = new ol.source.Vector();
  jsonSource_kmeans_walk_points.addFeatures(features_kmeans_walk_points);
  var lyr_kmeans_walk_points = new ol.layer.Vector({
    source: jsonSource_kmeans_walk_points, 
    style: style_kmeans_walk_points,
    title: 'K-means walk points'
  });

  // Visualize CLUSTERS layer
  lyr_kmeans_walk_clusters.set('name', 'kmeans_walk_clusters');
  map.addLayer(lyr_kmeans_walk_clusters);
  // Visualize POINTS layer
  lyr_kmeans_walk_points.set('name', 'kmeans_walk_points');
  map.addLayer(lyr_kmeans_walk_points);

  // Popup CLUSTERS settings
  lyr_kmeans_walk_clusters.set('fieldAliases', {'cid': '<i class="fas fa-tags" style="padding-right:5px"></i> ID',
    'radius': '<i class="fas fa-ruler" style="padding-right:5px"></i> Radius [meters]',
    'points': '<i class="fas fa-map-marker-alt" style="padding-right:5px"></i> Points number'});
  lyr_kmeans_walk_clusters.set('fieldImages', {'cid': 'Hidden', 'radius': 'Hidden', 'points': ''});
  lyr_kmeans_walk_clusters.set('fieldLabels', {'cid': 'header label', 'radius': 'header label', 'points': 'header label'});
}



// BIKE
function kmeans_bike_render(json_kmeans_bike_clusters, json_kmeans_bike_points) {  
  // Remove CLUSTERS kmeans layer if already exists
  map.getLayers().getArray()
    .filter(layer => layer.get('name') === 'kmeans_bike_clusters')
    .forEach(layer => map.removeLayer(layer));
  // Remove POINTS kmeans layer if already exists
  map.getLayers().getArray()
    .filter(layer => layer.get('name') === 'kmeans_bike_points')
    .forEach(layer => map.removeLayer(layer));
  
  // Create CLUSTERS layer
  var format_kmeans_bike_clusters = new ol.format.GeoJSON();
  var features_kmeans_bike_clusters = format_kmeans_bike_clusters.readFeatures(json_kmeans_bike_clusters, 
    {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});
  var jsonSource_kmeans_bike_clusters = new ol.source.Vector();
  jsonSource_kmeans_bike_clusters.addFeatures(features_kmeans_bike_clusters);
  var lyr_kmeans_bike_clusters = new ol.layer.Vector({
    source: jsonSource_kmeans_bike_clusters, 
    style: style_kmeans_bike_clusters,
    title: 'K-means bike clusters'
  });
  // Create POINTS layer
  var format_kmeans_bike_points = new ol.format.GeoJSON();
  var features_kmeans_bike_points = format_kmeans_bike_points.readFeatures(json_kmeans_bike_points, 
    {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});
  var jsonSource_kmeans_bike_points = new ol.source.Vector();
  jsonSource_kmeans_bike_points.addFeatures(features_kmeans_bike_points);
  var lyr_kmeans_bike_points = new ol.layer.Vector({
    source: jsonSource_kmeans_bike_points, 
    style: style_kmeans_bike_points,
    title: 'K-means bike points'
  });

  // Visualize CLUSTERS layer
  lyr_kmeans_bike_clusters.set('name', 'kmeans_bike_clusters');
  map.addLayer(lyr_kmeans_bike_clusters);
  // Visualize POINTS layer
  lyr_kmeans_bike_points.set('name', 'kmeans_bike_points');
  map.addLayer(lyr_kmeans_bike_points);

  // Popup CLUSTERS settings
  lyr_kmeans_bike_clusters.set('fieldAliases', {'cid': '<i class="fas fa-tags" style="padding-right:5px"></i> ID',
    'radius': '<i class="fas fa-ruler" style="padding-right:5px"></i> Radius [meters]',
    'points': '<i class="fas fa-map-marker-alt" style="padding-right:5px"></i> Points number'});
  lyr_kmeans_bike_clusters.set('fieldImages', {'cid': 'Hidden', 'radius': 'Hidden', 'points': ''});
  lyr_kmeans_bike_clusters.set('fieldLabels', {'cid': 'header label', 'radius': 'header label', 'points': 'header label'});
}



// CAR
function kmeans_car_render(json_kmeans_car_clusters, json_kmeans_car_points) {    
  // Remove CLUSTERS kmeans layer if already exists
  map.getLayers().getArray()
    .filter(layer => layer.get('name') === 'kmeans_car_clusters')
    .forEach(layer => map.removeLayer(layer));
  // Remove POINTS kmeans layer if already exists
  map.getLayers().getArray()
    .filter(layer => layer.get('name') === 'kmeans_car_points')
    .forEach(layer => map.removeLayer(layer));
  
  // Create CLUSTERS layer
  var format_kmeans_car_clusters = new ol.format.GeoJSON();
  var features_kmeans_car_clusters = format_kmeans_car_clusters.readFeatures(json_kmeans_car_clusters, 
    {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});
  var jsonSource_kmeans_car_clusters = new ol.source.Vector();
  jsonSource_kmeans_car_clusters.addFeatures(features_kmeans_car_clusters);
  var lyr_kmeans_car_clusters = new ol.layer.Vector({
    source: jsonSource_kmeans_car_clusters, 
    style: style_kmeans_car_clusters,
    title: 'K-means car clusters'
  });
  
  // Create POINTS layer
  var format_kmeans_car_points = new ol.format.GeoJSON();
  var features_kmeans_car_points = format_kmeans_car_points.readFeatures(json_kmeans_car_points, 
    {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});
  var jsonSource_kmeans_car_points = new ol.source.Vector();
  jsonSource_kmeans_car_points.addFeatures(features_kmeans_car_points);
  var lyr_kmeans_car_points = new ol.layer.Vector({
    source: jsonSource_kmeans_car_points, 
    style: style_kmeans_car_points,
    title: 'K-means car points'
  });

  // Visualize CLUSTERS layer
  lyr_kmeans_car_clusters.set('name', 'kmeans_car_clusters');
  map.addLayer(lyr_kmeans_car_clusters);
  // Visualize POINTS layer
  lyr_kmeans_car_points.set('name', 'kmeans_car_points');
  map.addLayer(lyr_kmeans_car_points);

  // Popup CLUSTERS settings
  lyr_kmeans_car_clusters.set('fieldAliases', {'cid': '<i class="fas fa-tags" style="padding-right:5px"></i> ID',
    'radius': '<i class="fas fa-ruler" style="padding-right:5px"></i> Radius [meters]',
    'points': '<i class="fas fa-map-marker-alt" style="padding-right:5px"></i> Points number'});
  lyr_kmeans_car_clusters.set('fieldImages', {'cid': 'Hidden', 'radius': 'Hidden', 'points': ''});
  lyr_kmeans_car_clusters.set('fieldLabels', {'cid': 'header label', 'radius': 'header label', 'points': 'header label'});
}



/**
 * Layer visibility handlers
 * on k-means layer checkbox trigger changes the visibility
 */

// WALK
$(document).ready(function(){
  $('#kmeans_walk_visibility').change(function(event){
    var visible = $(this).is(":checked");
    map.getLayers().forEach(function(lyr) {
      if ((lyr.get('name') === 'kmeans_walk_clusters') || 
      (lyr.get('name') === 'kmeans_walk_points')) {
        lyr.setVisible(visible);
      }
    });
  });
});



// BIKE
$(document).ready(function(){
  $('#kmeans_bike_visibility').change(function(event){
    var visible = $(this).is(":checked");
    map.getLayers().forEach(function(lyr) {
      if ((lyr.get('name') === 'kmeans_bike_clusters') || 
      (lyr.get('name') === 'kmeans_bike_points')) {
        lyr.setVisible(visible);
      }
    });
  });
});



// CAR
$(document).ready(function(){
  $('#kmeans_car_visibility').change(function(event){
    var visible = $(this).is(":checked");
    map.getLayers().forEach(function(lyr) {
      if ((lyr.get('name') === 'kmeans_car_clusters') || 
      (lyr.get('name') === 'kmeans_car_points')) {
        lyr.setVisible(visible);
      }
    });
  });
});
