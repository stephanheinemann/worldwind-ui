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
package com.cfar.swim.worldwind.ui.time;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;

import com.cfar.swim.worldwind.session.Scenario;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import jfxtras.scene.control.LocalDateTimePicker;

/**
 * Realizes a presenter of a time view.
 * 
 * @author Stephan Heinemann
 *
 */
public class TimePresenter implements Initializable {
	
	/** the time pane of the time view */
	@FXML
	private AnchorPane timePane;
	
	/** the date and time picker of this time presenter */
	private LocalDateTimePicker picker;
	
	/** the time change listener of this time presenter */
	private final TimeChangeListener tcl = new TimeChangeListener();
	
	/** the active scenario of this time presenter */
	private Scenario scenario = null;
	
	/**
	 * Initializes this time presenter.
	 * 
	 * @param location unused
	 * @param resources unused
	 * 
	 * @see Initializable#initialize(URL, ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.picker = new LocalDateTimePicker(LocalDateTime.now(ZoneId.of("UTC")));
		this.timePane.getChildren().add(this.picker);
		this.picker.setValueValidationCallback(new PlanningTimeCallback());
		this.picker.addEventFilter(KeyEvent.KEY_PRESSED, new PlanningTimeKeyHandler());
		AnchorPane.setTopAnchor(this.picker, 5d);
		AnchorPane.setLeftAnchor(this.picker, 5d);
		AnchorPane.setRightAnchor(this.picker, 5d);
		AnchorPane.setBottomAnchor(this.picker, 5d);
		
		Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
		session.addActiveScenarioChangeListener(new ActiveScenarioChangeListener());
		this.initScenario();
		this.initTime();
	}
	
	/**
	 * Initializes the scenario of this time presenter.
	 */
	public void initScenario() {
		if (null != this.scenario) {
			this.scenario.removePropertyChangeListener(this.tcl);
		}
		this.scenario = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).getActiveScenario();
		this.scenario.addTimeChangeListener(this.tcl);
	}
	
	/**
	 * Initializes the time of this time presenter.
	 */
	public void initTime() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				picker.setLocalDateTime(scenario.getTime().toLocalDateTime());
				picker.layout();
			}
		});
	}
	
	/**
	 * Realizes a planning time callback.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class PlanningTimeCallback implements Callback<LocalDateTime, Boolean> {
		
		/**
		 * Sets the time of the active scenario.
		 * 
		 * @param localDateTime the date and time to be set
		 * 
		 * @see Callback#call(Object)
		 */
		@Override
		public Boolean call(LocalDateTime localDateTime) {
			if ((null != localDateTime) && !scenario.isTimed()) {
				scenario.setTime(ZonedDateTime.of(localDateTime, ZoneId.of("UTC")));
			}
			return true;
		}
	}
	
	/**
	 * Realizes a planning time key handler.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class PlanningTimeKeyHandler implements EventHandler<KeyEvent> {

		/** the maximum planning time step duration */
		private final Duration DURATION_MAX = Duration.ofHours(24);
		
		/** the current planning time step duration */
		private Duration duration = Duration.ofMinutes(10);
		
		/**
		 * Handles the key events to change the current planning time and
		 * planning time step duration.
		 * 
		 * @param event the key event associated with the date and time picker
		 * 
		 * @see EventHandler#handle(javafx.event.Event)
		 */
		@Override
		public void handle(KeyEvent event) {
			if (KeyCode.RIGHT.equals(event.getCode())) {
				if (event.isControlDown()) {
					// increase time step
					if (0 > this.duration.compareTo(DURATION_MAX)) {
						this.duration = this.duration.plusMinutes(1);
					}
				} else {
					// obtain increased time
					picker.setLocalDateTime(picker.getLocalDateTime().plus(this.duration));
					picker.getValueValidationCallback().call(picker.getLocalDateTime());
				}
				event.consume();
			} else if (KeyCode.LEFT.equals(event.getCode())) {
				if (event.isControlDown()) {
					// decrease time step
					if (0 < this.duration.compareTo(Duration.ZERO)) {
						this.duration = this.duration.minusMinutes(1);
					}
				} else {
					// obtain decreased time
					picker.setLocalDateTime(picker.getLocalDateTime().minus(this.duration));
					picker.getValueValidationCallback().call(picker.getLocalDateTime());
				}
				event.consume();
			}
		}
	}
	
	/**
	 * Realizes a time change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class TimeChangeListener implements PropertyChangeListener {
		
		/**
		 * Initializes the time if the time changes.
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
