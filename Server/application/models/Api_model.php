<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

Class Api_Model extends CI_Model
{	

	function __construct()
    {
       	parent::__construct();		// Construct the parent class
    }

    /** This functions returns true if the point given as input is in the 
        city center of Bologna
    **/
    function check_valid_point($pos)
    {
        $sql = "SELECT ST_WITHIN(ST_GeomFromText(?, 4326), 
                                ST_BUFFER(ST_GeomFromText('POINT( 11.34431 44.49439)', 4326), 0.02))";

        $query = $this->db->query($sql, [$pos]);

        $result = $query->result_array();
        return ($result[0]["st_within"] == 't'); 
    } 

	/** This functions takes as input a user id, an activity and a new position
	    and it returns true if it manages to update the corresponding path
	    or false if the query was not successful or if there was no path associated
    **/
    function update_path($pathId, $act, $pos)
    {
    	$sql = "UPDATE paths.path_$act
                SET (geom, last_update) = 
                    (ST_AddPoint(geom, ST_GeomFromText(?, 4326)), current_date)
                WHERE id = ?";

    	$result = $this->db->query($sql, [$pos, $pathId]);

    	return ($result && ($this->db->affected_rows()>0));
    } 


    /** This functions returns an array containing the geofences associated
    	to the position and activity given in input
    **/
    function retrieve_geofence($act, $pos)
    {
    	$sql = "SELECT id, message 
                FROM geofences.geofence_$act 
                WHERE ST_CONTAINS(geom, ST_GeomFromText(?, 4326))";

    	$query = $this->db->query($sql, [$pos]);

        return $query->result_array();
    } 

    /** This functions creates three new paths associated to the user in the database,
        it adds the current position to the path corresponding to the current activity
        and it returns the pathId associated to the paths
    **/
    public function create_paths($act, $pos)
    {
        //array with the other two activities
        $activities = array('walk', 'bike', 'car');
        $activities = array_diff($activities, array($act));

        //start transaction (so we have the same id for every table)
        $this->db->trans_start();
        //create the path and insert the position 
    	$sql = "INSERT INTO paths.path_$act (geom, last_update)
                VALUES (ST_MakeLine(ST_GeomFromText(?, 4326), 
                                    ST_GeomFromText(?, 4326)), current_date)";

    	$insert_query = $this->db->query($sql, [$pos, $pos]);
        $insert_id = $this->db->insert_id();

        foreach($activities as $activity)
        {
            //create the other two paths (with no geometry value)
            $sql = "INSERT INTO paths.path_$activity (last_update) VALUES (current_date)";

	    	$insert_query = $this->db->query($sql);
        }
        //end transaction
        $this->db->trans_complete();

        //return the id
        return $insert_id;
    }


	/** This functions returns the minimum distance in meters between the position and the nearest
        geofence (or the border of the current geofence)
    **/
    public function compute_distance($geofenceId, $pos, $activity)
    {
        //if the user is not currently in a geofence
        if(is_null($geofenceId))
        {
            //compute the distance in meters between the position of the user and the closest geofence
	        $sql = "SELECT min(ST_DistanceSphere(geom, ST_GeomFromText(?, 4326))) as distance
                    FROM geofences.geofence_$activity";

	    	$query = $this->db->query($sql, [$pos]);
        }
        else
        {
            //compute the distance to the border of the current geofence
          	$sql = "SELECT ST_DistanceSphere(ST_ExteriorRing(geom), 
          									 ST_GeomFromText(?, 4326)) as distance
                    FROM geofences.geofence_$activity
                    WHERE id = ?";

	    	$query = $this->db->query($sql, [$pos, $geofenceId]);
        }
        
        $result = $query->result_array();

        return isset($result[0]['distance'])? $result[0]['distance'] : 0;
    }

    /** This function takes as input a geofence id and an activity 
      	and it updates the number of activations of the geofence
    **/
    public function new_activation($geofenceId, $activity)
    {
    	$sql = "UPDATE geofences.geofence_$activity 
                SET intensity = intensity + 1
                WHERE id = ?";

    	$this->db->query($sql, [$geofenceId]);
    }

}


		
	
