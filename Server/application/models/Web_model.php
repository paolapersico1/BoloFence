<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

Class Web_Model extends CI_Model
{	

	function __construct()
    	{
       		parent::__construct();		// Construct the parent class
    	}


	/**
	 * Gets all the tables data needed to init the view page
	 * Output:
	 * 	- key-value array of geojson objects
	 */
	function get_geojsons()
	{
		$activities = array('walk', 'bike', 'car');
		$result = array();
		
		// Gets geofences, paths and arrival points per each activity
		foreach($activities as $activity)
		{
			// Geofences
			$query_geofence = $this->db->query("SELECT json_build_object(
								'type', 'FeatureCollection',
								'features', json_agg(
									json_build_object(
										'type', 'Feature',
										'geometry', ST_AsGeoJSON(GF.geom)::json,
										'properties', json_build_object(
											'message', GF.message,
											'intensity', GF.intensity
											)
										)
									)
								)
							   FROM (
								SELECT *
								FROM geofences.geofence_$activity
								) AS GF");

			// Paths 
			$query_path = $this->db->query("SELECT json_build_object(
								'type', 'FeatureCollection',
								'features', json_agg(
									json_build_object(
										'type', 'Feature',
										'geometry', ST_AsGeoJSON(P.geom)::json
										)
									)
								)
							   FROM (
								SELECT *
								FROM paths.path_$activity
								WHERE geom IS NOT NULL
								) AS P");

			// Arrival points
			$query_arrival = $this->db->query("SELECT json_build_object(
								'type', 'FeatureCollection',
								'features', json_agg(
									json_build_object(
										'type', 'Feature',
                                        'geometry', ST_AsGeoJSON(A.arrival)::json,
                                        'properties', json_build_object(
                                            'id', A.id
                                           ) 
										)
									)
								)
							   FROM (
								SELECT P.id, ST_EndPoint(P.geom) AS arrival
								FROM paths.path_$activity AS P
								WHERE P.geom IS NOT NULL
								) AS A");
		
			// key-value array of query results 	
			$result += array('json_geofence_'.$activity => $query_geofence->result(),
				    	 'json_path_'.$activity.'_geom' => $query_path->result(),
				    	 'json_path_'.$activity.'_arrival' => $query_arrival->result());
		}
        
        return $result;
	}



	/**
	 *  Static copy of the arrival points.
	 *  Dropped and re-created at each view page refresh.
	 *  Used for a consistent kmean evaluation in relation to the view static data.
	 */
	function set_tmp_arrival()
	{
        $activities = array('walk', 'bike', 'car');
		
        foreach($activities as $activity){

			// Drop old
			$this->db->query("DROP TABLE IF EXISTS paths.path_".$activity."_arrival CASCADE");
			
			// Create current
			$this->db->query("CREATE TABLE paths.path_".$activity."_arrival AS (
					SELECT ST_EndPoint(P.geom) AS arrival
					FROM paths.path_$activity AS P
					WHERE P.geom IS NOT NULL
					)");
		}
	}



	/**
     * Defines the clusters for arrival points of the specified mobility
     * exploiting K-Means algorithm
     *
	 * Input: 
	 * 	- postData = array('k' => integer, 'activity' => string)
	 * Output:
	 * 	- geojson file containing clusters geom
	 * 	- geojson file containing clusters points
	 */
	function get_kmeans($postData)
	{
		$k = $postData['k'];
		$activity = $postData['activity'];

		$this->db->query("DROP MATERIALIZED VIEW IF EXISTS km_points CASCADE;");
	    
        // Applies K-Means alg to arrival points    
		$sql = ("CREATE MATERIALIZED VIEW km_points AS (
				SELECT ST_ClusterKMeans(PA.arrival, ?) OVER() AS cid,
					PA.arrival AS arrival
				FROM paths.path_".$activity."_arrival PA
				WHERE PA.arrival IS NOT NULL
                );
                CREATE INDEX ON km_points(arrival);");
		$km_points = $this->db->query($sql, [$k]);

        // Creates geojson polygons for each cluster
		$km_clusters_json = $this->db->query("SELECT json_build_object(
						'type', 'FeatureCollection',
						'features', json_agg(
							json_build_object(
								'type', 'Feature',
								'geometry', ST_AsGeoJSON(KM.cluster_polygon)::json,
								'properties', json_build_object(
									'cid', KM.cid,
									'points', KM.intensity
									)
								)
							)
						)
						FROM (
							SELECT KMP.cid,
								ST_Buffer(ST_ConvexHull(ST_Collect(KMP.arrival)), 0.0003) AS cluster_polygon,
								COUNT(KMP.arrival) AS intensity
							FROM km_points KMP
							GROUP BY KMP.cid)
						AS KM");

        // Creates geojson points for each cluster
		$km_points_json = $this->db->query("SELECT json_build_object(
						'type', 'FeatureCollection',
						'features', json_agg(
							json_build_object(
								'type', 'Feature',
								'geometry', ST_AsGeoJSON(KMP.arrival)::json,
								'properties', json_build_object(
									'cid', KMP.cid
									)
								)
							)
						)
                        FROM km_points KMP");

		$this->db->query("DROP MATERIALIZED VIEW IF EXISTS km_points CASCADE;");

		$result = array("km_".$activity."_clusters" => $km_clusters_json->result(),
			"km_".$activity."_points" => $km_points_json->result());

		return $result;
    }



	/**
	 * Generates potential new geofence exploiting DBSCAN algorithm
	 * Input: 
	 * 	- postData = (int) precision, (string) activity.
	 * Output:
	 * 	- geojson file containing the best cluster as a potential new geofence
	 */
	function new_geofence($postData)
	{
		$percentile = $postData['precision'];
		$activity = $postData['activity'];

        // Drop old views
        $this->db->query("DROP MATERIALIZED VIEW IF EXISTS paths_filter CASCADE");

        // Paths minus the segment that stays in a geofence
        $paths_filter = $this->db->query ("CREATE MATERIALIZED VIEW paths_filter AS (
                SELECT P.id, ST_Difference(P.geom, G.geom) AS geom
                FROM paths.path_$activity P,
                (SELECT ST_MakeValid(ST_Collect(geom)) AS geom FROM geofences.geofence_$activity) G
                WHERE P.geom IS NOT NULL);
                CREATE INDEX ON paths_filter(geom);");

        // For each path pair selects the closest points on the line between them
        $optimal_points = $this->db->query ("CREATE MATERIALIZED VIEW optimal_points AS (
                SELECT P1.id,
                    ST_ClosestPoint(P1.geom, P2.geom) AS geom,
                    ST_Distance(P1.geom, P2.geom) AS distance
                FROM paths_filter P1 JOIN
                    paths_filter P2 ON (P2.id <> P1.id)
                WHERE P1.geom IS NOT NULL AND
                    P2.geom IS NOT NULL 
                ORDER BY distance ASC, P1.id ASC);
                CREATE INDEX ON optimal_points(geom);"); 

        // Percentile(x) on closest points distance in order to automatically choose eps value
        $sql = ("SELECT percentile_cont(?)
                    WITHIN GROUP (ORDER BY OP.distance) AS eps
                    FROM optimal_points OP");
        $eps_sql = $this->db->query($sql, [$percentile]);
        $row = $eps_sql->result_array();
        $eps = $row[0]['eps'];        
        
        // DBSCAN on closest points
		$sql = ("CREATE VIEW dbs_paths AS (
				SELECT ST_ClusterDBSCAN(OP.geom, eps:=?, minpoints:=1) OVER() AS cid,
				    OP.geom AS geom
                FROM optimal_points OP
                WHERE OP.distance < ?)");
        $dbs_paths = $this->db->query($sql, [$eps, $eps+0.0001]);

        // Polygon from clusters
		$dbs_paths_union = $this->db->query("CREATE VIEW dbs_paths_union AS (
							SELECT DP.cid,
								ST_Buffer(ST_ConvexHull(ST_Collect(DP.geom)), 0.0003) AS tmp_geofence,
								COUNT(DP.geom) AS intensity
                            FROM dbs_paths DP
                            WHERE DP.cid IS NOT NULL
							GROUP BY DP.cid)");

        // Getting the cluster with highest intensity and transforming it to geojson
		$new_geofence_json = $this->db->query("SELECT json_build_object(
						'type', 'FeatureCollection',
						'features', json_agg(
							json_build_object(
								'type', 'Feature',
								'geometry', ST_AsGeoJSON(NG.geofence)::json,
								'properties', json_build_object(
									'intensity', NG.p_intens
									)
								)
							)
						)
						FROM (
                            SELECT DPU.tmp_geofence AS geofence,
                                SUM(CASE WHEN ST_Intersects(DPU.tmp_geofence, P.geom) THEN 1 ELSE 0 END) AS p_intens
                            FROM dbs_paths_union DPU, paths.path_".$activity." P
                            WHERE DPU.intensity IN (
                                SELECT MAX(intensity)
                                FROM dbs_paths_union)
                            GROUP BY cid, intensity, geofence)
                        AS NG");

        // Drop current views
        $this->db->query("DROP MATERIALIZED VIEW IF EXISTS paths_filter CASCADE;");
		
		$result = $new_geofence_json->result();

		return $result;
    }
}
