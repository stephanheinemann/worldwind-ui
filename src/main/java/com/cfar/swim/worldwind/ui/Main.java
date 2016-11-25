package com.cfar.swim.worldwind.ui;
	
import com.sun.javafx.application.LauncherImpl;

public class Main {
	
	public static void main(String[] args) {
		// TODO: use external artifact with preloader manifest
		LauncherImpl.launchApplication(WorldwindPlanner.class, SplashScreenLoader.class, args);
	}
}
