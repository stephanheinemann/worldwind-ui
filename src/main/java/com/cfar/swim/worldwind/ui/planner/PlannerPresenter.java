package com.cfar.swim.worldwind.ui.planner;

import java.net.URL;
import java.util.ResourceBundle;

import com.cfar.swim.worldwind.ui.plan.PlanView;
import com.cfar.swim.worldwind.ui.scenario.ScenarioView;
import com.cfar.swim.worldwind.ui.world.WorldView;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.layout.AnchorPane;

public class PlannerPresenter implements Initializable {
	
	@FXML
	private AnchorPane worldPane;
	
	@FXML
	private Accordion plannerAccordion;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		WorldView worldView = new WorldView();
		this.worldPane.getChildren().add(worldView.getView());
		
		ScenarioView scenarioView = new ScenarioView();
		this.plannerAccordion.getPanes().add(scenarioView.getView());
		
		PlanView planView = new PlanView();
		this.plannerAccordion.getPanes().add(planView.getView());
	}
	
}
