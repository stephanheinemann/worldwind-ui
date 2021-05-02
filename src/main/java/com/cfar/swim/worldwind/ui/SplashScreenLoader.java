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

import com.cfar.swim.worldwind.ui.splash.SplashView;

import javafx.application.Application;
import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Realizes a splash screen loader.
 * 
 * @author Stephan Heinemann
 *
 */
public class SplashScreenLoader extends Preloader {
	
	/** the splash screen stage of this splash screen loader */
	private Stage splashScreen;
	
	/** the spash scene of this splash screen loader */
	private Scene splashScene;
	
	/**
	 * Initializes this splash screen loader loading the splash
	 * view and caching the splash images.
	 * 
	 * @see Application#init()
	 */
	@Override
	public void init() throws Exception {
		SplashView splash = new SplashView();
    	splashScene = new Scene(splash.getView(), 600, 400);
    	splashScene.setFill(Color.LIGHTGRAY);
	}
	
	/**
	 * Starts this splash screen loader.
	 * 
	 * @see Preloader#start(Stage)
	 */
    @Override
    public void start(Stage stage) throws Exception {
    	splashScreen = stage;
        splashScreen.initStyle(StageStyle.UNDECORATED);
        splashScreen.setScene(this.splashScene);
        splashScreen.show();
    }
    
    /**
     * Handles an application notification which closes this
     * splash screen loader once the application has loaded.
     * 
     * @see Preloader#handleApplicationNotification(PreloaderNotification)
     */
    @Override
    public void handleApplicationNotification(PreloaderNotification notification) {    	
    	if (notification instanceof StateChangeNotification) {
        	splashScreen.close();
        }
    }

}
