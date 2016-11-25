package com.cfar.swim.worldwind.ui.environment;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ResourceBundle;

import com.cfar.swim.worldwind.planning.Environment;
import com.cfar.swim.worldwind.session.Scenario;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.util.StringConverter;

public class EnvironmentPresenter implements Initializable {

	@FXML
	private TreeView<Environment> environment;
	
	private EnvironmentChangeListener ecl = new EnvironmentChangeListener();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.environment.setCellFactory(TextFieldTreeCell.forTreeView(new EnvironmentConverter()));
		Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
		session.addActiveScenarioChangeListener(new ActiveScenarioChangeListener());
		initScenario();
		initEnvironment();
	}
	
	public void initScenario() {
		Scenario scenario = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).getActiveScenario();
		scenario.removePropertyChangeListener(this.ecl);
		scenario.addEnvironmentChangeListener(this.ecl);
	}
	
	public void initEnvironment() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Environment activeEnvironment = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).getActiveScenario().getEnvironment();
				environment.setRoot(new TreeItem<Environment>(activeEnvironment));
				environment.getRoot().setExpanded(false);
				initEnvironment(environment.getRoot());
			}
		});
	}
	
	public void initEnvironment(TreeItem<Environment> parentItem) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (parentItem.getValue().isRefined()) {
					for (Environment child : parentItem.getValue().getRefinements()) {
						TreeItem<Environment> childItem = new TreeItem<>(child);
						parentItem.getChildren().add(childItem);
						initEnvironment(childItem);
					}
				}
			}
		});
	}
	
	public void refineEnvironment() {
		Environment selectedEnv = this.environment.getSelectionModel().getSelectedItem().getValue();
		if (!selectedEnv.isRefined()) {
			Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
			selectedEnv.refine(2);
			session.getActiveScenario().notifyEnvironmentChange();
		}
	}
	
	public void coarsenEnvironment() {
		Environment selectedEnv = this.environment.getSelectionModel().getSelectedItem().getValue();
		if (selectedEnv.isRefined()) {
			Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
			selectedEnv.coarsen();
			session.getActiveScenario().notifyEnvironmentChange();
		}
	}
	
	private class EnvironmentConverter extends StringConverter<Environment> {

		@Override
		public String toString(Environment environment) {
			return Integer.toString(environment.getRefinements().size());
		}

		@Override
		public Environment fromString(String environment) {
			return null;
		}
	}
	
	private class ActiveScenarioChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initScenario();
			initEnvironment();
		}
	}
	
	private class EnvironmentChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initEnvironment();
		}
	}

}
