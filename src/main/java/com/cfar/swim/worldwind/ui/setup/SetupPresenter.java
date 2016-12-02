/**
 * Copyright (c) 2016, Stephan Heinemann (UVic Center for Aerospace Research)
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

import com.cfar.swim.worldwind.ai.Planner;
import com.cfar.swim.worldwind.aircraft.Aircraft;
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
	private ScrollPane aircraftPropertiesPane;
	
	@FXML
	private ScrollPane envPropertiesPane;
	
	@FXML
	private ScrollPane plannerPropertiesPane;
	
	@FXML
	private ComboBox<String> aircraft;
	
	@FXML
	private ComboBox<String> environment;
	
	@FXML
	private ComboBox<String> planner;
	
	@Inject
	private SetupModel setupModel;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.initAircraft();
		this.initEnvironment();
		this.initPlanner();
	}
	
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
				setupModel.setAircraftProperties(aircraftSpec.getProperties().clone());
				PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(setupModel.getAircraftProperties()));
				aircraftPropertiesPane.setContent(propertySheet);
				aircraft.valueProperty().addListener(new AircraftChangeListener());
				aircraft.layout();
			}
		});
	}
	
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
				setupModel.setEnvironmentProperties(envSpec.getProperties().clone());
				PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(setupModel.getEnvironmentProperties()));
				envPropertiesPane.setContent(propertySheet);
				environment.valueProperty().addListener(new EnvironmentChangeListener());
				environment.layout();
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
				
				Specification<Planner> plannerSpec = session.getSetup().getPlannerSpecification();
				planner.getSelectionModel().select(plannerSpec.getId());
				setupModel.setPlannerProperties(plannerSpec.getProperties().clone());
				PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(setupModel.getPlannerProperties()));
				plannerPropertiesPane.setContent(propertySheet);
				planner.valueProperty().addListener(new PlannerChangeListener());
				planner.layout();
			}
		});
	}
	
	private class AircraftChangeListener implements ChangeListener<String> {

		@Override
		public void changed(ObservableValue<? extends String> observable, String oldAircraftId, String newAircraftId) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
					Specification<Aircraft> aircraftSpec = session.getAircraftSpecification(newAircraftId);
					setupModel.setAircraftProperties(aircraftSpec.getProperties().clone());
					PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(setupModel.getAircraftProperties()));
					aircraftPropertiesPane.setContent(propertySheet);
					aircraftPropertiesPane.layout();
				}
			});
		}
	}
	
	private class EnvironmentChangeListener implements ChangeListener<String> {

		@Override
		public void changed(ObservableValue<? extends String> observable, String oldEnvId, String newEnvId) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
					Specification<Environment> envSpec = session.getEnvironmentSpecification(newEnvId);
					setupModel.setEnvironmentProperties(envSpec.getProperties().clone());
					PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(setupModel.getEnvironmentProperties()));
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
					setupModel.setPlannerProperties(plannerSpec.getProperties().clone());
					PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(setupModel.getPlannerProperties()));
					plannerPropertiesPane.setContent(propertySheet);
					plannerPropertiesPane.layout();
				}
			});
		}
	}
	
}
