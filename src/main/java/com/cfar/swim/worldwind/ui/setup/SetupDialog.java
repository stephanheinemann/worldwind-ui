package com.cfar.swim.worldwind.ui.setup;

import com.cfar.swim.worldwind.session.Setup;

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
	private Setup setup = null;
	
	public SetupDialog(String title, String header, String icon) {
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
			if (dialogButton.equals(ButtonType.OK)) {
				if (null == this.setup) {
					this.setup = new Setup();
				}
				
				// TODO: use SetupModel instead (property editors)
				// TODO: only update Setup on OK, disregard SetupModel on CANCEL
				// TODO: fill setup
			}
			
			return this.setup;
		});
	
	}
	
	public void selectTab(int index) {	
		this.setupView.getView().getSelectionModel().select(index);
	}
	
	public Setup getSetup() {
		return this.setup;
	}
	
	public void setSetup(Setup setup) {
		this.setup = setup;
	}
	
}
