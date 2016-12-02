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

public class EnvironmentPresenter implements Initializable {

	@FXML
	private TreeView<Environment> environment;
	
	Scenario scenario = null;
	private EnvironmentChangeListener ecl = new EnvironmentChangeListener();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.environment.setCellFactory(TextFieldTreeCell.forTreeView(new EnvironmentConverter()));
		Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
		session.addActiveScenarioChangeListener(new ActiveScenarioChangeListener());
		initScenario();
		initEnvironment();
	}
	
	public void initScenario() {
		// remove change listeners from the previous scenario if any
		if (null != this.scenario) {
			this.scenario.removePropertyChangeListener(this.ecl);
		}
		this.scenario = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).getActiveScenario();
		this.scenario.addEnvironmentChangeListener(this.ecl);
	}
	
	public void initEnvironment() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Environment activeEnvironment = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).getActiveScenario().getEnvironment();
				environment.setRoot(new TreeItem<Environment>(activeEnvironment));
				environment.getRoot().setExpanded(false);
				initEnvironment(environment.getRoot());
			}
		});
	}
	
	public void initEnvironment(TreeItem<Environment> parentItem) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (parentItem.getValue().isRefined()) {
					for (Environment child : parentItem.getValue().getRefinements()) {
						TreeItem<Environment> childItem = new TreeItem<>(child);
						parentItem.getChildren().add(childItem);
						initEnvironment(childItem);
					}
				}
			}
		});
	}
	
	public void refineEnvironment() {
		Environment selectedEnv = this.environment.getSelectionModel().getSelectedItem().getValue();
		if (!selectedEnv.isRefined()) {
			Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
			selectedEnv.refine(2);
			session.getActiveScenario().notifyEnvironmentChange();
		}
	}
	
	public void coarsenEnvironment() {
		Environment selectedEnv = this.environment.getSelectionModel().getSelectedItem().getValue();
		if (selectedEnv.isRefined()) {
			Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
			selectedEnv.coarsen();
			session.getActiveScenario().notifyEnvironmentChange();
		}
	}
	
	private class EnvironmentConverter extends StringConverter<Environment> {

		@Override
		public String toString(Environment environment) {
			return Integer.toString(environment.getRefinements().size());
		}

		@Override
		public Environment fromString(String environment) {
			return null;
		}
	}
	
	private class ActiveScenarioChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initScenario();
			initEnvironment();
		}
	}
	
	private class EnvironmentChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initEnvironment();
		}
	}

}
