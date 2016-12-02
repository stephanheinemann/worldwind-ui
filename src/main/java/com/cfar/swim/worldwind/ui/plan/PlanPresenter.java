/**
 * Copyright (c) 2016, Stephan Heinemann (UVic Center for Aerospace Research)
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
package com.cfar.swim.worldwind.ui.plan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Iterator;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.inject.Inject;

import com.cfar.swim.worldwind.planning.Waypoint;
import com.cfar.swim.worldwind.render.annotations.DepictionAnnotation;
import com.cfar.swim.worldwind.session.Scenario;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;
import com.cfar.swim.worldwind.ui.plan.waypoint.WaypointDialog;
import com.cfar.swim.worldwind.util.Depiction;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525GraphicFactory;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.util.Callback;

/**
 * Realizes a presenter for a plan view.
 * 
 * @author Stephan Heinemann
 *
 */
public class PlanPresenter implements Initializable {
	
	/** the waypoint symbol of this plan presenter */
	@Inject private String waypointSymbol;
	
	/** the plan tree table view of this plan presenter */
	@FXML
	private TreeTableView<Waypoint> plan;
	
	/** the designation column of this plan presenter */
	@FXML
	private TreeTableColumn<Waypoint, String> designationColumn;
	
	/** the location column of this plan presenter */
	@FXML
	private TreeTableColumn<Waypoint, String> locationColumn;
	
	/** the altitude column of this plan presenter */
	@FXML
	private TreeTableColumn<Waypoint, Double> altitudeColumn;
	
	/** the cost column of this plan presenter */
	@FXML
	private TreeTableColumn<Waypoint, Double> costsColumn;
	
	/** the distance to go column of this plan presenter */
	@FXML
	private TreeTableColumn<Waypoint, Double> distanceToGoColumn;
	
	/** the time to go column of this plan presenter */
	@FXML
	private TreeTableColumn<Waypoint, String> timeToGoColumn;
	
	/** the estimated time over column of this plan presenter */
	@FXML
	private TreeTableColumn<Waypoint, String> estimatedTimeOverColumn;
	
	/** the actual time over column of this plan presenter */
	@FXML
	private TreeTableColumn<Waypoint, String> actualTimeOverColumn;
	
	/** the active planning scenario (model) of this plan presenter */
	Scenario scenario = null;
	
	/** the waypoints change listener of this plan presenter */
	WaypointsChangeListener wcl = new WaypointsChangeListener();
	
	/** the trajectory change listener of this plan presenter */
	TrajectoryChangeListener tcl = new TrajectoryChangeListener();
	
	/** the military symbol factory of this plan presenter */
	MilStd2525GraphicFactory symbolFactory = new MilStd2525GraphicFactory();
	
	/**
	 * Initializes this plan presenter.
	 * 
	 * @param location unused
	 * @param resources unused
	 * 
	 * @see Initializable#initialize(URL, ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		LatLon latlon = new LatLon(Angle.ZERO, Angle.ZERO);
		Waypoint waypoint = new Waypoint(new Position(latlon, 0));
		plan.setRoot(new TreeItem<Waypoint>(waypoint));
		plan.setShowRoot(false);
		
		this.designationColumn.setCellValueFactory(
			(TreeTableColumn.CellDataFeatures<Waypoint, String> param) ->
			new ReadOnlyStringWrapper(param.getValue().getValue().getDesignator()));
		
		this.locationColumn.setCellValueFactory(
			(TreeTableColumn.CellDataFeatures<Waypoint, String> param) ->
			new ReadOnlyStringWrapper(param.getValue().getValue().toString()));
		
		this.altitudeColumn.setCellValueFactory(
			new TreeItemPropertyValueFactory<Waypoint, Double>("altitude"));
		
		this.costsColumn.setCellValueFactory(
			new TreeItemPropertyValueFactory<Waypoint, Double>("g"));
		
		this.estimatedTimeOverColumn.setCellValueFactory(new EtoCellValueFactory());
		
		Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
		session.addActiveScenarioChangeListener(new ActiveScenarioChangeListener());
		this.initScenario();
		this.initPlan();
	}
	
	/**
	 * Initializes the active scenario of this plan presenter registering
	 * change listeners.
	 */
	public void initScenario() {
		// remove change listeners from the previous scenario if any
		if (null != this.scenario) {
			this.scenario.removePropertyChangeListener(this.wcl);
			this.scenario.removePropertyChangeListener(this.tcl);
		}
		this.scenario = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).getActiveScenario();
		this.scenario.addWaypointsChangeListener(this.wcl);
		this.scenario.addTrajectoryChangeListener(this.tcl);
	}
	
	/**
	 * Initializes the plan of this plan presenter populating the plan view
	 * according to the active scenario.
	 */
	public void initPlan() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				plan.getRoot().getChildren().clear();
				
				Iterator<Waypoint> waypointIterator = scenario.getWaypoints().iterator();
				
				if (waypointIterator.hasNext()) {
					Waypoint current = waypointIterator.next();
					TreeItem<Waypoint> waypointItem = new TreeItem<Waypoint>(current);
					plan.getRoot().getChildren().add(waypointItem);
					
					while (waypointIterator.hasNext()) {
						Waypoint next = waypointIterator.next();
						for (Waypoint legWaypoint : scenario.getTrajectoryLeg(current, next)) {
							TreeItem<Waypoint> legWaypointItem = new TreeItem<Waypoint>(legWaypoint);
							waypointItem.getChildren().add(legWaypointItem);
						}
						current = next;
						waypointItem = new TreeItem<Waypoint>(current);
						plan.getRoot().getChildren().add(waypointItem);
					}
				}
				
				plan.refresh();
			}
		});
	}
	
	/**
	 * Adds a waypoint to the active scenario.
	 */
	public void addWaypoint() {
		WaypointDialog waypointDialog = new WaypointDialog(WaypointDialog.TITLE_ADD, WaypointDialog.HEADER_ADD);
		Optional<Waypoint> optWaypoint = waypointDialog.showAndWait();
		if (optWaypoint.isPresent()) {
			Waypoint waypoint = optWaypoint.get();
			waypoint.setDepiction(new Depiction(symbolFactory.createPoint(Waypoint.SIDC_NAV_WAYPOINT_POI, waypoint, null)));
			waypoint.getDepiction().setAnnotation(new DepictionAnnotation(this.waypointSymbol, "?", waypoint));
			waypoint.getDepiction().setVisible(true);
			this.scenario.addWaypoint(waypoint);
		}
		// TODO: check out ControlsFX (central repository)
	}
	
	/**
	 * Updates an existing waypoint of the active scenario.
	 * Any previously computed trajectory is removed.
	 */
	public void editWaypoint() {
		TreeItem<Waypoint> waypointItem = this.plan.getSelectionModel().getSelectedItem();
		if (null != waypointItem) {
			Waypoint waypoint = waypointItem.getValue();
			WaypointDialog waypointDialog = new WaypointDialog(WaypointDialog.TITLE_EDIT, WaypointDialog.HEADER_EDIT);
			waypointDialog.setWaypoint(waypoint);
			Optional<Waypoint> optWaypoint = waypointDialog.showAndWait();
			if (optWaypoint.isPresent()) {
				Waypoint editedWaypoint = optWaypoint.get();
				editedWaypoint.setDepiction(new Depiction(symbolFactory.createPoint(Waypoint.SIDC_NAV_WAYPOINT_POI, editedWaypoint, null)));
				editedWaypoint.getDepiction().setAnnotation(new DepictionAnnotation(this.waypointSymbol, editedWaypoint.getDesignator(), editedWaypoint));
				editedWaypoint.getDepiction().setVisible(true);
				this.scenario.clearTrajectory();
				this.scenario.updateWaypoint(waypoint, editedWaypoint);
				
				// the first waypoint can be the aircraft which has to be moved accordingly
				if (this.plan.getSelectionModel().isSelected(0) && this.scenario.hasAircraft()) {
					this.scenario.moveAircraft(editedWaypoint);
				}
			}
		}
	}
	
	/**
	 * Removes a waypoint from the active scenario.
	 * Any previously computed trajectory is removed.
	 */
	public void removeWaypoint() {
		TreeItem<Waypoint> waypointItem = this.plan.getSelectionModel().getSelectedItem();
		if (null != waypointItem) {
			Waypoint waypoint = waypointItem.getValue();
			this.scenario.clearTrajectory();
			this.scenario.removeWaypoint(waypoint);
			
			// the first waypoint can be the aircraft which has to be removed accordingly
			if (this.plan.getSelectionModel().isSelected(0) && this.scenario.hasAircraft()) {
				this.scenario.removeAircraft();
			}
		}
	}
	
	/**
	 * Removes all waypoints from the active scenario.
	 * Any previously computed trajectory is removed.
	 */
	public void clearWaypoints() {
		this.scenario.clearTrajectory();
		this.scenario.clearWaypoints();
		
		if (this.scenario.hasAircraft()) {
			this.scenario.removeAircraft();
		}
	}
	
	/**
	 * Realizes a waypoints change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class WaypointsChangeListener implements PropertyChangeListener {
		
		/**
		 * Initializes the plan if the waypoints change.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initPlan();
		}
	}
	
	/**
	 * Realizes a trajectory change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class TrajectoryChangeListener implements PropertyChangeListener {
		
		/**
		 * Initializes the plan if the trajectory changes.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initPlan();
		}
	}
	
	/**
	 * Realizes an active scenario change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class ActiveScenarioChangeListener implements PropertyChangeListener {
		
		/**
		 * Initializes the scenario and plan if the active scenario changes.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initScenario();
			initPlan();
		}
	}
	
	/**
	 * Realizes an estimated time over cell value factory to display
	 * waypoints in the ETO column of the plan view.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class EtoCellValueFactory implements Callback<CellDataFeatures<Waypoint, String>, ObservableValue<String>> {
		
		/**
		 * Creates an cell value for the ETO column of the waypoint plan.
		 * 
		 * @param param the cell data features
		 * 
		 * @return the cell value for the ETO column of the waypoint plan
		 * 
		 * @see Callback#call(Object)
		 */
		@Override
		public ObservableValue<String> call(CellDataFeatures<Waypoint, String> param) {
			ReadOnlyStringWrapper value = null;
			Waypoint waypoint = param.getValue().getValue();
			
			if (null != waypoint.getEto()) {
				value = new ReadOnlyStringWrapper(waypoint.getEto().toString());
			}
			
			return value;
		}
	}
	
}
