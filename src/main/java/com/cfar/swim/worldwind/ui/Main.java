package com.cfar.swim.worldwind.ui;
	
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.planner.PlannerView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
	
	public static final String APPLICATION_TITLE = "Worldwind Planner";
	
	@Override
	public void start(Stage primaryStage) {
		try {
			SessionManager.getInstance().addSession(new Session(Main.APPLICATION_TITLE));
			PlannerView plannerView = new PlannerView();
			Scene scene = new Scene(plannerView.getView());
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle(Main.APPLICATION_TITLE);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
