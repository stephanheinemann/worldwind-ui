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
package com.cfar.swim.worldwind.ui.threshold;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.cfar.swim.worldwind.session.Scenario;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.input.InputEvent;

/**
 * Realizes a presenter of a threshold view.
 * 
 * @author Stephan Heinemann
 *
 */
public class ThresholdPresenter implements Initializable {
	
	/** the cost threshold slider of the threshold view */
	@FXML
	private Slider thresholdSlider;
	
	/** the cost threshold change listener of this threshold presenter */
	private final ThresholdChangeListener tcl = new ThresholdChangeListener();
	
	/** the active scenario of this threshold presenter */
	private Scenario scenario = null;
	
	/** the executor of this threshold presenter */
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	/**
	 * Initializes this threshold presenter.
	 * 
	 * @param location unused
	 * @param resources unused
	 * 
	 * @see Initializable#initialize(URL, ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		thresholdSlider.setOnMouseReleased(new ThresholdInputHandler());
		thresholdSlider.setOnKeyPressed(new ThresholdInputHandler());
		
		Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
		session.addActiveScenarioChangeListener(new ActiveScenarioChangeListener());
		
		this.initScenario();
		this.initThreshold();
	}
	
	/**
	 * Initializes the scenario of this threshold presenter.
	 */
	public void initScenario() {
		if (null != this.scenario) {
			this.scenario.removePropertyChangeListener(this.tcl);
		}
		this.scenario = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).getActiveScenario();
		this.scenario.addThresholdChangeListener(this.tcl);
	}
	
	/**
	 * Initializes the cost threshold of this threshold presenter.
	 */
	public void initThreshold() {
		double threshold = scenario.getThreshold();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				thresholdSlider.setValue(threshold);
				thresholdSlider.layout();
			}
		});
	}
	
	/**
	 * Realizes a cost threshold input handler.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class ThresholdInputHandler implements EventHandler<InputEvent> {
		
		/**
		 * Handles a cost threshold input updating the cost threshold of the
		 * active scenario.
		 * 
		 * @param event the input event associated with the cost threshold input
		 * 
		 * @see EventHandler#handle(javafx.event.Event)
		 */
		@Override
		public void handle(InputEvent event) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
					session.getActiveScenario().setThreshold(thresholdSlider.getValue());
				}
			});
		}
	}
	
	/**
	 * Realizes a cost threshold change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class ThresholdChangeListener implements PropertyChangeListener {
		
		/**
		 * Initializes the cost threshold if the cost threshold changes.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initThreshold();
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
		 * Initializes the scenario and cost threshold if the active scenario changes.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initScenario();
			initThreshold();
		}
	}
	
}
