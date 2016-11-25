package com.cfar.swim.worldwind.ui;

import com.cfar.swim.worldwind.ui.splash.SplashView;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreenLoader extends Preloader {
	
	private Stage splashScreen;
	private Scene splashScene;
	
	@Override
	public void init() throws Exception {
		SplashView splash = new SplashView();
    	splashScene = new Scene(splash.getView(), 600, 400);
    	splashScene.setFill(Color.LIGHTGRAY);
	}
	
    @Override
    public void start(Stage stage) throws Exception {
    	splashScreen = stage;
        splashScreen.initStyle(StageStyle.UNDECORATED);
        splashScreen.setScene(this.splashScene);
        splashScreen.show();
    }
    
    @Override
    public void handleApplicationNotification(PreloaderNotification notification) {    	
    	if (notification instanceof StateChangeNotification) {
        	splashScreen.close();
        }
    }

}
