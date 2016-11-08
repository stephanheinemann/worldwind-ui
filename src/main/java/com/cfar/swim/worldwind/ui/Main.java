package com.cfar.swim.worldwind.ui;
	
import com.cfar.swim.worldwind.ui.planner.PlannerView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			PlannerView plannerView = new PlannerView();
			Scene scene = new Scene(plannerView.getView());
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("Worldwind Planner");
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
