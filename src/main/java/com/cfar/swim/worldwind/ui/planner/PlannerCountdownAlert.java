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
package com.cfar.swim.worldwind.ui.planner;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogEvent;
import javafx.util.Duration;

/**
 * Realizes a planner countdown alert that can be confirmed as long as the
 * countdown is running and is automatically cancelled the once the countdown
 * has finished.
 * 
 * @author Stephan Heinemann
 *
 */
public class PlannerCountdownAlert extends PlannerAlert {
	
	/** the countdown of this planner countdown alert */
	private LongProperty countdown;
	
	/**
	 * Constructs a new planner countdown alert with the specified alert type
	 * and countdown limits.
	 * 
	 * @param alertType the alert type of this planner countdown alert
	 * @param start the start of the countdown
	 * @param stop the stop of the countdown
	 * 
	 * @see PlannerAlert#PlannerAlert(AlertType)
	 */
	public PlannerCountdownAlert(AlertType alertType, long start, long stop) {
		super(alertType);
		this.countdown = new SimpleLongProperty(start);
		this.contentTextProperty().bind(countdown.asString());
		this.getDialogPane().getStylesheets().add(
				   getClass().getResource("planner.css").toExternalForm());
		this.setOnShown(new EventHandler<DialogEvent>() {
			@Override
			public void handle(DialogEvent arg0) {
		        countdown.set(start);
		        Timeline timeline = new Timeline();
		        timeline.getKeyFrames().add(
		                new KeyFrame(Duration.seconds(Math.abs(start - stop)),
		                new KeyValue(countdown, stop)));
		        timeline.playFromStart();
		        timeline.setOnFinished(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						Button cancel = (Button) getDialogPane()
								.lookupButton(ButtonType.CANCEL);
						cancel.fire();
					}
				});
			}
		});
	}
	
}
