package com.cfar.swim.worldwind.ui.setup;

import com.airhacks.afterburner.views.FXMLView;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TabPane;

public class SetupView extends FXMLView {
	
	@Override
	public TabPane getView() {
		return (TabPane) super.getView();
	}
	
	@SuppressWarnings("unchecked")
	public ComboBox<String> getEnvironment() {
		return (ComboBox<String>) this.getView().lookup("#environment");
	}
	
	@SuppressWarnings("unchecked")
	public ComboBox<String> getPlanner() {
		return (ComboBox<String>) this.getView().lookup("#planner");
	}
	
}
