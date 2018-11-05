/**
 * Copyright (c) 2016, Stephan Heinemann (UVic Center for Aerospace Research)
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.xml.sax.InputSource;

import com.cfar.swim.worldwind.ai.PlanRevisionListener;
import com.cfar.swim.worldwind.ai.Planner;
import com.cfar.swim.worldwind.ai.prm.fadprm.FADPRMPlanner;
import com.cfar.swim.worldwind.aircraft.Aircraft;
import com.cfar.swim.worldwind.aircraft.CombatIdentification;
import com.cfar.swim.worldwind.aircraft.Iris;
import com.cfar.swim.worldwind.connections.Datalink;
import com.cfar.swim.worldwind.geom.Box;
import com.cfar.swim.worldwind.iwxxm.IwxxmLoader;
import com.cfar.swim.worldwind.planning.CostInterval;
import com.cfar.swim.worldwind.planning.DesirabilityZone;
import com.cfar.swim.worldwind.planning.Environment;
import com.cfar.swim.worldwind.planning.SamplingEnvironment;
import com.cfar.swim.worldwind.planning.TrackPoint;
import com.cfar.swim.worldwind.planning.Trajectory;
import com.cfar.swim.worldwind.planning.Waypoint;
import com.cfar.swim.worldwind.registries.Specification;
import com.cfar.swim.worldwind.render.Obstacle;
import com.cfar.swim.worldwind.render.airspaces.TerrainBox;
import com.cfar.swim.worldwind.render.annotations.ControlAnnotation;
import com.cfar.swim.worldwind.render.annotations.DepictionAnnotation;
import com.cfar.swim.worldwind.session.Scenario;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.session.Setup;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;
import com.cfar.swim.worldwind.ui.desirabilityzones.DesirabilityDialog;
import com.cfar.swim.worldwind.ui.planner.PlannerAlert;
import com.cfar.swim.worldwind.ui.planner.PlannerAlertResult;
import com.cfar.swim.worldwind.ui.setup.SetupDialog;
import com.cfar.swim.worldwind.ui.setup.SetupModel;
import com.cfar.swim.worldwind.ui.setupWaypoint.SetupWaypointDialog;
import com.cfar.swim.worldwind.util.Depiction;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525GraphicFactory;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwindx.examples.util.SectorSelector;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Realizes a presenter of a world view.
 * 
 * @author Stephan Heinemann
 *
 */
public class WorldPresenter implements Initializable {

	/** the aircraft icon of the world view */
	@Inject
	private String aircraftIcon;

	/** the swim icon of the world view */
	@Inject
	private String swimIcon;
	
	/** the terrain icon of the world view */
	@Inject
	private String terrainIcon;

	/** the environment icon of the world view */
	@Inject
	private String environmentIcon;

	/** the point of interest icon of the world view */
	@Inject
	private String poiIcon;

	/** the planner icon of the world view */
	@Inject
	private String plannerIcon;

	/** the datalink icon of the world view */
	@Inject
	private String datalinkIcon;

	/** the upload icon of the world view */
	@Inject
	private String uploadIcon;

	/** the take-off icon of the world view */
	@Inject
	private String takeoffIcon;

	/** the land icon of the world view */
	@Inject
	private String landIcon;

	/** the view icon of the world view */
	@Inject
	private String viewIcon;
	
	/** the desirability icon of the world view */
	@Inject
	private String desirabilityIcon;

	/** the setup icon of the world view */
	@Inject
	private String setupIcon;

	/** the no action command */
	public static final String ACTION_NONE = "WorldPresenter.ActionCommand.None";

	/** the set aircraft action command */
	public static final String ACTION_AICRAFT_SET = "WorldPresenter.ActionCommand.AircraftSet";

	/** the setup aircraft action command */
	public static final String ACTION_AIRCAFT_SETUP = "WorldPresenter.ActionCommand.AircraftSetup";

	/** the load swim action command */
	public static final String ACTION_SWIM_LOAD = "WorldPresenter.ActionCommand.SwimLoad";

	/** the setup swim action command */
	public static final String ACTION_SWIM_SETUP = "WorldPresenter.ActionCommand.SwimSetup";
	
	/** the load terrain action command */
	public static final String ACTION_TERRAIN_LOAD = "WorldPresenter.ActionCommand.TerrainLoad";

	/** the setup terrain action command */
	public static final String ACTION_TERRAIN_SETUP = "WorldPresenter.ActionCommand.TerrainSetup";

	/** the enclose environment action command */
	public static final String ACTION_ENVIRONMENT_ENCLOSE = "WorldPresenter.ActionCommand.EnvironmentEnclose";

	/** the setup environment action command */
	public static final String ACTION_ENVIRONMENT_SETUP = "WorldPresenter.ActionCommand.EnvironmentSetup";

	/** the enclose desirability zone action command */
	public static final String ACTION_DESIRABILITY_ENCLOSE = "WorldPresenter.ActionCommand.DesirabilityEnclose";

	/** the setup desirability zone action command */
	public static final String ACTION_DESIRABILITY_SETUP = "WorldPresenter.ActionCommand.DesirabilitySetup";

	/** the edit waypoint action command */
	public static final String ACTION_WAYPOINT_EDIT = "WorldPresenter.ActionCommand.WaypointEdit";

	/** the setup waypoint action command */
	public static final String ACTION_WAYPOINT_SETUP = "WorldPresenter.ActionCommand.WaypointSetup";

	/** the plan action command */
	public static final String ACTION_PLANNER_PLAN = "WorldPresenter.ActionCommand.PlannerPlan";

	/** the setup planner action command */
	public static final String ACTION_PLANNER_SETUP = "WorldPresenter.ActionCommand.PlannerSetup";

	/** the monitor action command */
	public static final String ACTION_DATALINK_MONITOR = "WorldPresenter.ActionCommand.DatalinkMonitor";

	/** the setup datalink action command */
	public static final String ACTION_DATALINK_SETUP = "WorldPresenter.ActionCommand.DatalinkSetup";

	/** the upload action command */
	public static final String ACTION_TRANSFER_UPLOAD = "WorldPresenter.ActionCommand.TransferUpload";
	
	/** the start misision action command */
	public static final String ACTION_START_MISSION = "WorldPresenter.ActionCommand.StartMisiion";

	/** the take-off action command */
	public static final String ACTION_FLIGHT_TAKEOFF = "WorldPresenter.ActionCommand.FlightTakeOff";

	/** the setup flight action command */
	public static final String ACTION_FLIGHT_SETUP = "WorldPresenter.ActionCommand.FlightSetup";

	/** the land action command */
	public static final String ACTION_FLIGHT_LAND = "WorldPresenter.ActionCommand.Land";

	/** the return action command */
	public static final String ACTION_FLIGHT_RETURN = "WorldPresenter.ActionCommand.Return";
	
	/** the auto action command */
	public static final String ACTION_FLIGHT_AUTO = "WorldPresenter.ActionCommand.Auto";

	/** the cycle view action command */
	public static final String ACTION_VIEW_CYCLE = "WorldPresenter.ActionCommand.ViewCycle";

	/** the reset view action command */
	public static final String ACTION_VIEW_RESET = "WorldPresenter.ActionCommand.ViewReset";
	
	// TODO: Temporary for telemetry
	@Inject
	private String telemetryIcon;
	public static final String ACTION_TELEMETRY_WRITE = "WorldPresenter.ActionCommand.TelemetryWrite";
	public static final String ACTION_TELEMETRY_READ = "WorldPresenter.ActionCommand.TelemetryRead";

	// TODO: consider to move all visible UI text into properties files

	/** the file chooser open swim file title */
	public static final String FILE_CHOOSER_TITLE_SWIM = "Open SWIM File";

	/** the file chooser swim file description */
	public static final String FILE_CHOOSER_SWIM = "SWIM Files";

	/** the file chooser swim file extension */
	public static final String FILE_CHOOSER_EXTENSION_SWIM = "*.xml";

	/** the file chooser open terrain file title */
	public static final String FILE_CHOOSER_TITLE_TERRAIN = "Open Terrain File";

	/** the file chooser terrain file description */
	public static final String FILE_CHOOSER_TERRAIN = "Terrain Files";

	/** the file chooser terrain file extension */
	public static final String FILE_CHOOSER_EXTENSION_TERRAIN = "*.csv";

	/** the world pane of the world view */
	@FXML
	private AnchorPane worldNodePane;

	/** the world node of the world view (swing inside fx) */
	@FXML
	private SwingNode worldNode;

	/** the monitor circle of the world view */
	@FXML
	private Circle monitorCircle;

	/** the world model of this world presenter */
	@Inject
	private WorldModel worldModel;

	/** the setup model of this world presenter */
	@Inject
	private SetupModel setupModel;

	/** the world window of this world presenter */
	private final WorldWindowGLJPanel wwd = new WorldWindowGLJPanel();

	/** the control layer of this world presenter */
	private final AnnotationLayer controlLayer = new AnnotationLayer();

	/** the status layer of this world presenter */
	private final AnnotationLayer statusLayer = new AnnotationLayer();

	/** the aircraft layer of this world presenter */
	private final RenderableLayer aircraftLayer = new RenderableLayer();

	/** the environment layer of this world presenter */
	private final RenderableLayer environmentLayer = new RenderableLayer();

	/** the desirability zones layer of this world presenter */
	private final RenderableLayer desirabilityZonesLayer = new RenderableLayer();

	/** the waypoint layer of this world presenter */
	private final RenderableLayer waypointLayer = new RenderableLayer();

	/** the obstacles layer of this world presenter */
	private final RenderableLayer obstaclesLayer = new RenderableLayer();

	/** the terrain obstacles layer of this world presenter */
	private final RenderableLayer terrainObstaclesLayer = new RenderableLayer();

	/** the track layer of this world presenter */
	private final MarkerLayer trackLayer = new MarkerLayer();

	/** the symbol factory of this world presenter */
	private final MilStd2525GraphicFactory symbolFactory = new MilStd2525GraphicFactory();

	/** the sector selector of this world presenter */
	private final SectorSelector sectorSelector = new SectorSelector(wwd);

	/** the desirability sector selector of this world presenter */
	private final SectorSelector desirabilitySectorSelector = new SectorSelector(wwd);

	/** the active scenario of this world presenter */
	private Scenario scenario = null;

	/** the time change listener of this world presenter */
	private final TimeChangeListener timeCl = new TimeChangeListener();

	/** the threshold cost change listener of this world presenter */
	private final ThresholdChangeListener thresholdCl = new ThresholdChangeListener();

	/** the aircraft change listener of this world presenter */
	private final AircraftChangeListener aircraftCl = new AircraftChangeListener();

	/** the environment change listener of this world presenter */
	private final EnvironmentChangeListener environmentCl = new EnvironmentChangeListener();

	/** the desirability zones change listener of this world presenter */
	private final DesirabilityZonesChangeListener desirabilityCl = new DesirabilityZonesChangeListener();

	/** the waypoints change listener of this world presenter */
	private final WaypointsChangeListener waypointsCl = new WaypointsChangeListener();

	/** the trajectory change listener of this world presenter */
	private final TrajectoryChangeListener trajectoryCl = new TrajectoryChangeListener();

	/** the obstacles change listener of this world presenter */
	private final ObstaclesChangeListener obstaclesCl = new ObstaclesChangeListener();

	/** the terrain obstacles change listener of this world presenter */
	private final TerrainObstaclesChangeListener terrainObstaclesCl = new TerrainObstaclesChangeListener();

	/** the track change listener of this world presenter */
	private final TrackChangeListener trackCl = new TrackChangeListener();

	/** the executor of this world presenter */
	private final Executor executor = Executors.newSingleThreadScheduledExecutor();

	/**
	 * Initializes this world presenter.
	 * 
	 * @param location unused
	 * @param resources unused
	 * 
	 * @see Initializable#initialize(URL, ResourceBundle)
	 */
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

		this.desirabilitySectorSelector.setInteriorColor(Color.WHITE);
		this.desirabilitySectorSelector.setInteriorOpacity(0.5d);
		this.desirabilitySectorSelector.setBorderColor(Color.WHITE);
		this.desirabilitySectorSelector.setBorderWidth(1d);
		this.desirabilitySectorSelector.addPropertyChangeListener(SectorSelector.SECTOR_PROPERTY,
				new DesirabilitySectorChangeListener());

		this.initScenario();
		this.initAircraft();
		this.initEnvironment();
		this.initDesirabilityZones();
		this.initObstacles();
		this.initTerrainObstacles();
		this.initPlan();
		this.initTrack();
		this.initView();
	}

	/**
	 * Initializes the scenario of this world presenter.
	 */
	public void initScenario() {
		// remove change listeners from the previous scenario if any
		if (null != this.scenario) {
			this.scenario.removePropertyChangeListener(this.timeCl);
			this.scenario.removePropertyChangeListener(this.thresholdCl);
			this.scenario.removePropertyChangeListener(this.aircraftCl);
			this.scenario.removePropertyChangeListener(this.environmentCl);
			this.scenario.removePropertyChangeListener(this.desirabilityCl);
			this.scenario.removePropertyChangeListener(this.waypointsCl);
			this.scenario.removePropertyChangeListener(this.trajectoryCl);
			this.scenario.removePropertyChangeListener(this.obstaclesCl);
			this.scenario.removePropertyChangeListener(this.terrainObstaclesCl);
		}
		this.scenario = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE).getActiveScenario();
		this.scenario.addTimeChangeListener(this.timeCl);
		this.scenario.addThresholdChangeListener(this.thresholdCl);
		this.scenario.addAircraftChangeListener(this.aircraftCl);
		this.scenario.addEnvironmentChangeListener(this.environmentCl);
		this.scenario.addDesirabilityZonesChangeListener(this.desirabilityCl);
		this.scenario.addWaypointsChangeListener(this.waypointsCl);
		this.scenario.addTrajectoryChangeListener(this.trajectoryCl);
		this.scenario.addObstaclesChangeListener(this.obstaclesCl);
		this.scenario.addTerrainObstaclesChangeListener(this.terrainObstaclesCl);
	}

	/**
	 * Initializes the aircraft of this world presenter.
	 */
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

	/**
	 * Initializes the environment of this world presenter.
	 */
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

	/**
	 * Initializes the desirability zones of this world presenter.
	 */
	public void initDesirabilityZones() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				desirabilityZonesLayer.removeAllRenderables();
				for (DesirabilityZone zone : scenario.getDesirabilityZones()) {
					desirabilityZonesLayer.addRenderable(zone);
				}
				wwd.redraw();
			}
		});
	}

	/**
	 * Initializes the obstacles of this world presenter.
	 */
	public void initObstacles() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				obstaclesLayer.removeAllRenderables();
				// TODO: investigate CME observed here
				obstaclesLayer.addRenderables(scenario.getObstacles());
				wwd.redraw();
			}
		});
	}

	/**
	 * Initializes the terrain obstacles of this world presenter.
	 */
	public void initTerrainObstacles() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				terrainObstaclesLayer.removeAllRenderables();
				terrainObstaclesLayer.addRenderables(scenario.getTerrainObstacles());
				wwd.redraw();
			}
		});
	}

	/**
	 * Initializes the plan of this world presenter.
	 */
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

	/**
	 * Initializes the track of this world presenter.
	 */
	public void initTrack() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ArrayList<Marker> markers = new ArrayList<>();
				for (TrackPoint trackPoint : scenario.getDatalink().getAircraftTrack()) {
					markers.add(trackPoint);
				}
				trackLayer.setMarkers(markers);
				wwd.redraw();
			}
		});
	}

	/**
	 * Initializes the view of this world presenter.
	 */
	public void initView() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ViewMode viewMode = getViewMode();
				if (!viewMode.equals(ViewMode.FIX)) {
					View view = wwd.getView();

					if (view instanceof BasicOrbitView) {
						BasicOrbitView basicOrbitView = (BasicOrbitView) view;
						if (viewMode.equals(ViewMode.PLANNED_ABOVE)) {
							if (scenario.hasAircraft()) {
								basicOrbitView.setCenterPosition(
										scenario.getAircraft().getReferencePosition());
								// TODO: plan does not contain attitude (airdata, heading)
							} else {
								view(false);
							}
						} else if (viewMode.equals(ViewMode.ACTUAL_ABOVE)) {
							TrackPoint last = scenario.getDatalink().getLastTrackPoint();
							if (null != last) {
								basicOrbitView.setCenterPosition(last.getPosition());
								basicOrbitView.setHeading(last.getHeading());
							} else {
								view(false);
							}
						}
					} else if (view instanceof BasicFlyView) {
						BasicFlyView basicFlyView = (BasicFlyView) view;
						if (viewMode.equals(ViewMode.PLANNED_CHASE)) {
							if (scenario.hasAircraft()) {
								// TODO: plan does not contain attitude (airdata, heading)
							} else {
								view(false);
							}
						} else if (viewMode.equals(ViewMode.PLANNED_FPV)) {
							if (scenario.hasAircraft()) {
								// TODO: plan does not contain attitude (airdata, heading)
							} else {
								view(false);
							}
						} else if (viewMode.equals(ViewMode.ACTUAL_CHASE)) {
							// TrackPoint previous = scenario.getDatalink().getPreviousTrackPoint(5);
							TrackPoint previous = scenario.getDatalink().getFirstTrackPoint();
							if (null != previous) {
								basicFlyView.setEyePosition(previous.getPosition());
								basicFlyView.setHeading(previous.getHeading());
								basicFlyView.setPitch(previous.getPitch().add(Angle.POS90));
								basicFlyView.setRoll(previous.getRoll());
							} else {
								view(false);
							}
						} else if (viewMode.equals(ViewMode.ACTUAL_FPV)) {
							TrackPoint last = scenario.getDatalink().getLastTrackPoint();
							if (null != last) {
								basicFlyView.setEyePosition(last.getPosition());
								basicFlyView.setHeading(last.getHeading());
								basicFlyView.setPitch(last.getPitch().add(Angle.POS90));
								basicFlyView.setRoll(last.getRoll());
							} else {
								view(false);
							}
						}
					}
				}
			}
		});
	}

	/**
	 * Gets the world mode of this world presenter.
	 * 
	 * @return the world mode of this world presenter
	 */
	private WorldMode getWorldMode() {
		return this.worldModel.getWorldMode();
	}

	/**
	 * Sets the world mode of this world presenter.
	 * 
	 * @param worldMode the world mode to be set
	 */
	private void setWorldMode(WorldMode worldMode) {
		this.worldModel.setWorldMode(worldMode);
		this.displayStatus(worldMode.toString());
	}

	/**
	 * Gets the view mode of this world presenter.
	 * 
	 * @return the view mode of this world presenter
	 */
	private ViewMode getViewMode() {
		return this.worldModel.getViewMode();
	}

	/**
	 * Sets the view mode of this world presenter.
	 * 
	 * @param viewMode the view mode to be set
	 */
	private void setViewMode(ViewMode viewMode) {
		this.worldModel.setViewMode(viewMode);
	}

	/**
	 * Displays a status in the status layer of this world presenter.
	 * 
	 * @param status the status to be displayed
	 */
	private void displayStatus(String status) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				statusLayer.getAnnotations().iterator().next().setText(status);
				wwd.redraw();
			}
		});
	}

	/**
	 * Displays the datalink monitor circle of this world presenter.
	 * 
	 * @param status the status to be displayed
	 */
	private void displayMonitor(boolean display) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (display) {
					monitorCircle.toFront();
					monitorCircle.setVisible(true);
				} else {
					monitorCircle.toBack();
					monitorCircle.setVisible(false);
				}
			}
		});
	}

	/**
	 * Opens the setup dialog with a specified tab.
	 * 
	 * @param tabIndex the tab index of the tab to be opened
	 */
	private void setup(int tabIndex) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				setWorldMode(WorldMode.VIEW);
				SetupDialog setupDialog = new SetupDialog(SetupDialog.TITLE_SETUP, SetupDialog.HEADER_SETUP, setupIcon,
						setupModel);
				setupDialog.selectTab(tabIndex);
				setupDialog.showAndWait();

				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);

				// TODO: only store actual changes (comparable properties)

				// store the selected planner to the active scenario
				Specification<Planner> plannerSpec = session.getSetup().getPlannerSpecification();
				session.getActiveScenario().setPlanner(session.getPlannerFactory().createInstance(plannerSpec));

				// store the selected datalink to the active scenario
				if (session.getActiveScenario().getDatalink().isMonitoring()) {
					// stop monitoring
					session.getActiveScenario().getDatalink().stopMonitoring();
					session.getActiveScenario().getDatalink().removePropertyChangeListener(trackCl);
					// setup datalink
					Specification<Datalink> datalinkSpec = session.getSetup().getDatalinkSpecification();
					session.getActiveScenario().setDatalink(session.getDatalinkFactory().createInstance(datalinkSpec));
					// start monitoring
					session.getActiveScenario().getDatalink().addTrackChangeListener(trackCl);
					session.getActiveScenario().getDatalink().startMonitoring();
				} else {
					Specification<Datalink> datalinkSpec = session.getSetup().getDatalinkSpecification();
					session.getActiveScenario().setDatalink(session.getDatalinkFactory().createInstance(datalinkSpec));
				}
			}
		});
	}
	

	/**
	 * Opens the setup desirability dialog.
	 */
	private void setupDesirability() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				setWorldMode(WorldMode.VIEW);
				DesirabilityDialog desirabilityDialog = new DesirabilityDialog(DesirabilityDialog.TITLE_DESIRABILITY,
						DesirabilityDialog.HEADER_DESIRABILITY, setupIcon);
				Optional<Double> inputDesirability = desirabilityDialog.showAndWait();
				if (inputDesirability.isPresent()) {
					double desirability = inputDesirability.get();
					int r = 0, g = 0, b = 0;
					if (desirability <= 0.5) {
						r = 255;
						Double greenDouble = 510 * desirability;
						g = greenDouble.intValue();
						Double blueDouble = 510 * desirability;
						b = blueDouble.intValue();
					}
					if (desirability > 0.5) {
						Double redDouble = -510 * desirability + 510;
						r = redDouble.intValue();
						g = 255;
						Double blueDouble = -510 * desirability + 510;
						b = blueDouble.intValue();
					}
					desirabilitySectorSelector.setInteriorColor(new Color(r, g, b));
					desirabilitySectorSelector.setBorderColor(new Color(r, g, b));
				}
			}
		});
	}

	/**
	 * Opens the setup desirability dialog.
	 */
	private void setupWaypoint() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				setWorldMode(WorldMode.VIEW);
				SetupWaypointDialog waypointDialog = new SetupWaypointDialog(SetupWaypointDialog.TITLE_SETUP,
						SetupWaypointDialog.HEADER_SETUP);
				Optional<Double> inputAltitude = waypointDialog.showAndWait();
			}
		});
	}
	/**
	 * Opens a file dialog and loads a SWIM file asynchronously.
	 * 
	 * @param title the title of the file dialog
	 * @param extensions the extension filter of the file dialog
	 */
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
								setWorldMode(WorldMode.LOADING);
								// TODO: generic SWIM loader
								IwxxmLoader loader = new IwxxmLoader();
								Set<Obstacle> obstacles = loader.load(new InputSource(new FileInputStream(file)));
								for (Obstacle obstacle : obstacles) {
									scenario.addObstacle(obstacle);
								}
								setWorldMode(WorldMode.VIEW);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
			}
		});
	}

	/**
	 * Opens a file dialog and loads a terrain file asynchronously.
	 * 
	 * @param title the title of the file dialog
	 * @param extensions the extension filter of the file dialog
	 */
	private void loadTerrain(String title, ExtensionFilter[] extensions) {
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
								setWorldMode(WorldMode.LOADING);
								String line = "";
								try (BufferedReader br = new BufferedReader(new FileReader(file))) {
						            while ((line = br.readLine()) != null) {
						                String[] values = line.split(",");

										double lat0 = Double.parseDouble(values[0]);
										double lon0 = Double.parseDouble(values[1]);
										double lat1 = Double.parseDouble(values[2]);
										double lon1 = Double.parseDouble(values[3]);
										double left = Double.parseDouble(values[4]);
										double right = Double.parseDouble(values[5]);
										double bottom = Double.parseDouble(values[6]);
										double top = Double.parseDouble(values[7]);

										scenario.addTerrainObstacle(new TerrainBox(LatLon.fromDegrees(lat0, lon0),
												LatLon.fromDegrees(lat1, lon1), left, right, bottom, top));
						            }
						        } catch (IOException e) {
						            e.printStackTrace();
						        }
								setWorldMode(WorldMode.VIEW);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
			}
		});
	}
	
	/**
	 * Loads the default terrain file asynchronously.
	 */
	private void defaultLoadTerrain() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				setWorldMode(WorldMode.LOADING);
				// Add Obstacle 
				// Tecnico
				File file = new File("TecnicoTerrain.csv");
				String line = "";
				try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		            while ((line = br.readLine()) != null) {
		                String[] values = line.split(",");

						double lat0 = Double.parseDouble(values[0]);
						double lon0 = Double.parseDouble(values[1]);
						double lat1 = Double.parseDouble(values[2]);
						double lon1 = Double.parseDouble(values[3]);
						double left = Double.parseDouble(values[4]);
						double right = Double.parseDouble(values[5]);
						double bottom = Double.parseDouble(values[6]);
						double top = Double.parseDouble(values[7]);

						scenario.addTerrainObstacle(new TerrainBox(LatLon.fromDegrees(lat0, lon0),
								LatLon.fromDegrees(lat1, lon1), left, right, bottom, top));
		            }
		        } catch (IOException e) {
		            e.printStackTrace();
		        }

				// Test scenario
//				Position origin = Position.fromDegrees(38.737, -9.137, 80); //TestClass
				Position origin = Position.fromDegrees(38.73692, -9.13843, 98); //RealFlight
				Position destination = Position.fromDegrees(38.7367, -9.1402, 105);
				// Add aircraft
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				MilStd2525GraphicFactory symbolFactory = new MilStd2525GraphicFactory();
				Waypoint wpt = new Waypoint(origin);
				// Waypoint wpt = new Waypoint(Position.fromDegrees(38.73692, -9.13843, 98));
				wpt.setDepiction(new Depiction(symbolFactory.createPoint(Waypoint.SIDC_NAV_WAYPOINT_POI, wpt, null)));
				wpt.getDepiction().setVisible(true);
				Iris iris = new Iris(wpt, 2.5, CombatIdentification.FRIEND);
				iris.moveTo(wpt);
				iris.setCostInterval(new CostInterval(
						"iris",
						ZonedDateTime.now(ZoneId.of("UTC")).minusYears(10),
						ZonedDateTime.now(ZoneId.of("UTC")).plusYears(10),
						100d));
				session.getActiveScenario().setAircraft(iris);
				session.getActiveScenario().addWaypoint(0, wpt);
				
				// Add Wpt1
				Waypoint wpt1 = new Waypoint(destination);
				// Waypoint wpt1 = new Waypoint(Position.fromDegrees(38.73762, -9.13948, 105));
				wpt1.setDepiction(new Depiction(symbolFactory.createPoint(Waypoint.SIDC_NAV_WAYPOINT_POI, wpt1, null)));
				wpt1.getDepiction().setVisible(true);
				session.getActiveScenario().addWaypoint(1, wpt1);
				
				// Add Wpt2
//				Waypoint wpt2 = new Waypoint(Position.fromDegrees(38.73668, -9.14024, 105));
//				wpt2.setDepiction(new Depiction(symbolFactory.createPoint(Waypoint.SIDC_NAV_WAYPOINT_POI, wpt2, null)));
//				wpt2.getDepiction().setVisible(true);
//				session.getActiveScenario().addWaypoint(2, wpt2);
				
				// Define Environment
				Globe globe = new Earth();
				Sector tecnico = new Sector(
						Angle.fromDegrees(38.7381),
						Angle.fromDegrees(38.7354),
						Angle.fromDegrees(-9.1408),
						Angle.fromDegrees(-9.1364));
				session.getActiveScenario().setSector(tecnico);
				gov.nasa.worldwind.geom.Box boxNASA = Sector.computeBoundingBox(globe, 1.0, tecnico, 80d, 109d);
				// Create environment from box
				SamplingEnvironment samplingEnv = new SamplingEnvironment(new Box(boxNASA));
				samplingEnv.setGlobe(globe);
				session.getActiveScenario().setEnvironment(samplingEnv);
				initEnvironment();
				
				setWorldMode(WorldMode.VIEW);
			}
		});
	}
	
	/**
	 * Loads the default SWIM file asynchronously.
	 */
	private void defaultLoadSWIM() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				setWorldMode(WorldMode.LOADING);
				// Add Obstacle 
				// Tecnico
				File file = new File("sigmet-victoria-thunderstrom.xml");
				if (null != file) {
					executor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								setWorldMode(WorldMode.LOADING);
								// TODO: generic SWIM loader
								IwxxmLoader loader = new IwxxmLoader();
								Set<Obstacle> obstacles = loader.load(new InputSource(new FileInputStream(file)));
								for (Obstacle obstacle : obstacles) {
									scenario.addObstacle(obstacle);
								}
								setWorldMode(WorldMode.VIEW);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
				File file2 = new File("sigmet-victoria-tropCyc.xml");
				if (null != file2) {
					executor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								setWorldMode(WorldMode.LOADING);
								// TODO: generic SWIM loader
								IwxxmLoader loader = new IwxxmLoader();
								Set<Obstacle> obstacles = loader.load(new InputSource(new FileInputStream(file2)));
								for (Obstacle obstacle : obstacles) {
									scenario.addObstacle(obstacle);
								}
								setWorldMode(WorldMode.VIEW);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}

				// Test scenario
				Position origin = Position.fromDegrees(48.4705, -123.259, 10);
				Position destination = Position.fromDegrees(48.4745, -123.251, 40);
				// Add aircraft
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				MilStd2525GraphicFactory symbolFactory = new MilStd2525GraphicFactory();
				Waypoint wpt = new Waypoint(origin);
				wpt.setDepiction(new Depiction(symbolFactory.createPoint(Waypoint.SIDC_NAV_WAYPOINT_POI, wpt, null)));
				wpt.getDepiction().setVisible(true);
				Iris iris = new Iris(wpt, 2.5, CombatIdentification.FRIEND);
				iris.moveTo(wpt);
				iris.setCostInterval(new CostInterval(
						"iris",
						ZonedDateTime.now(ZoneId.of("UTC")).minusYears(10),
						ZonedDateTime.now(ZoneId.of("UTC")).plusYears(10),
						100d));
				session.getActiveScenario().setAircraft(iris);
				session.getActiveScenario().addWaypoint(0, wpt);
				
				// Add Wpt1
				Waypoint wpt1 = new Waypoint(destination);
				wpt1.setDepiction(new Depiction(symbolFactory.createPoint(Waypoint.SIDC_NAV_WAYPOINT_POI, wpt1, null)));
				wpt1.getDepiction().setVisible(true);
				session.getActiveScenario().addWaypoint(1, wpt1);
				
				
				// Define Environment
				Globe globe = new Earth();
				Sector sea = new Sector(
						Angle.fromDegrees(48.470),
						Angle.fromDegrees(48.475),
						Angle.fromDegrees(-123.26),
						Angle.fromDegrees(-123.25));
				session.getActiveScenario().setSector(sea);
				gov.nasa.worldwind.geom.Box boxNASA = Sector.computeBoundingBox(globe, 1.0, sea, 0d, 50d);
				// Create environment from box
				SamplingEnvironment samplingEnv = new SamplingEnvironment(new Box(boxNASA));
				samplingEnv.setGlobe(globe);
				session.getActiveScenario().setEnvironment(samplingEnv);
				initEnvironment();
				
				setWorldMode(WorldMode.VIEW);
			}
			
		});
	}
	
	/**
	 * Opens a planner alert with a specified alert type, title, header and content.
	 * A result can be passed for synchronization.
	 * 
	 * @param type the type of the planner alert
	 * @param title the title of the planner alert
	 * @param header the header of the planner alert
	 * @param content the content of the planner alert
	 * @param result the result to be notified for synchronization
	 * 
	 * @see PlannerAlert
	 */
	private void alert(
			AlertType type,
			String title, String header, String content,
			PlannerAlertResult result) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				PlannerAlert alert = new PlannerAlert(type);
				alert.setTitle(title);
				alert.setHeaderText(header);
				alert.setContentText(content);
				Optional<ButtonType> optButtonType = alert.showAndWait();
				if (null != result) {
					if (optButtonType.isPresent()) {
						result.setOk(optButtonType.get().equals(ButtonType.OK));
					} else {
						result.setOk(false);
					}
				}
			}
		});
	}

	/**
	 * Plans a trajectory along the waypoints of the active scenario asynchronously.
	 */
	private void plan() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				Specification<Planner> plannerSpec = session.getSetup().getPlannerSpecification();
				// TODO: reconsider always creating a new planner versus updating environment
				// and aircraft
				session.getActiveScenario().setPlanner(session.getPlannerFactory().createInstance(plannerSpec));
				Planner planner = session.getActiveScenario().getPlanner();
				Position origin = null;
				Position destination = null;
				List<Position> waypoints = new ArrayList<Position>();
				waypoints.addAll(session.getActiveScenario().getWaypoints());

				if (planner.supports(planner.getAircraft()) &&
						planner.supports(planner.getEnvironment()) &&
						planner.supports(waypoints) &&
						1 < waypoints.size()) {

					setWorldMode(WorldMode.PLANNING);
					origin = waypoints.remove(0);
					destination = waypoints.remove(waypoints.size() - 1);

					// listen for plan revisions
					planner.addPlanRevisionListener(new PlanRevisionListener() {
						@Override
						public void revisePlan(Trajectory trajectory) {
							if (!trajectory.isEmpty()) {
								styleTrajectory(trajectory);
								session.getActiveScenario().setTrajectory(trajectory);
								// printToFile(trajectory);
								
								Thread.yield();
							} else {
								alert(
										AlertType.WARNING,
										PlannerAlert.ALERT_TITLE_TRAJECTORY_INVALID,
										PlannerAlert.ALERT_HEADER_TRAJECTORY_INVALID,
										PlannerAlert.ALERT_CONTENT_TRAJECTORY_INVALID,
										null);
							}
						}

						@Override
						public void reviseObstacle() {
							try {
								IwxxmLoader loader = new IwxxmLoader();
								Set<Obstacle> obstacles = loader.load(
										new InputSource(new FileInputStream(new File("./sigmet-tecnico-ts.xml"))));
								for (Obstacle obstacle : obstacles) {
									scenario.addObstacle(obstacle);
									planner.getEnvironment().embed(obstacle);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						
						
						@Override
						public Waypoint reviseAircraftTimedPosition() {
							Waypoint aircraftTimedPosition = null;
							try {
								Session session = SessionManager.getInstance()
										.getSession(WorldwindPlanner.APPLICATION_TITLE);
								Datalink datalink = session.getActiveScenario().getDatalink();
								if (!datalink.isConnected()) {
									return null;
								}
								aircraftTimedPosition = datalink.getAircraftTimedPosition();
							} catch (Exception e) {
								e.printStackTrace();
							}
							return aircraftTimedPosition;
						}

						@Override
						public Position reviseAircraftPosition() {
							Position aircraftPosition = null;
							try {
								Session session = SessionManager.getInstance()
										.getSession(WorldwindPlanner.APPLICATION_TITLE);
								Datalink datalink = session.getActiveScenario().getDatalink();
								if (!datalink.isConnected()) {
									return null;
								}
								aircraftPosition = datalink.getAircraftPosition();
							} catch (Exception e) {
								e.printStackTrace();
							}
							return aircraftPosition;
						}

						@Override
						public boolean reviseDatalinkPlan() {
							Session session = SessionManager.getInstance()
									.getSession(WorldwindPlanner.APPLICATION_TITLE);
							Datalink datalink = session.getActiveScenario().getDatalink();

							if (!datalink.isConnected()) {
								return false;
							} else {
								if (session.getActiveScenario().hasTrajectory()) {
									datalink.uploadFlightPath(session.getActiveScenario().getTrajectory());
									System.out.println("uploaded");
									if (datalink.getAircraftMode() != "AUTO") {
										System.out.println("takeoffing");
										datalink.takeOff();
									}
								}
								return true;
							}
						}
					});

					if (waypoints.isEmpty()) {
						planner.plan(origin, destination, session.getActiveScenario().getTime());
						if(planner instanceof FADPRMPlanner) {
							session.getActiveScenario().playTime();
							((FADPRMPlanner) planner).planDynamic();
						}
					} else {
						planner.plan(origin, destination, waypoints, session.getActiveScenario().getTime());
					}

					setWorldMode(WorldMode.VIEW);
				} else {
					alert(
							AlertType.ERROR,
							PlannerAlert.ALERT_TITLE_PLANNER_INVALID,
							PlannerAlert.ALERT_HEADER_PLANNER_INVALID,
							PlannerAlert.ALERT_CONTENT_PLANNER_INVALID,
							null);
				}
			}
		});
	}

	/**
	 * Toggles the datalink monitor of the active scenario asynchronously.
	 */
	private void monitor() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				Datalink datalink = session.getActiveScenario().getDatalink();

				if (!datalink.isConnected()) {
					datalink.connect();
				}

				if (!scenario.hasAircraft()) {
					Waypoint waypoint = new Waypoint(datalink.getAircraftPosition());
					waypoint.setDepiction(
							new Depiction(symbolFactory.createPoint(Waypoint.SIDC_NAV_WAYPOINT_POI, waypoint, null)));
					waypoint.getDepiction().setVisible(true);

					Specification<Aircraft> aircraftSpec = session.getSetup().getAircraftSpecification();
					Aircraft aircraft = session.getAircraftFactory().createInstance(aircraftSpec);
					aircraft.moveTo(waypoint);
					aircraft.setCostInterval(
							new CostInterval(aircraftSpec.getId(), ZonedDateTime.now(ZoneId.of("UTC")).minusYears(10),
									ZonedDateTime.now(ZoneId.of("UTC")).plusYears(10), 100d));

					scenario.addWaypoint(0, waypoint);
					scenario.setAircraft(aircraft);
				}
				if (datalink.isConnected()) {
					if (datalink.isMonitoring()) {
						datalink.stopMonitoring();
						datalink.removePropertyChangeListener(trackCl);
					} else {
						datalink.addTrackChangeListener(trackCl);
						datalink.startMonitoring();
					}
					displayMonitor(datalink.isMonitoring());
				} else {
					alert(
							AlertType.ERROR,
							PlannerAlert.ALERT_TITLE_DATALINK_INVALID,
							PlannerAlert.ALERT_HEADER_DATALINK_INVALID,
							PlannerAlert.ALERT_CONTENT_DATALINK_INVALID,
							null);
				}
			}
		});
	}

	/**
	 * Uploads a trajectory via the datalink of the active scenario asynchronously.
	 */
	private void upload() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				Datalink datalink = session.getActiveScenario().getDatalink();

				if (!datalink.isConnected()) {
					datalink.connect();
				}

				if (datalink.isConnected() && session.getActiveScenario().hasTrajectory()) {
					setWorldMode(WorldMode.UPLOADING);
					Trajectory trajectory = session.getActiveScenario().getTrajectory();
					datalink.uploadFlightPath(trajectory);
					setWorldMode(WorldMode.VIEW);
				} else {
					alert(
							AlertType.ERROR,
							PlannerAlert.ALERT_TITLE_DATALINK_INVALID,
							PlannerAlert.ALERT_HEADER_DATALINK_INVALID,
							PlannerAlert.ALERT_CONTENT_DATALINK_INVALID,
							null);
				}
			}
		});
	}

	/**
	 * Starts a mission, by changing the aircraft mode to AUTO.
	 */
	private void startMission() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				Datalink datalink = session.getActiveScenario().getDatalink();

				if (!datalink.isConnected()) {
					datalink.connect();
				}
				// TODO: check if aircraft has an uploaded mission, instead of checking if
				// active scenario has trajectory
				if (datalink.isConnected() && session.getActiveScenario().hasTrajectory()) {
					setWorldMode(WorldMode.UPLOADING);
					datalink.setAircraftMode("AUTO");
					setWorldMode(WorldMode.VIEW);
				} else {
					alert(
							AlertType.ERROR,
							PlannerAlert.ALERT_TITLE_DATALINK_INVALID,
							PlannerAlert.ALERT_HEADER_DATALINK_INVALID,
							PlannerAlert.ALERT_CONTENT_DATALINK_INVALID,
							null);
				}
			}
		});
	}

	/**
	 * Issues a take-off command via the datalink of the active scenario
	 * asynchronously.
	 */
	private void takeoff() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				PlannerAlertResult clearance = new PlannerAlertResult();
				alert(
						AlertType.CONFIRMATION,
						PlannerAlert.ALERT_TITLE_TAKEOFF_CONFIRM,
						PlannerAlert.ALERT_HEADER_TAKEOFF_CONFIRM,
						PlannerAlert.ALERT_CONTENT_TAKEOFF_CONFIRM,
						clearance);

				if (clearance.isOk()) {
					setWorldMode(WorldMode.LAUNCHING);
					Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
					Datalink datalink = session.getActiveScenario().getDatalink();

					if (!datalink.isConnected()) {
						datalink.connect();
					}

					if (datalink.isConnected()) { //&& session.getActiveScenario().hasTrajectory()) {
						// datalink.disableAircraftSafety();
						datalink.takeOff(); // TODO: flight (envelope) setup
					} else {
						alert(
								AlertType.ERROR,
								PlannerAlert.ALERT_TITLE_DATALINK_INVALID,
								PlannerAlert.ALERT_HEADER_DATALINK_INVALID,
								PlannerAlert.ALERT_CONTENT_DATALINK_INVALID,
								null);
					}
					setWorldMode(WorldMode.VIEW);
				}
			}
		});
	}

	/**
	 * Issues a land command via the datalink of the active scenario asynchronously.
	 * 
	 * @param returnToLaunch indicates whether or not the landing shall be performed
	 *            at the launch position
	 */
	private void land(boolean returnToLaunch) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				PlannerAlertResult clearance = new PlannerAlertResult();
				alert(
						AlertType.CONFIRMATION,
						PlannerAlert.ALERT_TITLE_LAND_CONFIRM,
						PlannerAlert.ALERT_HEADER_LAND_CONFIRM,
						PlannerAlert.ALERT_CONTENT_LAND_CONFIRM,
						clearance);

				if (clearance.isOk()) {
					setWorldMode(WorldMode.LANDING);
					Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
					Datalink datalink = session.getActiveScenario().getDatalink();

					if (!datalink.isConnected()) {
						datalink.connect();
					}

					if (datalink.isConnected()) {
						if (returnToLaunch) {
							datalink.returnToLaunch();
						} else {
							datalink.land();
						}
					} else {
						alert(
								AlertType.ERROR,
								PlannerAlert.ALERT_TITLE_DATALINK_INVALID,
								PlannerAlert.ALERT_HEADER_DATALINK_INVALID,
								PlannerAlert.ALERT_CONTENT_DATALINK_INVALID,
								null);
					}
					setWorldMode(WorldMode.VIEW);
				}
			}
		});
	}

	/**
	 * Cycles or resets the view mode of the world view.
	 * 
	 * @param cycle indicates whether to cycle or otherwise reset the world view
	 */
	private void view(boolean cycle) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				BasicOrbitView basicOrbitView;

				if (cycle) {
					switch (getViewMode()) {
					case FIX:
						basicOrbitView = new BasicOrbitView();
						basicOrbitView.setZoom(10000);
						wwd.setView(basicOrbitView);
						setViewMode(ViewMode.PLANNED_ABOVE);
						break;
					case PLANNED_ABOVE:
					case PLANNED_CHASE:
					case PLANNED_FPV:
						basicOrbitView = new BasicOrbitView();
						basicOrbitView.setZoom(10000);
						wwd.setView(basicOrbitView);
						setViewMode(ViewMode.ACTUAL_ABOVE);
						break;
					case ACTUAL_ABOVE:
						wwd.setView(new BasicFlyView());
						setViewMode(ViewMode.ACTUAL_CHASE);
						break;
					case ACTUAL_CHASE:
						wwd.setView(new BasicFlyView());
						setViewMode(ViewMode.ACTUAL_FPV);
						break;
					case ACTUAL_FPV:
					default:
						basicOrbitView = new BasicOrbitView();
						basicOrbitView.setCenterPosition(wwd.getView().getCurrentEyePosition());
						basicOrbitView.setZoom(10000);
						wwd.setView(basicOrbitView);
						setViewMode(ViewMode.FIX);
						break;
					}
				} else {
					basicOrbitView = new BasicOrbitView();
					basicOrbitView.setCenterPosition(wwd.getView().getCurrentEyePosition());
					basicOrbitView.setZoom(10000);
					wwd.setView(basicOrbitView);
					setViewMode(ViewMode.FIX);
				}
			}
		});
	}

	/**
	 * Styles a computed trajectory for display.
	 * 
	 * @param trajectory the trajectory to be styled
	 */
	private void styleTrajectory(Trajectory trajectory) {
		trajectory.setVisible(true);
		trajectory.setShowPositions(true);
		trajectory.setDrawVerticals(true);
		trajectory.setAttributes(new BasicShapeAttributes());
		trajectory.getAttributes().setOutlineMaterial(Material.MAGENTA);
		trajectory.getAttributes().setOutlineWidth(5d);
		trajectory.getAttributes().setOutlineOpacity(0.5d);
		for (Waypoint waypoint : trajectory.getWaypoints()) {
			Depiction depiction = new Depiction(
					symbolFactory.createPoint(Waypoint.SICD_NAV_WAYPOINT_ROUTE, waypoint, null));
			depiction.setAnnotation(new DepictionAnnotation(waypoint.getEto().toString(), waypoint));
			depiction.setVisible(true);
			waypoint.setDepiction(depiction);
		}
	}

	/**
	 * Realizes a world window initializer.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class WorldInitializer implements Runnable {
		
		/**
		 * 
		 * @param icon
		 * @param offset
		 * @param primaryAction
		 * @param secondaryAction
		 * @return
		 */
		private ControlAnnotation createControlAnnotation(String icon, int offset, String primaryAction,
				String secondaryAction) {
			ControlAnnotation controlAnnotation = new ControlAnnotation(icon);
			controlAnnotation.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + offset, 25));
			controlAnnotation.setPrimaryActionCommand(primaryAction);
			controlAnnotation.setSecondaryActionCommand(secondaryAction);
			
			return controlAnnotation;
		}

		/**
		 * Initializes the world window.
		 * 
		 * @see Runnable#run()
		 */
		@Override
		public void run() {
			JPanel worldPanel = new JPanel(new BorderLayout());

			// initialize world window
			wwd.setModel(new BasicModel());
			// TODO: load higher quality maps, possibly configurable and per session
			// TODO: higher DTED levels (worldwind.xml)
			wwd.getModel().getLayers().getLayerByName("Bing Imagery").setEnabled(true);

			// add view controls
			ViewControlsLayer viewControlsLayer = new ViewControlsLayer();
			wwd.getModel().getLayers().add(viewControlsLayer);
			wwd.addSelectListener(new ViewControlsSelectListener(wwd, viewControlsLayer));

			// add scenario data
			wwd.getModel().getLayers().add(aircraftLayer);
			wwd.getModel().getLayers().add(environmentLayer);
			wwd.getModel().getLayers().add(desirabilityZonesLayer);
			wwd.getModel().getLayers().add(waypointLayer);
			wwd.getModel().getLayers().add(obstaclesLayer);
			wwd.getModel().getLayers().add(terrainObstaclesLayer);
			wwd.getModel().getLayers().add(trackLayer);

			// add planner controls
			int offset = -400;
			ControlAnnotation aircraftControl = this.createControlAnnotation(aircraftIcon, offset,
					WorldPresenter.ACTION_AICRAFT_SET, WorldPresenter.ACTION_AIRCAFT_SETUP);
			aircraftControl.addActionListener(new AircraftControlListener());
			offset += 75;
			
			ControlAnnotation terrainControl = this.createControlAnnotation(terrainIcon, offset,
					WorldPresenter.ACTION_TERRAIN_LOAD, WorldPresenter.ACTION_TERRAIN_SETUP);
			terrainControl.addActionListener(new TerrainControlListener());
			offset += 75;
			
			ControlAnnotation swimControl = this.createControlAnnotation(swimIcon, offset,
					WorldPresenter.ACTION_SWIM_LOAD, WorldPresenter.ACTION_SWIM_SETUP);
			swimControl.addActionListener(new SwimControlListener());
			offset += 75;
			
			ControlAnnotation environmentControl = this.createControlAnnotation(environmentIcon, offset,
					WorldPresenter.ACTION_ENVIRONMENT_ENCLOSE, WorldPresenter.ACTION_ENVIRONMENT_SETUP);
			environmentControl.addActionListener(new EnvironmentControlListener());
			offset += 75;
			
			ControlAnnotation desirabilityControl = this.createControlAnnotation(desirabilityIcon, offset,
					WorldPresenter.ACTION_DESIRABILITY_ENCLOSE, WorldPresenter.ACTION_DESIRABILITY_SETUP);
			desirabilityControl.addActionListener(new DesirabilityZoneControlListener());
			offset += 75;
			
			ControlAnnotation poiControl = this.createControlAnnotation(poiIcon, offset,
					WorldPresenter.ACTION_WAYPOINT_EDIT, WorldPresenter.ACTION_WAYPOINT_SETUP);
			poiControl.addActionListener(new WaypointsControlListener());
			offset += 75;
			
			ControlAnnotation plannerControl = this.createControlAnnotation(plannerIcon, offset,
					WorldPresenter.ACTION_PLANNER_PLAN, WorldPresenter.ACTION_PLANNER_SETUP);
			plannerControl.addActionListener(new PlannerControlListener());
			offset += 75;
			
			ControlAnnotation datalinkControl = this.createControlAnnotation(datalinkIcon, offset,
					WorldPresenter.ACTION_DATALINK_MONITOR, WorldPresenter.ACTION_DATALINK_SETUP);
			datalinkControl.addActionListener(new DatalinkControlListener());
			offset += 75;
			
			ControlAnnotation uploadControl = this.createControlAnnotation(uploadIcon, offset,
					WorldPresenter.ACTION_TRANSFER_UPLOAD, WorldPresenter.ACTION_START_MISSION);
			uploadControl.addActionListener(new UploadControlListener());
			offset += 75;
			
			ControlAnnotation takeoffControl = this.createControlAnnotation(takeoffIcon, offset,
					WorldPresenter.ACTION_FLIGHT_TAKEOFF, WorldPresenter.ACTION_FLIGHT_SETUP);
			takeoffControl.addActionListener(new TakeOffControlListener());
			offset += 75;
			
			ControlAnnotation landControl = this.createControlAnnotation(landIcon, offset,
					WorldPresenter.ACTION_FLIGHT_LAND, WorldPresenter.ACTION_FLIGHT_RETURN);
			landControl.addActionListener(new LandControlListener());
			offset += 75;
			
			ControlAnnotation viewControl = this.createControlAnnotation(viewIcon, offset,
					WorldPresenter.ACTION_VIEW_CYCLE, WorldPresenter.ACTION_VIEW_RESET);
			viewControl.addActionListener(new ViewControlListener());
			
			// TODO Temporary for telemetry
			offset += 75;
			ControlAnnotation telemetryControl = this.createControlAnnotation(telemetryIcon, offset,
					WorldPresenter.ACTION_TELEMETRY_WRITE, WorldPresenter.ACTION_TELEMETRY_READ);
			telemetryControl.addActionListener(new TelemetryControlListener());

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
			controlLayer.addAnnotation(datalinkControl);
			wwd.addSelectListener(datalinkControl);
			controlLayer.addAnnotation(uploadControl);
			wwd.addSelectListener(uploadControl);
			controlLayer.addAnnotation(takeoffControl);
			wwd.addSelectListener(takeoffControl);
			controlLayer.addAnnotation(landControl);
			wwd.addSelectListener(landControl);
			controlLayer.addAnnotation(viewControl);
			wwd.addSelectListener(viewControl);
			controlLayer.addAnnotation(desirabilityControl);
			wwd.addSelectListener(desirabilityControl);
			controlLayer.addAnnotation(terrainControl);
			wwd.addSelectListener(terrainControl);
			controlLayer.addAnnotation(telemetryControl);
			wwd.addSelectListener(telemetryControl);
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
					int offset = -400;
					aircraftControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + offset, 25));
					offset += 75;
					terrainControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + offset, 25));
					offset += 75;
					swimControl.getAttributes().setDrawOffset((new Point((wwd.getWidth() / 2) + offset, 25)));
					offset += 75;
					environmentControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + offset, 25));
					offset += 75;
					desirabilityControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + offset, 25));
					offset += 75;
					poiControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + offset, 25));
					offset += 75;
					plannerControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + offset, 25));
					offset += 75;
					datalinkControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + offset, 25));
					offset += 75;
					uploadControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + offset, 25));
					offset += 75;
					takeoffControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + offset, 25));
					offset += 75;
					landControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + offset, 25));
					offset += 75;
					viewControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + offset, 25));
					offset += 75;
					telemetryControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + offset, 25));
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

	/**
	 * Realizes a world mouse listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class WorldMouseListener extends MouseAdapter {

		/**
		 * Handles a world mouse event depending on the current world mode.
		 * 
		 * @param e the world mouse event
		 * 
		 * @see MouseAdapter#mouseClicked(MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO: swing thread maybe not necessary
			if (getWorldMode().equals(WorldMode.AIRCRAFT)) {
				SwingUtilities.invokeLater(new AircraftMouseHandler());
			} else if (getWorldMode().equals(WorldMode.WAYPOINT)) {
				SwingUtilities.invokeLater(new WaypointMouseHandler());
			}
			// TODO: pickable support ...
		}
	}

	/**
	 * Realizes a sector change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class SectorChangeListener implements PropertyChangeListener {

		/**
		 * Obtains a selected sector and creates an environment based on the sector if
		 * it has changed.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (getWorldMode().equals(WorldMode.ENVIRONMENT)) {
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
					setWorldMode(WorldMode.VIEW);
				}
			}
		}
	}

	/**
	 * Realizes a desirability sector change listener.
	 * 
	 * @author Henrique Ferreira
	 *
	 */
	private class DesirabilitySectorChangeListener implements PropertyChangeListener {

		/**
		 * Obtains a selected sector and creates a desirability zone based on the sector
		 * if it has changed.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (getWorldMode().equals(WorldMode.ENVIRONMENT)) {
				if (null == evt.getNewValue()) {
					Sector envSector = desirabilitySectorSelector.getSector();
					if (null != envSector) {
						Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
						session.getActiveScenario().addDesirabilityZone(envSector,
								session.getSetup().getDesirabilitySpecification());
						initDesirabilityZones();
					}
					desirabilitySectorSelector.disable();
					setWorldMode(WorldMode.VIEW);
				}
			}
		}
	}

	/**
	 * Realizes an aircraft mouse handler.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class AircraftMouseHandler implements Runnable {

		/**
		 * Sets an aircraft and creates an associated waypoint at the mouse position.
		 * 
		 * @see Runnable#run()
		 */
		@Override
		public void run() {
			Position clickedPosition = wwd.getCurrentPosition();
			if (null != clickedPosition) {
				Waypoint waypoint = new Waypoint(clickedPosition);
				waypoint.setDepiction(
						new Depiction(symbolFactory.createPoint(Waypoint.SIDC_NAV_WAYPOINT_POI, waypoint, null)));
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

				setWorldMode(WorldMode.VIEW);
			}
		}
	}

	/**
	 * Realizes a waypoint mouse handler.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class WaypointMouseHandler implements Runnable {

		/**
		 * Creates a new waypoint at the mouse position.
		 * 
		 * @see Runnable#run()
		 */
		@Override
		public void run() {
			Position clickedPosition = wwd.getCurrentPosition();
			if (null != clickedPosition) {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				Setup setup = session.getSetup();
				double alt = setup.getDefaultWaypointHeight();
				Waypoint waypoint = null;
				if(alt>=0) {
					waypoint = new Waypoint(new Position(clickedPosition.getLatitude(), clickedPosition.getLongitude(), alt));
				}
				else {
					waypoint = new Waypoint(clickedPosition); 
				}
				waypoint.setDepiction(
						new Depiction(symbolFactory.createPoint(Waypoint.SIDC_NAV_WAYPOINT_POI, waypoint, null)));
				waypoint.getDepiction().setVisible(true);
				scenario.addWaypoint(waypoint);
			}
		}
	}

	/**
	 * Realizes a time change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class TimeChangeListener implements PropertyChangeListener {

		/**
		 * Initializes the aircraft, environment and obstacles if the time changes.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			// TODO: possibly only redraw layers
			initAircraft();
			initEnvironment();
			initObstacles();
			initTerrainObstacles();
		}
	}

	/**
	 * Realizes a threshold cost change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class ThresholdChangeListener implements PropertyChangeListener {

		/**
		 * Initializes the aircraft, environment and obstacles if the threshold changes.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			// TODO: possibly only redraw layers
			initAircraft();
			initEnvironment();
			initObstacles();
			initTerrainObstacles();
		}
	}

	/**
	 * Realizes an aircraft change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class AircraftChangeListener implements PropertyChangeListener {

		/**
		 * Initializes the aircraft if it changes.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initAircraft();
			initView();
		}
	}

	/**
	 * Realizes an environment change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class EnvironmentChangeListener implements PropertyChangeListener {

		/**
		 * Initializes the environment if it changes.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initEnvironment();
		}
	}

	/**
	 * Realizes a desirability zone change listener.
	 * 
	 * @author Henrique Ferreira
	 *
	 */
	private class DesirabilityZonesChangeListener implements PropertyChangeListener {

		/**
		 * Initializes the desirability zones if they change.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initDesirabilityZones();
		}
	}

	/**
	 * Realizes a waypoints change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class WaypointsChangeListener implements PropertyChangeListener {

		/**
		 * Initializes the plan if the waypoints change.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initPlan();
		}
	}

	/**
	 * Realizes a trajectory change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class TrajectoryChangeListener implements PropertyChangeListener {

		/**
		 * Initializes the plan if the trajectory changes.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initPlan();
		}
	}

	/**
	 * Realizes an obstacles change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class ObstaclesChangeListener implements PropertyChangeListener {

		/**
		 * Initializes the obstacles if they change.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initObstacles();
		}
	}

	/**
	 * Realizes a terrain obstacles change listener.
	 * 
	 * @author Henrique Ferreira
	 *
	 */
	private class TerrainObstaclesChangeListener implements PropertyChangeListener {

		/**
		 * Initializes the terrain obstacles if they change.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initTerrainObstacles();
		}
	}

	/**
	 * Realizes a track change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class TrackChangeListener implements PropertyChangeListener {

		/**
		 * Initializes the track if it changes.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initTrack();
			initView();
		}
	}

	/**
	 * Realizes an active scenario change listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class ActiveScenarioChangeListener implements PropertyChangeListener {

		/**
		 * Initializes the scenario, aircraft, environment, obstacles and plan if the
		 * active scenario changes.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			initScenario();
			initAircraft();
			initEnvironment();
			initDesirabilityZones();
			initObstacles();
			initTerrainObstacles();
			initPlan();
			initTrack();
			initView();
		}
	}

	/**
	 * Realizes an aircraft control listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class AircraftControlListener implements ActionListener {

		/**
		 * Performs the aircraft control action.
		 * 
		 * @param e the action event associated with the aircraft control action
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_AICRAFT_SET:
				setWorldMode(WorldMode.AIRCRAFT);
				break;
			case WorldPresenter.ACTION_AIRCAFT_SETUP:
				setup(SetupDialog.AIRCRAFT_TAB_INDEX);
				break;
			}
		}
	}

	/**
	 * Realizes a SWIM control listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class SwimControlListener implements ActionListener {

		/**
		 * Performs the SWIM control action.
		 * 
		 * @param e the action event associated with the SWIM control action
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_SWIM_LOAD:
				load(WorldPresenter.FILE_CHOOSER_TITLE_SWIM,
						new ExtensionFilter[] { new ExtensionFilter(
								WorldPresenter.FILE_CHOOSER_SWIM,
								WorldPresenter.FILE_CHOOSER_EXTENSION_SWIM) });
				break;
			case WorldPresenter.ACTION_SWIM_SETUP:
				defaultLoadSWIM(); // TODO: Temporary for flight testing
				// setup(SetupDialog.SWIM_TAB_INDEX);
				break;
			}
		}
	}

	/**
	 * Realizes an environment control listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class EnvironmentControlListener implements ActionListener {

		/**
		 * Performs the environment control action.
		 * 
		 * @param e the action event associated with the environment control action
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_ENVIRONMENT_ENCLOSE:
				setWorldMode(WorldMode.ENVIRONMENT);
				sectorSelector.enable();
				break;
			case WorldPresenter.ACTION_ENVIRONMENT_SETUP:
				setup(SetupDialog.ENVIRONMENT_TAB_INDEX);
				break;
			}
		}
	}

	/**
	 * Realizes a waypoints control listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class WaypointsControlListener implements ActionListener {

		/**
		 * Performs the waypoints control action.
		 * 
		 * @param e the action event associated with the waypoints control action
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_WAYPOINT_EDIT:
				setWorldMode(WorldMode.WAYPOINT);
				break;
			case WorldPresenter.ACTION_WAYPOINT_SETUP:
				// TODO: waypoint setup (types of waypoint graphics: POI, RWP, ...)
				setupWaypoint();
				setWorldMode(WorldMode.VIEW);
				break;
			}
		}
	}

	/**
	 * Realizes a planner control listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class PlannerControlListener implements ActionListener {

		/**
		 * Performs the planner control action.
		 * 
		 * @param e the action event associated with the planner control action
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_PLANNER_PLAN:
				plan();
				break;
			case WorldPresenter.ACTION_PLANNER_SETUP:
				setup(SetupDialog.PLANNER_TAB_INDEX);
				break;
			}
		}
	}

	/**
	 * Realizes an datalink control listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class DatalinkControlListener implements ActionListener {

		/**
		 * Performs the datalink control action.
		 * 
		 * @param e the action event associated with the datalink control action
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_DATALINK_MONITOR:
				monitor();
				break;
			case WorldPresenter.ACTION_DATALINK_SETUP:
				setup(SetupDialog.DATALINK_TAB_INDEX);
				break;
			}
		}
	}

	/**
	 * Realizes an upload control listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class UploadControlListener implements ActionListener {

		/**
		 * Performs the upload control action.
		 * 
		 * @param e the action event associated with the upload control action
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_TRANSFER_UPLOAD:
				upload();
				break;
			case WorldPresenter.ACTION_START_MISSION:
				startMission();
				// TODO: possibly download flight-log, TransferControlListener
				// Sets the aircraft mode to AUTO, in order to begin the autonomous mission
				break;
			}
		}
	}

	/**
	 * Realizes a take-off control listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class TakeOffControlListener implements ActionListener {

		/**
		 * Performs the take-off control action.
		 * 
		 * @param e the action event associated with the take-off control action
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_FLIGHT_TAKEOFF:
				takeoff();
				break;
			case WorldPresenter.ACTION_FLIGHT_SETUP:
				// TODO: setup(SetupDialog.FLIGHT_TAB_INDEX);
				break;
			}
		}
	}

	/**
	 * Realizes a land control listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class LandControlListener implements ActionListener {

		/**
		 * Performs the land control action.
		 * 
		 * @param e the action event associated with the land control action
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_FLIGHT_LAND:
				land(false);
				break;
			case WorldPresenter.ACTION_FLIGHT_RETURN:
				land(true);
				break;
			}
		}
	}

	/**
	 * Realizes a view control listener.
	 * 
	 * @author Stephan Heinemann
	 *
	 */
	private class ViewControlListener implements ActionListener {

		/**
		 * Performs the view control action.
		 * 
		 * @param e the action event associated with the view control action
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_VIEW_CYCLE:
				view(true);
				break;
			case WorldPresenter.ACTION_VIEW_RESET:
				view(false);
				break;
			}
		}
	}

	/**
	 * Realizes a desirability zone control listener.
	 * 
	 * @author Henrique Ferreira
	 *
	 */
	private class DesirabilityZoneControlListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_DESIRABILITY_ENCLOSE:
				setWorldMode(WorldMode.ENVIRONMENT);
				desirabilitySectorSelector.enable();
				break;
			case WorldPresenter.ACTION_DESIRABILITY_SETUP:
				setupDesirability();
				break;
			}
		}
	}
	
	/**
	 * Realizes a terrain control listener.
	 * 
	 * @author Manuel Rosa
	 * @author Henrique Ferreira
	 *
	 */
	private class TerrainControlListener implements ActionListener {

		/**
		 * Performs the terrain control action.
		 * 
		 * @param e the action event associated with the terrain control action
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_TERRAIN_LOAD:
				loadTerrain(WorldPresenter.FILE_CHOOSER_TITLE_TERRAIN,
						new ExtensionFilter[] { new ExtensionFilter(
								WorldPresenter.FILE_CHOOSER_TERRAIN,
								WorldPresenter.FILE_CHOOSER_EXTENSION_TERRAIN) });
				break;
			case WorldPresenter.ACTION_TERRAIN_SETUP:
				defaultLoadTerrain();  // TODO: Temporary for flight testing
				break;
			}
		}
	}
	
	// TODO Temporary for telemetry
	private class TelemetryControlListener implements ActionListener {

		/**
		 * Performs the terrain control action.
		 * 
		 * @param e the action event associated with the terrain control action
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
			Scenario scenario = session.getActiveScenario();
			switch (e.getActionCommand()) {
			case WorldPresenter.ACTION_TELEMETRY_WRITE:
				printToFile(scenario.getTrajectory());
				break;
			case WorldPresenter.ACTION_TELEMETRY_READ:
				// Read lat-lon-hei
				File file = new File("worldwind-comm/position.txt");
				Scanner sc = null;
				try {
					sc = new Scanner(file);
				} catch (FileNotFoundException exc) {
					exc.printStackTrace();
				}
				sc.useDelimiter(" ");
				double lat = Double.parseDouble(sc.next());
				double lon = Double.parseDouble(sc.next());
				double alt = Double.parseDouble(sc.next());
				// Add aircraft
				if (scenario.hasAircraft() && (0 < scenario.getWaypoints().size())) {
					scenario.removeWaypoint(0);
				}
				MilStd2525GraphicFactory symbolFactory = new MilStd2525GraphicFactory();
				Waypoint wpt = new Waypoint(Position.fromDegrees(lat, lon, alt));
				wpt.setDepiction(new Depiction(symbolFactory.createPoint(Waypoint.SIDC_NAV_WAYPOINT_POI, wpt, null)));
				wpt.getDepiction().setVisible(true);
				Iris iris = new Iris(wpt, 2.5, CombatIdentification.FRIEND);
				iris.moveTo(wpt);
				iris.setCostInterval(new CostInterval(
						"iris",
						ZonedDateTime.now(ZoneId.of("UTC")).minusYears(10),
						ZonedDateTime.now(ZoneId.of("UTC")).plusYears(10),
						100d));
				session.getActiveScenario().setAircraft(iris);
				session.getActiveScenario().addWaypoint(0, wpt);
				break;
			}
		}
	}
	
	public void printToFile(Trajectory trajectory) {
		try {
			System.out.println("creating file");
			PrintWriter printWriter = new PrintWriter("worldwind-comm/waypoints.txt", "UTF-8");
			ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
			for (Waypoint waypoint : trajectory.getWaypoints()) {
				waypoints.add(waypoint);
			}
			for (int i = 0; i < trajectory.getLength(); i++) {
				printWriter.printf("%f	%f	%f\n",
						waypoints.get(i).getLatitude().degrees,
						waypoints.get(i).getLongitude().degrees,
						waypoints.get(i).elevation);
			}
			printWriter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
