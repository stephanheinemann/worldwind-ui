/**
 * Copyright (c) 2018, Henrique Ferreira (UVic Center for Aerospace Research)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.cfar.swim.worldwind.ui.desirabilityzones;

import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.session.Setup;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

/**
 * Realizes a desirability zone dialog to set the desirability value.
 * 
 * @author Henrique Ferreira
 *
 */
public class DesirabilityDialog extends Dialog<Double> {

	/** the desirability dialog title for addition */
	public static final String TITLE_DESIRABILITY = "Set Desirability Zone";

	/** the desirability dialog header for addition */
	public static final String HEADER_DESIRABILITY = "Set the zone desirability";

	/** the desirability value node identifier */
	private static final String DESIRABILITY_ID = "#desirability";
	
	/** the desirability value node identifier */
	private static final String FLOOR_ID = "#floor";
	
	/** the desirability value node identifier */
	private static final String CEILING_ID = "#ceiling";

	/** the desirability value text field of this desirability dialog */
	private TextField desirability;
	
	/** the floor altitude text field of this desirability dialog */
	private TextField floor;
	
	/** the ceiling altitude text field of this desirability dialog */
	private TextField ceiling;

	/** the desirability double value of this desirability dialog */
	private double desirabilityValue;
	
	/** the floor altitude double value of this desirability dialog */
	private double floorValue;
	
	/** the ceiling altitude double value of this desirability dialog */
	private double ceilingValue;
	
	/** indicates whether or not the desirability value input is valid */
	private boolean isValidDesirability = false;
	

	/**
	 * Constructs a new desirability zone dialog with a specified title, header and icon.
	 * 
	 * @param title the title of this desirability dialog
	 * @param header the header of this desirability dialog
	 * @param icon the icon of this desirabibility dialog
	 */
	public DesirabilityDialog(String title, String header, String icon) {
		this.setTitle(title);
		this.setHeaderText(header);

		ImageView imageView = new ImageView(icon);
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(50d);
		this.setGraphic(imageView);

		this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		ButtonType clear = new ButtonType("Clear");
		this.getDialogPane().getButtonTypes().add(clear);
		this.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

		DesirabilityView desirabilityView = new DesirabilityView();
		desirability = ((TextField) desirabilityView.getView().lookup(DesirabilityDialog.DESIRABILITY_ID));
		floor = ((TextField) desirabilityView.getView().lookup(DesirabilityDialog.FLOOR_ID));
		ceiling = ((TextField) desirabilityView.getView().lookup(DesirabilityDialog.CEILING_ID));
		desirability.textProperty().addListener(new DesirabilityValidator());
		this.getDialogPane().setContent(desirabilityView.getView());
		
		Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
		desirability.setText(Double.toString(session.getSetup().getDesirabilitySpecification()));
		floor.setText(Double.toString(session.getSetup().getFloorDesirabilitySpecification()));
		ceiling.setText(Double.toString(session.getSetup().getCeilingDesirabilitySpecification()));
		
		this.setResultConverter(dialogButton -> {
			Setup setup = session.getSetup();
			if (dialogButton.equals(ButtonType.OK)) {
				desirabilityValue = Double.parseDouble(this.desirability.getText());
				floorValue = Double.parseDouble(this.floor.getText());
				ceilingValue = Double.parseDouble(this.ceiling.getText());
				setup.setDesirabilitySpecification(desirabilityValue);
				setup.setFloorDesirabilitySpecification(floorValue);
				setup.setCeilingDesirabilitySpecification(ceilingValue);
			}
			if (dialogButton.equals(ButtonType.CANCEL)) {
				desirabilityValue = setup.getDesirabilitySpecification();
			}
			if (dialogButton.equals(clear)) {
				desirabilityValue = setup.getDesirabilitySpecification();
				session.getActiveScenario().clearDesirabilityZones();
			}

			return desirabilityValue;
		});
	}

	/**
	 * Realizes a desirability value validator.
	 * 
	 * @author Henrique Ferreira
	 *
	 */
	private class DesirabilityValidator implements ChangeListener<String> {

		/**
		 * Validates the desirability value if it changes.
		 * 
		 * @param observable the observable
		 * @param oldValue the old desirability value
		 * @param newValue the new desirability value
		 */
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			try {
				double desirability = Double.parseDouble(newValue);
				isValidDesirability = ((0d <= desirability) && (1 >= desirability));
				getDialogPane().lookupButton(ButtonType.OK).setDisable(!isValidDesirability);
			} catch (Exception e) {
				isValidDesirability = false;
				getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
			}
		}
	}
}