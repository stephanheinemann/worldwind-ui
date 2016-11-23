package com.cfar.swim.worldwind.ui.setup;

import com.cfar.swim.worldwind.session.Setup;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.ImageView;

public class SetupDialog extends Dialog<Setup> {

	public static final String TITLE_SETUP = "Setup Planner Session";
	public static final String HEADER_SETUP = "Setup the Planning Session";
	
	private Setup setup = null;
	
	public SetupDialog(String title, String header, String icon) {
		this.setTitle(title);
		this.setHeaderText(header);
		
		ImageView imageView = new ImageView(icon);
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(50d);
		this.setGraphic(imageView);
		
		SetupView setupView = new SetupView();
		this.getDialogPane().setContent(setupView.getView());
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
	
	public void setSetup(Setup setup) {
		this.setup = setup;
	}

}
