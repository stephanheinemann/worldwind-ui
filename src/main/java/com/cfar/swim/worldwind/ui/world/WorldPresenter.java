package com.cfar.swim.worldwind.ui.world;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.cfar.swim.worldwind.planning.Waypoint;
import com.cfar.swim.worldwind.render.annotations.ControlAnnotation;
import com.cfar.swim.worldwind.session.Scenario;
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
import gov.nasa.worldwind.symbology.SymbologyConstants;
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
	public static final String ACTION_POI_EDIT = "WorldPresenter.ActionCommand.PointOfInterestEdit";
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
	RenderableLayer planningLayer = new RenderableLayer();
	MilStd2525GraphicFactory symbolFactory = new MilStd2525GraphicFactory();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		SwingUtilities.invokeLater(new WorldInitializer());
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
			wwd.getModel().getLayers().add(planningLayer);
			
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
			poiControl.setPrimaryActionCommand(WorldPresenter.ACTION_POI_EDIT);
			poiControl.setSecondaryActionCommand(WorldPresenter.ACTION_NONE);
			poiControl.addActionListener(new PointOfInterestControlListener());
			
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
    		
    		// TODO: implement an edit mode for the plan
    		// TODO: click on positions during edit - locations will be added to plan
    		// TODO: use safe default altitude (to be edited)
    		// TODO: locations and altitudes can be edited once added
    		
    		// TODO: selected waypoints will be displayed as POINT waypoints
    		// TODO: computed waypoints will be displayed as ROUTE waypoints
    		// TODO: OPTIMAL / LEAST RISK routes can be tagged using appropriate symbology
    		
    		wwd.addMouseListener(new WorldMouseListener());
		}
	}
	
	private class WorldMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (worldModel.getMode().equals(WorldMode.POI)) {
				SwingUtilities.invokeLater(new PointOfInterestMouseHandler());
			}
		}
	}
	
	private class PointOfInterestMouseHandler implements Runnable {

		@Override
		public void run() {
			Position clickedPosition = wwd.getCurrentPosition();
			if (null != clickedPosition) {
				Waypoint waypoint = new Waypoint(clickedPosition);
				waypoint.setDepiction(new Depiction(symbolFactory.createPoint(Waypoint.SIDC_NAV_WAYPOINT_POI, waypoint, null)));
				waypoint.getDepiction().setModifier(SymbologyConstants.UNIQUE_DESIGNATION, waypoint.getDesignator());
				waypoint.getDepiction().setVisible(true);
				planningLayer.addRenderable(waypoint);
				
				Scenario scenario = SessionManager.getInstance().getSession(Main.APPLICATION_TITLE).getScenario("default");
				scenario.addPointOfInterest(waypoint);
			}
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
				break;
			case WorldPresenter.ACTION_ENVIRONMENT_SETUP:
				break;
			}
			System.out.println("pressed...." + e.getActionCommand());
		}
	}
	
	private class PointOfInterestControlListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_POI_EDIT:
				worldModel.setMode(WorldMode.POI);
				statusLayer.getAnnotations().iterator().next().setText(WorldMode.POI.toString());
				break;
			default:
				worldModel.setMode(WorldMode.VIEW);
				statusLayer.getAnnotations().iterator().next().setText(WorldMode.VIEW.toString());
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
