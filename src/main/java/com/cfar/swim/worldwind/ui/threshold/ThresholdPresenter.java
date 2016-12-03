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
package com.cfar.swim.worldwind.ui.threshold;

import java.net.URL;
import java.util.ResourceBundle;

import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;

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
	
	/** the threshold cost slider of the threshold view */
	@FXML
	private Slider thresholdSlider;
	
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
		thresholdSlider.setOnMouseReleased(new ThresholdCostInputHandler());
		thresholdSlider.setOnKeyPressed(new ThresholdCostInputHandler());
	}
	
	/**
	 * Realizes a threshold cost input handler.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class ThresholdCostInputHandler implements EventHandler<InputEvent> {
		
		/**
		 * Handles a threshold cost input updating the threshold cost
		 * of the active scenario.
		 * 
		 * @param event the input event associated with the threshold cost input
		 * 
		 * @see EventHandler#handle(javafx.event.Event)
		 */
		@Override
		public void handle(InputEvent event) {
			Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
			session.getActiveScenario().setThreshold(thresholdSlider.getValue());
		}
	}
	
}
