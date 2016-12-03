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
	private WorldMode mode;

	/** the property change support of this world model */
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	/**
	 * Constructs a new world model in view mode.
	 */
	public WorldModel() {
		this.mode = WorldMode.VIEW;
	}
	
	/**
	 * Sets the mode of this world model.
	 * 
	 * @param mode the mode to be set
	 */
	public void setMode(WorldMode mode) {
		this.mode = mode;
		this.pcs.firePropertyChange("mode", null, this.mode);
	}
	
	/**
	 * Gets the mode of this world model.
	 * 
	 * @return the mode of this world model
	 */
	public WorldMode getMode() {
		return this.mode;
	}
	
	/**
	 * Adds a mode change listener to this world model.
	 * 
	 * @param listener the property change listener to be added
	 */
	public void addModeChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener("mode", listener);
	}

}
