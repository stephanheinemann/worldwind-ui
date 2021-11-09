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
package com.cfar.swim.worldwind.ui.scenario;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.cfar.swim.worldwind.jaxb.ScenarioMarshaller;
import com.cfar.swim.worldwind.jaxb.ScenarioUnmarshaller;
import com.cfar.swim.worldwind.session.Scenario;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.StringConverter;

/**
 * Realizes a presenter of a scenario view.
 * 
 * @author Stephan Heinemann
 *
 */
public class ScenarioPresenter implements Initializable {
	
	// TODO: include new scenario id into dictionary
	// TODO: move all visible UI text into properties files
	
	/** the file chooser open scenario file title */
	public static final String FILE_CHOOSER_TITLE_SCENARIO = "Open Scenario File";
	
	/** the file chooser scenario file description */
	public static final String FILE_CHOOSER_SCENARIO = "Scenario Files";
	
	/** the file chooser scenario file extension */
	public static final String FILE_CHOOSER_EXTENSION_SCENARIO = "*.xml";
	
	/** the list of scenarios of the scenario view */
	@FXML
	private ListView<Scenario> scenarios;
	
	/** the executor of this scenario presenter */
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	/**
	 * Initializes this scenario presenter.
	 * 
	 * @param location unused
	 * @param resources unused
	 * 
	 * @see Initializable#initialize(URL, ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
		session.addScenariosChangeListener(new ScenariosChangeListener());
		session.addActiveScenarioChangeListener(new ActiveScenarioChangeListener());
		this.scenarios.setCellFactory(listView -> new ScenarioListCell());
		this.scenarios.getItems().add(session.getActiveScenario());
	}
	
	/**
	 * Activates a selected scenario of the scenario view.
	 */
	public void activateScenario() {
		if (!this.scenarios.isEditable()) {
			Scenario scenario = this.scenarios.getSelectionModel().getSelectedItem();
			if (null != scenario) {
				SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).setActiveScenario(scenario);
			}
		}
	}
	
	/**
	 * Adds a scenario to the scenario view.
	 */
	public void addScenario() {
		if (!this.scenarios.isEditable()) {
			Scenario scenario = new Scenario("New Scenario");
			this.scenarios.getItems().add(scenario);
			this.scenarios.layout(); // without layout, edit will not work
			this.scenarios.setEditable(true);
			this.scenarios.scrollTo(scenarios.getItems().indexOf(scenario));
			this.scenarios.edit(scenarios.getItems().indexOf(scenario));
		}
	}
	
	/**
	 * Removes a scenario from the scenario view.
	 */
	public void removeScenario() {
		if (!this.scenarios.isEditable()) {
			Scenario scenario = this.scenarios.getSelectionModel().getSelectedItem();
			if (null != scenario) {
				SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).removeScenario(scenario);
			}
		}
	}
	
	/**
	 * Removes all scenarios from the scenario view.
	 */
	public void clearScenarios() {
		if (!this.scenarios.isEditable()) {
			SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).clearScenarios();
		}
	}
	
	/**
	 * Loads a scenario.
	 */
	public void loadScenario() {
		if (!this.scenarios.isEditable()) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					FileChooser fileChooser = new FileChooser();
					fileChooser.setTitle(ScenarioPresenter.FILE_CHOOSER_TITLE_SCENARIO);
					fileChooser.getExtensionFilters().addAll(
							new ExtensionFilter[] { new ExtensionFilter(
									ScenarioPresenter.FILE_CHOOSER_SCENARIO,
									ScenarioPresenter.FILE_CHOOSER_EXTENSION_SCENARIO)});
					File file = fileChooser.showOpenDialog(null);
					
					if (null != file) {
						executor.execute(new Runnable() {
							@Override
							public void run() {
								try {
									ScenarioUnmarshaller marshaller = new ScenarioUnmarshaller();
									Scenario scenario = marshaller.unmarshalScenario(file);
									if ((null != scenario)) {
										SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).addScenario(scenario);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						});
					}
				}
			});
		}
	}
	
	/**
	 * Saves a scenario.
	 */
	public void saveScenario() {
		if (!this.scenarios.isEditable()) {
			Scenario scenario = this.scenarios.getSelectionModel().getSelectedItem();
			if (null != scenario) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						FileChooser fileChooser = new FileChooser();
						fileChooser.setTitle(ScenarioPresenter.FILE_CHOOSER_TITLE_SCENARIO);
						fileChooser.getExtensionFilters().addAll(
								new ExtensionFilter[] { new ExtensionFilter(
										ScenarioPresenter.FILE_CHOOSER_SCENARIO,
										ScenarioPresenter.FILE_CHOOSER_EXTENSION_SCENARIO)});
						File file = fileChooser.showSaveDialog(null);
						
						if (null != file) {
							executor.execute(new Runnable() {
								@Override
								public void run() {
									try {
										ScenarioMarshaller marshaller = new ScenarioMarshaller();
										marshaller.marshalScenario(scenario, file);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
						}
					}
				});
			}
		}
	}
	
	/**
	 * Realizes a scenario converter to populate the scenario view.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class ScenarioConverter extends StringConverter<Scenario> {
		
		/**
		 * Converts a scenario to a string representation.
		 * 
		 * @param scenario the scenario
		 * 
		 * @return the scenario identifier
		 * 
		 * @see StringConverter#toString()
		 */
		@Override
		public String toString(Scenario scenario) {
			String scenarioId = null;
			if (null != scenario) {
				scenarioId = scenario.getId();
			}
			return scenarioId;
		}
		
		/**
		 * Converts a string representation to a scenario.
		 * 
		 * @param scenarioId the scenario identifier
		 * 
		 * @return a new scenario with the scenario identifier
		 * 
		 * @see StringConverter#fromString(String)
		 */
		@Override
		public Scenario fromString(String scenarioId) {
			Scenario scenario = null;
			if (null != scenarioId) {
				scenario = new Scenario(scenarioId);
			}
			return scenario;
		}
	}
	
	/**
	 * Realizes a scenario text field list cell.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class ScenarioListCell extends TextFieldListCell<Scenario> {
		
		/**
		 * Creates a new scenario list cell.
		 */
		public ScenarioListCell() {
			super();
			this.setConverter(new ScenarioConverter());
		}
		
		/**
		 * Updates a scenario list cell item and highlights the active scenario.
		 * 
		 * @param scenario the scenario
		 * @param empty indicates an empty update
		 * 
		 * @see TextFieldListCell#updateItem(Object, boolean)
		 */
		@Override
		public void updateItem(Scenario scenario, boolean empty) {
			super.updateItem(scenario, empty);
			
			if (!empty && (null != scenario)) {
				String family = this.getFont().getFamily();
				double size = this.getFont().getSize();
				if (scenario.isEnabled()) {
					this.setFont(Font.font(family, FontWeight.BOLD, size));
				} else {
					this.setFont(Font.font(family, FontWeight.NORMAL, size));
				}
			}
		}
		
		/**
		 * Commits a modified scenario if valid.
		 * 
		 * @param scenario the scenario to be committed
		 * 
		 * @see TextFieldListCell#commitEdit(Object)
		 */
		@Override
		public void commitEdit(Scenario scenario) {
			if (!scenarios.getItems().contains(scenario) && !scenario.getId().trim().isEmpty()) {
				super.commitEdit(scenario);
				scenarios.setEditable(false);
				SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).addScenario(scenario);
			}
		}
		
		/**
		 * Cancels the modification of a scenario.
		 * 
		 * @see TextFieldListCell#cancelEdit()
		 */
		@Override
		public void cancelEdit() {
			super.cancelEdit();
			scenarios.getItems().remove(scenarios.getItems().size() - 1);
			scenarios.layout();
			scenarios.setEditable(false);
		}
	}
	
	/**
	 * Realizes a scenarios change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class ScenariosChangeListener implements PropertyChangeListener {
		
		/**
		 * Updates the scenario view if the scenarios change.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			Platform.runLater(new Runnable() {
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					scenarios.getItems().clear();
					for (Scenario scenario : (Iterable<Scenario>) evt.getNewValue()) {
						if (scenario.getId().equals(Scenario.DEFAULT_SCENARIO_ID)) {
							scenarios.getItems().add(0, scenario);
						} else {
							scenarios.getItems().add(scenario);
						}
					}
					scenarios.refresh();
				}
			});
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
		 * Updates the scenario view if the active scenario changes.
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
					scenarios.refresh();
				}
			});
		}
	}
	
}
