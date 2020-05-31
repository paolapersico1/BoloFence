<?php
defined('BASEPATH') OR exit('No direct script access allowed');

use chriskacerguis\RestServer\RestController;

class Api extends RestController{

	function __construct()
    {
        // Construct the parent class
        parent::__construct();
        $this->load->model('api_model');
    }

    /** This REST API function gets (using POST method) a position, 
        an activity (walk, car, bike) and a pathId all packed as a geojson feature 
        and it returns the geofence associated to that feature and a new pathId 
        (or the old one if it is already associated to the paths in the database)
    **/
	public function geofence_post()
    {
        //get the geojson values
        $properties = $this->post('properties');
        $act = isset($properties['activity'])? $properties['activity'] : null;
        $pathId = isset($properties['pathId'])? $properties['pathId'] : 0;
        $currentGeofence = isset($properties['currentGeofence'])? $properties['currentGeofence'] : null;
        $geometry = $this->post('geometry');
        $coordinates = isset($geometry['coordinates'])? $geometry['coordinates'] : null;

        //if no coordinates are sent
        if ($coordinates == null || !is_numeric($coordinates[0]) || !is_numeric($coordinates[1]))
        {
            // Set the response and exit
            $this->response( [
                'status' => false,
                'error' => 'No valid location provided'
            ], 404 );
        }
        //if the activity isn't a valid one
        else if ($act != "walk" && $act != "bike" && $act != "car")
        {
            // Set the response and exit
            $this->response( [
                'status' => false,
                'error' => 'Activity issue'
            ], 404 );
        }
        else
        {
            $result = null;
            $insert_id = $pathId;

            //create a point from the longitude and the latitude
            $position = 'POINT('.$coordinates[0]. ' '.$coordinates[1].')';

            //check if point is in the city center of Bologna
            $valid = $this->api_model->check_valid_point($position);

            if($valid)
            {
                //retrieve the associated geofence
                $result = $this->api_model->retrieve_geofence($act, $position);

                $new = false;   //flag set to true if we need to create a new path

                //if a valid pathId is sent
                if(is_int($pathId) && $pathId != 0)
                {
                    //add position to the associated path
                    $update_query = $this->api_model->update_path($pathId, $act, $position);

                    //if the query was not executed
                    if(!$update_query)
                    {
                        $new = true;
                    }
                }
                else 
                {
                    $new = true;
                }

                if($new)
                {
                    //create new paths and get new pathId
                    $insert_id = $this->api_model->create_paths($act, $position);
                }
            }

            //if there is not an associated geofence 
            if($result == null)
            {
                //instead of the geofence message we send an info message
                $result = array();
                $result[0]['message'] = 'No geofence found';
                $result[0]['sleepTime'] = $this->compute_sleep_time(null, $position, $act);
            }
            //if it's not the first time we enter in a geofence
            else if($result[0]['message'] == $currentGeofence)
            {
                //instead of the geofence message we send an info message
                $geofenceId = $result[0]['id'];
                $result = array();
                $result[0]['message'] = 'No new geofence';
                $result[0]['sleepTime'] = $this->compute_sleep_time($geofenceId, $position, $act);
            }
            else
            {
                $geofenceId = $result[0]['id'];
                $result[0]['sleepTime'] = $this->compute_sleep_time($geofenceId, $position, $act);
                //we also update the number of activations of the geofence
                $this->api_model->new_activation($geofenceId, $act);
            }
            //in each case we send also the pathId and the time to sleep
            $result[0]['pathId'] = $insert_id;
            $this->response($result, 200);
        }
    }


    /** This functions computes the minimum distance between the position and the nearest
        geofence (or the border of the current geofence) and returns the time to 
        walk/cycle/drive that distance in seconds
    **/
    public function compute_sleep_time($geofenceId, $position, $activity)
    {
        $distance = $this->api_model->compute_distance($geofenceId, $position, $activity);
        $minimum_st = 10;    //minimum threshold in seconds

        //compute time to sleep in seconds
        if($activity == "walk")
        {
            //average walking speed = 1.4 m/s
            $sleepTime = $distance / 1.4;
        }
        else if($activity == "bike")
        {
            //average cycling speed = 15.5 km/h
            $sleepTime = ($distance * 3600)/15500.0;
        }
        else if($activity == "car")
        {
            //speed limit in Bologna = 30 km/h
            $sleepTime = ($distance * 3600)/30000.0;
        }

        //sleepTime is 2/3 of the time it takes to get to a new geofence
        $sleepTime = (2.0 * $sleepTime)/3;

        if($sleepTime < $minimum_st)
        {
            $sleepTime = $minimum_st;
        }

        return $sleepTime;
    }
}

