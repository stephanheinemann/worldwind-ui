package com.cfar.swim.worldwind.ui.plan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.inject.Inject;

import com.cfar.swim.worldwind.planning.Waypoint;
import com.cfar.swim.worldwind.render.annotations.DepictionAnnotation;
import com.cfar.swim.worldwind.session.Scenario;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.Main;
import com.cfar.swim.worldwind.ui.plan.waypoint.WaypointDialog;
import com.cfar.swim.worldwind.util.Depiction;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525GraphicFactory;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

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
		
		Session session = SessionManager.getInstance().getSession(Main.APPLICATION_TITLE);
		session.addActiveScenarioChangeListener(new ActiveScenarioChangeListener());
		this.initScenario();
		this.initPlan();
	}
	
	public void initScenario() {
		if (null != this.scenario) {
			this.scenario.removePropertyChangeListener(this.wcl);
		}
		this.scenario = SessionManager.getInstance().getSession(Main.APPLICATION_TITLE).getActiveScenario();
		this.scenario.addWaypointsChangeListener(this.wcl);
	}
	
	public void initPlan() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				plan.getRoot().getChildren().clear();
				for (Waypoint waypoint : scenario.getWaypoints()) {
					plan.getRoot().getChildren().add(new TreeItem<Waypoint>(waypoint));
				}
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
				this.scenario.updateWaypoint(waypoint, editedWaypoint);
			}
		}
	}
	
	public void removeWaypoint() {
		TreeItem<Waypoint> waypointItem = this.plan.getSelectionModel().getSelectedItem();
		if (null != waypointItem) {
			Waypoint waypoint = waypointItem.getValue();
			this.scenario.removeWaypoint(waypoint);
		}
	}
	
	public void clearWaypoints() {
		this.scenario.clearWaypoints();
	}
	
	private class WaypointsChangeListener implements PropertyChangeListener {

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
	
}
