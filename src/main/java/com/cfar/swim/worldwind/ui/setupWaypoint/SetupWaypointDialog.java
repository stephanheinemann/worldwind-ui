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
package com.cfar.swim.worldwind.ui.setupWaypoint;

import java.awt.image.BufferedImage;

import com.cfar.swim.worldwind.planning.Waypoint;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.session.Setup;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525PointGraphicRetriever;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

/**
 * Realizes a waypoint setup dialog to modify waypoints parameters.
 * 
 * @author Henrique Ferreira
 *
 */
public class SetupWaypointDialog extends Dialog<Double> {

	/** the waypoint dialog title for addition */
	public static final String TITLE_SETUP = "Setup Waypoint";
	
	/** the waypoint dialog header for addition */
	public static final String HEADER_SETUP = "Set waypoint parameters";
	
	/** the waypoint altitude node identifier */
	private static final String ALTITUDE_ID = "#altitude";
	
	/** the altitude text field of this waypoint dialog */
	private TextField altitude;
	
	/** indicates whether or not the altitude input is valid */
	private boolean isValidAltitude = false;
	
	/**
	 * Constructs a new waypoint dialog with a specified title and header.
	 * 
	 * @param title the title of this waypoint dialog
	 * @param header the header of this waypoint dialog
	 */
	public SetupWaypointDialog(String title, String header) {
		this.setTitle(title);
		this.setHeaderText(header);
		
		String iconPath = Configuration.getStringValue(AVKey.MIL_STD_2525_ICON_RETRIEVER_PATH);
		MilStd2525PointGraphicRetriever iconRetriever = new MilStd2525PointGraphicRetriever(iconPath);
		BufferedImage icon = iconRetriever.createIcon(Waypoint.SIDC_NAV_WAYPOINT_POI, null);
		ImageView imageView = new ImageView(SwingFXUtils.toFXImage(icon, null));
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(50d);
		this.setGraphic(imageView);
		
		this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		this.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
		
		WaypointView waypointView = new WaypointView();
		altitude = ((TextField) waypointView.getView().lookup(SetupWaypointDialog.ALTITUDE_ID));
		altitude.textProperty().addListener(new AltitudeValidator());		
		this.getDialogPane().setContent(waypointView.getView());
		Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
		altitude.setText(Double.toString(session.getSetup().getDefaultWaypointHeight()));
		
		this.setResultConverter(dialogButton -> {
			Setup setup = session.getSetup();
			double alt = 0d;
			if (dialogButton.equals(ButtonType.OK)) {
				alt = Double.parseDouble(this.altitude.getText());
				setup.setDefaultWaypointHeight(alt);
			}
			if (dialogButton.equals(ButtonType.CANCEL)) {
				alt = setup.getDefaultWaypointHeight();
			}
			
			return alt;
		});
	}
	
	/**
	 * Realizes an altitude validator.
	 * 
	 * @author Henrique Ferreira
	 *
	 */
	private class AltitudeValidator implements ChangeListener<String> {
		
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
				Double.parseDouble(newValue);
				isValidAltitude = true;
				getDialogPane().lookupButton(ButtonType.OK).setDisable(!isValidAltitude);
			} catch (Exception e) {
				isValidAltitude = false;
				getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
			}
		}
	}
	
}
