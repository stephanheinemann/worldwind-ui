package com.cfar.swim.worldwind.ui;

import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.planner.PlannerView;

import javafx.application.Application;
import javafx.application.Preloader.StateChangeNotification;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WorldwindPlanner extends Application {

	public static final String APPLICATION_TITLE = "Worldwind Planner";

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			SessionManager.getInstance().addSession(new Session(WorldwindPlanner.APPLICATION_TITLE));
			PlannerView plannerView = new PlannerView();
			Scene scene = new Scene(plannerView.getView());
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle(WorldwindPlanner.APPLICATION_TITLE);
			primaryStage.show();
			this.notifyPreloader(new StateChangeNotification(null));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
