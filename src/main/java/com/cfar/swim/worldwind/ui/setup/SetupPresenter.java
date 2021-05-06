/**
 * Copyright (c) 2021, Stephan Heinemann (UVic Center for Aerospace Research)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.cfar.swim.worldwind.ui.setup;

import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;

import com.cfar.swim.worldwind.aircraft.Aircraft;
import com.cfar.swim.worldwind.connections.Datalink;
import com.cfar.swim.worldwind.connections.SwimConnection;
import com.cfar.swim.worldwind.environments.Environment;
import com.cfar.swim.worldwind.planners.Planner;
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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

/**
 * Realizes a presenter of a setup view.
 * 
 * @author Stephan Heinemann
 *
 */
public class SetupPresenter implements Initializable {
	
	/** the aircraft properties pane of the setup view */
	@FXML
	private ScrollPane aircraftPropertiesPane;
	
	/** the environment properties pane of the setup view */
	@FXML
	private ScrollPane envPropertiesPane;
	
	/** the planner properties pane of the setup view */
	@FXML
	private ScrollPane plannerPropertiesPane;
	
	/** the datalink properties pane of the setup view */
	@FXML
	private ScrollPane datalinkPropertiesPane;
	
	/** the SWIM connection properties pane of the setup view */
	@FXML
	private ScrollPane swimConnectionPropertiesPane;
	
	/** the aircraft selector of the setup view */
	@FXML
	private ComboBox<String> aircraft;
	
	/** the environment selector of the setup view */
	@FXML
	private ComboBox<String> environment;
	
	/** the planner selector of the setup view */
	@FXML
	private ComboBox<String> planner;
	
	/** the datalink selector of the setup view */
	@FXML
	private ComboBox<String> datalink;
	
	/** the SWIM connection selector of the setup view */
	@FXML
	private ComboBox<String> swimConnection;
	
	/** the aircraft description label of the setup view */
	@FXML
	private Label aircraftDescription;
	
	/** the environment description label of the setup view */
	@FXML
	private Label environmentDescription;
	
	/** the planner description label of the setup view */
	@FXML
	private Label plannerDescription;
	
	/** the SWIM description label of the setup view */
	@FXML
	private Label swimDescription;
	
	/** the datalink description label of the setup view */
	@FXML
	private Label datalinkDescription;
	
	/** the setup model to be modified in the setup view */
	@Inject
	private SetupModel setupModel;
	
	/**
	 * Initializes this setup presenter.
	 * 
	 * @param location unused
	 * @param resources unused
	 * 
	 * @see Initializable#initialize(URL, ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.initAircraft();
		this.initEnvironment();
		this.initPlanner();
		this.initDatalink();
		this.initSwimConnection();
	}
	
	/**
	 * Initializes the aircraft setup of the setup view.
	 */
	public void initAircraft() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				for (Specification<Aircraft> aircraftSpec : session.getAircraftSpecifications()) {
					aircraft.getItems().add(aircraftSpec.getId());
				}
				
				Specification<Aircraft> aircraftSpec = session.getSetup().getAircraftSpecification();
				aircraft.getSelectionModel().select(aircraftSpec.getId());
				aircraftDescription.setText(aircraftSpec.getDescription());
				setupModel.setAircraftProperties(aircraftSpec.getProperties().clone());
				PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(setupModel.getAircraftProperties()));
				propertySheet.setMode(PropertySheet.Mode.CATEGORY);
				propertySheet.setPrefWidth(aircraftPropertiesPane.getWidth());
				aircraftPropertiesPane.setContent(propertySheet);
				aircraft.valueProperty().addListener(new AircraftChangeListener());
				aircraft.layout();
			}
		});
	}
	
	/**
	 * Initializes the environment setup of the setup view.
	 */
	public void initEnvironment() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				for (Specification<Environment> envSpec : session.getEnvironmentSpecifications()) {
					environment.getItems().add(envSpec.getId());
				}
				
				Specification<Environment> envSpec = session.getSetup().getEnvironmentSpecification();
				environment.getSelectionModel().select(envSpec.getId());
				environmentDescription.setText(envSpec.getDescription());
				setupModel.setEnvironmentProperties(envSpec.getProperties().clone());
				PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(setupModel.getEnvironmentProperties()));
				propertySheet.setMode(PropertySheet.Mode.CATEGORY);
				propertySheet.setPrefWidth(envPropertiesPane.getWidth());
				envPropertiesPane.setContent(propertySheet);
				environment.valueProperty().addListener(new EnvironmentChangeListener());
				environment.layout();
			}
		});
	}
	
	/**
	 * Initializes the planner of the setup view.
	 */
	public void initPlanner() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				for (Specification<Planner> plannerSpec : session.getPlannerSpecifications()) {
					planner.getItems().add(plannerSpec.getId());
				}
				
				Specification<Planner> plannerSpec = session.getSetup().getPlannerSpecification();
				planner.getSelectionModel().select(plannerSpec.getId());
				plannerDescription.setText(plannerSpec.getDescription());
				setupModel.setPlannerProperties(plannerSpec.getProperties().clone());
				PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(setupModel.getPlannerProperties()));
				propertySheet.setMode(PropertySheet.Mode.CATEGORY);
				propertySheet.setPrefWidth(plannerPropertiesPane.getWidth());
				plannerPropertiesPane.setContent(propertySheet);
				planner.valueProperty().addListener(new PlannerChangeListener());
				planner.layout();
			}
		});
	}
	
	/**
	 * Initializes the datalink of the setup view.
	 */
	public void initDatalink() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				for (Specification<Datalink> datalinkSpec : session.getDatalinkSpecifications()) {
					datalink.getItems().add(datalinkSpec.getId());
				}
				
				Specification<Datalink> datalinkSpec = session.getSetup().getDatalinkSpecification();
				datalink.getSelectionModel().select(datalinkSpec.getId());
				datalinkDescription.setText(datalinkSpec.getDescription());
				setupModel.setDatalinkProperties(datalinkSpec.getProperties().clone());
				PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(setupModel.getDatalinkProperties()));
				propertySheet.setMode(PropertySheet.Mode.CATEGORY);
				propertySheet.setPrefWidth(datalinkPropertiesPane.getWidth());
				datalinkPropertiesPane.setContent(propertySheet);
				datalink.valueProperty().addListener(new DatalinkChangeListener());
				datalink.layout();
			}
		});
	}
	
	/**
	 * Initializes the SWIM connection of the setup view.
	 */
	public void initSwimConnection() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				for (Specification<SwimConnection> swimConnectionSpec : session.getSwimConnectionSpecifications()) {
					swimConnection.getItems().add(swimConnectionSpec.getId());
				}
				
				Specification<SwimConnection> swimConnectionSpec = session.getSetup().getSwimConnectionSpecification();
				swimConnection.getSelectionModel().select(swimConnectionSpec.getId());
				swimDescription.setText(swimConnectionSpec.getDescription());
				setupModel.setSwimConnectionProperties(swimConnectionSpec.getProperties().clone());
				PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(setupModel.getSwimConnectionProperties()));
				propertySheet.setMode(PropertySheet.Mode.CATEGORY);
				propertySheet.setPrefWidth(swimConnectionPropertiesPane.getWidth());
				swimConnectionPropertiesPane.setContent(propertySheet);
				swimConnection.valueProperty().addListener(new SwimConnectionChangeListener());
				swimConnection.layout();
			}
		});
	}
	
	/**
	 * Realizes an aircraft change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class AircraftChangeListener implements ChangeListener<String> {
		
		/**
		 * Updates the aircraft setup if the aircraft changes.
		 * 
		 * @param observable the observable associated with the aircraft change
		 * @param oldAircraftId the old aircraft identifier
		 * @param newAircraftId the new aircraft identifier
		 * 
		 * @see ChangeListener#changed(ObservableValue, Object, Object)
		 */
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldAircraftId, String newAircraftId) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
					Specification<Aircraft> aircraftSpec = session.getAircraftSpecification(newAircraftId);
					aircraftDescription.setText(aircraftSpec.getDescription());
					setupModel.setAircraftProperties(aircraftSpec.getProperties().clone());
					PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(setupModel.getAircraftProperties()));
					propertySheet.setMode(PropertySheet.Mode.CATEGORY);
					propertySheet.setPrefWidth(aircraftPropertiesPane.getWidth());
					aircraftPropertiesPane.setContent(propertySheet);
					aircraftPropertiesPane.layout();
				}
			});
		}
	}
	
	/**
	 * Realizes an environment change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class EnvironmentChangeListener implements ChangeListener<String> {
		
		/**
		 * Updates the environment setup if the environment changes.
		 * 
		 * @param observable the observable associated with the environment change
		 * @param oldEnvId the old environment identifier
		 * @param newEnvId the new environment identifier
		 * 
		 * @see ChangeListener#changed(ObservableValue, Object, Object)
		 */
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldEnvId, String newEnvId) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
					Specification<Environment> envSpec = session.getEnvironmentSpecification(newEnvId);
					environmentDescription.setText(envSpec.getDescription());
					setupModel.setEnvironmentProperties(envSpec.getProperties().clone());
					PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(setupModel.getEnvironmentProperties()));
					propertySheet.setMode(PropertySheet.Mode.CATEGORY);
					propertySheet.setPrefWidth(envPropertiesPane.getWidth());
					envPropertiesPane.setContent(propertySheet);
					envPropertiesPane.layout();
				}
			});
		}
	}
	
	/**
	 * Realizes a planner change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class PlannerChangeListener implements ChangeListener<String> {
		
		/**
		 * Updates the planner setup if the planner changes.
		 * 
		 * @param observable the observable associated with the planner change
		 * @param oldPlannerId the old planner identifier
		 * @param newPlannerId the new planner identifier
		 * 
		 * @see ChangeListener#changed(ObservableValue, Object, Object)
		 */
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldPlannerId, String newPlannerId) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
					Specification<Planner> plannerSpec = session.getPlannerSpecification(newPlannerId);
					plannerDescription.setText(plannerSpec.getDescription());
					setupModel.setPlannerProperties(plannerSpec.getProperties().clone());
					PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(setupModel.getPlannerProperties()));
					propertySheet.setMode(PropertySheet.Mode.CATEGORY);
					propertySheet.setPrefWidth(plannerPropertiesPane.getWidth());
					plannerPropertiesPane.setContent(propertySheet);
					plannerPropertiesPane.layout();
				}
			});
		}
	}
	
	/**
	 * Realizes a datalink change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class DatalinkChangeListener implements ChangeListener<String> {
		
		/**
		 * Updates the datalink setup if the datalink changes.
		 * 
		 * @param observable the observable associated with the datalink change
		 * @param oldDatalinkId the old datalink identifier
		 * @param newDatalinkId the new datalink identifier
		 * 
		 * @see ChangeListener#changed(ObservableValue, Object, Object)
		 */
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldDatalinkId, String newDatalinkId) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
					Specification<Datalink> datalinkSpec = session.getDatalinkSpecification(newDatalinkId);
					datalinkDescription.setText(datalinkSpec.getDescription());
					setupModel.setDatalinkProperties(datalinkSpec.getProperties().clone());
					PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(setupModel.getDatalinkProperties()));
					propertySheet.setMode(PropertySheet.Mode.CATEGORY);
					propertySheet.setPrefWidth(datalinkPropertiesPane.getWidth());
					datalinkPropertiesPane.setContent(propertySheet);
					datalinkPropertiesPane.layout();
				}
			});
		}
	}
	
	/**
	 * Realizes a SWIM connection change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class SwimConnectionChangeListener implements ChangeListener<String> {
		
		/**
		 * Updates the SWIM connection setup if the SWIM connection changes.
		 * 
		 * @param observable the observable associated with the SWIM connection change
		 * @param oldSwimConnectionId the old SWIM connection identifier
		 * @param newSwimConnectionId the new SWIM connection identifier
		 * 
		 * @see ChangeListener#changed(ObservableValue, Object, Object)
		 */
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldSwimConnectionId, String newSwimConnectionId) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
					Specification<SwimConnection> swimConnectionSpec = session.getSwimConnectionSpecification(newSwimConnectionId);
					swimDescription.setText(swimConnectionSpec.getDescription());
					setupModel.setSwimConnectionProperties(swimConnectionSpec.getProperties().clone());
					PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(setupModel.getSwimConnectionProperties()));
					propertySheet.setMode(PropertySheet.Mode.CATEGORY);
					propertySheet.setPrefWidth(swimConnectionPropertiesPane.getWidth());
					swimConnectionPropertiesPane.setContent(propertySheet);
					swimConnectionPropertiesPane.layout();
				}
			});
		}
	}
	
}
