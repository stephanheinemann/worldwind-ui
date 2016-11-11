package com.cfar.swim.worldwind.ui.world;

import java.awt.BorderLayout;
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

import com.cfar.swim.worldwind.render.annotations.ControlAnnotation;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
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
	
	public static final String ACTION_AICRAFT_LOAD = "WorldPresenter.ActionCommand.AircraftLoad";
	public static final String ACTION_AIRCAFT_SETUP = "WorldPresenter.ActionCommand.AircraftSetup";
	public static final String ACTION_SWIM_LOAD = "WorldPresenter.ActionCommand.SwimLoad";
	public static final String ACTION_SWIM_SETUP = "WorldPresenter.ActionCommand.SwimSetup";
	public static final String ACTION_ENVIRONMENT_ENCLOSE = "WorldPresenter.ActionCommand.EnvironmentEnclose";
	public static final String ACTION_ENVIRONMENT_SETUP = "WorldPresenter.ActionCommand.EnvironmentSetup";
	public static final String ACTION_POI_EDIT = "WorldPresenter.ActionCommand.PointOfInterestEdit";
	public static final String ACTION_POI_VIEW = "WorldPresenter.ActionCommand.PointOfInterestView";
	public static final String ACTION_PLANNER_PLAN = "WorldPresenter.ActionCommand.PlannerPlan";
	public static final String ACTION_PLANNER_SETUP = "WorldPresenter.ActionCommand.PlannerSetup";
	public static final String ACTION_TAKEOFF = "WorldPresenter.ActionCommand.TakeOff";
	public static final String ACTION_LAND = "WorldPresenter.ActionCommand.Land";
	
	@FXML
	AnchorPane worldNodePane;
	
	@FXML
	SwingNode worldNode;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	JPanel worldPanel = new JPanel(new BorderLayout());
            	
            	WorldWindowGLJPanel wwd = new WorldWindowGLJPanel();
                wwd.setPreferredSize(new java.awt.Dimension(1366, 480));
        		wwd.setModel(new BasicModel());
        		
        		ViewControlsLayer viewControlsLayer = new ViewControlsLayer();
        		wwd.getModel().getLayers().add(viewControlsLayer);
        		wwd.addSelectListener(new ViewControlsSelectListener(wwd, viewControlsLayer));
        		
        		/*
        		Configuration.setValue(
                		AVKey.MIL_STD_2525_ICON_RETRIEVER_PATH,
                		ClassLoader.getSystemResource("milstd2525"));
        		
        		String iconPath = Configuration.getStringValue(AVKey.MIL_STD_2525_ICON_RETRIEVER_PATH);
        		MilStd2525IconRetriever iconRetriever = new MilStd2525IconRetriever(iconPath);
        		BufferedImage image = iconRetriever.createIcon("G-GPGPO--------", null);
        		*/
        		
        		
        		ControlAnnotation aircraftControl = new ControlAnnotation(aircraftIcon);
                aircraftControl.getAttributes().setDrawOffset(new Point(425, 25));
                aircraftControl.setPrimaryActionCommand(WorldPresenter.ACTION_AICRAFT_LOAD);
                aircraftControl.setSecondaryActionCommand(WorldPresenter.ACTION_AIRCAFT_SETUP);
                aircraftControl.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("pressed...." + e.getActionCommand());
					}
                	
                });
        		
                ControlAnnotation swimControl = new ControlAnnotation(swimIcon);
                swimControl.getAttributes().setDrawOffset(new Point(500, 25));
                swimControl.setPrimaryActionCommand(WorldPresenter.ACTION_SWIM_LOAD);
                swimControl.setSecondaryActionCommand(WorldPresenter.ACTION_SWIM_SETUP);
                swimControl.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("pressed...." + e.getActionCommand());
					}
                	
                });
                
        		ControlAnnotation environmentControl = new ControlAnnotation(environmentIcon);
                environmentControl.getAttributes().setDrawOffset(new Point(575, 25));
                environmentControl.setPrimaryActionCommand(WorldPresenter.ACTION_ENVIRONMENT_ENCLOSE);
                environmentControl.setSecondaryActionCommand(ACTION_ENVIRONMENT_SETUP);
                environmentControl.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("pressed...." + e.getActionCommand());
					}
                	
                });
                
                ControlAnnotation poiControl = new ControlAnnotation(poiIcon);
                poiControl.getAttributes().setDrawOffset(new Point(650, 25));
                poiControl.setPrimaryActionCommand(WorldPresenter.ACTION_POI_EDIT);
                poiControl.setSecondaryActionCommand(WorldPresenter.ACTION_POI_VIEW);
                poiControl.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("pressed...."  + e.getActionCommand());
					}
                	
                });
                
                ControlAnnotation plannerControl = new ControlAnnotation(plannerIcon);
                plannerControl.getAttributes().setDrawOffset(new Point(725, 25));
                plannerControl.setPrimaryActionCommand(WorldPresenter.ACTION_PLANNER_PLAN);
                plannerControl.setSecondaryActionCommand(WorldPresenter.ACTION_PLANNER_SETUP);
                plannerControl.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("pressed...." + e.getActionCommand());
					}
                	
                });
                
                ControlAnnotation takeoffControl = new ControlAnnotation(takeoffIcon);
                takeoffControl.getAttributes().setDrawOffset(new Point(800, 25));
                takeoffControl.setPrimaryActionCommand(WorldPresenter.ACTION_TAKEOFF);
                takeoffControl.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("pressed...." + e.getActionCommand());
					}
                	
                });
                
                ControlAnnotation landControl = new ControlAnnotation(landIcon);
                landControl.getAttributes().setDrawOffset(new Point(875, 25));
                landControl.setPrimaryActionCommand(WorldPresenter.ACTION_LAND);
                landControl.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("pressed...."  + e.getActionCommand());
					}
                	
                });
                
                AnnotationLayer annotationLayer = new AnnotationLayer();
                annotationLayer.addAnnotation(aircraftControl);
                wwd.addSelectListener(aircraftControl);
                annotationLayer.addAnnotation(swimControl);
                wwd.addSelectListener(swimControl);
                annotationLayer.addAnnotation(environmentControl);
                wwd.addSelectListener(environmentControl);
                annotationLayer.addAnnotation(poiControl);
                wwd.addSelectListener(poiControl);
                annotationLayer.addAnnotation(plannerControl);
                wwd.addSelectListener(plannerControl);
                annotationLayer.addAnnotation(takeoffControl);
                wwd.addSelectListener(takeoffControl);
                annotationLayer.addAnnotation(landControl);
                wwd.addSelectListener(landControl);
                wwd.getModel().getLayers().add(annotationLayer);
        		
        		worldPanel.add(wwd, BorderLayout.PAGE_START);
        		
        		StatusBar statusBar = new StatusBar();
        		statusBar.setEventSource(wwd);
        		worldPanel.add(statusBar, BorderLayout.PAGE_END);
        		
        		worldNode.setContent(worldPanel);
        		
        		// TODO: implement an edit mode for the plan
        		// TODO: click on positions during edit - locations will be added to plan
        		// TODO: use safe default altitude (to be edited)
        		// TODO: locations and altitudes can be edited once added
        		// TODO: represent and highlight all waypoints with 2525 symbols
        		
        		// TODO: selected waypoints will be displayed as POINT waypoints
        		// TODO: computed waypoints will be displayed as ROUTE waypoints
        		// TODO: OPTIMAL / LEAST RISK routes can be tagged using appropriate symbology
        		
        		wwd.addMouseListener(new MouseAdapter() {
        			public void mouseClicked(MouseEvent e) {
        				Position clickedPosition = wwd.getCurrentPosition();
        				if (null != clickedPosition) {
	        				double latitude = clickedPosition.getLatitude().getDegrees();
	        				double longitude = clickedPosition.getLongitude().getDegrees();
	        				double altitude = clickedPosition.getAltitude();
	        				System.out.println("Clicked Latitude: " + latitude);
	        				System.out.println("Clicked Longitude: " + longitude);
	        				System.out.println("Clicked Altitude: " + altitude);
        				}
        			}
        		});
            }
        });
	}
	
}
