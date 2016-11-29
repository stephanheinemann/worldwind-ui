package com.cfar.swim.worldwind.ui.planner;

import java.net.URL;
import java.util.ResourceBundle;

import com.cfar.swim.worldwind.ui.environment.EnvironmentView;
import com.cfar.swim.worldwind.ui.plan.PlanView;
import com.cfar.swim.worldwind.ui.scenario.ScenarioView;
import com.cfar.swim.worldwind.ui.threshold.ThresholdView;
import com.cfar.swim.worldwind.ui.time.TimeView;
import com.cfar.swim.worldwind.ui.world.WorldView;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.layout.AnchorPane;

public class PlannerPresenter implements Initializable {
	
	@FXML
	private AnchorPane worldPane;
	
	@FXML
	private Accordion timeAccordion;
	
	@FXML
	private Accordion swimAccordion;
	
	@FXML
	private Accordion plannerAccordion;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		WorldView worldView = new WorldView();
		this.worldPane.getChildren().add(worldView.getView());
		AnchorPane.setTopAnchor(worldView.getView(), 0d);
		AnchorPane.setLeftAnchor(worldView.getView(), 0d);
		AnchorPane.setRightAnchor(worldView.getView(), 0d);
		AnchorPane.setBottomAnchor(worldView.getView(), 0d);
		
		TimeView timeView = new TimeView();
		this.timeAccordion.getPanes().add(timeView.getView());
		this.timeAccordion.setExpandedPane(timeView.getView());
		
		ThresholdView thresholdView = new ThresholdView();
		this.swimAccordion.getPanes().add(thresholdView.getView());
		this.swimAccordion.setExpandedPane(thresholdView.getView());
		
		ScenarioView scenarioView = new ScenarioView();
		this.plannerAccordion.getPanes().add(scenarioView.getView());
		
		EnvironmentView environmentView = new EnvironmentView();
		this.plannerAccordion.getPanes().add(environmentView.getView());
		
		PlanView planView = new PlanView();
		this.plannerAccordion.getPanes().add(planView.getView());
		this.plannerAccordion.setExpandedPane(planView.getView());
	}
	
}
