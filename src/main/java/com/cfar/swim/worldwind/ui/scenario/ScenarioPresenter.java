package com.cfar.swim.worldwind.ui.scenario;

import java.net.URL;
import java.util.ResourceBundle;

import com.cfar.swim.worldwind.session.Scenario;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.Main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

public class ScenarioPresenter implements Initializable {

	@FXML
	ListView<String> scenarios;
	
	public ScenarioPresenter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Scenario scenario = SessionManager.getInstance().getSession(Main.APPLICATION_TITLE).getActiveScenario();
		this.scenarios.getItems().add(scenario.getId());
	}

	public void activateScenario(ActionEvent event) {
		
	}
	
	public void addScenario() {}
	
	public void removeScenario() {}
	
	public void clearScenarios() {}
	
}
