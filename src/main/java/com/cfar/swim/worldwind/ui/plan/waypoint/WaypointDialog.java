/**
 * Copyright (c) 2021, Stephan Heinemann (UVic Center for Aerospace Research)
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
package com.cfar.swim.worldwind.ui.plan.waypoint;

import java.awt.image.BufferedImage;

import com.cfar.swim.worldwind.planning.Waypoint;
import com.cfar.swim.worldwind.ui.util.ResourceBundleLoader;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525PointGraphicRetriever;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

/**
 * Realizes a waypoint dialog to add or modify waypoints of a plan.
 * 
 * @author Stephan Heinemann
 *
 */
public class WaypointDialog extends Dialog<Waypoint> {

	/** the waypoint dialog title for addition */
	public static final String TITLE_ADD = ResourceBundleLoader
			.getDictionaryBundle()
			.getString("waypoint.dialog.add.title");
	
	/** the waypoint dialog header for addition */
	public static final String HEADER_ADD = ResourceBundleLoader
			.getDictionaryBundle()
			.getString("waypoint.dialog.add.header");
	
	/** the waypoint dialog title for modification */
	public static final String TITLE_EDIT = ResourceBundleLoader
			.getDictionaryBundle()
			.getString("waypoint.dialog.edit.title");
	
	/** the waypoint dialog header for modification */
	public static final String HEADER_EDIT = ResourceBundleLoader
			.getDictionaryBundle()
			.getString("waypoint.dialog.edit.header");
	
	/** the waypoint latitude node identifier */
	private static final String LATITUDE_ID = "#latitude";
	
	/** the waypoint longitude node identifier */
	private static final String LONGITUDE_ID = "#longitude";
	
	/** the waypoint altitude node identifier */
	private static final String ALTITUDE_ID = "#altitude";
	
	/** the latitude text field of this waypoint dialog */
	private TextField latitude;
	
	/** the longitude text field of this waypoint dialog */
	private TextField longitude;
	
	/** the altitude text field of this waypoint dialog */
	private TextField altitude;
	
	/** indicates whether or not the latitude input is valid */
	private boolean isValidLatitude = false;
	
	/** indicates whether or not the longitude input is valid */
	private boolean isValidLongitude = false;
	
	/** indicates whether or not the altitude input is valid */
	private boolean isValidAltitude = false;
	
	/**
	 * Constructs a new waypoint dialog with a specified title and header.
	 * 
	 * @param title the title of this waypoint dialog
	 * @param header the header of this waypoint dialog
	 */
	public WaypointDialog(String title, String header) {
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
		latitude = ((TextField) waypointView.getView().lookup(WaypointDialog.LATITUDE_ID));
		latitude.textProperty().addListener(new LatitudeValidator());
		longitude = ((TextField) waypointView.getView().lookup(WaypointDialog.LONGITUDE_ID));
		longitude.textProperty().addListener(new LongitudeValidator());
		altitude = ((TextField) waypointView.getView().lookup(WaypointDialog.ALTITUDE_ID));
		altitude.textProperty().addListener(new AltitudeValidator());		
		this.getDialogPane().setContent(waypointView.getView());
		
		this.setResultConverter(dialogButton -> {
			Waypoint waypoint = null;
			
			if (dialogButton.equals(ButtonType.OK)) {
				Angle lat = Angle.fromDegreesLatitude(Double.parseDouble(this.latitude.getText()));
				Angle lon = Angle.fromDegreesLongitude(Double.parseDouble(this.longitude.getText()));
				double alt = Double.parseDouble(this.altitude.getText());
				return new Waypoint(new Position(lat, lon, alt));
			}
			
			return waypoint;
		});
	}
	
	/**
	 * Sets the waypoint of this waypoint dialog.
	 * 
	 * @param waypoint the waypoint to be set
	 */
	public void setWaypoint(Waypoint waypoint) {
		latitude.setText(Double.toString(waypoint.getLatitude().getDegrees()));
		longitude.setText(Double.toString(waypoint.getLongitude().getDegrees()));
		altitude.setText(Double.toString(waypoint.getAltitude()));
	}
	
	/**
	 * Realizes a latitude validator.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class LatitudeValidator implements ChangeListener<String> {
		
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
				double latitude = Double.parseDouble(newValue);
				isValidLatitude = ((-90d <= latitude) && (+90d >= latitude));
				getDialogPane().lookupButton(ButtonType.OK).setDisable(!isValidLatitude || !isValidLongitude || !isValidAltitude);
			} catch (Exception e) {
				isValidLatitude = false;
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
	private class LongitudeValidator implements ChangeListener<String> {
		
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
				double longitude = Double.parseDouble(newValue);
				isValidLongitude = ((-180d <= longitude) && (+180d >= longitude));
				getDialogPane().lookupButton(ButtonType.OK).setDisable(!isValidLatitude || !isValidLongitude || !isValidAltitude);
			} catch (Exception e) {
				isValidLongitude = false;
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
				getDialogPane().lookupButton(ButtonType.OK).setDisable(!isValidLatitude || !isValidLongitude || !isValidAltitude);
			} catch (Exception e) {
				isValidAltitude = false;
				getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
			}
		}
	}
	
}
