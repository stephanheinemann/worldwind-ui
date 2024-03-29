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
package com.cfar.swim.worldwind.ui.plan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import javafx.beans.property.ReadOnlyDoubleWrapper;
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
	@Inject
	private String waypointSymbol;
	
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
	private TreeTableColumn<Waypoint, Number> costsColumn;
	
	/** the distance to go column of this plan presenter */
	@FXML
	private TreeTableColumn<Waypoint, Number> distanceToGoColumn;
	
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
	private Scenario scenario = null;
	
	/** the waypoints change listener of this plan presenter */
	private final WaypointsChangeListener wcl = new WaypointsChangeListener();
	
	/** the trajectory change listener of this plan presenter */
	private final TrajectoryChangeListener tcl = new TrajectoryChangeListener();
	
	/** the military symbol factory of this plan presenter */
	private final MilStd2525GraphicFactory symbolFactory = new MilStd2525GraphicFactory();
	
	/** the executor of this plan presenter */
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	
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
		
		this.costsColumn.setCellValueFactory(new CostsCellValueFactory());
		this.distanceToGoColumn.setCellValueFactory(new DtgCellValueFactory());
		this.timeToGoColumn.setCellValueFactory(new TtgCellValueFactory());
		this.estimatedTimeOverColumn.setCellValueFactory(new EtoCellValueFactory());
		this.actualTimeOverColumn.setCellValueFactory(new AtoCellValueFactory());
		
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
		Iterator<Waypoint> waypointIterator = scenario.getWaypoints().iterator();
		TreeMap<Waypoint, List<Waypoint>> legs = new TreeMap<>(new Comparator<Waypoint>() {
			@Override
			public int compare(Waypoint w1, Waypoint w2) {
				return w1.getDesignator().compareTo(w2.getDesignator());
			}
		});
		
		if (waypointIterator.hasNext()) {
			Waypoint current = waypointIterator.next();
			while (waypointIterator.hasNext()) {
				Waypoint next = waypointIterator.next();
				legs.put(current, scenario.getTrajectoryLeg(current, next));
				current = next;
			}
			legs.put(current, Collections.emptyList());
		}
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				plan.getRoot().getChildren().clear();
				
				for (Waypoint waypoint : legs.keySet()) {
					TreeItem<Waypoint> waypointItem = new TreeItem<Waypoint>(waypoint);
					plan.getRoot().getChildren().add(waypointItem);
					
					for (Waypoint legWaypoint : legs.get(waypoint)) {
						TreeItem<Waypoint> legWaypointItem = new TreeItem<Waypoint>(legWaypoint);
						waypointItem.getChildren().add(legWaypointItem);
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
			
			this.executor.execute(new Runnable() {
				@Override
				public void run() {
					scenario.addWaypoint(waypoint);
				}
			});
		}
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
				boolean firstSelected = this.plan.getSelectionModel().isSelected(0);
				
				this.executor.execute(new Runnable() {
					@Override
					public void run() {
						scenario.clearTrajectory();
						scenario.updateWaypoint(waypoint, editedWaypoint);
						
						// the first waypoint can be the aircraft which has to be moved accordingly
						if (firstSelected && scenario.hasAircraft()) {
							scenario.moveAircraft(editedWaypoint);
						}
					}
				});
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
			boolean firstSelected = this.plan.getSelectionModel().isSelected(0);
			
			this.executor.execute(new Runnable() {
				@Override
				public void run() {
					scenario.clearTrajectory();
					scenario.removeWaypoint(waypoint);
					
					// the first waypoint can be the aircraft which has to be removed accordingly
					if (firstSelected && scenario.hasAircraft()) {
						scenario.removeAircraft();
					}
				}
			});
		}
	}
	
	/**
	 * Removes all waypoints from the active scenario.
	 * Any previously computed trajectory is removed.
	 */
	public void clearWaypoints() {
		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				scenario.clearTrajectory();
				scenario.clearWaypoints();
				
				if (scenario.hasAircraft()) {
					scenario.removeAircraft();
				}
			}
		});
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
	 * Realizes a costs cell value factory to display
	 * waypoints in the costs column of the plan view.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class CostsCellValueFactory implements Callback<CellDataFeatures<Waypoint, Number>, ObservableValue<Number>> {
		
		/**
		 * Creates a cell value for the costs column of the waypoint plan.
		 * 
		 * @param param the cell data features
		 * 
		 * @return the cell value for the costs column of the waypoint plan
		 * 
		 * @see Callback#call(Object)
		 */
		@Override
		public ObservableValue<Number> call(CellDataFeatures<Waypoint, Number> param) {
			ReadOnlyDoubleWrapper value = null;
			Waypoint waypoint = param.getValue().getValue();
			
			if (Double.POSITIVE_INFINITY != waypoint.getCost()) {
				value = new ReadOnlyDoubleWrapper(waypoint.getCost());
			}
			
			return value;
		}
	}
	
	/**
	 * Realizes a distance to go cell value factory to display
	 * waypoints in the DTG column of the plan view.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class DtgCellValueFactory implements Callback<CellDataFeatures<Waypoint, Number>, ObservableValue<Number>> {
		
		/**
		 * Creates a cell value for the DTG column of the waypoint plan.
		 * 
		 * @param param the cell data features
		 * 
		 * @return the cell value for the DTG column of the waypoint plan
		 * 
		 * @see Callback#call(Object)
		 */
		@Override
		public ObservableValue<Number> call(CellDataFeatures<Waypoint, Number> param) {
			ReadOnlyDoubleWrapper value = null;
			Waypoint waypoint = param.getValue().getValue();
			
			if (Double.POSITIVE_INFINITY != waypoint.getDtg()) {
				value = new ReadOnlyDoubleWrapper(waypoint.getDtg());
			}
			
			return value;
		}
	}
	
	/**
	 * Realizes a time to go cell value factory to display
	 * waypoints in the TTG column of the plan view.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class TtgCellValueFactory implements Callback<CellDataFeatures<Waypoint, String>, ObservableValue<String>> {
		
		/**
		 * Creates a cell value for the TTG column of the waypoint plan.
		 * 
		 * @param param the cell data features
		 * 
		 * @return the cell value for the TTG column of the waypoint plan
		 * 
		 * @see Callback#call(Object)
		 */
		@Override
		public ObservableValue<String> call(CellDataFeatures<Waypoint, String> param) {
			ReadOnlyStringWrapper value = null;
			Waypoint waypoint = param.getValue().getValue();
			
			if (null != waypoint.getTtg()) {
				value = new ReadOnlyStringWrapper(waypoint.getTtg().toString());
			}
			
			return value;
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
		 * Creates a cell value for the ETO column of the waypoint plan.
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
	
	/**
	 * Realizes an actual time over cell value factory to display
	 * waypoints in the ATO column of the plan view.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class AtoCellValueFactory implements Callback<CellDataFeatures<Waypoint, String>, ObservableValue<String>> {
		
		/**
		 * Creates a cell value for the ATO column of the waypoint plan.
		 * 
		 * @param param the cell data features
		 * 
		 * @return the cell value for the ATO column of the waypoint plan
		 * 
		 * @see Callback#call(Object)
		 */
		@Override
		public ObservableValue<String> call(CellDataFeatures<Waypoint, String> param) {
			ReadOnlyStringWrapper value = null;
			Waypoint waypoint = param.getValue().getValue();
			
			if (null != waypoint.getAto()) {
				value = new ReadOnlyStringWrapper(waypoint.getAto().toString());
			}
			
			return value;
		}
	}
	
}
