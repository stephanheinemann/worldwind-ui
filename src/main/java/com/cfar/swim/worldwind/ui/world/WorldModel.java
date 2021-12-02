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
package com.cfar.swim.worldwind.ui.world;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Realizes a world model which shares application state.
 * 
 * @author Stephan Heinemann
 *
 */
public class WorldModel {
	
	/** the world mode of the world model */
	private WorldMode worldMode, prevWorldMode;
	
	/** the view mode of the world model */
	private ViewMode viewMode;

	/** the property change support of this world model */
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	/**
	 * Constructs a new world model in view world mode with fix view mode.
	 */
	public WorldModel() {
		this.worldMode = WorldMode.VIEW;
		this.prevWorldMode = WorldMode.VIEW;
		this.viewMode = ViewMode.FIX;
	}
	
	/**
	 * Gets the world mode of this world model.
	 * 
	 * @return the world mode of this world model
	 */
	public synchronized WorldMode getWorldMode() {
		return this.worldMode;
	}
	
	/**
	 * Sets the world mode of this world model.
	 * 
	 * @param worldMode the world mode to be set
	 */
	public synchronized void setWorldMode(WorldMode worldMode) {
		this.worldMode = worldMode;
		this.pcs.firePropertyChange("worldMode", null, this.worldMode);
	}
	
	/**
	 * Gets the view mode of this world model.
	 * 
	 * @return the view mode of this world model
	 */
	public synchronized ViewMode getViewMode() {
		return this.viewMode;
	}
	
	/**
	 * Sets the view mode of this world model.
	 * 
	 * @param viewMode the view mode to be set
	 */
	public synchronized void setViewMode(ViewMode viewMode) {
		this.viewMode = viewMode;
		this.pcs.firePropertyChange("viewMode", null, this.viewMode);
	}
	
	/**
	 * Adds a world mode change listener to this world model.
	 * 
	 * @param listener the world mode change listener to be added
	 */
	public synchronized void addWorldModeChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener("worldMode", listener);
	}
	
	/**
	 * Adds a view mode change listener to this world model.
	 * 
	 * @param listener the view mode change listener to be added
	 */
	public synchronized void addViewModeChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener("viewMode", listener);
	}
	
	/**
	 * Transitions this world model into the aircraft mode.
	 * 
	 * @return true if the world model has transitioned into the aircraft mode,
	 *         false otherwise
	 */
	public synchronized boolean aircraft() {
		if ((WorldMode.VIEW == this.getWorldMode())
				|| (WorldMode.ENVIRONMENT == this.getWorldMode())
				|| (WorldMode.WAYPOINT == this.getWorldMode())) {
			this.setWorldMode(WorldMode.AIRCRAFT);
		}
		return (WorldMode.ENVIRONMENT == this.getWorldMode());
	}
	
	/**
	 * Determines whether or not this world model is in the aircraft mode.
	 * 
	 * @return true if this world model is in the aircraft mode,
	 *         false otherwise
	 */
	public synchronized boolean isAircraft() {
		return (WorldMode.AIRCRAFT == this.getWorldMode());
	}
	
	/**
	 * Transitions this world model into the environment mode.
	 * 
	 * @return true if the world model has transitioned into the environment
	 *         mode, false otherwise
	 */
	public synchronized boolean environment() {
		if ((WorldMode.VIEW == this.getWorldMode())
				|| (WorldMode.AIRCRAFT == this.getWorldMode())
				|| (WorldMode.WAYPOINT == this.getWorldMode())) {
			this.setWorldMode(WorldMode.ENVIRONMENT);
		}	
		return (WorldMode.ENVIRONMENT == this.getWorldMode());
	}
	
	/**
	 * Determines whether or not this world model is in the environment mode.
	 * 
	 * @return true if this world model is in the environment mode,
	 *         false otherwise
	 */
	public synchronized boolean isEnvironment() {
		return (WorldMode.ENVIRONMENT == this.getWorldMode());
	}
	
	/**
	 * Transitions this world model into the landing mode.
	 * 
	 * @return true if the world model has transitioned into the landing mode,
	 *         false otherwise
	 */
	public synchronized boolean land() {
		if ((WorldMode.VIEW == this.getWorldMode())
				|| (WorldMode.ENVIRONMENT == this.getWorldMode())
				|| (WorldMode.AIRCRAFT == this.getWorldMode())
				|| (WorldMode.WAYPOINT == this.getWorldMode())) {
			this.setWorldMode(WorldMode.LANDING);
		}
		return (WorldMode.LANDING == this.getWorldMode());
	}
	
	/**
	 * Determines whether or not this world model is in the landing mode.
	 * 
	 * @return true if this world model is in the landing mode,
	 *         false otherwise
	 */
	public synchronized boolean isLanding() {
		return (WorldMode.LANDING == this.getWorldMode());
	}
	
	/**
	 * Transitions this world model into the launching mode.
	 * 
	 * @return true if the world model has transitioned into the launching
	 *         mode, false otherwise
	 */
	public synchronized boolean launch() {
		if ((WorldMode.VIEW == this.getWorldMode())
				|| (WorldMode.ENVIRONMENT == this.getWorldMode())
				|| (WorldMode.AIRCRAFT == this.getWorldMode())
				|| (WorldMode.WAYPOINT == this.getWorldMode())) {
			this.setWorldMode(WorldMode.LAUNCHING);
		}
		return (WorldMode.LAUNCHING == this.getWorldMode());
	}
	
	/**
	 * Determines whether or not this world model is in the launching mode.
	 * 
	 * @return true if this world model is in the launching mode,
	 *         false otherwise
	 */
	public synchronized boolean isLaunching() {
		return (WorldMode.LAUNCHING == this.getWorldMode());
	}
	
	/**
	 * Transitions this world model into the loading mode.
	 * 
	 * @return true if the world model has transitioned into the loading mode,
	 *         false otherwise
	 */
	public synchronized boolean load() {
		boolean wasBusy = (WorldMode.LOADING == this.getWorldMode()
				|| (WorldMode.SAVING == this.getWorldMode()));
		if (!wasBusy) {
			this.prevWorldMode = this.getWorldMode();
			this.setWorldMode(WorldMode.LOADING);
		}
		return !wasBusy;
	}
	
	/**
	 * Determines whether or not this world model is in the loading mode.
	 * 
	 * @return true if this world model is in the loading mode,
	 *         false otherwise
	 */
	public synchronized boolean isLoading() {
		return (WorldMode.LOADING == this.getWorldMode());
	}
	
	/**
	 * Transitions this world model into the pre-loading mode.
	 * 
	 * @return true if the world model has transitioned into the pre-loading
	 *         mode, false otherwise
	 */
	public synchronized boolean loaded() {
		if (WorldMode.LOADING == this.getWorldMode()) {
			this.setWorldMode(this.prevWorldMode);
			this.notifyAll();
		}
		return (this.getWorldMode() == this.prevWorldMode);
	}
	
	/**
	 * Transitions this world model into the managing mode.
	 * 
	 * @return true if the world model has transitioned into the managing mode,
	 *         false otherwise
	 */
	public synchronized boolean manage() {
		if ((WorldMode.VIEW == this.getWorldMode())
				|| (WorldMode.ENVIRONMENT == this.getWorldMode())
				|| (WorldMode.AIRCRAFT == this.getWorldMode())
				|| (WorldMode.WAYPOINT == this.getWorldMode())) {
			this.setWorldMode(WorldMode.MANAGING);
		}
		return (WorldMode.MANAGING == this.getWorldMode());
	}
	
	/**
	 * Determines whether or not this world model is in the managing mode.
	 * 
	 * @return true if this world model is in the managing mode,
	 *         false otherwise
	 */
	public synchronized boolean isManaging() {
		return (WorldMode.MANAGING == this.getWorldMode());
	}
	
	/**
	 * Transitions this world model into the planning mode.
	 * 
	 * @return true if the world model has transitioned into the planning mode,
	 *         false otherwise
	 */
	public synchronized boolean plan() {
		if ((WorldMode.VIEW == this.getWorldMode())
				|| (WorldMode.ENVIRONMENT == this.getWorldMode())
				|| (WorldMode.AIRCRAFT == this.getWorldMode())
				|| (WorldMode.WAYPOINT == this.getWorldMode())) {
			this.setWorldMode(WorldMode.PLANNING);
		}
		return (WorldMode.PLANNING == this.getWorldMode());
	}
	
	/**
	 * Determines whether or not this world model is in the planning mode.
	 * 
	 * @return true if this world model is in the planning mode,
	 *         false otherwise
	 */
	public synchronized boolean isPlanning() {
		return (WorldMode.PLANNING == this.getWorldMode());
	}
	
	/**
	 * Transitions this world model into the saving mode.
	 * 
	 * @return true if the world model has transitioned into the saving mode,
	 *         false otherwise
	 */
	public synchronized boolean save() {
		boolean wasBusy = (WorldMode.LOADING == this.getWorldMode()
				|| (WorldMode.SAVING == this.getWorldMode()));
		if (!wasBusy) {
			this.prevWorldMode = this.getWorldMode();
			this.setWorldMode(WorldMode.SAVING);
		}
		return !wasBusy;
	}
	
	/**
	 * Determines whether or not this world model is in the saving mode.
	 * 
	 * @return true if this world model is in the saving mode,
	 *         false otherwise
	 */
	public synchronized boolean isSaving() {
		return (WorldMode.SAVING == this.getWorldMode());
	}
	
	/**
	 * Transitions this world model into the pre-saving mode.
	 * 
	 * @return true if the world model has transitioned into the pre-saving
	 *         mode, false otherwise
	 */
	public synchronized boolean saved() {
		if (WorldMode.SAVING == this.getWorldMode()) {
			this.setWorldMode(this.prevWorldMode);
			this.notifyAll();
		}
		return (this.getWorldMode() == this.prevWorldMode);
	}
	
	/**
	 * Transitions this world model into the terminating mode.
	 * 
	 * @return true if the world model has transitioned into the terminating
	 *         mode, false otherwise
	 */
	public synchronized boolean terminate() {
		if ((WorldMode.PLANNING == this.getWorldMode())
				|| (WorldMode.MANAGING == this.getWorldMode())) {
			this.setWorldMode(WorldMode.TERMINATING);
		}
		return (WorldMode.TERMINATING == this.getWorldMode());
	}
	
	/**
	 * Determines whether or not this world model is in the terminating mode.
	 * 
	 * @return true if this world model is in the terminating mode,
	 *         false otherwise
	 */
	public synchronized boolean isTerminating() {
		return (WorldMode.TERMINATING == this.getWorldMode());
	}
	
	/**
	 * Transitions this world model into the uploading mode.
	 * 
	 * @return true if the world model has transitioned into the uploading
	 *         mode, false otherwise
	 */
	public synchronized boolean upload() {
		if ((WorldMode.VIEW == this.getWorldMode())
				|| (WorldMode.ENVIRONMENT == this.getWorldMode())
				|| (WorldMode.AIRCRAFT == this.getWorldMode())
				|| (WorldMode.WAYPOINT == this.getWorldMode())) {
			this.setWorldMode(WorldMode.UPLOADING);
		}
		return (WorldMode.UPLOADING == this.getWorldMode());
	}
	
	/**
	 * Determines whether or not this world model is in the uploading mode.
	 * 
	 * @return true if this world model is in the uploading mode,
	 *         false otherwise
	 */
	public synchronized boolean isUploading() {
		return (WorldMode.UPLOADING == this.getWorldMode());
	}
	
	/**
	 * Transitions this world model into the view mode. The transition suspends
	 * if the world mode is loading or saving.
	 * 
	 * @return true if the world model has transitioned into the view mode,
	 *         false otherwise
	 */
	public synchronized boolean view() {
		while ((WorldMode.LOADING == this.getWorldMode())
				|| (WorldMode.SAVING == this.getWorldMode())) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.setWorldMode(WorldMode.VIEW);
		return true;
	}
	
	/**
	 * Determines whether or not this world model is in the view mode.
	 * 
	 * @return true if this world model is in the view mode,
	 *         false otherwise
	 */
	public synchronized boolean isView() {
		return (WorldMode.VIEW == this.getWorldMode());
	}
	
	/**
	 * Transitions this world model into the waypoint mode.
	 * 
	 * @return true if the world model has transitioned into the waypoint mode,
	 *         false otherwise
	 */
	public synchronized boolean waypoint() {
		if ((WorldMode.VIEW == this.getWorldMode())
				|| (WorldMode.AIRCRAFT == this.getWorldMode())
				|| (WorldMode.ENVIRONMENT == this.getWorldMode())) {
			this.setWorldMode(WorldMode.WAYPOINT);
		}
		return (WorldMode.WAYPOINT == this.getWorldMode());
	}
	
	/**
	 * Determines whether or not this world model is in the waypoint mode.
	 * 
	 * @return true if this world model is in the waypoint mode,
	 *         false otherwise
	 */
	public synchronized boolean isWaypoint() {
		return (WorldMode.WAYPOINT == this.getWorldMode());
	}
	
}
