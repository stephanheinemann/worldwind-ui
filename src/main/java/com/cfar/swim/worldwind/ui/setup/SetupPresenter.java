package com.cfar.swim.worldwind.ui.setup;

import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;

import com.cfar.swim.worldwind.ai.Planner;
import com.cfar.swim.worldwind.planning.Environment;
import com.cfar.swim.worldwind.registries.Specification;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;

public class SetupPresenter implements Initializable {
	
	@FXML
	private ScrollPane envPropertiesPane;
	
	@FXML
	private ScrollPane plannerPropertiesPane;
	
	@FXML
	private ComboBox<String> environment;
	
	@FXML
	private ComboBox<String> planner;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.initEnvironment();
		this.initPlanner();
	}
	
	public void initEnvironment() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				for (Specification<Environment> envSpec : session.getEnvironmentSpecifications()) {
					environment.getItems().add(envSpec.getId());
				}
				environment.layout();
				environment.getSelectionModel().selectFirst();
				
				String envId = environment.getSelectionModel().getSelectedItem();
				Specification<Environment> envSpec = session.getEnvironmentSpecification(envId);
				PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(envSpec.getProperties()));
				envPropertiesPane.setContent(propertySheet);
				
				environment.valueProperty().addListener(new EnvironmentChangeListener());
			}
		});
	}
	
	public void initPlanner() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				for (Specification<Planner> plannerSpec : session.getPlannerSpecifications()) {
					planner.getItems().add(plannerSpec.getId());
				}
				planner.layout();
				planner.getSelectionModel().selectFirst();
				
				String plannerId = planner.getSelectionModel().getSelectedItem();
				Specification<Planner> plannerSpec = session.getPlannerSpecification(plannerId);
				PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(plannerSpec.getProperties()));
				plannerPropertiesPane.setContent(propertySheet);
				
				planner.valueProperty().addListener(new PlannerChangeListener());
			}
		});
	}
	
	private class EnvironmentChangeListener implements ChangeListener<String> {

		@Override
		public void changed(ObservableValue<? extends String> observable, String oldEnvId, String newEnvId) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
					Specification<Environment> envSpec = session.getEnvironmentSpecification(newEnvId);
					PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(envSpec.getProperties()));
					envPropertiesPane.setContent(propertySheet);
					envPropertiesPane.layout();
				}
			});
		}
	}
	
	private class PlannerChangeListener implements ChangeListener<String> {

		@Override
		public void changed(ObservableValue<? extends String> observable, String oldPlannerId, String newPlannerId) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
					Specification<Planner> plannerSpec = session.getPlannerSpecification(newPlannerId);
					PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(plannerSpec.getProperties()));
					plannerPropertiesPane.setContent(propertySheet);
					plannerPropertiesPane.layout();
				}
			});
		}
	}
	

}
