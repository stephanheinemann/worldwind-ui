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
package com.cfar.swim.worldwind.ui.planner;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;

import com.cfar.swim.worldwind.ui.environment.EnvironmentView;
import com.cfar.swim.worldwind.ui.plan.PlanView;
import com.cfar.swim.worldwind.ui.scenario.ScenarioView;
import com.cfar.swim.worldwind.ui.swim.SwimView;
import com.cfar.swim.worldwind.ui.threshold.ThresholdView;
import com.cfar.swim.worldwind.ui.time.TimeView;
import com.cfar.swim.worldwind.ui.timer.TimerView;
import com.cfar.swim.worldwind.ui.world.WorldModel;
import com.cfar.swim.worldwind.ui.world.WorldView;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;

/**
 * Realizes a presenter for a planner view which is the main view of the
 * planning application.
 * 
 * @author Stephan Heinemann
 *
 */
public class PlannerPresenter implements Initializable {
	
	/** the world pane of the planner view containing the world view */
	@FXML
	private AnchorPane worldPane;
	
	/** the time accordion of the planner view */
	@FXML
	private Accordion timeAccordion;
	
	/** the SWIM accordion of the planner view */
	@FXML
	private Accordion swimAccordion;
	
	/** the planner accordion of the planner view */
	@FXML
	private Accordion plannerAccordion;
	
	/** the progress bar of the planner view */
	@FXML
	private ProgressBar progressBar;
	
	/** the progress indicator of the planner view */
	@FXML
	private ProgressIndicator progressIndicator;
	
	/** the world model of the world presenter */
	@Inject
	private WorldModel worldModel;
	
	/** the title of the about dialog (planner alert) */
	@Inject
	private String aboutTitle;
	
	/** the header of the about dialog (planner alert) */ 
	@Inject
	private String aboutHeader;
	
	/** the content of the about dialog (planner alert) */
	@Inject
	private String aboutContent;
	
	/**
	 * Initializes this planner presenter.
	 * 
	 * @param location unused
	 * @param resources unused
	 * 
	 * @see Initializable#initialize(URL, ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		WorldView worldView = new WorldView();
		this.worldPane.getChildren().add(worldView.getView());
		AnchorPane.setTopAnchor(worldView.getView(), 0d);
		AnchorPane.setLeftAnchor(worldView.getView(), 0d);
		AnchorPane.setRightAnchor(worldView.getView(), 0d);
		AnchorPane.setBottomAnchor(worldView.getView(), 0d);
		
		TimeView timeView = new TimeView();
		this.timeAccordion.getPanes().add(timeView.getView());
		this.timeAccordion.setExpandedPane(timeView.getView());
		
		TimerView timerView = new TimerView();
		this.timeAccordion.getPanes().add(timerView.getView());
		
		ThresholdView thresholdView = new ThresholdView();
		this.swimAccordion.getPanes().add(thresholdView.getView());
		this.swimAccordion.setExpandedPane(thresholdView.getView());
		
		SwimView swimView = new SwimView();
		this.swimAccordion.getPanes().add(swimView.getView());
		
		ScenarioView scenarioView = new ScenarioView();
		this.plannerAccordion.getPanes().add(scenarioView.getView());
		
		EnvironmentView environmentView = new EnvironmentView();
		this.plannerAccordion.getPanes().add(environmentView.getView());
		
		PlanView planView = new PlanView();
		this.plannerAccordion.getPanes().add(planView.getView());
		this.plannerAccordion.setExpandedPane(planView.getView());
		
		this.progressIndicator.setVisible(false);
		this.worldModel.addWorldModeChangeListener(new ModeChangeListener());
	}
	
	/**
	 * Exits the planner application.
	 */
	public void exit() {
		Platform.exit();
		System.exit(0);
	}
	
	/**
	 * Opens the about dialog of the planner application.
	 */
	public void about() {
		PlannerAlert about = new PlannerAlert(AlertType.INFORMATION);
		about.setTitle(this.aboutTitle);
		about.setHeaderText(this.aboutHeader);
		about.setContentText(this.aboutContent);
		about.showAndWait();
	}
	
	/**
	 * Realizes a world mode change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class ModeChangeListener implements PropertyChangeListener {
		
		/**
		 * Enables and disables the progress bar and indicator if the world
		 * mode changes.
		  * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					switch (worldModel.getWorldMode()) {
					case PLANNING:
					case LOADING:
					case UPLOADING:
					case LAUNCHING:
					case LANDING:
						progressBar.setProgress(-1d);
						progressIndicator.toFront();
						progressIndicator.setVisible(true);
						progressIndicator.setProgress(-1d);
						break;
					default:
						progressBar.setProgress(0d);
						progressIndicator.toBack();
						progressIndicator.setVisible(false);
						progressIndicator.setProgress(0d);
					}
				}
			});
		}
	}
	
}
