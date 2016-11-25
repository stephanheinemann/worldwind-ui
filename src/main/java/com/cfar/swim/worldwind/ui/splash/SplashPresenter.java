package com.cfar.swim.worldwind.ui.splash;

import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SplashPresenter implements Initializable {

	@Inject private String uvicLogo;
	@Inject private String cfarLogo;
	
	@FXML
	ImageView uvic;
	
	@FXML
	ImageView cfar;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ClassLoader classLoader = this.getClass().getClassLoader();
		uvic.setImage(new Image(classLoader.getResourceAsStream(uvicLogo)));
		cfar.setImage(new Image(classLoader.getResourceAsStream(cfarLogo)));
	}

}
