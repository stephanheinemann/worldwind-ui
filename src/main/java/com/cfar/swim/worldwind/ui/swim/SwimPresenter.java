package com.cfar.swim.worldwind.ui.swim;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
				swimList.getItems().addAll(
					session.getActiveScenario().getObstacles()
						.stream()
						.map(o -> o.getCostInterval().getId())
						.distinct()
						.collect(Collectors.toSet()));
				swimList.refresh();
			}
		});
	}
	
	public void addSwimItem() {
		
	}
	
	public void removeSwimItem() {
		String swimId = swimList.getSelectionModel().getSelectedItem();
		if (null != swimId) {
			scenario.removeObstacles(swimId);
			swimList.getItems().remove(swimId);
			swimList.refresh();
		}
	}
	
	public void clearSwimItems() {
		scenario.clearObstacles();
		swimList.getItems().clear();
		swimList.refresh();
	}
	
	public void enableSwimItem() {
		String swimId = swimList.getSelectionModel().getSelectedItem();
		if (null != swimId) {
			scenario.enableObstacles(swimId);
		}
	}
	
	public void disableSwimItem() {
		String swimId = swimList.getSelectionModel().getSelectedItem();
		if (null != swimId) {
			scenario.disableObstacles(swimId);
		}
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
