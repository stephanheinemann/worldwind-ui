package com.cfar.swim.worldwind.ui.setup;

import com.cfar.swim.worldwind.ai.Planner;
import com.cfar.swim.worldwind.planning.Environment;
import com.cfar.swim.worldwind.registries.Specification;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.session.Setup;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.ImageView;

public class SetupDialog extends Dialog<Setup> {

	public static final String TITLE_SETUP = "Setup Planner Session";
	public static final String HEADER_SETUP = "Setup the Planning Session";
	
	public static final int AIRCRAFT_TAB_INDEX = 0;
	public static final int ENVIRONMENT_TAB_INDEX = 1;
	public static final int SWIM_TAB_INDEX = 2;
	public static final int PLANNER_TAB_INDEX = 3;
	
	private SetupView setupView = null;
	
	public SetupDialog(String title, String header, String icon, SetupModel setupModel) {
		this.setTitle(title);
		this.setHeaderText(header);
		
		ImageView imageView = new ImageView(icon);
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(50d);
		this.setGraphic(imageView);
		
		this.setupView = new SetupView();
		this.getDialogPane().setContent(this.setupView.getView());
		this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		this.setResultConverter(dialogButton -> {
			Setup setup = null;
			
			if (dialogButton.equals(ButtonType.OK)) {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				setup = session.getSetup();
				String envId = this.setupView.getEnvironment().getValue();
				Specification<Environment> envSpec = session.getEnvironmentSpecification(envId);
				envSpec.setProperties(setupModel.getEnvironmentProperties());
				setup.setEnvironmentSpecification(envSpec);
				
				String plannerId = this.setupView.getPlanner().getValue();
				Specification<Planner> plannerSpec = session.getPlannerSpecification(plannerId);
				plannerSpec.setProperties(setupModel.getPlannerProperties());
				setup.setPlannerSpecification(plannerSpec);
				
				// TODO: fill setup
			}
			
			return setup;
		});
	
	}
	
	public void selectTab(int index) {	
		this.setupView.getView().getSelectionModel().select(index);
	}
	
}
