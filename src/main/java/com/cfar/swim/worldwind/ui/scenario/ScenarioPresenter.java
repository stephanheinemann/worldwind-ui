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
package com.cfar.swim.worldwind.ui.scenario;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ResourceBundle;

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
import javafx.util.StringConverter;

public class ScenarioPresenter implements Initializable {

	@FXML
	ListView<Scenario> scenarios;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
		session.addScenariosChangeListener(new ScenariosChangeListener());
		session.addActiveScenarioChangeListener(new ActiveScenarioChangeListener());
		this.scenarios.setCellFactory(listView -> new ScenarioListCell());
		this.scenarios.getItems().add(session.getActiveScenario());
	}

	public void activateScenario() {
		if (!this.scenarios.isEditable()) {
			Scenario scenario = this.scenarios.getSelectionModel().getSelectedItem();
			if (null != scenario) {
				SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).setActiveScenario(scenario);
			}
		}
	}
	
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
	
	public void removeScenario() {
		if (!this.scenarios.isEditable()) {
			Scenario scenario = this.scenarios.getSelectionModel().getSelectedItem();
			if (null != scenario) {
				SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).removeScenario(scenario);
			}
		}
	}
	
	public void clearScenarios() {
		if (!this.scenarios.isEditable()) {
			SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).clearScenarios();
		}
	}
	
	private class ScenarioConverter extends StringConverter<Scenario> {

		@Override
		public String toString(Scenario scenario) {
			String scenarioId = null;
			if (null != scenario) {
				scenarioId = scenario.getId();
			}
			return scenarioId;
		}

		@Override
		public Scenario fromString(String scenarioId) {
			Scenario scenario = null;
			if (null != scenarioId) {
				scenario = new Scenario(scenarioId);
			}
			return scenario;
		}
	}
	
	private class ScenarioListCell extends TextFieldListCell<Scenario> {
		
		public ScenarioListCell() {
			super();
			this.setConverter(new ScenarioConverter());
		}
		
		@Override
		public void updateItem(Scenario scenario, boolean empty) {
			super.updateItem(scenario, empty);
			
			if (null != scenario) {
				String family = this.getFont().getFamily();
				double size = this.getFont().getSize();
				if (scenario.isEnabled()) {
					this.setFont(Font.font(family, FontWeight.BOLD, size));
				} else {
					this.setFont(Font.font(family, FontWeight.NORMAL, size));
				}
			}
		}
		
		@Override
		public void commitEdit(Scenario scenario) {
			if (!scenarios.getItems().contains(scenario) && !scenario.getId().trim().isEmpty()) {
				super.commitEdit(scenario);
				scenarios.setEditable(false);
				SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).addScenario(scenario);
			}
		}
		
		@Override
		public void cancelEdit() {
			super.cancelEdit();
			scenarios.getItems().remove(scenarios.getItems().size() - 1);
			scenarios.layout();
			scenarios.setEditable(false);
		}
	}
	
	private class ScenariosChangeListener implements PropertyChangeListener {
		
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
	
	private class ActiveScenarioChangeListener implements PropertyChangeListener {

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
