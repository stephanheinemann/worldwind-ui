package com.cfar.swim.worldwind.ui.planner;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.stage.StageStyle;

public class PlannerAlert extends Alert {

	public static final String ALERT_TITLE_PLANNER_INVALID = "Planner Invalid";
	public static final String ALERT_HEADER_PLANNER_INVALID = "The planner is invalid";
	public static final String ALERT_CONTENT_PLANNER_INVALID =
			"Please check if the aircraft, environment and waypoints are supported.";
	
	public PlannerAlert(AlertType alertType) {
		super(alertType);
		this.initStyle(StageStyle.UNDECORATED);
		this.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		this.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
	}

	public PlannerAlert(AlertType alertType, String contentText, ButtonType... buttons) {
		super(alertType, contentText, buttons);
		this.initStyle(StageStyle.UNDECORATED);
		this.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		this.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
	}

}
