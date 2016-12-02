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
