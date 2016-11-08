package com.cfar.swim.worldwind.ui.plan.waypoint;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

public class WaypointPresenter implements Initializable {

	@FXML
	private TextField latitude;
	
	@FXML
	private TextField longitude;
	
	@FXML
	private TextField altitude;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

}
