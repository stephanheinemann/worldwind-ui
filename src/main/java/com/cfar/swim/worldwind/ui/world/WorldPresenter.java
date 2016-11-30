package com.cfar.swim.worldwind.ui.world;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.xml.sax.InputSource;

import com.cfar.swim.worldwind.ai.Planner;
import com.cfar.swim.worldwind.aircraft.Aircraft;
import com.cfar.swim.worldwind.iwxxm.IwxxmLoader;
import com.cfar.swim.worldwind.planning.CostInterval;
import com.cfar.swim.worldwind.planning.Environment;
import com.cfar.swim.worldwind.planning.Trajectory;
import com.cfar.swim.worldwind.planning.Waypoint;
import com.cfar.swim.worldwind.registries.Specification;
import com.cfar.swim.worldwind.render.Obstacle;
import com.cfar.swim.worldwind.render.annotations.ControlAnnotation;
import com.cfar.swim.worldwind.render.annotations.DepictionAnnotation;
import com.cfar.swim.worldwind.session.Scenario;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;
import com.cfar.swim.worldwind.ui.planner.PlannerAlert;
import com.cfar.swim.worldwind.ui.setup.SetupDialog;
import com.cfar.swim.worldwind.ui.setup.SetupModel;
import com.cfar.swim.worldwind.util.Depiction;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525GraphicFactory;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwindx.examples.util.SectorSelector;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class WorldPresenter implements Initializable {
	
	@Inject private String aircraftIcon;
	@Inject private String swimIcon;
	@Inject private String environmentIcon;
	@Inject private String poiIcon;
	@Inject private String plannerIcon;
	@Inject private String takeoffIcon;
	@Inject private String landIcon;
	@Inject private String setupIcon;
	
	public static final String ACTION_NONE = "WorldPresenter.ActionCommand.None";
	public static final String ACTION_AICRAFT_SET = "WorldPresenter.ActionCommand.AircraftSet";
	public static final String ACTION_AIRCAFT_SETUP = "WorldPresenter.ActionCommand.AircraftSetup";
	public static final String ACTION_SWIM_LOAD = "WorldPresenter.ActionCommand.SwimLoad";
	public static final String ACTION_SWIM_SETUP = "WorldPresenter.ActionCommand.SwimSetup";
	public static final String ACTION_ENVIRONMENT_ENCLOSE = "WorldPresenter.ActionCommand.EnvironmentEnclose";
	public static final String ACTION_ENVIRONMENT_SETUP = "WorldPresenter.ActionCommand.EnvironmentSetup";
	public static final String ACTION_WAYPOINT_EDIT = "WorldPresenter.ActionCommand.WaypointEdit";
	public static final String ACTION_WAYPOINT_SETUP = "WorldPresenter.ActionCommand.WaypointSetup";
	public static final String ACTION_PLANNER_PLAN = "WorldPresenter.ActionCommand.PlannerPlan";
	public static final String ACTION_PLANNER_SETUP = "WorldPresenter.ActionCommand.PlannerSetup";
	public static final String ACTION_TAKEOFF = "WorldPresenter.ActionCommand.TakeOff";
	public static final String ACTION_LAND = "WorldPresenter.ActionCommand.Land";
	
	public static final String FILE_CHOOSER_TITLE_SWIM = "Open SWIM File";
	public static final String FILE_CHOOSER_SWIM = "SWIM Files";
	public static final String FILE_CHOOSER_EXTENSION_SWIM = "*.xml";
	
	@FXML
	private AnchorPane worldNodePane;
	
	@FXML
	private SwingNode worldNode;
	
	@Inject
	WorldModel worldModel;
	
	@Inject
	SetupModel setupModel;
	
	WorldWindowGLJPanel wwd = new WorldWindowGLJPanel();
	AnnotationLayer controlLayer = new AnnotationLayer();
	AnnotationLayer statusLayer = new AnnotationLayer();
	RenderableLayer aircraftLayer = new RenderableLayer();
	RenderableLayer environmentLayer = new RenderableLayer();
	RenderableLayer waypointLayer = new RenderableLayer();
	RenderableLayer obstaclesLayer = new RenderableLayer();
	MilStd2525GraphicFactory symbolFactory = new MilStd2525GraphicFactory();
	SectorSelector sectorSelector = new SectorSelector(wwd);
	
	Scenario scenario = null;
	TimeChangeListener timeCl = new TimeChangeListener();
	ThresholdChangeListener thresholdCl = new ThresholdChangeListener();
	AircraftChangeListener aircraftCl = new AircraftChangeListener();
	EnvironmentChangeListener environmentCl = new EnvironmentChangeListener();
	WaypointsChangeListener waypointsCl = new WaypointsChangeListener();
	TrajectoryChangeListener trajectoryCl = new TrajectoryChangeListener();
	ObstaclesChangeListener obstaclesCl = new ObstaclesChangeListener();
	
	Executor executor = Executors.newSingleThreadScheduledExecutor();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		SwingUtilities.invokeLater(new WorldInitializer());
		Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
		session.addActiveScenarioChangeListener(new ActiveScenarioChangeListener());
		
		this.sectorSelector.setInteriorColor(Color.MAGENTA);
		this.sectorSelector.setInteriorOpacity(0.5d);
		this.sectorSelector.setBorderColor(Color.MAGENTA);
		this.sectorSelector.setBorderWidth(1d);
		this.sectorSelector.addPropertyChangeListener(SectorSelector.SECTOR_PROPERTY, new SectorChangeListener());
		
		this.initScenario();
		this.initAircraft();
		this.initEnvironment();
		this.initObstacles();
		this.initPlan();
	}
	
	public void initScenario() {
		// remove change listeners from the previous scenario if any
		if (null != this.scenario) {
			this.scenario.removePropertyChangeListener(this.timeCl);
			this.scenario.removePropertyChangeListener(this.thresholdCl);
			this.scenario.removePropertyChangeListener(this.aircraftCl);
			this.scenario.removePropertyChangeListener(this.environmentCl);
			this.scenario.removePropertyChangeListener(this.waypointsCl);
			this.scenario.removePropertyChangeListener(this.trajectoryCl);
			this.scenario.removePropertyChangeListener(this.obstaclesCl);
		}
		this.scenario = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).getActiveScenario();
		this.scenario.addTimeChangeListener(this.timeCl);
		this.scenario.addThresholdChangeListener(this.thresholdCl);
		this.scenario.addAircraftChangeListener(this.aircraftCl);
		this.scenario.addEnvironmentChangeListener(this.environmentCl);
		this.scenario.addWaypointsChangeListener(this.waypointsCl);
		this.scenario.addTrajectoryChangeListener(this.trajectoryCl);
		this.scenario.addObstaclesChangeListener(this.obstaclesCl);
	}
	
	public void initAircraft() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				aircraftLayer.removeAllRenderables();
				if (scenario.hasAircraft()) {
					aircraftLayer.addRenderable(scenario.getAircraft());
				}
				wwd.redraw();
			}
		});
	}
	
	public void initEnvironment() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				environmentLayer.removeAllRenderables();
				environmentLayer.addRenderable(scenario.getEnvironment());
				wwd.redraw();
			}
		});
	}
	
	public void initObstacles() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				obstaclesLayer.removeAllRenderables();
				obstaclesLayer.addRenderables(scenario.getObstacles());
				wwd.redraw();
			}
		});
	}
	
	public void initPlan() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				waypointLayer.removeAllRenderables();
				waypointLayer.addRenderables(scenario.getWaypoints());
				waypointLayer.addRenderable(scenario.getTrajectory());
				wwd.redraw();
			}
		});
	}
	
	private void setMode(WorldMode mode) {
		this.worldModel.setMode(mode);
		this.displayStatus(mode.toString());
	}
	
	private WorldMode getMode() {
		return this.worldModel.getMode();
	}
	
	private void displayStatus(String status) {
		statusLayer.getAnnotations().iterator().next().setText(status);
	}
	
	private void setup(int tabIndex) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				SetupDialog setupDialog = new SetupDialog(SetupDialog.TITLE_SETUP, SetupDialog.HEADER_SETUP, setupIcon, setupModel);
				setupDialog.selectTab(tabIndex);
				setupDialog.showAndWait();
			}			
		});
	}
	
	private void load(String title, ExtensionFilter[] extensions) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle(title);
				fileChooser.getExtensionFilters().addAll(extensions);
				File file = fileChooser.showOpenDialog(null);
				
				if (null != file) {
					executor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								// TODO: generic SWIM loader
								IwxxmLoader loader = new IwxxmLoader();
								Set<Obstacle> obstacles = loader.load(new InputSource(new FileInputStream(file)));
								for (Obstacle obstacle : obstacles) {
									scenario.addObstacle(obstacle);
								}
								setMode(WorldMode.VIEW);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
			}
		});
	}
	
	private void alert(AlertType type, String title, String header, String content) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				PlannerAlert alert = new PlannerAlert(type);
				alert.setTitle(title);
				alert.setHeaderText(header);
				alert.setContentText(content);
				alert.showAndWait();
			}
		});
	}
	
	private void plan() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				Specification<Planner> plannerSpec = session.getSetup().getPlannerSpecification();
				Planner planner = session.getPlannerFactory().createInstance(plannerSpec);
				Position origin = null;
				Position destination = null;
				List<Position> waypoints = new ArrayList<Position>();
				waypoints.addAll(session.getActiveScenario().getWaypoints());
				
				if (planner.supports(planner.getAircraft()) &&
					planner.supports(planner.getEnvironment()) &&
					planner.supports(waypoints) &&
					1 < waypoints.size()) {
					
					origin = waypoints.remove(0);
					destination = waypoints.remove(waypoints.size() - 1);
				
					Trajectory trajectory = null;
					
					if (waypoints.isEmpty()) {
						trajectory = planner.plan(origin, destination, session.getActiveScenario().getTime());
					} else {
						trajectory = planner.plan(origin, destination, waypoints, session.getActiveScenario().getTime());
					}
					
					styleTrajectory(trajectory);
					session.getActiveScenario().setTrajectory(trajectory);
				
				} else {
					alert(
						AlertType.ERROR,
						PlannerAlert.ALERT_TITLE_PLANNER_INVALID,
						PlannerAlert.ALERT_HEADER_PLANNER_INVALID,
						PlannerAlert.ALERT_CONTENT_PLANNER_INVALID);
				}
				
				setMode(WorldMode.VIEW);
			}
		});
	}
	
	private void styleTrajectory(Trajectory trajectory) {
		trajectory.setVisible(true);
		trajectory.setShowPositions(true);
		trajectory.setDrawVerticals(true);
		trajectory.setAttributes(new BasicShapeAttributes());
		trajectory.getAttributes().setOutlineMaterial(Material.MAGENTA);
		trajectory.getAttributes().setOutlineWidth(5d);
		trajectory.getAttributes().setOutlineOpacity(0.5d);
		for (Waypoint waypoint : trajectory.getWaypoints()) {
			Depiction depiction = new Depiction(symbolFactory.createPoint(Waypoint.SICD_NAV_WAYPOINT_ROUTE, waypoint, null));
			depiction.setAnnotation(new DepictionAnnotation(waypoint.getEto().toString(), waypoint));
			depiction.setVisible(true);
			waypoint.setDepiction(depiction);
		}
	}
	
	private class WorldInitializer implements Runnable {

		@Override
		public void run() {
			JPanel worldPanel = new JPanel(new BorderLayout());
			
			// initialize world window
			wwd.setModel(new BasicModel());
			// TODO: load higher quality bing maps
			// possibly configurable (street or bing) and per session
			
			// add view controls
			ViewControlsLayer viewControlsLayer = new ViewControlsLayer();
			wwd.getModel().getLayers().add(viewControlsLayer);
			wwd.addSelectListener(new ViewControlsSelectListener(wwd, viewControlsLayer));
			
			// add scenario data
			wwd.getModel().getLayers().add(aircraftLayer);
			wwd.getModel().getLayers().add(environmentLayer);
			wwd.getModel().getLayers().add(waypointLayer);
			wwd.getModel().getLayers().add(obstaclesLayer);
			
			// add planner controls
			ControlAnnotation aircraftControl = new ControlAnnotation(aircraftIcon);
			aircraftControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) - 200, 25));
			aircraftControl.setPrimaryActionCommand(WorldPresenter.ACTION_AICRAFT_SET);
			aircraftControl.setSecondaryActionCommand(WorldPresenter.ACTION_AIRCAFT_SETUP);
			aircraftControl.addActionListener(new AircraftControlListener());
			
			ControlAnnotation swimControl = new ControlAnnotation(swimIcon);
			swimControl.getAttributes().setDrawOffset((new Point((wwd.getWidth() / 2) - 125, 25)));
			swimControl.setPrimaryActionCommand(WorldPresenter.ACTION_SWIM_LOAD);
			swimControl.setSecondaryActionCommand(WorldPresenter.ACTION_SWIM_SETUP);
			swimControl.addActionListener(new SwimControlListener());
			
			ControlAnnotation environmentControl = new ControlAnnotation(environmentIcon);
			environmentControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) - 50, 25));
			environmentControl.setPrimaryActionCommand(WorldPresenter.ACTION_ENVIRONMENT_ENCLOSE);
			environmentControl.setSecondaryActionCommand(ACTION_ENVIRONMENT_SETUP);
			environmentControl.addActionListener(new EnvironmentControlListener());
			
			ControlAnnotation poiControl = new ControlAnnotation(poiIcon);
			poiControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 25, 25));
			poiControl.setPrimaryActionCommand(WorldPresenter.ACTION_WAYPOINT_EDIT);
			poiControl.setSecondaryActionCommand(WorldPresenter.ACTION_WAYPOINT_SETUP);
			poiControl.addActionListener(new WaypointsControlListener());
			
			ControlAnnotation plannerControl = new ControlAnnotation(plannerIcon);
			plannerControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 100, 25));
			plannerControl.setPrimaryActionCommand(WorldPresenter.ACTION_PLANNER_PLAN);
			plannerControl.setSecondaryActionCommand(WorldPresenter.ACTION_PLANNER_SETUP);
			plannerControl.addActionListener(new PlannerControlListener());
			
			ControlAnnotation takeoffControl = new ControlAnnotation(takeoffIcon);
			takeoffControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 175, 25));
			takeoffControl.setPrimaryActionCommand(WorldPresenter.ACTION_TAKEOFF);
			takeoffControl.setSecondaryActionCommand(WorldPresenter.ACTION_NONE);
			takeoffControl.addActionListener(new TakeOffControlListener());
			
			ControlAnnotation landControl = new ControlAnnotation(landIcon);
			landControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 250, 25));
			landControl.setPrimaryActionCommand(WorldPresenter.ACTION_LAND);
			landControl.setSecondaryActionCommand(WorldPresenter.ACTION_NONE);
			landControl.addActionListener(new LandControlListener());
			
			controlLayer.addAnnotation(aircraftControl);
			wwd.addSelectListener(aircraftControl);
			controlLayer.addAnnotation(swimControl);
			wwd.addSelectListener(swimControl);
			controlLayer.addAnnotation(environmentControl);
			wwd.addSelectListener(environmentControl);
			controlLayer.addAnnotation(poiControl);
			wwd.addSelectListener(poiControl);
			controlLayer.addAnnotation(plannerControl);
			wwd.addSelectListener(plannerControl);
			controlLayer.addAnnotation(takeoffControl);
			wwd.addSelectListener(takeoffControl);
			controlLayer.addAnnotation(landControl);
			wwd.addSelectListener(landControl);
			wwd.getModel().getLayers().add(controlLayer);
			
			// add on-screen status
			ScreenAnnotation statusAnnotation = new ScreenAnnotation("----------",
					new Point(wwd.getWidth() / 2, wwd.getHeight() - 75));
			statusAnnotation.setAlwaysOnTop(true);
			statusAnnotation.getAttributes().setAdjustWidthToText("----------");
			statusAnnotation.getAttributes().setTextAlign(AVKey.CENTER);
			statusAnnotation.getAttributes().setTextColor(Color.BLACK);
			statusAnnotation.getAttributes().setBackgroundColor(Color.LIGHT_GRAY);
			statusAnnotation.getAttributes().setBorderColor(Color.BLACK);
			statusAnnotation.getAttributes().setOpacity(0.5d);
			statusAnnotation.setText(WorldMode.VIEW.toString());
			statusLayer.addAnnotation(statusAnnotation);
			wwd.getModel().getLayers().add(statusLayer);
			
			// add resize listener for controls
			wwd.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent event) {
					aircraftControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) - 200, 25));
					swimControl.getAttributes().setDrawOffset((new Point((wwd.getWidth() / 2) - 125, 25)));
					environmentControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) - 50, 25));
					poiControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 25, 25));
					plannerControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 100, 25));
					takeoffControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 175, 25));
					landControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 250, 25));
					statusAnnotation.setScreenPoint(new Point(wwd.getWidth() / 2, wwd.getHeight() - 75));
				}
			});
			
			// set world panel / node layout
			worldPanel.add(wwd, BorderLayout.CENTER);
			StatusBar statusBar = new StatusBar();
			statusBar.setEventSource(wwd);
			worldPanel.add(statusBar, BorderLayout.PAGE_END);
			worldNode.setContent(worldPanel);
			
    		// TODO: use safe default altitude (to be edited)
    		// TODO: selected waypoints will be displayed as POINT waypoints
    		// TODO: computed waypoints will be displayed as ROUTE waypoints
    		// TODO: OPTIMAL / LEAST RISK routes can be tagged using appropriate symbology
    		
    		wwd.addMouseListener(new WorldMouseListener());
		}
	}
	
	private class WorldMouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			// TODO: swing thread maybe not necessary
			if (getMode().equals(WorldMode.AIRCRAFT)) {
				SwingUtilities.invokeLater(new AircraftMouseHandler());
			} else if (getMode().equals(WorldMode.WAYPOINT)) {
				SwingUtilities.invokeLater(new WaypointMouseHandler());
			}
			// TODO: pickable support ...
		}
	}
	
	private class SectorChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (getMode().equals(WorldMode.ENVIRONMENT)) {
				if (null == evt.getNewValue()) {
					Sector envSector = sectorSelector.getSector();
					if (null != envSector) {
						Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
						session.getActiveScenario().setSector(envSector);
						Specification<Environment> envSpec = session.getSetup().getEnvironmentSpecification();
						Environment env = session.getEnvironmentFactory().createInstance(envSpec);
						session.getActiveScenario().setEnvironment(env);
						initEnvironment();
					}
					sectorSelector.disable();
				}
			}
		}
	}
	
	private class AircraftMouseHandler implements Runnable {

		@Override
		public void run() {
			Position clickedPosition = wwd.getCurrentPosition();
			if (null != clickedPosition) {
				Waypoint waypoint = new Waypoint(clickedPosition);
				waypoint.setDepiction(new Depiction(symbolFactory.createPoint(Waypoint.SIDC_NAV_WAYPOINT_POI, waypoint, null)));
				waypoint.getDepiction().setVisible(true);
				
				if (scenario.hasAircraft() && (0 < scenario.getWaypoints().size())) {
					scenario.removeWaypoint(0);
				}
				scenario.addWaypoint(0, waypoint);
				
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				Specification<Aircraft> aircraftSpec = session.getSetup().getAircraftSpecification();
				Aircraft aircraft = session.getAircraftFactory().createInstance(aircraftSpec);
				aircraft.moveTo(waypoint);
				aircraft.setCostInterval(new CostInterval(
        				aircraftSpec.getId(),
        				ZonedDateTime.now(ZoneId.of("UTC")).minusYears(10),
        				ZonedDateTime.now(ZoneId.of("UTC")).plusYears(10),
        				100d));
				scenario.setAircraft(aircraft);
				
				setMode(WorldMode.VIEW);
			}
		}
	}
	
	private class WaypointMouseHandler implements Runnable {

		@Override
		public void run() {
			Position clickedPosition = wwd.getCurrentPosition();
			if (null != clickedPosition) {
				Waypoint waypoint = new Waypoint(clickedPosition);
				waypoint.setDepiction(new Depiction(symbolFactory.createPoint(Waypoint.SIDC_NAV_WAYPOINT_POI, waypoint, null)));
				waypoint.getDepiction().setVisible(true);
				scenario.addWaypoint(waypoint);
			}
		}
	}
	
	private class TimeChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			// TODO: environment, aircraft, obstacles
			// TODO: possibly only redraw layers
			initEnvironment();
			initObstacles();
		}
	}
	
	private class ThresholdChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			// TODO: environment, aircraft, obstacles
			// TODO: possibly only redraw layers
			initEnvironment();
			initObstacles();
		}
	}
	
	private class AircraftChangeListener implements PropertyChangeListener {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initAircraft();
		}
	}
	
	private class EnvironmentChangeListener implements PropertyChangeListener {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initEnvironment();
		}
	}
	
	private class WaypointsChangeListener implements PropertyChangeListener {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initPlan();
		}
	}
	
	private class TrajectoryChangeListener implements PropertyChangeListener {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initPlan();
		}
	}
	
	private class ObstaclesChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initObstacles();
		}
	}
	
	private class ActiveScenarioChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initScenario();
			initEnvironment();
			initObstacles();
			initPlan();
		}
	}
	
	private class AircraftControlListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_AICRAFT_SET:
				setMode(WorldMode.AIRCRAFT);
				break;
			case WorldPresenter.ACTION_AIRCAFT_SETUP:
				setMode(WorldMode.VIEW);
				setup(SetupDialog.AIRCRAFT_TAB_INDEX);
				break;
			}
		}
	}
	
	private class SwimControlListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_SWIM_LOAD:
				setMode(WorldMode.LOADING);
				load(WorldPresenter.FILE_CHOOSER_TITLE_SWIM,
					new ExtensionFilter[] { new ExtensionFilter(
						WorldPresenter.FILE_CHOOSER_SWIM,
						WorldPresenter.FILE_CHOOSER_EXTENSION_SWIM)});
				break;
			case WorldPresenter.ACTION_SWIM_SETUP:
				setMode(WorldMode.VIEW);
				setup(SetupDialog.SWIM_TAB_INDEX);
				break;
			}
		}
	}
	
	private class EnvironmentControlListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_ENVIRONMENT_ENCLOSE:
				setMode(WorldMode.ENVIRONMENT);
				sectorSelector.enable();
				break;
			case WorldPresenter.ACTION_ENVIRONMENT_SETUP:
				setMode(WorldMode.VIEW);
				setup(SetupDialog.ENVIRONMENT_TAB_INDEX);
				break;
			}
		}
	}
	
	private class WaypointsControlListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_WAYPOINT_EDIT:
				setMode(WorldMode.WAYPOINT);
				break;
			case WorldPresenter.ACTION_WAYPOINT_SETUP:
				// TODO: waypoint setup (types of waypoint graphics: POI, RWP, ...)
				setMode(WorldMode.VIEW);
				break;
			}
		}
	}
	
	private class PlannerControlListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_PLANNER_PLAN:
				setMode(WorldMode.PLANNING);
				plan();
				break;
			case WorldPresenter.ACTION_PLANNER_SETUP:
				setMode(WorldMode.VIEW);
				setup(SetupDialog.PLANNER_TAB_INDEX);
				break;
			}
		}
	}
	
	private class TakeOffControlListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("pressed...." + e.getActionCommand());
		}
	}
	
	private class LandControlListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("pressed...." + e.getActionCommand());
		}
	}
	
}
