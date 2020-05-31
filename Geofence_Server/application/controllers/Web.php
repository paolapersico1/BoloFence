<?php
defined('BASEPATH') OR exit('No direct script access allowed');


class Web extends CI_Controller
{

	function __construct()
    	{
		parent::__construct();			// Construct the parent class
		$this->load->helper('url');		// loading base_url()
		$this->load->model('web_model');	// loading model
	
	}

	/**
	 * Index Page for this controller.
	 *
	 * Maps to the following URL
	 * 		http://example.com/index.php/welcome
	 *	- or -
	 * 		http://example.com/index.php/welcome/index
	 *	- or -
	 * Since this controller is set as the default controller in
	 * config/routes.php, it's displayed at http://example.com/
	 *
	 * So any other public methods not prefixed with an underscore will
	 * map to /index.php/welcome/<method_name>
	 * @see https://codeigniter.com/user_guide/general/urls.html
	 */

	public function index()
	{
		// creating current table datastamp
		$this->web_model->set_tmp_arrival();	

		// getting geojson obj from model queries.
		// these objs are assigned in view and used in layer builder script.
		$geojsons = $this->web_model->get_geojsons();
		
		// Cycle over geojsons result and assign each json to its key in data
		if($geojsons){
			foreach($geojsons as $key => $geojson)
			{
				$data[$key] = $geojson[0]->json_build_object;
			}
			$this->load->view('qgis2web/index.html', $data);
		}
		else {
			print_r("Ops! Your data is empty.");
		}
	}

	// Triggered by ajax post request
	// returns kmeans geojson
	public function kmeans()
	{
		$postData = $this->input->post();	// contains k and activity type
		$kmeans_data = $this->web_model->get_kmeans($postData);

		if($kmeans_data){
			foreach($kmeans_data as $key => $kmeans)
			{
				$result[$key] = $kmeans[0]->json_build_object;
			}
		}
		else {
			print_r("Ops! Your data is empty.");
		}

		echo json_encode($result);
    }

    // Triggered by ajax post request
    // returns new geofence geojson
    public function new_geofence()
    {
        $postData = $this->input->post();
        $newGeofence = $this->web_model->new_geofence($postData);

        echo $newGeofence[0]->json_build_object;
    }
}
