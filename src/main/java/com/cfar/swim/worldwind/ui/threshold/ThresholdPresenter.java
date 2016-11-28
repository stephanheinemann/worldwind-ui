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

public class ThresholdPresenter implements Initializable {

	@FXML
	Slider thresholdSlider;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		thresholdSlider.setOnMouseReleased(new ThresholdCostInputHandler());
		thresholdSlider.setOnKeyPressed(new ThresholdCostInputHandler());
	}
	
	private class ThresholdCostInputHandler implements EventHandler<InputEvent> {
		
		@Override
		public void handle(InputEvent event) {
			Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
			session.getActiveScenario().setThreshold(thresholdSlider.getValue());
		}
	}
	
}
