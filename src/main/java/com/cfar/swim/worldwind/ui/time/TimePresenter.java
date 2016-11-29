package com.cfar.swim.worldwind.ui.time;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;

import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import jfxtras.scene.control.LocalDateTimePicker;

public class TimePresenter implements Initializable {
	
	@FXML
	private AnchorPane timePane;
	
	private LocalDateTimePicker picker;

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
	}

	private class PlanningTimeCallback implements Callback<LocalDateTime, Boolean> {
		
		@Override
		public Boolean call(LocalDateTime localDateTime) {
			if (null != localDateTime) {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				session.getActiveScenario().setTime(ZonedDateTime.of(localDateTime, ZoneId.of("UTC")));
			}
			return true;
		}
	}
	
	private class PlanningTimeKeyHandler implements EventHandler<KeyEvent> {

		/** the maximum planning time step duration */
		private final Duration DURATION_MAX = Duration.ofHours(24);
		
		/** the current planning time step duration */
		private Duration duration = Duration.ofMinutes(10);
		
		/**
		 * Handles the key events to change the current planning time and
		 * planning time step duration.
		 */
		@Override
		public void handle(KeyEvent event) {
			if (KeyCode.RIGHT.equals(event.getCode())) {
				if (event.isControlDown()) {
					if (0 > this.duration.compareTo(DURATION_MAX)) {
						this.duration = this.duration.plusMinutes(1);
					}
				} else {
					picker.setLocalDateTime(picker.getLocalDateTime().plus(this.duration));
					picker.getValueValidationCallback().call(picker.getLocalDateTime());
				}
				event.consume();
			} else if (KeyCode.LEFT.equals(event.getCode())) {
				if (event.isControlDown()) {
					if (0 < this.duration.compareTo(Duration.ZERO)) {
						this.duration = this.duration.minusMinutes(1);
					}
				} else {
					picker.setLocalDateTime(picker.getLocalDateTime().minus(this.duration));
					picker.getValueValidationCallback().call(picker.getLocalDateTime());
				}
				event.consume();
			}
		}
	}
	
}
