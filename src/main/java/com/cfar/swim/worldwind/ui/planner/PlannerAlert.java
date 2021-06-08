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

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.StageStyle;

/**
 * Realizes a planning application alert dialog which can be used
 * anywhere in the application with the appropriate alert type. 
 * 
 * @author Stephan Heinemann
 *
 */
public class PlannerAlert extends Alert {
	
	// TODO: obtain UI text from dictionary resource bundle
	
	/** the title of an invalid properties setup alert */
	public static final String ALERT_TITLE_PROPERTIES_INVALID = "Setup Properties Invalid";
	
	/** the header of an invalid properties setup alert */
	public static final String ALERT_HEADER_PROPERTIES_INVALID = "The setup properties are invalid.";
	
	/** the content of an invalid properties setup alert */
	public static final String ALERT_CONTENT_PROPERTIES_INVALID = "Please check the following properties:";
	
	/** the title of an invalid planner setup alert */
	public static final String ALERT_TITLE_PLANNER_INVALID = "Planner Invalid";
	
	/** the header of an invalid planner setup alert */
	public static final String ALERT_HEADER_PLANNER_INVALID = "The planner is invalid.";
	
	/** the content of an invalid planner setup alert */
	public static final String ALERT_CONTENT_PLANNER_INVALID =
			"Please check if the aircraft, environment and waypoints are supported.";
	
	/** the title of an invalid SWIM setup alert */
	public static final String ALERT_TITLE_SWIM_INVALID = "SWIM Invalid";
	
	/** the header of an invalid SWIM setup alert */
	public static final String ALERT_HEADER_SWIM_INVALID = "The SWIM connection is invalid.";
	
	/** the content of an invalid SWIM setup alert */
	public static final String ALERT_CONTENT_SWIM_INVALID = "Please check the SWIM connection setup.";
	
	/** the title of an invalid datalink setup alert */
	public static final String ALERT_TITLE_DATALINK_INVALID = "Datalink Invalid";
	
	/** the header of an invalid datalink setup alert */
	public static final String ALERT_HEADER_DATALINK_INVALID = "The datalink or trajectory are invalid.";
	
	/** the content of an invalid datalink setup alert */
	public static final String ALERT_CONTENT_DATALINK_INVALID =
			"Please check the datalink setup and the computed trajectory.";
	
	/** the title of a take-off confirmation alert */
	public static final String ALERT_TITLE_TAKEOFF_CONFIRM = "Confirm Take-Off";
	
	/** the header of a take-off confirmation alert */
	public static final String ALERT_HEADER_TAKEOFF_CONFIRM = "Confirm the take-off clearance.";
	
	/** the content of a take-off confirmation alert */
	public static final String ALERT_CONTENT_TAKEOFF_CONFIRM =
			"Please confirm the take-off clearance. The aircraft is about to take-off.";
	
	/** the title of a land confirmation alert */
	public static final String ALERT_TITLE_LAND_CONFIRM = "Confirm Landing";
	
	/** the header of a land confirmation alert */
	public static final String ALERT_HEADER_LAND_CONFIRM = "Confirm the landing clearance.";
	
	/** the content of a land confirmation alert */
	public static final String ALERT_CONTENT_LAND_CONFIRM =
			"Please confirm the landing clearance. The aircraft is about to land.";
	
	/**
	 * Constructs a new planner alert with a specified alert type.
	 * 
	 * @param alertType the alert type of this planner alert
	 * 
	 * @see Alert#Alert(AlertType)
	 */
	public PlannerAlert(AlertType alertType) {
		super(alertType);
		this.initStyle(StageStyle.UNDECORATED);
		this.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		this.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
	}
	
	/**
	 * Constructs a new planner alert with a specified alert type,
	 * content and buttons.
	 * 
	 * @param alertType the alert type of this planner alert
	 * @param contentText the content of this planner alert
	 * @param buttons the buttons of this planner alert
	 * 
	 * @see Alert#Alert(AlertType, String, ButtonType...)
	 */
	public PlannerAlert(AlertType alertType, String contentText, ButtonType... buttons) {
		super(alertType, contentText, buttons);
		this.initStyle(StageStyle.UNDECORATED);
		this.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		this.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
	}

}
