package com.cfar.swim.worldwind.ui.plan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ResourceBundle;

import com.cfar.swim.worldwind.planning.Waypoint;
import com.cfar.swim.worldwind.session.Scenario;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.Main;
import com.cfar.swim.worldwind.ui.plan.waypoint.WaypointView;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PlanPresenter implements Initializable {

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
		
		Scenario scenario = SessionManager.getInstance().getSession(Main.APPLICATION_TITLE).getScenario("default");
		scenario.addPointsOfInterestChangeListener(new PointsOfInterestChangeListener());
	}

	public void addWaypoint(ActionEvent event) {
		WaypointView waypointView = new WaypointView();
		
		Stage dialog = new Stage();
	    dialog.setScene(new Scene(waypointView.getView()));
	    dialog.setTitle("Add Waypoint");
	    dialog.initModality(Modality.WINDOW_MODAL);
	    //dialog.initOwner(((Node) event.getSource()).getScene().getWindow() );
	    dialog.show();
		
		System.out.println("add waypoint");
		// TODO: check out ControlsFX (central repository)
	}
	
	public void removeWaypoint() {
		System.out.println("remove waypoint");
	}
	
	private class PointsOfInterestChangeListener implements PropertyChangeListener {

		@SuppressWarnings("unchecked")
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			plan.getRoot().getChildren().clear();
			for (Waypoint waypoint : (Iterable<Waypoint>) evt.getNewValue()) {
				plan.getRoot().getChildren().add(new TreeItem<Waypoint>(waypoint));
			}
		}
		
	}
	
}
