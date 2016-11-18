package com.cfar.swim.worldwind.ui.world;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.cfar.swim.worldwind.planning.Waypoint;
import com.cfar.swim.worldwind.render.annotations.ControlAnnotation;
import com.cfar.swim.worldwind.session.Scenario;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.Main;
import com.cfar.swim.worldwind.util.Depiction;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525GraphicFactory;
import gov.nasa.worldwind.util.StatusBar;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

public class WorldPresenter implements Initializable {
	
	@Inject private String aircraftIcon;
	@Inject private String swimIcon;
	@Inject private String environmentIcon;
	@Inject private String poiIcon;
	@Inject private String plannerIcon;
	@Inject private String takeoffIcon;
	@Inject private String landIcon;
	
	public static final String ACTION_NONE = "WorldPresenter.ActionCommand.None";
	public static final String ACTION_AICRAFT_LOAD = "WorldPresenter.ActionCommand.AircraftLoad";
	public static final String ACTION_AIRCAFT_SETUP = "WorldPresenter.ActionCommand.AircraftSetup";
	public static final String ACTION_SWIM_LOAD = "WorldPresenter.ActionCommand.SwimLoad";
	public static final String ACTION_SWIM_SETUP = "WorldPresenter.ActionCommand.SwimSetup";
	public static final String ACTION_ENVIRONMENT_ENCLOSE = "WorldPresenter.ActionCommand.EnvironmentEnclose";
	public static final String ACTION_ENVIRONMENT_SETUP = "WorldPresenter.ActionCommand.EnvironmentSetup";
	public static final String ACTION_WAYPOINT_EDIT = "WorldPresenter.ActionCommand.WaypointEdit";
	public static final String ACTION_WAYPOINT_SETUP = "WorldPresenter.ActionCommand.WaypointSetup";
	public static final String ACTION_PLANNER_PLAN = "WorldPresenter.ActionCommand.PlannerPlan";
	public static final String ACTION_PLANNER_SETUP = "WorldPresenter.ActionCommand.PlannerSetup";
	public static final String ACTION_TAKEOFF = "WorldPresenter.ActionCommand.TakeOff";
	public static final String ACTION_LAND = "WorldPresenter.ActionCommand.Land";
	
	@FXML
	AnchorPane worldNodePane;
	
	@FXML
	SwingNode worldNode;

	WorldModel worldModel = new WorldModel();
	WorldWindowGLJPanel wwd = new WorldWindowGLJPanel();
	AnnotationLayer controlLayer = new AnnotationLayer();
	AnnotationLayer statusLayer = new AnnotationLayer();
	RenderableLayer waypointLayer = new RenderableLayer();
	MilStd2525GraphicFactory symbolFactory = new MilStd2525GraphicFactory();
	
	Scenario scenario = null;
	WaypointsChangeListener wcl = new WaypointsChangeListener();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		SwingUtilities.invokeLater(new WorldInitializer());
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
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				waypointLayer.removeAllRenderables();
				waypointLayer.addRenderables(scenario.getWaypoints());
				wwd.redraw();
			}
		});
	}
	
	private void displayStatus(String status) {
		statusLayer.getAnnotations().iterator().next().setText(status);
	}
	
	private class WorldInitializer implements Runnable {

		@Override
		public void run() {
			JPanel worldPanel = new JPanel(new BorderLayout());
			
			// initialize world window
			wwd.setPreferredSize(new java.awt.Dimension(1366, 480));
			wwd.setModel(new BasicModel());
			
			// add view controls
			ViewControlsLayer viewControlsLayer = new ViewControlsLayer();
			wwd.getModel().getLayers().add(viewControlsLayer);
			wwd.addSelectListener(new ViewControlsSelectListener(wwd, viewControlsLayer));
			
			// add planner controls
			wwd.getModel().getLayers().add(waypointLayer);
			
			ControlAnnotation aircraftControl = new ControlAnnotation(aircraftIcon);
			aircraftControl.getAttributes().setDrawOffset(new Point(425, 25));
			aircraftControl.setPrimaryActionCommand(WorldPresenter.ACTION_AICRAFT_LOAD);
			aircraftControl.setSecondaryActionCommand(WorldPresenter.ACTION_AIRCAFT_SETUP);
			aircraftControl.addActionListener(new AircraftControlListener());
			
			ControlAnnotation swimControl = new ControlAnnotation(swimIcon);
			swimControl.getAttributes().setDrawOffset(new Point(500, 25));
			swimControl.setPrimaryActionCommand(WorldPresenter.ACTION_SWIM_LOAD);
			swimControl.setSecondaryActionCommand(WorldPresenter.ACTION_SWIM_SETUP);
			swimControl.addActionListener(new SwimControlListener());
			
			ControlAnnotation environmentControl = new ControlAnnotation(environmentIcon);
			environmentControl.getAttributes().setDrawOffset(new Point(575, 25));
			environmentControl.setPrimaryActionCommand(WorldPresenter.ACTION_ENVIRONMENT_ENCLOSE);
			environmentControl.setSecondaryActionCommand(ACTION_ENVIRONMENT_SETUP);
			environmentControl.addActionListener(new EnvironmentControlListener());
			
			ControlAnnotation poiControl = new ControlAnnotation(poiIcon);
			poiControl.getAttributes().setDrawOffset(new Point(650, 25));
			poiControl.setPrimaryActionCommand(WorldPresenter.ACTION_WAYPOINT_EDIT);
			poiControl.setSecondaryActionCommand(WorldPresenter.ACTION_WAYPOINT_SETUP);
			poiControl.addActionListener(new WaypointsControlListener());
			
			ControlAnnotation plannerControl = new ControlAnnotation(plannerIcon);
			plannerControl.getAttributes().setDrawOffset(new Point(725, 25));
			plannerControl.setPrimaryActionCommand(WorldPresenter.ACTION_PLANNER_PLAN);
			plannerControl.setSecondaryActionCommand(WorldPresenter.ACTION_PLANNER_SETUP);
			plannerControl.addActionListener(new PlannerControlListener());
			
			ControlAnnotation takeoffControl = new ControlAnnotation(takeoffIcon);
			takeoffControl.getAttributes().setDrawOffset(new Point(800, 25));
			takeoffControl.setPrimaryActionCommand(WorldPresenter.ACTION_TAKEOFF);
			takeoffControl.setSecondaryActionCommand(WorldPresenter.ACTION_NONE);
			takeoffControl.addActionListener(new TakeOffControlListener());
			
			ControlAnnotation landControl = new ControlAnnotation(landIcon);
			landControl.getAttributes().setDrawOffset(new Point(875, 25));
			landControl.setPrimaryActionCommand(WorldPresenter.ACTION_LAND);
			landControl.setSecondaryActionCommand(WorldPresenter.ACTION_NONE);
			landControl.addActionListener(new LandControlListener());
			
			controlLayer.addAnnotation(aircraftControl);
			wwd.addSelectListener(aircraftControl);
			controlLayer.addAnnotation(swimControl);
			wwd.addSelectListener(swimControl);
			controlLayer.addAnnotation(environmentControl);
			wwd.addSelectListener(environmentControl);
			controlLayer.addAnnotation(poiControl);
			wwd.addSelectListener(poiControl);
			controlLayer.addAnnotation(plannerControl);
			wwd.addSelectListener(plannerControl);
			controlLayer.addAnnotation(takeoffControl);
			wwd.addSelectListener(takeoffControl);
			controlLayer.addAnnotation(landControl);
			wwd.addSelectListener(landControl);
			wwd.getModel().getLayers().add(controlLayer);
			
			// add on-screen status
			ScreenAnnotation statusAnnotation = new ScreenAnnotation("----------", new Point(650, 400));
			statusAnnotation.setAlwaysOnTop(true);
			statusAnnotation.getAttributes().setAdjustWidthToText("----------");
			statusAnnotation.getAttributes().setTextAlign(AVKey.CENTER);
			statusAnnotation.getAttributes().setTextColor(Color.BLACK);
			statusAnnotation.getAttributes().setBackgroundColor(Color.LIGHT_GRAY);
			statusAnnotation.getAttributes().setBorderColor(Color.BLACK);
			statusAnnotation.getAttributes().setOpacity(0.5d);
			statusAnnotation.setText(WorldMode.VIEW.toString());
			statusLayer.addAnnotation(statusAnnotation);
			wwd.getModel().getLayers().add(statusLayer);
			
			// set world panel / node layout
			worldPanel.add(wwd, BorderLayout.PAGE_START);
			
			StatusBar statusBar = new StatusBar();
			statusBar.setEventSource(wwd);
			worldPanel.add(statusBar, BorderLayout.PAGE_END);
			
			worldNode.setContent(worldPanel);
			
    		// TODO: use safe default altitude (to be edited)
    		// TODO: selected waypoints will be displayed as POINT waypoints
    		// TODO: computed waypoints will be displayed as ROUTE waypoints
    		// TODO: OPTIMAL / LEAST RISK routes can be tagged using appropriate symbology
    		
    		wwd.addMouseListener(new WorldMouseListener());
		}
	}
	
	private class WorldMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (worldModel.getMode().equals(WorldMode.WAYPOINT)) {
				SwingUtilities.invokeLater(new WaypointMouseHandler());
			}
		}
	}
	
	private class WaypointMouseHandler implements Runnable {

		@Override
		public void run() {
			Position clickedPosition = wwd.getCurrentPosition();
			if (null != clickedPosition) {
				Waypoint waypoint = new Waypoint(clickedPosition);
				waypoint.setDepiction(new Depiction(symbolFactory.createPoint(Waypoint.SIDC_NAV_WAYPOINT_POI, waypoint, null)));
				waypoint.getDepiction().setVisible(true);
				scenario.addWaypoint(waypoint);
			}
		}
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
	
	private class AircraftControlListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("pressed...." + e.getActionCommand());
		}
	}
	
	private class SwimControlListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("pressed...." + e.getActionCommand());
		}
	}
	
	private class EnvironmentControlListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_ENVIRONMENT_ENCLOSE:
				worldModel.setMode(WorldMode.ENVIRONMENT);
				displayStatus(WorldMode.ENVIRONMENT.toString());
				break;
			case WorldPresenter.ACTION_ENVIRONMENT_SETUP:
				worldModel.setMode(WorldMode.VIEW);
				displayStatus(WorldMode.VIEW.toString());
				break;
			}
			System.out.println("pressed...." + e.getActionCommand());
		}
	}
	
	private class WaypointsControlListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_WAYPOINT_EDIT:
				worldModel.setMode(WorldMode.WAYPOINT);
				displayStatus(WorldMode.WAYPOINT.toString());
				break;
			case WorldPresenter.ACTION_WAYPOINT_SETUP:
				// TODO: waypoint setup (types of waypoint graphics: POI, RWP, ...)
				worldModel.setMode(WorldMode.VIEW);
				displayStatus(WorldMode.VIEW.toString());
				break;
			default:
				
			}
			System.out.println("pressed...." + e.getActionCommand());
		}
	}
	
	private class PlannerControlListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("pressed...." + e.getActionCommand());
		}
	}
	
	private class TakeOffControlListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("pressed...." + e.getActionCommand());
		}
	}
	
	private class LandControlListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("pressed...." + e.getActionCommand());
		}
	}
	
}
