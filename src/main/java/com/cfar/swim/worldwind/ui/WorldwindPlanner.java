package com.cfar.swim.worldwind.ui;

import com.airhacks.afterburner.injection.Injector;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.planner.PlannerView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader.StateChangeNotification;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WorldwindPlanner extends Application {

	public static final String APPLICATION_TITLE = "Worldwind Planner";

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			/*
			Map<Object, Object> customProperties = new HashMap<>();
	        customProperties.put("date", date);
	        Injector.setConfigurationSource(customProperties::get);
	        */
			SessionManager.getInstance().addSession(new Session(WorldwindPlanner.APPLICATION_TITLE));
			PlannerView plannerView = new PlannerView();
			Scene scene = new Scene(plannerView.getView());
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle(WorldwindPlanner.APPLICATION_TITLE);
			primaryStage.show();
			this.notifyPreloader(new StateChangeNotification(null));
			primaryStage.setOnCloseRequest(e -> { Platform.exit(); System.exit(0); });
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop() throws Exception {
        super.stop();
		Injector.forgetAll();
    }

}
