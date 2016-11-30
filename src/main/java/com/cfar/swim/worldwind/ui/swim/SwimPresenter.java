package com.cfar.swim.worldwind.ui.swim;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ResourceBundle;

import com.cfar.swim.worldwind.render.Obstacle;
import com.cfar.swim.worldwind.session.Scenario;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

public class SwimPresenter implements Initializable {

	@FXML
	private ListView<String> swimList;

	private Scenario scenario = null;
	
	private ObstaclesChangeListener ocl = new ObstaclesChangeListener();
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
		session.addActiveScenarioChangeListener(new ActiveScenarioChangeListener());
		this.initScenario();
		this.initObstacles();
	}

	private void initScenario() {
		if (null != this.scenario) {
			this.scenario.removePropertyChangeListener(this.ocl);
		}
		Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
		this.scenario = session.getActiveScenario();
		this.scenario.addObstaclesChangeListener(this.ocl);
	}
	
	private void initObstacles() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				swimList.getItems().clear();
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				for (Obstacle obstacle : session.getActiveScenario().getObstacles()) {
					swimList.getItems().add(obstacle.getCostInterval().getId());
				}
				swimList.refresh();
			}
		});
	}
	
	public void enableSwimItem() {
		System.out.println("enabling...");
	}
	
	public void disableSwimItem() {
		System.out.println("disabling...");
	}
	
	private class ObstaclesChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initObstacles();
		}
	}
	
	private class ActiveScenarioChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initScenario();
			initObstacles();
		}
	}
	
}
