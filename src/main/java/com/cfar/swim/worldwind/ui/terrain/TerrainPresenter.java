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
package com.cfar.swim.worldwind.ui.terrain;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import com.cfar.swim.worldwind.session.Scenario;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;
import com.cfar.swim.worldwind.ui.world.WorldModel;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Realizes a presenter of a terrain view.
 * 
 * @author Stephan Heinemann
 *
 */
public class TerrainPresenter implements Initializable {
	
	// TODO: consider to move all visible UI text into properties files
	
	/** the file chooser open terrain file title */
	public static final String FILE_CHOOSER_TITLE_TERRAIN = "Open Terrain File";
	
	/** the file chooser terrain file description */
	public static final String FILE_CHOOSER_TERRAIN = "Terrain Files";
	
	/** the file chooser terrain file extension */
	public static final String FILE_CHOOSER_EXTENSION_TERRAIN = "*.tif";
	
	/** the terrain list of the terrain view */
	@FXML
	private ListView<String> terrainList;
	
	/** the world model of this terrain presenter */
	@Inject
	private WorldModel worldModel;
	
	/** the active scenario of this terrain presenter */
	private Scenario scenario = null;
	
	/** the terrain change listener of this terrain presenter */
	private TerrainChangeListener tcl = new TerrainChangeListener();
	
	/** the executor of this terrain presenter */
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	/**
	 * Initializes this terrain presenter.
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
		this.initTerrain();
	}
	
	/**
	 * Initializes the scenario of this terrain presenter.
	 */
	private void initScenario() {
		if (null != this.scenario) {
			this.scenario.removePropertyChangeListener(this.tcl);
		}
		Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
		this.scenario = session.getActiveScenario();
		this.scenario.addTerrainChangeListener(this.tcl);
	}
	
	/**
	 * Initializes the terrain of this terrain presenter.
	 */
	private void initTerrain() {
		Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
		Set<String> terrainNames = session.getActiveScenario().getTerrainNames();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				terrainList.getItems().clear();
				terrainList.getItems().addAll(terrainNames);
				terrainList.refresh();
			}
		});
	}
	
	/**
	 * Adds a terrain item to the terrain view.
	 */
	public void addTerrainItem() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(TerrainPresenter.FILE_CHOOSER_TITLE_TERRAIN);
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter[] { new ExtensionFilter(
						TerrainPresenter.FILE_CHOOSER_TERRAIN,
						TerrainPresenter.FILE_CHOOSER_EXTENSION_TERRAIN)});
		File file = fileChooser.showOpenDialog(null);
		
		if (null != file) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					if (worldModel.load()) {
						scenario.addTerrain(file);
						worldModel.loaded();
					}
				}
			});
		}
	}
	
	/**
	 * Removes a terrain item from the terrain view.
	 */
	public void removeTerrainItem() {
		String terrain = terrainList.getSelectionModel().getSelectedItem();
		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				if (null != terrain) {
					scenario.removeTerrain(terrain);
				}
			}
		});
	}
	
	/**
	 * Removes all terrain items from the terrain view.
	 */
	public void clearTerrainItems() {
		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				scenario.clearTerrain();
			}
		});
	}
	
	/**
	 * Realizes a terrain change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class TerrainChangeListener implements PropertyChangeListener {
		
		/**
		 * Initializes the terrain if it has changed.
		 * 
		 * @param evt the property change event associated with the obstacle change
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initTerrain();
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
		 * Initializes the scenario and terrain if the active scenario has changed.
		 * 
		 * @param evt the property change event associate with the active scenario change
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initScenario();
			initTerrain();
		}
	}
	
}
