package com.cfar.swim.worldwind.ui.world;

import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
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
            	WorldWindowGLJPanel wwd = new WorldWindowGLJPanel();
                wwd.setPreferredSize(new java.awt.Dimension(1366, 500));
        		wwd.setModel(new BasicModel());
        		worldNode.setContent(wwd);
            }
        });
	}
	
}
