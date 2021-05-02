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
package com.cfar.swim.worldwind.ui.swim;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.xml.sax.InputSource;

import com.cfar.swim.worldwind.iwxxm.IwxxmLoader;
import com.cfar.swim.worldwind.render.Obstacle;
import com.cfar.swim.worldwind.session.Scenario;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Realizes a presenter of a swim view.
 * 
 * @author Stephan Heinemann
 *
 */
public class SwimPresenter implements Initializable {
	
	// TODO: consider to move all visible UI text into properties files
	
	/** the file chooser open swim file title */
	public static final String FILE_CHOOSER_TITLE_SWIM = "Open SWIM File";
	
	/** the file chooser swim file description */
	public static final String FILE_CHOOSER_SWIM = "SWIM Files";
	
	/** the file chooser swim file extension */
	public static final String FILE_CHOOSER_EXTENSION_SWIM = "*.xml";
	
	/** the swim list of the swim view */
	@FXML
	private ListView<String> swimList;
	
	/** the active scenario of this swim presenter */
	private Scenario scenario = null;
	
	/** the obstacle change listener of this swim presenter */
	private ObstaclesChangeListener ocl = new ObstaclesChangeListener();
	
	/** the executor of this swim presenter */
	private final Executor executor = Executors.newSingleThreadScheduledExecutor();
	
	/**
	 * Initializes this swim presenter.
	 * 
	 * @param location unused
	 * @param resources unused
	 * 
	 * @see Initializable#initialize(URL, ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
		session.addActiveScenarioChangeListener(new ActiveScenarioChangeListener());
		this.initScenario();
		this.initObstacles();
	}
	
	/**
	 * Initializes the scenario of this swim presenter.
	 */
	private void initScenario() {
		if (null != this.scenario) {
			this.scenario.removePropertyChangeListener(this.ocl);
		}
		Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
		this.scenario = session.getActiveScenario();
		this.scenario.addObstaclesChangeListener(this.ocl);
	}
	
	/**
	 * Initializes the obstacles of this swim presenter.
	 */
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
	
	/**
	 * Adds a swim item to the swim view.
	 */
	public void addSwimItem() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle(SwimPresenter.FILE_CHOOSER_TITLE_SWIM);
				fileChooser.getExtensionFilters().addAll(
						new ExtensionFilter[] { new ExtensionFilter(
								SwimPresenter.FILE_CHOOSER_SWIM,
								SwimPresenter.FILE_CHOOSER_EXTENSION_SWIM)});
				File file = fileChooser.showOpenDialog(null);
				
				if (null != file) {
					// TODO: this and other concurrent scenario modifications
					// need to be controlled by a scenario executor
					// TODO: status and progress bar available to all presenters
					executor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								//setWorldMode(WorldMode.LOADING);
								// TODO: generic SWIM loader
								IwxxmLoader loader = new IwxxmLoader();
								Set<Obstacle> obstacles = loader.load(new InputSource(new FileInputStream(file)));
								for (Obstacle obstacle : obstacles) {
									scenario.addObstacle(obstacle);
								}
								//setWorldMode(WorldMode.VIEW);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
			}
		});
	}
	
	/**
	 * Removes a swim item from the swim view.
	 */
	public void removeSwimItem() {
		String swimId = swimList.getSelectionModel().getSelectedItem();
		if (null != swimId) {
			scenario.removeObstacles(swimId);
			swimList.getItems().remove(swimId);
			swimList.refresh();
		}
	}
	
	/**
	 * Removes all swim items from the swim view.
	 */
	public void clearSwimItems() {
		scenario.clearObstacles();
		swimList.getItems().clear();
		swimList.refresh();
	}
	
	/**
	 * Enables a selected swim item of the swim view.
	 */
	public void enableSwimItem() {
		String swimId = swimList.getSelectionModel().getSelectedItem();
		if (null != swimId) {
			scenario.enableObstacles(swimId);
		}
	}
	
	/**
	 * Disables a selected swim item of the swim view.
	 */
	public void disableSwimItem() {
		String swimId = swimList.getSelectionModel().getSelectedItem();
		if (null != swimId) {
			scenario.disableObstacles(swimId);
		}
	}
	
	/**
	 * Realizes an obstacle change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class ObstaclesChangeListener implements PropertyChangeListener {
		
		/**
		 * Initializes the obstacles if they have changed.
		 * 
		 * @param evt the property change event associated with the obstacle change
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initObstacles();
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
		 * Initializes the scenario and obstacles if the active scenario has changed.
		 * 
		 * @param evt the property change event associate with the active scenario change
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initScenario();
			initObstacles();
		}
	}
	
}
