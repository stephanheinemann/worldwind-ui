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
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.cfar.swim.worldwind.ai.DynamicPlanner;
import com.cfar.swim.worldwind.ai.PlanRevisionListener;
import com.cfar.swim.worldwind.ai.Planner;
import com.cfar.swim.worldwind.aircraft.Aircraft;
import com.cfar.swim.worldwind.connections.Datalink;
import com.cfar.swim.worldwind.connections.SwimConnection;
import com.cfar.swim.worldwind.planning.CostInterval;
import com.cfar.swim.worldwind.planning.Environment;
import com.cfar.swim.worldwind.planning.TrackPoint;
import com.cfar.swim.worldwind.planning.Trajectory;
import com.cfar.swim.worldwind.planning.Waypoint;
import com.cfar.swim.worldwind.registries.Specification;
import com.cfar.swim.worldwind.render.annotations.ControlAnnotation;
import com.cfar.swim.worldwind.render.annotations.DepictionAnnotation;
import com.cfar.swim.worldwind.session.Scenario;
import com.cfar.swim.worldwind.session.Session;
import com.cfar.swim.worldwind.session.SessionManager;
import com.cfar.swim.worldwind.ui.WorldwindPlanner;
import com.cfar.swim.worldwind.ui.planner.PlannerAlert;
import com.cfar.swim.worldwind.ui.planner.PlannerAlertResult;
import com.cfar.swim.worldwind.ui.setup.SetupDialog;
import com.cfar.swim.worldwind.ui.setup.SetupModel;
import com.cfar.swim.worldwind.util.Depiction;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
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

/**
 * Realizes a presenter of a world view.
 * 
 * @author Stephan Heinemann
 *
 */
public class WorldPresenter implements Initializable {
	
	/** the environment icon of the world view */
	@Inject private String environmentIcon;
	
	/** the aircraft icon of the world view */
	@Inject private String aircraftIcon;
	
	/** the point of interest icon of the world view */
	@Inject private String poiIcon;
	
	/** the planner icon of the world view */
	@Inject private String plannerIcon;
	
	/** the swim icon of the world view */
	@Inject private String swimIcon;
	
	/** the datalink icon of the world view */
	@Inject private String datalinkIcon;
	
	/** the upload icon of the world view */
	@Inject private String uploadIcon;
	
	/** the take-off icon of the world view */
	@Inject private String takeoffIcon;
	
	/** the land icon of the world view */
	@Inject private String landIcon;
	
	/** the view icon of the world view */
	@Inject private String viewIcon;
	
	/** the setup icon of the world view */
	@Inject private String setupIcon;
	
	/** the no action command */
	public static final String ACTION_NONE = "WorldPresenter.ActionCommand.None";
	
	/** the enclose environment action command */
	public static final String ACTION_ENVIRONMENT_ENCLOSE = "WorldPresenter.ActionCommand.EnvironmentEnclose";
	
	/** the setup environment action command */
	public static final String ACTION_ENVIRONMENT_SETUP = "WorldPresenter.ActionCommand.EnvironmentSetup";
	
	/** the set aircraft action command */
	public static final String ACTION_AICRAFT_SET = "WorldPresenter.ActionCommand.AircraftSet";
	
	/** the setup aircraft action command */
	public static final String ACTION_AIRCAFT_SETUP = "WorldPresenter.ActionCommand.AircraftSetup";
	
	/** the edit waypoint action command */
	public static final String ACTION_WAYPOINT_EDIT = "WorldPresenter.ActionCommand.WaypointEdit";
	
	/** the setup waypoint action command */
	public static final String ACTION_WAYPOINT_SETUP = "WorldPresenter.ActionCommand.WaypointSetup";
	
	/** the plan action command */
	public static final String ACTION_PLANNER_PLAN = "WorldPresenter.ActionCommand.PlannerPlan";
	
	/** the setup planner action command */
	public static final String ACTION_PLANNER_SETUP = "WorldPresenter.ActionCommand.PlannerSetup";
	
	/** the connect swim action command */
	public static final String ACTION_SWIM_CONNECT = "WorldPresenter.ActionCommand.SwimConnect";
	
	/** the setup swim action command */
	public static final String ACTION_SWIM_SETUP = "WorldPresenter.ActionCommand.SwimSetup";
	
	/** the connect datalink action command */
	public static final String ACTION_DATALINK_CONNECT = "WorldPresenter.ActionCommand.DatalinkConnect";
	
	/** the setup datalink action command */
	public static final String ACTION_DATALINK_SETUP = "WorldPresenter.ActionCommand.DatalinkSetup";
	
	/** the upload action command */
	public static final String ACTION_TRANSFER_UPLOAD = "WorldPresenter.ActionCommand.TransferUpload";
	
	/** the take-off action command */
	public static final String ACTION_FLIGHT_TAKEOFF = "WorldPresenter.ActionCommand.FlightTakeOff";
	
	/** the setup flight action command */
	public static final String ACTION_FLIGHT_SETUP = "WorldPresenter.ActionCommand.FlightSetup";
	
	/** the land action command */
	public static final String ACTION_FLIGHT_LAND = "WorldPresenter.ActionCommand.Land";
	
	/** the return action command */
	public static final String ACTION_FLIGHT_RETURN = "WorldPresenter.ActionCommand.Return";
	
	/** the cycle view action command */
	public static final String ACTION_VIEW_CYCLE = "WorldPresenter.ActionCommand.ViewCycle";
	
	/** the reset view action command */
	public static final String ACTION_VIEW_RESET = "WorldPresenter.ActionCommand.ViewReset";
	
	// TODO: consider to move all visible UI text into properties files
	
	/** the file chooser open swim file title */
	public static final String FILE_CHOOSER_TITLE_SWIM = "Open SWIM File";
	
	/** the file chooser swim file description */
	public static final String FILE_CHOOSER_SWIM = "SWIM Files";
	
	/** the file chooser swim file extension */
	public static final String FILE_CHOOSER_EXTENSION_SWIM = "*.xml";
	
	/** the world pane of the world view */
	@FXML
	private AnchorPane worldNodePane;
	
	/** the world node of the world view (swing inside fx) */
	@FXML
	private SwingNode worldNode;
	
	/** the environment control of this world presenter */
	private ControlAnnotation environmentControl;
	
	/** the aircraft control of this world presenter */
	private ControlAnnotation aircraftControl;
	
	/** the point of interest control of this world presenter */
	private ControlAnnotation poiControl;
	
	/** the planner control of this world presenter */
	private ControlAnnotation plannerControl;
	
	/** the SWIM control of this world presenter */
	private ControlAnnotation swimControl;
	
	/** the datalink control of this world presenter */
	private ControlAnnotation datalinkControl;
	
	/** the upload control of this world presenter */
	private ControlAnnotation uploadControl;
	
	/** the take-off control of this world presenter */
	private ControlAnnotation takeoffControl;
	
	/** the land control of this world presenter */
	private ControlAnnotation landControl;
	
	/** the view control of this world presenter */
	private ControlAnnotation viewControl;
	
	/** the status annotation of this world presenter */
	private ScreenAnnotation statusAnnotation;
	
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
	
	/** the waypoint layer of this world presenter */
	private final RenderableLayer waypointLayer = new RenderableLayer();
	
	/** the obstacles layer of this world presenter */
	private final RenderableLayer obstaclesLayer = new RenderableLayer();
	
	/** the track layer of this world presenter */
	private final MarkerLayer trackLayer = new MarkerLayer();
	
	/** the symbol factory of this world presenter */
	private final MilStd2525GraphicFactory symbolFactory = new MilStd2525GraphicFactory();
	
	/** the sector selector of this world presenter */
	private final SectorSelector sectorSelector = new SectorSelector(wwd);
	
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
	
	/** the waypoints change listener of this world presenter */
	private final WaypointsChangeListener waypointsCl = new WaypointsChangeListener();
	
	/** the trajectory change listener of this world presenter */
	private final TrajectoryChangeListener trajectoryCl = new TrajectoryChangeListener();
	
	/** the obstacles change listener of this world presenter */
	private final ObstaclesChangeListener obstaclesCl = new ObstaclesChangeListener();
	
	/** the track change listener of this world presenter */
	private final TrackChangeListener trackCl = new TrackChangeListener();
	
	/** the sequential executor of this world presenter */
	private final Executor executor = Executors.newSingleThreadExecutor();
	
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
		
		this.initScenario();
		this.initAircraft();
		this.initEnvironment();
		this.initObstacles();
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
							//TrackPoint previous = scenario.getDatalink().getPreviousTrackPoint(5);
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
	
	// TODO: review moding, synchronization, busy cycle and possible actions carefully
	
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
			
		// display status according to world mode
		this.displayStatus(worldMode.toString());
		
		// frame controls according to world mode
		this.frameControl(this.aircraftControl, false);
		this.frameControl(this.environmentControl, false);
		this.frameControl(this.landControl, false);
		this.frameControl(this.takeoffControl, false);
		this.frameControl(this.plannerControl, false);
		this.frameControl(this.uploadControl, false);
		this.frameControl(this.poiControl, false);
		
		switch (worldMode) {
		case AIRCRAFT:
			this.frameControl(this.aircraftControl, true);
			break;
		case ENVIRONMENT:
			this.frameControl(this.environmentControl, true);
			break;
		case LANDING:
			this.frameControl(this.landControl, true);
			break;
		case LAUNCHING:
			this.frameControl(this.takeoffControl, true);
			break;
		case PLANNING:
			this.frameControl(this.plannerControl, true);
			break;
		case UPLOADING:
			this.frameControl(this.uploadControl, true);
			break;
		case WAYPOINT:
			this.frameControl(this.poiControl, true);
			break;
		default:
			this.frameControl(this.aircraftControl, false);
			this.frameControl(this.environmentControl, false);
			this.frameControl(this.landControl, false);
			this.frameControl(this.takeoffControl, false);
			this.frameControl(this.plannerControl, false);
			this.frameControl(this.uploadControl, false);
			this.frameControl(this.poiControl, false);
			break;
		}
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
				statusAnnotation.setText(status);
				wwd.redraw();
			}
		});
	}
	
	/**
	 * Frames a control annotation of this world presenter.
	 * 
	 * @param control the control annotation
	 * @param frame the frame status to be realized
	 */
	private void frameControl(ControlAnnotation control, boolean frame) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (frame) {
					control.frame();
				} else {
					control.unframe();
				}
				wwd.redraw();
			}
		});
	}
	
	/**
	 * Opens a planner alert with a specified alert type, title, header and
	 * content. A result can be passed for synchronization.
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
	 * Opens the setup dialog with a specified tab.
	 * 
	 * @param tabIndex the tab index of the tab to be opened
	 */
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
	
	/**
	 * Enables the environment creation mode.
	 */
	private void environment() {
		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				if ((WorldMode.VIEW == getWorldMode())
						|| (WorldMode.AIRCRAFT == getWorldMode())
						|| (WorldMode.WAYPOINT == getWorldMode())) {
					setWorldMode(WorldMode.ENVIRONMENT);
					sectorSelector.enable();
				}
			}
		});
	}
	
	/**
	 * Creates a new environment based on a selected sector.
	 */
	private void createEnvironment() {
		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				if (WorldMode.ENVIRONMENT == getWorldMode()) {
					Sector envSector = sectorSelector.getSector();
					if (null != envSector) {
						Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
						session.getActiveScenario().setSector(envSector);
						Specification<Environment> envSpec = session.getSetup().getEnvironmentSpecification();
						session.getEnvironmentFactory().setSpecification(envSpec);
						Environment env = session.getEnvironmentFactory().createInstance();
						session.getActiveScenario().setEnvironment(env);
						initEnvironment();
					}
					sectorSelector.disable();
					setWorldMode(WorldMode.VIEW);
				}
			}
		});
	} 
	
	/**
	 * Enables the aircraft creation mode.
	 */
	private void aircraft() {
		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				if ((WorldMode.VIEW == getWorldMode())
						|| (WorldMode.ENVIRONMENT == getWorldMode())
						|| (WorldMode.WAYPOINT == getWorldMode())) {
					setWorldMode(WorldMode.AIRCRAFT);
				}
			}
		});
	}
	
	/**
	 * Creates a new aircraft at the clicked position.
	 * 
	 * @param amh the aircraft mouse handler to execute
	 */
	private void aircraft(AircraftMouseHandler amh) {
		this.executor.execute(amh);
	}
	
	/**
	 * Enables the point of interest creation mode.
	 */
	private void waypoint() {
		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				if ((WorldMode.VIEW == getWorldMode())
						|| (WorldMode.AIRCRAFT == getWorldMode())
						|| (WorldMode.ENVIRONMENT == getWorldMode())) {
					setWorldMode(WorldMode.WAYPOINT);
				}
			}
		});
	}
	
	/**
	 * Creates a new waypoint at the clicked position.
	 * 
	 * @param wmh the waypoint mouse handler to execute
	 */
	private void waypoint(WaypointMouseHandler wmh) {
		this.executor.execute(wmh);
	}
	
	/**
	 * Plans a trajectory along the waypoints of the active scenario.
	 */
	private void plan() {
		if (WorldMode.PLANNING == getWorldMode()) {
			Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
			Planner planner = session.getActiveScenario().getPlanner();
			
			// terminate running dynamic planner
			if (planner instanceof DynamicPlanner) {
				((DynamicPlanner) planner).terminate();
			}
		} else {
			this.executor.execute(new Runnable() {
				@Override
				public void run() {
					if ((WorldMode.VIEW == getWorldMode())
							|| (WorldMode.ENVIRONMENT == getWorldMode())
							|| (WorldMode.AIRCRAFT == getWorldMode())
							|| (WorldMode.WAYPOINT == getWorldMode())) {
					
						Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
						Planner planner = session.getActiveScenario().getPlanner();
						
						if (!planner.matches(session.getSetup().getPlannerSpecification())
								|| (planner.getAircraft() != session.getActiveScenario().getAircraft())
								|| (planner.getEnvironment() != session.getActiveScenario().getEnvironment())) {
							// create new planner
							Specification<Planner> plannerSpec = session.getSetup().getPlannerSpecification();
							session.getPlannerFactory().setSpecification(plannerSpec);
							planner = session.getPlannerFactory().createInstance();
							session.getActiveScenario().setPlanner(planner);
						} else if (planner instanceof DynamicPlanner) {
							// recycle terminated dynamic planner
							((DynamicPlanner) planner).recycle();
						}
						
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
							
							// listen for plan revisions
							planner.addPlanRevisionListener(new PlanRevisionListener() {
								@Override
								public void revisePlan(Trajectory trajectory) {
									// TODO: clearing trajectory versus uploading empty trajectory
									//if (!trajectory.isEmpty()) {
										styleTrajectory(trajectory);
										session.getActiveScenario().setTrajectory(trajectory);
										// TODO: consider automatic data uplink if connected
										// TODO: anytime, dyanmic and online planners
										System.out.println("revising plan...");
										Thread.yield();
									//}
								}
							});
							
							// TODO: consider asynchronous planning
							setWorldMode(WorldMode.PLANNING);
							if (waypoints.isEmpty()) {
								planner.plan(origin, destination, session.getActiveScenario().getTime());
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
				}
			});
		}
	}
	
	/**
	 * Establishes a SWIM connection.
	 */
	private void swim() {
		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				SwimConnection swimConnection = session.getActiveScenario().getSwimConnection();
				
				if (swimConnection.isConnected()) {
					// disconnect current SWIM connection
					swimConnection.disconnect();
				} else {
					if (!swimConnection.matches(session.getSetup().getSwimConnectionSpecification())) {
						// create new SWIM connection
						Specification<SwimConnection> swimConnectionSpec = session.getSetup().getSwimConnectionSpecification();
						session.getSwimConnectionFactory().setSpecification(swimConnectionSpec);
						swimConnection = session.getSwimConnectionFactory().createInstance();
						session.getActiveScenario().setSwimConnection(swimConnection);
						session.getActiveScenario().getSwimConnection().setObstacleManager(session.getActiveScenario());
					}
					// connect current SWIM connection
					swimConnection.connect();
					
					if (!swimConnection.isConnected()) {
						alert(
							AlertType.ERROR,
							PlannerAlert.ALERT_TITLE_SWIM_INVALID,
							PlannerAlert.ALERT_HEADER_SWIM_INVALID,
							PlannerAlert.ALERT_CONTENT_SWIM_INVALID,
							null);
					}
				}
				
				frameControl(swimControl, swimConnection.isConnected());
			}
		});
	}
	
	/**
	 * Establishes a datalink connection.
	 */
	private void datalink() {
		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
				Datalink datalink = session.getActiveScenario().getDatalink();
				
				if (datalink.isConnected()) {
					if (datalink.isMonitoring()) {
						// disable monitoring
						datalink.stopMonitoring();
						datalink.removePropertyChangeListener(trackCl);
					}
					// disconnect current datlink
					datalink.disconnect();
				} else {
					if (!datalink.matches(session.getSetup().getDatalinkSpecification())) {
						// create new datalink
						Specification<Datalink> datalinkSpec = session.getSetup().getDatalinkSpecification();
						session.getDatalinkFactory().setSpecification(datalinkSpec);
						datalink = session.getDatalinkFactory().createInstance();
						session.getActiveScenario().setDatalink(datalink);
					}
					// connect current datalink
					datalink.connect();
					
					if (datalink.isConnected()) {
						// enable monitoring
						datalink.addTrackChangeListener(trackCl);
						datalink.startMonitoring();
					} else {
						alert(
							AlertType.ERROR,
							PlannerAlert.ALERT_TITLE_DATALINK_INVALID,
							PlannerAlert.ALERT_HEADER_DATALINK_INVALID,
							PlannerAlert.ALERT_CONTENT_DATALINK_INVALID,
							null);
					}
				}
				
				frameControl(datalinkControl, datalink.isConnected());
			}
		});
	}
	
	/**
	 * Uploads a trajectory via the datalink of the active scenario.
	 */
	private void upload() {
		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				if ((WorldMode.VIEW == getWorldMode())
						|| (WorldMode.ENVIRONMENT == getWorldMode())
						|| (WorldMode.AIRCRAFT == getWorldMode())
						|| (WorldMode.WAYPOINT == getWorldMode())) {
					
					
					Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
					Datalink datalink = session.getActiveScenario().getDatalink();
					
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
			}
		});
	}
	
	/**
	 * Issues a take-off command via the datalink of the active scenario.
	 */
	private void takeoff() {
		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				if ((WorldMode.VIEW == getWorldMode())
						|| (WorldMode.ENVIRONMENT == getWorldMode())
						|| (WorldMode.AIRCRAFT == getWorldMode())
						|| (WorldMode.WAYPOINT == getWorldMode())) {
					
					PlannerAlertResult clearance = new PlannerAlertResult();
					alert(
						AlertType.CONFIRMATION,
						PlannerAlert.ALERT_TITLE_TAKEOFF_CONFIRM,
						PlannerAlert.ALERT_HEADER_TAKEOFF_CONFIRM,
						PlannerAlert.ALERT_CONTENT_TAKEOFF_CONFIRM,
						clearance);
					
					if (clearance.isOk()) {
						Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
						Datalink datalink = session.getActiveScenario().getDatalink();
						
						if (datalink.isConnected() && session.getActiveScenario().hasTrajectory()) {
							setWorldMode(WorldMode.LAUNCHING);
							//datalink.disableAircraftSafety();
							//datalink.armAircraft();
							datalink.takeOff(); // TODO: flight (envelope) setup
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
				}
			}
		});
	}
	
	/**
	 * Issues a land command via the datalink of the active scenario.
	 * 
	 * @param returnToLaunch indicates whether or not the landing shall be
	 *                       performed at the launch position
	 */
	private void land(boolean returnToLaunch) {
		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				if ((WorldMode.VIEW == getWorldMode())
						|| (WorldMode.ENVIRONMENT == getWorldMode())
						|| (WorldMode.AIRCRAFT == getWorldMode())
						|| (WorldMode.WAYPOINT == getWorldMode())) {
				
					PlannerAlertResult clearance = new PlannerAlertResult();
					alert(
						AlertType.CONFIRMATION,
						PlannerAlert.ALERT_TITLE_LAND_CONFIRM,
						PlannerAlert.ALERT_HEADER_LAND_CONFIRM,
						PlannerAlert.ALERT_CONTENT_LAND_CONFIRM,
						clearance);
					
					if (clearance.isOk()) {
						Session session = SessionManager.getInstance().getSession(WorldwindPlanner.APPLICATION_TITLE);
						Datalink datalink = session.getActiveScenario().getDatalink();
						
						if (datalink.isConnected()) {
							setWorldMode(WorldMode.LANDING);
							if (returnToLaunch) {
								datalink.returnToLaunch();
							} else {
								datalink.land();
							}
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
				}
			}
		});
	}
	
	/**
	 * Cycles or resets the view mode of the world view.
	 * 
	 * @param cycle indicates whether to cycle or otherwise
	 *              reset the world view
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
			Depiction depiction = new Depiction(symbolFactory.createPoint(Waypoint.SICD_NAV_WAYPOINT_ROUTE, waypoint, null));
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
			wwd.getModel().getLayers().add(waypointLayer);
			wwd.getModel().getLayers().add(obstaclesLayer);
			wwd.getModel().getLayers().add(trackLayer);
			
			// add planner controls
			environmentControl = new ControlAnnotation(environmentIcon);
			environmentControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) - 325, 25));
			environmentControl.setPrimaryActionCommand(WorldPresenter.ACTION_ENVIRONMENT_ENCLOSE);
			environmentControl.setSecondaryActionCommand(ACTION_ENVIRONMENT_SETUP);
			environmentControl.addActionListener(new EnvironmentControlListener());
			
			aircraftControl = new ControlAnnotation(aircraftIcon);
			aircraftControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) - 250, 25));
			aircraftControl.setPrimaryActionCommand(WorldPresenter.ACTION_AICRAFT_SET);
			aircraftControl.setSecondaryActionCommand(WorldPresenter.ACTION_AIRCAFT_SETUP);
			aircraftControl.addActionListener(new AircraftControlListener());
			
			poiControl = new ControlAnnotation(poiIcon);
			poiControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) - 175, 25));
			poiControl.setPrimaryActionCommand(WorldPresenter.ACTION_WAYPOINT_EDIT);
			poiControl.setSecondaryActionCommand(WorldPresenter.ACTION_WAYPOINT_SETUP);
			poiControl.addActionListener(new WaypointsControlListener());
			
			plannerControl = new ControlAnnotation(plannerIcon);
			plannerControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) -100, 25));
			plannerControl.setPrimaryActionCommand(WorldPresenter.ACTION_PLANNER_PLAN);
			plannerControl.setSecondaryActionCommand(WorldPresenter.ACTION_PLANNER_SETUP);
			plannerControl.addActionListener(new PlannerControlListener());
			
			swimControl = new ControlAnnotation(swimIcon);
			swimControl.getAttributes().setDrawOffset((new Point((wwd.getWidth() / 2) - 25, 25)));
			swimControl.setPrimaryActionCommand(WorldPresenter.ACTION_SWIM_CONNECT);
			swimControl.setSecondaryActionCommand(WorldPresenter.ACTION_SWIM_SETUP);
			swimControl.addActionListener(new SwimControlListener());
			
			datalinkControl = new ControlAnnotation(datalinkIcon);
			datalinkControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 50, 25));
			datalinkControl.setPrimaryActionCommand(WorldPresenter.ACTION_DATALINK_CONNECT);
			datalinkControl.setSecondaryActionCommand(WorldPresenter.ACTION_DATALINK_SETUP);
			datalinkControl.addActionListener(new DatalinkControlListener());
			
			uploadControl = new ControlAnnotation(uploadIcon);
			uploadControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 125, 25));
			uploadControl.setPrimaryActionCommand(WorldPresenter.ACTION_TRANSFER_UPLOAD);
			uploadControl.setSecondaryActionCommand(WorldPresenter.ACTION_NONE);
			uploadControl.addActionListener(new UploadControlListener());
			
			takeoffControl = new ControlAnnotation(takeoffIcon);
			takeoffControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 200, 25));
			takeoffControl.setPrimaryActionCommand(WorldPresenter.ACTION_FLIGHT_TAKEOFF);
			takeoffControl.setSecondaryActionCommand(WorldPresenter.ACTION_FLIGHT_SETUP);
			takeoffControl.addActionListener(new TakeOffControlListener());
			
			landControl = new ControlAnnotation(landIcon);
			landControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 275, 25));
			landControl.setPrimaryActionCommand(WorldPresenter.ACTION_FLIGHT_LAND);
			landControl.setSecondaryActionCommand(WorldPresenter.ACTION_FLIGHT_RETURN);
			landControl.addActionListener(new LandControlListener());
			
			viewControl = new ControlAnnotation(viewIcon);
			viewControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 350, 25));
			viewControl.setPrimaryActionCommand(WorldPresenter.ACTION_VIEW_CYCLE);
			viewControl.setSecondaryActionCommand(WorldPresenter.ACTION_VIEW_RESET);
			viewControl.addActionListener(new ViewControlListener());
			
			controlLayer.addAnnotation(environmentControl);
			wwd.addSelectListener(environmentControl);
			controlLayer.addAnnotation(aircraftControl);
			wwd.addSelectListener(aircraftControl);
			controlLayer.addAnnotation(poiControl);
			wwd.addSelectListener(poiControl);
			controlLayer.addAnnotation(plannerControl);
			wwd.addSelectListener(plannerControl);
			controlLayer.addAnnotation(swimControl);
			wwd.addSelectListener(swimControl);
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
			wwd.getModel().getLayers().add(controlLayer);
			
			// add on-screen status
			statusAnnotation = new ScreenAnnotation("----------",
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
					environmentControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) - 325, 25));
					aircraftControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) - 250, 25));
					poiControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) - 175, 25));
					plannerControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) - 100, 25));
					swimControl.getAttributes().setDrawOffset((new Point((wwd.getWidth() / 2) - 25, 25)));
					datalinkControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 50, 25));
					uploadControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 125, 25));
					takeoffControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 200, 25));
					landControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 275, 25));
					viewControl.getAttributes().setDrawOffset(new Point((wwd.getWidth() / 2) + 350, 25));
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
		public void mouseClicked(MouseEvent e)  {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					if (WorldMode.AIRCRAFT == getWorldMode()) {
						aircraft(new AircraftMouseHandler());
					} else if (WorldMode.WAYPOINT == getWorldMode()) {
						waypoint(new WaypointMouseHandler());
					}
				}
			});
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
		 * Obtains a selected sector and creates an environment based on the
		 * sector if it has changed.
		 * 
		 * @param evt the property change event
		 * 
		 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (null == evt.getNewValue()) {
				createEnvironment();
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
		 * Sets an aircraft and creates an associated waypoint at the mouse
		 * position.
		 * 
		 * @see Runnable#run()
		 */
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
				Aircraft aircraft = scenario.getAircraft();
				
				if (!scenario.hasAircraft() || !scenario.getAircraft().matches(aircraftSpec)) {
					session.getAircraftFactory().setSpecification(aircraftSpec);
					aircraft = session.getAircraftFactory().createInstance();
					aircraft.setCostInterval(new CostInterval(
	        				aircraftSpec.getId(),
	        				ZonedDateTime.now(ZoneId.of("UTC")).minusYears(10),
	        				ZonedDateTime.now(ZoneId.of("UTC")).plusYears(10),
	        				100d));
				}
				
				aircraft.moveTo(waypoint);
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
				Waypoint waypoint = new Waypoint(clickedPosition);
				waypoint.setDepiction(new Depiction(symbolFactory.createPoint(Waypoint.SIDC_NAV_WAYPOINT_POI, waypoint, null)));
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
		 * Initializes the aircraft, environment and obstacles if the threshold
		 * changes.
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
		 * Initializes the scenario, aircraft, environment, obstacles and plan
		 * if the active scenario changes.
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
			initObstacles();
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
				aircraft();
				break;
			case WorldPresenter.ACTION_AIRCAFT_SETUP:
				setup(SetupDialog.AIRCRAFT_TAB_INDEX);
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
				environment();
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
				waypoint();
				break;
			case WorldPresenter.ACTION_WAYPOINT_SETUP:
				// TODO: waypoint setup (types of waypoint graphics: POI, RWP, ...)
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
			case WorldPresenter.ACTION_SWIM_CONNECT:
				swim();
				break;
			case WorldPresenter.ACTION_SWIM_SETUP:
				setup(SetupDialog.SWIM_TAB_INDEX);
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
			case WorldPresenter.ACTION_DATALINK_CONNECT:
				datalink();
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
			case WorldPresenter.ACTION_NONE:
				// TODO: possibly download flight-log, TransferControlListener
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
	
}
