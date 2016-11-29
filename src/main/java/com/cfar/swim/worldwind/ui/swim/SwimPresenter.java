package com.cfar.swim.worldwind.ui.swim;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

public class SwimPresenter implements Initializable {

	@FXML
	private ListView<String> swimList;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		swimList.getItems().add("SWIM item");
	}

	public void enableSwimItem() {
		System.out.println("enabling...");
	}
	
	public void disableSwimItem() {
		System.out.println("disabling...");
	}
	
}
