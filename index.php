<?php

/**
 * File to handle all API requests
 * Accepts GET and POST
 * 
 * Each request will be identified by TAG
 * Response will be JSON data

  /**
 * check for POST request 
 */

if (true) {
    // get tag
    $tag = $_GET['tag'];

    // include db handler
    require_once 'include/DB_Functions.php';
    $db = new DB_Functions();

    // response Array
    $response = array("tag" => $tag, "success" => 0, "error" => 0);

    // check for tag type
    if ($tag == 'login') {
        // Request type is check Login
        $number = $_GET['number'];
        $password = $_GET['password'];

        // check for user
        $user = $db->getUserByEmailAndPassword($number, $password);
        if ($user != false) {
            // user found
            // echo json with success = 1
            $response["success"] = 1;
            $response["uid"] = $user["uid"];
            $response["user"]["number"] = $user["number"];
            echo json_encode($response);
        } else {
            // user not found
            // echo json with error = 1
            $response["error"] = 1;
            $response["error_msg"] = "Incorrect email or password!";
            echo json_encode($response);
        }
    } else if ($tag == 'register') {
        // Request type is Register new user
        $number = $_GET['number'];
        $password = $_GET['password'];
	$key = $_GET['publicKey'];
        // check if user is already existed
        if ($db->isUserExisted($number)) {
            // user is already existed - error response
            $response["error"] = 2;
            $response["error_msg"] = "User already existed";
            echo json_encode($response);
        } else {
            // store user
            $user = $db->storeUser($number, $password);
            if ($user) {
                // user stored successfully
		$keyres = $db->storeKey($number, $key);
		echo $keyres;
                $response["success"] = 1;
                $response["uid"] = $user["uid"];
                $response["user"]["number"] = $user["number"];
                echo json_encode($response);
            } else {
                // user failed to store
                $response["error"] = 1;
                $response["error_msg"] = "Error occured in Registartion";
                echo json_encode($response);
            }
        }
    } else {
        echo "Invalid Request";
    }
} else {
    echo "Access Denied";
}
?>
