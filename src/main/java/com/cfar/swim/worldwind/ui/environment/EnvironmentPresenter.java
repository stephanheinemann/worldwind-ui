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
package com.cfar.swim.worldwind.ui.environment;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ResourceBundle;

import com.cfar.swim.worldwind.planning.Environment;
import com.cfar.swim.worldwind.planning.PlanningContinuum;
import com.cfar.swim.worldwind.planning.PlanningGrid;
import com.cfar.swim.worldwind.planning.PlanningRoadmap;
import com.cfar.swim.worldwind.planning.SamplingEnvironment;
import com.cfar.swim.worldwind.session.Scenario;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.util.StringConverter;

/**
 * Realizes a presenter for an environment view.
 * 
 * @author Stephan Heinemann
 *
 */
public class EnvironmentPresenter implements Initializable {

	/** the environment tree view of this environment presenter */
	@FXML
	private TreeView<Environment> environment;

	/** the active planning scenario (model) of this environment presenter */
	Scenario scenario = null;

	/** the environment change listener of this environment presenter */
	private final EnvironmentChangeListener ecl = new EnvironmentChangeListener();

	/**
	 * Initializes this environment presenter.
	 * 
	 * @param location unused
	 * @param resources unused
	 * 
	 * @see Initializable#initialize(URL, ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.environment.setCellFactory(TextFieldTreeCell.forTreeView(new EnvironmentConverter()));
		Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
		session.addActiveScenarioChangeListener(new ActiveScenarioChangeListener());
		initScenario();
		initEnvironment();
	}

	/**
	 * Initializes the active scenario of this environment presenter registering an
	 * environment change listener.
	 */
	public void initScenario() {
		// remove change listeners from the previous scenario if any
		if (null != this.scenario) {
			this.scenario.removePropertyChangeListener(this.ecl);
		}
		this.scenario = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).getActiveScenario();
		this.scenario.addEnvironmentChangeListener(this.ecl);
	}

	/**
	 * Initializes the environment of this environment presenter populating the
	 * environment view according to the active scenario.
	 */
	public void initEnvironment() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Environment activeEnvironment = SessionManager.getInstance()
						.getSession(WorldwindPlanner.APPLICATION_TITLE).getActiveScenario().getEnvironment();
				environment.setRoot(new TreeItem<Environment>(activeEnvironment));
				environment.getRoot().setExpanded(false);
				initEnvironment(environment.getRoot());
			}
		});
	}

	/**
	 * Initializes the environment of this environment presenter populating the
	 * environment view recursively according to the active scenario.
	 * 
	 * @param parentItem the parent environment item to be populated
	 */
	public void initEnvironment(TreeItem<Environment> parentItem) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (parentItem.getValue() instanceof PlanningGrid) {
					PlanningGrid planningGrid = (PlanningGrid) parentItem.getValue();
					if (planningGrid.isRefined()) {
						for (Environment child : planningGrid.getRefinements()) {
							TreeItem<Environment> childItem = new TreeItem<>(child);
							parentItem.getChildren().add(childItem);
							initEnvironment(childItem);
						}
					}
				} else if (parentItem.getValue() instanceof SamplingEnvironment) {
					SamplingEnvironment samplingEnvironment = (SamplingEnvironment) parentItem.getValue();
					if (samplingEnvironment.isRefined()) {
						for (Environment child : samplingEnvironment.getRefinements()) {
							TreeItem<Environment> childItem = new TreeItem<>(child);
							parentItem.getChildren().add(childItem);
							initEnvironment(childItem);
						}
					}
				}
			}
		});
	}

	/**
	 * Refines a selected environment in the environment view to a higher resolution
	 * environment.
	 */
	public void refineEnvironment() {
		Environment selectedEnv = this.environment.getSelectionModel().getSelectedItem().getValue();
		if (selectedEnv instanceof PlanningGrid) {
			PlanningGrid planningGrid = (PlanningGrid) selectedEnv;
			if (!planningGrid.isRefined()) {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				planningGrid.refine(2);
				session.getActiveScenario().notifyEnvironmentChange();
			}
		} else if (selectedEnv instanceof PlanningContinuum) {
			PlanningContinuum planningContinuum = (PlanningContinuum) selectedEnv;
			if(!planningContinuum.isRefined()) {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				planningContinuum.refine(50);
				session.getActiveScenario().notifyEnvironmentChange();
			}
		} else if (selectedEnv instanceof PlanningRoadmap) {
			PlanningRoadmap planningRoadmap = (PlanningRoadmap) selectedEnv;
			Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
			planningRoadmap.refine(50);
			session.getActiveScenario().notifyEnvironmentChange();
		}
	}

	/**
	 * Coarsens a selected environment in the environment view to a lower resolution
	 * environment.
	 */
	public void coarsenEnvironment() {
		Environment selectedEnv = this.environment.getSelectionModel().getSelectedItem().getValue();
		if (selectedEnv instanceof PlanningGrid) {
			PlanningGrid planningGrid = (PlanningGrid) selectedEnv;
			if (planningGrid.isRefined()) {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				planningGrid.coarsen();
				session.getActiveScenario().notifyEnvironmentChange();
			}
		} else if (selectedEnv instanceof PlanningContinuum) {
			PlanningContinuum planningContinuum = (PlanningContinuum) selectedEnv;
			Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
			planningContinuum.coarsen();
			session.getActiveScenario().notifyEnvironmentChange();
		} else if (selectedEnv instanceof PlanningRoadmap) {
			PlanningRoadmap planningRoadmap = (PlanningRoadmap) selectedEnv;
			Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
			planningRoadmap.coarsen();
			session.getActiveScenario().notifyEnvironmentChange();
		}
		
	}

	/**
	 * Realizes an environment converter to populate the environment view.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class EnvironmentConverter extends StringConverter<Environment> {

		/**
		 * Converts an environment to a string representation.
		 * 
		 * @param environment the environment
		 * 
		 * @return the refinement degree string representation of the environment
		 * 
		 * @see StringConverter#toString()
		 */
		@Override
		public String toString(Environment environment) {
			// TODO: Review for planningContinuum
			if (environment instanceof PlanningGrid) {
				PlanningGrid planningGrid = (PlanningGrid) environment;
				return Integer.toString(planningGrid.getRefinements().size());
			} else if (environment instanceof PlanningContinuum) {
				PlanningContinuum planningContinuum = (PlanningContinuum) environment;
				String str = String.format("Diagonal: %.2f\nResolution: %.2f", planningContinuum.getDiameter(),
						planningContinuum.getResolution());
				return str;
			} else if (environment instanceof PlanningRoadmap) {
				PlanningRoadmap planningRoadmap = (PlanningRoadmap) environment;
				return Integer.toString(planningRoadmap.getRefinements().size());
			} else if (environment instanceof SamplingEnvironment) {
				SamplingEnvironment samplingEnvironment = (SamplingEnvironment) environment;
				return Integer.toString(samplingEnvironment.getRefinements().size());
			} else {
				return "TODO";
			}
			
		}

		/**
		 * Converts a string representation to an environment.
		 * 
		 * @param environment the string represenation of the environment
		 * 
		 * @return null (read-only environment view)
		 * 
		 * @see StringConverter#fromString(String)
		 */
		@Override
		public Environment fromString(String environment) {
			return null;
		}
	}

	/**
	 * Realizes an active scenario change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class ActiveScenarioChangeListener implements PropertyChangeListener {

		/**
		 * Initializes the scenario and environment if the active scenario changes.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initScenario();
			initEnvironment();
		}
	}

	/**
	 * Realizes an environment change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class EnvironmentChangeListener implements PropertyChangeListener {

		/**
		 * Initializes the environment if the environment changes.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initEnvironment();
		}
	}

}
