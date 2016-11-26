package com.cfar.swim.worldwind.ui.plan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Iterator;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.inject.Inject;

import com.cfar.swim.worldwind.geom.precision.PrecisionPosition;
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

public class PlanPresenter implements Initializable {

	@Inject private String waypointSymbol;
	
	@FXML
	private TreeTableView<Waypoint> plan;
	
	@FXML
	private TreeTableColumn<Waypoint, String> designationColumn;
	
	@FXML
	private TreeTableColumn<Waypoint, String> locationColumn;
	
	@FXML
	private TreeTableColumn<Waypoint, Double> altitudeColumn;
	
	@FXML
	private TreeTableColumn<Waypoint, Double> costsColumn;
	
	@FXML
	private TreeTableColumn<Waypoint, Double> distanceToGoColumn;
	
	@FXML
	private TreeTableColumn<Waypoint, String> timeToGoColumn;
	
	@FXML
	private TreeTableColumn<Waypoint, String> estimatedTimeOverColumn;
	
	@FXML
	private TreeTableColumn<Waypoint, String> actualTimeOverColumn;
	
	Scenario scenario = null;
	WaypointsChangeListener wcl = new WaypointsChangeListener();
	TrajectoryChangeListener tcl = new TrajectoryChangeListener();
	
	MilStd2525GraphicFactory symbolFactory = new MilStd2525GraphicFactory();
	
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
	
	public void initScenario() {
		if (null != this.scenario) {
			this.scenario.removePropertyChangeListener(this.wcl);
		}
		this.scenario = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).getActiveScenario();
		this.scenario.addWaypointsChangeListener(this.wcl);
		this.scenario.addTrajectoryChangeListener(this.tcl);
	}
	
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
				
				if (this.scenario.hasAircraft()) {
					PrecisionPosition aircraftReference = new PrecisionPosition(this.scenario.getAircraft().getReferencePosition());
					PrecisionPosition waypointReference = new PrecisionPosition(waypoint);
					if (aircraftReference.equals(waypointReference)) {
						this.scenario.getAircraft().moveTo(editedWaypoint);
						this.scenario.notifyAircraftChange();
					}
				}
			}
		}
	}
	
	public void removeWaypoint() {
		TreeItem<Waypoint> waypointItem = this.plan.getSelectionModel().getSelectedItem();
		if (null != waypointItem) {
			Waypoint waypoint = waypointItem.getValue();
			this.scenario.clearTrajectory();
			this.scenario.removeWaypoint(waypoint);
			
			if (this.scenario.hasAircraft()) {
				PrecisionPosition aircraftReference = new PrecisionPosition(this.scenario.getAircraft().getReferencePosition());
				PrecisionPosition waypointReference = new PrecisionPosition(waypoint);
				if (aircraftReference.equals(waypointReference)) {
					this.scenario.removeAircraft();
				}
			}
		}
	}
	
	public void clearWaypoints() {
		this.scenario.clearTrajectory();
		this.scenario.clearWaypoints();
		
		if (this.scenario.hasAircraft()) {
			this.scenario.removeAircraft();
		}
	}
	
	private class WaypointsChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initPlan();
		}
	}
	
	private class TrajectoryChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initPlan();
		}
	}
	
	private class ActiveScenarioChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initScenario();
			initPlan();
		}
	}
	
	private class EtoCellValueFactory implements Callback<CellDataFeatures<Waypoint, String>, ObservableValue<String>> {

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
