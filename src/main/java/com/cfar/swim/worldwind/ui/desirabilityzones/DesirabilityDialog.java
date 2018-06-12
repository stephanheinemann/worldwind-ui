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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
/**
 * @author Henrique Ferreira
 *
 */
public class DesirabilityDialog extends Dialog<Double> {
	
	/** the waypoint dialog title for addition */
	public static final String TITLE_DESIRABILITY = "Set Desirability Zone";
	
	/** the waypoint dialog header for addition */
	public static final String HEADER_DESIRABILITY = "Set the zone desirability";
	
	/** the waypoint latitude node identifier */
	private static final String DESIRABILITY_ID = "#desirability";
	
	/** the latitude text field of this waypoint dialog */
	private TextField desirability;
	
	/** indicates whether or not the latitude input is valid */
	private boolean isValidDesirability = false;
	
	/**
	 * Constructs a new waypoint dialog with a specified title and header.
	 * 
	 * @param title the title of this waypoint dialog
	 * @param header the header of this waypoint dialog
	 */
	public DesirabilityDialog(String title, String header, String icon) {
		this.setTitle(title);
		this.setHeaderText(header);
		
		ImageView imageView = new ImageView(icon);
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(50d);
		this.setGraphic(imageView);
		
		this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		DesirabilityView desirabilityView = new DesirabilityView();
		desirability = ((TextField) desirabilityView.getView().lookup(DesirabilityDialog.DESIRABILITY_ID));
		desirability.textProperty().addListener(new DesirabilityValidator());
		this.getDialogPane().setContent(desirabilityView.getView());
		
		this.setResultConverter(dialogButton -> {
			double desirability = 0.5d;
			
			if (dialogButton.equals(ButtonType.OK)) {
				desirability = Double.parseDouble(this.desirability.getText());
			}
			
			return desirability;
		});
	}
	
	/**
	 * Realizes a latitude validator.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class DesirabilityValidator implements ChangeListener<String> {
		
		/**
		 * Validates the latitude if it changes.
		 * 
		 * @param observable the observable
		 * @param oldValue the old latitude value
		 * @param newValue the new latitude value
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
