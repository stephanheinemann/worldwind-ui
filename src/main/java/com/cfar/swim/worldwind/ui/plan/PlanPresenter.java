package com.cfar.swim.worldwind.ui.plan;

import java.net.URL;
import java.util.ResourceBundle;

import com.cfar.swim.worldwind.planning.Waypoint;
import com.cfar.swim.worldwind.ui.plan.waypoint.WaypointView;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
	private TreeTableColumn<Waypoint, String> positionColumn;
	
	@FXML
	private TreeTableColumn<Waypoint, Double> costsColumn;
	
	@FXML
	private TreeTableColumn<Waypoint, String> distanceToGoColumn;
	
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
		
		this.positionColumn.setCellValueFactory(
			(TreeTableColumn.CellDataFeatures<Waypoint, String> param) ->
			new ReadOnlyStringWrapper(param.getValue().getValue().toString()));
		
		this.costsColumn.setCellValueFactory(
				new TreeItemPropertyValueFactory<Waypoint, Double>("g"));
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
	
}
