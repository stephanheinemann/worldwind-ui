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
package com.cfar.swim.worldwind.ui.plan.parameters;

import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

/**
 * Realizes a parameters dialog to change the parameters of an algorithm.
 * 
 * @author Henrique Ferreira
 *
 */
public class ParametersDialog extends Dialog<ArrayList<Double>> {

	/** the desirability dialog title for addition */
	public static final String TITLE_DESIRABILITY = "Set parameters";

	/** the desirability dialog header for addition */
	public static final String HEADER_DESIRABILITY = "Set anytime parameters";

	/** the waypoint latitude node identifier */
	private static final String INITIALINFLATION_ID = "#initialInflation";
	
	/** the waypoint longitude node identifier */
	private static final String FINALINFLATION_ID = "#finalInflation";
	
	/** the waypoint altitude node identifier */
	private static final String DEFLATION_ID = "#deflation";
	
	/** the latitude text field of this waypoint dialog */
	private TextField initialInflation;
	
	/** the longitude text field of this waypoint dialog */
	private TextField finalInflation;
	
	/** the altitude text field of this waypoint dialog */
	private TextField deflation;
	
	/** indicates whether or not the latitude input is valid */
	private boolean isValidInitialInflation = false;
	
	/** indicates whether or not the longitude input is valid */
	private boolean isValidFinalInflation = false;
	
	/** indicates whether or not the altitude input is valid */
	private boolean isValidDeflation = false;

	/**
	 * Constructs a new desirability zone dialog with a specified title and header.
	 * 
	 * @param title the title of this desirability dialog
	 * @param header the header of this desirability dialog
	 * @param icon the icon of this desirabibility dialog
	 */
	public ParametersDialog(String title, String header, String icon) {
		this.setTitle(title);
		this.setHeaderText(header);

		ImageView imageView = new ImageView(icon);
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(50d);
		this.setGraphic(imageView);

		this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

		ParametersView parametersView = new ParametersView();
		initialInflation = ((TextField) parametersView.getView().lookup(ParametersDialog.INITIALINFLATION_ID));
		initialInflation.textProperty().addListener(new InitialInflationValidator());
		finalInflation = ((TextField) parametersView.getView().lookup(ParametersDialog.FINALINFLATION_ID));
		finalInflation.textProperty().addListener(new FinalInflationValidator());
		deflation = ((TextField) parametersView.getView().lookup(ParametersDialog.DEFLATION_ID));
		deflation.textProperty().addListener(new DeflationValidator());		
		this.getDialogPane().setContent(parametersView.getView());
		
		initialInflation.setText(Double.toString(1d));
		finalInflation.setText(Double.toString(1d));
		deflation.setText(Double.toString(1d));
		
		this.setResultConverter(dialogButton -> {
			ArrayList<Double> parameters = new ArrayList<Double>();
			double initialInflation, finalInflation, deflation;
			if (dialogButton.equals(ButtonType.OK)) {
				initialInflation = Double.parseDouble(this.initialInflation.getText());
				finalInflation = Double.parseDouble(this.finalInflation.getText());
				deflation = Double.parseDouble(this.deflation.getText());
				parameters.add(initialInflation);
				parameters.add(finalInflation);
				parameters.add(deflation);
			}
			
			return parameters;
		});
	}
	
	/**
	 * Realizes a latitude validator.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class InitialInflationValidator implements ChangeListener<String> {
		
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
				double initialInflation = Double.parseDouble(newValue);
				double finalInflationDouble = Double.parseDouble(finalInflation.getText());
				isValidInitialInflation = ((+1d <= initialInflation) && (initialInflation >= finalInflationDouble));
				isValidFinalInflation = ((+1d <= finalInflationDouble) && (initialInflation >= finalInflationDouble));
				getDialogPane().lookupButton(ButtonType.OK).setDisable(!isValidInitialInflation || !isValidFinalInflation || !isValidDeflation);
			} catch (Exception e) {
				isValidInitialInflation = false;
				getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
			}
		}
	}
	
	/**
	 * Realizes a longitude validator.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class FinalInflationValidator implements ChangeListener<String> {
		
		/**
		 * Validates the longitude if it changes.
		 * 
		 * @param observable the observable
		 * @param oldValue the old longitude value
		 * @param newValue the new longitude value
		 */
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			try {
				double finalInflation = Double.parseDouble(newValue);
				double initialInflationDouble = Double.parseDouble(initialInflation.getText());
				isValidFinalInflation = ((+1d <= finalInflation) && (finalInflation <= initialInflationDouble));
				isValidInitialInflation = ((+1d <= initialInflationDouble) && (finalInflation <= initialInflationDouble));
				getDialogPane().lookupButton(ButtonType.OK).setDisable(!isValidInitialInflation || !isValidFinalInflation || !isValidDeflation);
			} catch (Exception e) {
				isValidFinalInflation = false;
				getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
			}
		}
	}
	
	/**
	 * Realizes an altitude validator.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class DeflationValidator implements ChangeListener<String> {
		
		/**
		 * Validates the altitude if it changes.
		 *  
		 * @param observable the observable
		 * @param oldValue the old altitude value
		 * @param newValue the new altitude value
		 */
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			try {
				double deflation = Double.parseDouble(newValue);
				isValidDeflation = (0d < deflation);
				getDialogPane().lookupButton(ButtonType.OK).setDisable(!isValidInitialInflation || !isValidFinalInflation || !isValidDeflation);
			} catch (Exception e) {
				isValidDeflation = false;
				getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
			}
		}
	}

}
