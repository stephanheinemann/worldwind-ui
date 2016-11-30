package com.cfar.swim.worldwind.ui.planner;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;

import com.cfar.swim.worldwind.ui.environment.EnvironmentView;
import com.cfar.swim.worldwind.ui.plan.PlanView;
import com.cfar.swim.worldwind.ui.scenario.ScenarioView;
import com.cfar.swim.worldwind.ui.swim.SwimView;
import com.cfar.swim.worldwind.ui.threshold.ThresholdView;
import com.cfar.swim.worldwind.ui.time.TimeView;
import com.cfar.swim.worldwind.ui.world.WorldModel;
import com.cfar.swim.worldwind.ui.world.WorldView;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
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
	
	@FXML
	private ProgressBar progressBar;
	
	@FXML
	private ProgressIndicator progressIndicator;
	
	@Inject
	private WorldModel worldModel;
	
	@Inject
	private String aboutTitle;
	
	@Inject
	private String aboutHeader;
	
	@Inject
	private String aboutContent;
	
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
		
		SwimView swimView = new SwimView();
		this.swimAccordion.getPanes().add(swimView.getView());
		
		ScenarioView scenarioView = new ScenarioView();
		this.plannerAccordion.getPanes().add(scenarioView.getView());
		
		EnvironmentView environmentView = new EnvironmentView();
		this.plannerAccordion.getPanes().add(environmentView.getView());
		
		PlanView planView = new PlanView();
		this.plannerAccordion.getPanes().add(planView.getView());
		this.plannerAccordion.setExpandedPane(planView.getView());
		
		this.progressIndicator.setVisible(false);
		this.worldModel.addModeChangeListener(new ModeChangeListener());
	}
	
	public void exit() {
		Platform.exit();
		System.exit(0);
	}
	
	public void about() {
		PlannerAlert about = new PlannerAlert(AlertType.INFORMATION);
		about.setTitle(this.aboutTitle);
		about.setHeaderText(this.aboutHeader);
		about.setContentText(this.aboutContent);
		about.showAndWait();
	}
	
	private class ModeChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					switch (worldModel.getMode()) {
					case PLANNING:
					case LOADING:
						progressBar.setProgress(-1d);
						progressIndicator.toFront();
						progressIndicator.setVisible(true);
						progressIndicator.setProgress(-1d);
						break;
					default:
						progressBar.setProgress(0d);
						progressIndicator.toBack();
						progressIndicator.setVisible(false);
						progressIndicator.setProgress(0d);
					}
				}
			});
		}
	}
	
}
