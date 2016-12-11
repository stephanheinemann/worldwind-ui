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
package com.cfar.swim.worldwind.ui.timer;

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
import javafx.scene.control.Label;

/**
 * Realizes a presenter of a timer view.
 * 
 * @author Stephan Heinemann
 *
 */
public class TimerPresenter implements Initializable {

	/** the time label of the timer view */
	@FXML
	private Label time;
	
	/** the time change listener of this timer presenter */
	private final TimeChangeListener tcl = new TimeChangeListener();
	
	/** the active scenario of this timer presenter */
	private Scenario scenario = null;
	
	/**
	 * Initializes this timer presenter.
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
		this.initTime();
	}
	
	/**
	 * Initializes the scenario of this timer presenter.
	 */
	public void initScenario() {
		if (null != this.scenario) {
			this.scenario.removePropertyChangeListener(this.tcl);
		}
		this.scenario = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).getActiveScenario();
		this.scenario.addTimeChangeListener(this.tcl);
	}
	
	/**
	 * Initializes the time of this timer presenter.
	 */
	public void initTime() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				time.setText(scenario.getTime().toString());
				time.layout();
			}
		});
	}
	
	/**
	 * Rewinds the time of the active scenario of this timer presenter.
	 */
	public void rewind() {
		this.scenario.rewindTime();
	}
	
	/**
	 * Stops the time of the active scenario of this timer presenter.
	 */
	public void stop() {
		this.scenario.stopTime();
	}
	
	/**
	 * Tracks the time of the active scenario of this timer presenter.
	 */
	public void track() {
		this.scenario.trackTime();
	}
	
	/**
	 * Plays the time of the active scenario of this timer presenter.
	 */
	public void play() {
		this.scenario.playTime();
	}
	
	/**
	 * Fast forwards the time of the active scenario of this timer presenter.
	 */
	public void fastForward() {
		this.scenario.fastForwardTime();
	}
	
	/**
	 * Realizes a time change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class TimeChangeListener implements PropertyChangeListener {
		
		/**
		 * Initializes the timer if the time changes.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initTime();
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
		 * Initializes the scenario and time if the active scenario changes.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initScenario();
			initTime();
		}
	}
	
}
