package com.cfar.swim.worldwind.ui.planner;

import java.net.URL;
import java.util.ResourceBundle;

import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.plan.PlanView;
import com.cfar.swim.worldwind.ui.world.WorldView;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

public class PlannerPresenter implements Initializable {
	
	@FXML
	private AnchorPane worldPane;
	
	@FXML
	private AnchorPane planPane;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		WorldView worldView = new WorldView();
		Parent worldViewParent = worldView.getView();
		this.worldPane.getChildren().add(worldViewParent);
		
		PlanView planView = new PlanView();
		Parent planViewParent = planView.getView();
		this.planPane.getChildren().add(planViewParent);
	}
	
}
