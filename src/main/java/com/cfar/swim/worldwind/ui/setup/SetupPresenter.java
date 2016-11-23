package com.cfar.swim.worldwind.ui.setup;

import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;

import com.cfar.swim.worldwind.planning.Environment;
import com.cfar.swim.worldwind.registries.Specification;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.Main;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;

public class SetupPresenter implements Initializable {
	
	@FXML
	private ScrollPane envPropertiesPane;
	
	@FXML
	private ComboBox<String> environment;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Session session = SessionManager.getInstance().getSession(Main.APPLICATION_TITLE);
		for (Specification<Environment> envSpec : session.getEnvironmentSpecifications()) {
			this.environment.getItems().add(envSpec.getId());
		}
		this.environment.layout();
		this.environment.getSelectionModel().selectFirst();
		
		String envId = environment.getSelectionModel().getSelectedItem();
		Specification<Environment> envSpec = session.getEnvironmentSpecification(envId);
		PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(envSpec.getProperties()));
		this.envPropertiesPane.setContent(propertySheet);
		
		this.environment.valueProperty().addListener(new EnvironmentChangeListener());
	}
	
	private class EnvironmentChangeListener implements ChangeListener<String> {

		@Override
		public void changed(ObservableValue<? extends String> observable, String oldEnvId, String newEnvId) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Session session = SessionManager.getInstance().getSession(Main.APPLICATION_TITLE);
					Specification<Environment> envSpec = session.getEnvironmentSpecification(newEnvId);
					PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(envSpec.getProperties()));
					envPropertiesPane.setContent(propertySheet);
					envPropertiesPane.layout();
				}
			});
		}
	}

}
