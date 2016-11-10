package com.cfar.swim.worldwind.ui.world;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwindx.examples.util.ButtonAnnotation;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

public class WorldPresenter implements Initializable {

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
        		
        		BufferedImage droneImage = null;
        		BufferedImage swimImage = null;
        		BufferedImage envImage = null;
        		BufferedImage editImage = null;
        		BufferedImage planImage = null;
        		BufferedImage takeoffImage = null;
        		BufferedImage landImage = null;
        		
        		try {
        			droneImage = ImageIO.read(this.getClass().getResourceAsStream("/icons/quadcopter-64x64.png"));
        			swimImage = ImageIO.read(this.getClass().getResourceAsStream("/icons/swim-64x64.png"));
        			envImage = ImageIO.read(this.getClass().getResourceAsStream("/icons/environment-64x64.png"));
        			editImage = ImageIO.read(this.getClass().getResourceAsStream("/icons/edit-64x64.png"));
					planImage = ImageIO.read(this.getClass().getResourceAsStream("/icons/plan-64x64.png"));
					takeoffImage = ImageIO.read(this.getClass().getResourceAsStream("/icons/quadcopter-takeoff-64x64.png"));
					landImage = ImageIO.read(this.getClass().getResourceAsStream("/icons/quadcopter-land-64x64.png"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
        		
        		ButtonAnnotation droneButton = new ButtonAnnotation(droneImage, droneImage);
        		droneButton.getAttributes().setFrameShape(AVKey.SHAPE_CIRCLE);
                droneButton.getAttributes().setDrawOffset(new Point(425, 25));
                droneButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("pressed....");
					}
                	
                });
        		
                ButtonAnnotation swimButton = new ButtonAnnotation(swimImage, swimImage);
        		swimButton.getAttributes().setFrameShape(AVKey.SHAPE_CIRCLE);
                swimButton.getAttributes().setDrawOffset(new Point(500, 25));
                swimButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("pressed....");
					}
                	
                });
                
        		ButtonAnnotation envButton = new ButtonAnnotation(envImage, envImage);
        		envButton.getAttributes().setFrameShape(AVKey.SHAPE_CIRCLE);
                envButton.getAttributes().setDrawOffset(new Point(575, 25));
                envButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("pressed....");
					}
                	
                });
                
                ButtonAnnotation editButton = new ButtonAnnotation(editImage, editImage);
        		editButton.getAttributes().setFrameShape(AVKey.SHAPE_CIRCLE);
                editButton.getAttributes().setDrawOffset(new Point(650, 25));
                editButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("pressed....");
					}
                	
                });
                
                ButtonAnnotation planButton = new ButtonAnnotation(planImage, planImage);
        		planButton.getAttributes().setFrameShape(AVKey.SHAPE_CIRCLE);
                planButton.getAttributes().setDrawOffset(new Point(725, 25));
                planButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("pressed....");
					}
                	
                });
                
                ButtonAnnotation takeoffButton = new ButtonAnnotation(takeoffImage, takeoffImage);
        		takeoffButton.getAttributes().setFrameShape(AVKey.SHAPE_CIRCLE);
                takeoffButton.getAttributes().setDrawOffset(new Point(800, 25));
                takeoffButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("pressed....");
					}
                	
                });
                
                ButtonAnnotation landButton = new ButtonAnnotation(landImage, landImage);
        		landButton.getAttributes().setFrameShape(AVKey.SHAPE_CIRCLE);
                landButton.getAttributes().setDrawOffset(new Point(875, 25));
                landButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("pressed....");
					}
                	
                });
                
                AnnotationLayer annotationLayer = new AnnotationLayer();
                annotationLayer.addAnnotation(droneButton);
                annotationLayer.addAnnotation(swimButton);
                annotationLayer.addAnnotation(envButton);
                annotationLayer.addAnnotation(editButton);
                annotationLayer.addAnnotation(planButton);
                annotationLayer.addAnnotation(takeoffButton);
                annotationLayer.addAnnotation(landButton);
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
