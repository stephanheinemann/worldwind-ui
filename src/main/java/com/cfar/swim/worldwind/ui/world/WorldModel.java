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
	private WorldMode worldMode;
	private ViewMode viewMode;

	/** the property change support of this world model */
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	/**
	 * Constructs a new world model in view world mode with
	 * fix view mode.
	 */
	public WorldModel() {
		this.worldMode = WorldMode.VIEW;
		this.viewMode = ViewMode.FIX;
	}
	
	/**
	 * Gets the world mode of this world model.
	 * 
	 * @return the world mode of this world model
	 */
	public WorldMode getWorldMode() {
		return this.worldMode;
	}
	
	/**
	 * Sets the world mode of this world model.
	 * 
	 * @param worldMode the world mode to be set
	 */
	public void setWorldMode(WorldMode worldMode) {
		this.worldMode = worldMode;
		this.pcs.firePropertyChange("worldMode", null, this.worldMode);
	}
	
	/**
	 * Gets the view mode of this world model.
	 * 
	 * @return the view mode of this world model
	 */
	public ViewMode getViewMode() {
		return this.viewMode;
	}
	
	/**
	 * Sets the view mode of this world model.
	 * 
	 * @param viewMode the view mode to be set
	 */
	public void setViewMode(ViewMode viewMode) {
		this.viewMode = viewMode;
		this.pcs.firePropertyChange("viewMode", null, this.viewMode);
	}
	
	/**
	 * Adds a world mode change listener to this world model.
	 * 
	 * @param listener the world mode change listener to be added
	 */
	public void addWorldModeChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener("worldMode", listener);
	}
	
	/**
	 * Adds a view mode change listener to this world model.
	 * 
	 * @param listener the view mode change listener to be added
	 */
	public void addViewModeChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener("viewMode", listener);
	}

}
