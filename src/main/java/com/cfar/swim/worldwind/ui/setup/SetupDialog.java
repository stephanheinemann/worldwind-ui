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

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

import com.cfar.swim.worldwind.aircraft.Aircraft;
import com.cfar.swim.worldwind.connections.Datalink;
import com.cfar.swim.worldwind.connections.SwimConnection;
import com.cfar.swim.worldwind.environments.Environment;
import com.cfar.swim.worldwind.managers.AutonomicManager;
import com.cfar.swim.worldwind.planners.Planner;
import com.cfar.swim.worldwind.registries.Properties;
import com.cfar.swim.worldwind.registries.Specification;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.session.Setup;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;
import com.cfar.swim.worldwind.ui.planner.PlannerAlert;
import com.cfar.swim.worldwind.ui.util.ResourceBundleLoader;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Realizes a setup dialog to setup a planning session with
 * an aircraft, an environment, SWIM data and a planner.
 * 
 * @author Stephan Heinemann
 *
 */
public class SetupDialog extends Dialog<Setup> {
	
	/** the title of a setup dialog */
	public static final String TITLE_SETUP =
			ResourceBundleLoader.getDictionaryBundle()
			.getString("setup.dialog.title");
	
	/** the header of a setup dialog */
	public static final String HEADER_SETUP =
			ResourceBundleLoader.getDictionaryBundle()
			.getString("setup.dialog.header");
	
	/** the aircraft tab index of a setup dialog */
	public static final int AIRCRAFT_TAB_INDEX = 0;
	
	/** the environment tab index of a setup dialog */
	public static final int ENVIRONMENT_TAB_INDEX = 1;
	
	/** the planner tab index of a setup dialog */
	public static final int PLANNER_TAB_INDEX = 2;
	
	/** the swim tab index of a setup dialog */
	public static final int SWIM_TAB_INDEX = 3;
	
	/** the datalink tab index of a setup dialog */
	public static final int DATALINK_TAB_INDEX = 4;
	
	/** the manager tab index of a setup dialog */
	public static final int MANAGER_TAB_INDEX = 5;
	
	/** the setup view populated in this setup dialog */
	private SetupView setupView = null;
	
	/**
	 * Constructs a new setup dialog with a specified title, header, icon and
	 * setup model. The setup model represents a modifiable copy of the actual
	 * setup which can be committed or disregarded in the dialog.
	 * 
	 * @param title the title of this setup dialog
	 * @param header the header of this setup dialog
	 * @param icon the icon of this setup dialog
	 * @param setupModel the setup model of this setup dialog
	 * 
	 * @see SetupModel
	 */
	public SetupDialog(String title, String header, String icon, SetupModel setupModel) {
		this.setTitle(title);
		this.setHeaderText(header);
		
		ImageView imageView = new ImageView(icon);
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(50d);
		this.setGraphic(imageView);
		
		this.setupView = new SetupView();
		this.getDialogPane().setContent(this.setupView.getView());
		this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		// add enter key event handler
		this.getDialogPane().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (KeyCode.ENTER == event.getCode()) {
				// focus properties pane to commit all changed properties
				this.getDialogPane().getContent().requestFocus();
			}
		});
		
		// add setup validation event filter
		this.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, event -> {
			PlatformResourceBundleLocator resourceBundleLocator =
					new PlatformResourceBundleLocator(
							com.cfar.swim.worldwind.util.ResourceBundleLoader.DICTIONARY_BUNDLE, null, true);
			Validator validator = Validation.byProvider(HibernateValidator.class)
					.configure()
					.messageInterpolator(new ResourceBundleMessageInterpolator(resourceBundleLocator))
					.buildValidatorFactory()
					.getValidator();
			Set<ConstraintViolation<SetupModel>> violations = validator.validate(setupModel);
			
			if (!violations.isEmpty()) {
				String valueViolations = violations.stream()
						.filter(v -> !(v.getInvalidValue() instanceof Properties<?>))
						.map(v -> "\n" + v.getMessage()
								+ " (" + v.getPropertyPath().toString()
								+ " = " + v.getInvalidValue() + ")")
						.reduce(String::concat).orElse("");
				String classViolations = violations.stream()
						.filter(v -> (v.getInvalidValue() instanceof Properties<?>))
						.map(v -> "\n" + v.getMessage()
								+ " (" + v.getPropertyPath().toString() + ")")
						.reduce(String::concat).orElse("");
				PlannerAlert alert = new PlannerAlert(AlertType.ERROR);
				alert.getDialogPane().setMinWidth(800d);
				alert.getDialogPane().setMinHeight(200d + (violations.size() * 20d));
				alert.setTitle(PlannerAlert.ALERT_TITLE_PROPERTIES_INVALID);
				alert.setHeaderText(PlannerAlert.ALERT_HEADER_PROPERTIES_INVALID);
				alert.setContentText(PlannerAlert.ALERT_CONTENT_PROPERTIES_INVALID
						+ valueViolations + classViolations);
				alert.showAndWait();
				event.consume();
			}
		});
		
		// add result converter
		this.setResultConverter(dialogButton -> {
			Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
			Setup setup = session.getSetup();
			
			if (dialogButton.equals(ButtonType.OK)) {
				String aircraftId = this.setupView.getAircraft().getValue();
				Specification<Aircraft> aircraftSpec = session.getAircraftSpecification(aircraftId);
				aircraftSpec.setProperties(setupModel.getAircraftProperties());
				setup.setAircraftSpecification(aircraftSpec);
				
				String envId = this.setupView.getEnvironment().getValue();
				Specification<Environment> envSpec = session.getEnvironmentSpecification(envId);
				envSpec.setProperties(setupModel.getEnvironmentProperties());
				setup.setEnvironmentSpecification(envSpec);
				
				String plannerId = this.setupView.getPlanner().getValue();
				Specification<Planner> plannerSpec = session.getPlannerSpecification(plannerId);
				plannerSpec.setProperties(setupModel.getPlannerProperties());
				setup.setPlannerSpecification(plannerSpec);
				
				String datalinkId = this.setupView.getDatalink().getValue();
				Specification<Datalink> datalinkSpec = session.getDatalinkSpecification(datalinkId);
				datalinkSpec.setProperties(setupModel.getDatalinkProperties());
				setup.setDatalinkSpecification(datalinkSpec);
				
				String swimConnectionId = this.setupView.getSwimConnection().getValue();
				Specification<SwimConnection> swimConnectionSpec = session.getSwimConnectionSpecification(swimConnectionId);
				swimConnectionSpec.setProperties(setupModel.getSwimConnectionProperties());
				setup.setSwimConnectionSpecification(swimConnectionSpec);
				
				String managerId = this.setupView.getManager().getValue();
				Specification<AutonomicManager> managerSpec = session.getManagerSpecification(managerId);
				managerSpec.setProperties(setupModel.getManagerProperties());
				setup.setManagerSpecification(managerSpec);
			}
			
			return setup;
		});
	
	}
	
	/**
	 * Selects a tab of this setup dialog.
	 * 
	 * @param index the index of the tab to be selected
	 */
	public void selectTab(int index) {	
		this.setupView.getView().getSelectionModel().select(index);
	}
	
}
