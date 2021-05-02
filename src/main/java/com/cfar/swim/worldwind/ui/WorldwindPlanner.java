/**
 * Copyright (c) 2021, Stephan Heinemann (UVic Center for Aerospace Research)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

/**
 * Realizes a planner application based on NASA Worldwind.
 * 
 * @author Stephan Heinemann
 *
 */
public class WorldwindPlanner extends Application {

	/** the title (and session identifier) of this application */
	public static final String APPLICATION_TITLE = "Worldwind Planner";
	
	/**
	 * Starts this planner application.
	 * 
	 * @param primaryStage the primary stage of this planner application
	 * 
	 * @throws Exception never (but prints stack trace)
	 * 
	 * @see Application#start(Stage)
	 */
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
	
	/**
	 * Stops this planner application clearing all injected properties.
	 * 
	 * @throws Exception if the super method throws it
	 * 
	 * @see Application#stop()
	 */
	@Override
	public void stop() throws Exception {
        super.stop();
		Injector.forgetAll();
    }

}
