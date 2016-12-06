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
package com.cfar.swim.worldwind.ui.setup;

import com.cfar.swim.worldwind.ai.Planner;
import com.cfar.swim.worldwind.aircraft.Aircraft;
import com.cfar.swim.worldwind.connections.Datalink;
import com.cfar.swim.worldwind.planning.Environment;
import com.cfar.swim.worldwind.registries.Properties;

/**
 * Realizes a setup model representing a copy of the actual setup.
 * It can be modified and committed, or otherwise disregarded in
 * a setup dialog.
 * 
 * @author Stephan Heinemann
 *
 * @see SetupDialog
 */
public class SetupModel {
	
	/** the aircraft properties of this setup model */
	private Properties<Aircraft> aircraftProperties;
	
	/** the environment properties of this setup model */
	private Properties<Environment> environmentProperties;
	
	/** the planner properties of this setup model */
	private Properties<Planner> plannerProperties;
	
	/** the datalink properties of this setup model */
	private Properties<Datalink> datalinkProperties;
	
	/**
	 * Gets the aircraft properties of this setup model.
	 * 
	 * @return the aircraft properties of this setup model
	 */
	public Properties<Aircraft> getAircraftProperties() {
		return this.aircraftProperties;
	}
	
	/**
	 * Sets the aircraft properties of this setup model.
	 * 
	 * @param aircraftProperties the aircraft properties to be set
	 */
	public void setAircraftProperties(Properties<Aircraft> aircraftProperties) {
		this.aircraftProperties = aircraftProperties;
	}
	
	/**
	 * Gets the environment properties of this setup model.
	 * 
	 * @return the environment properties of this setup model
	 */
	public Properties<Environment> getEnvironmentProperties() {
		return this.environmentProperties;
	}
	
	/**
	 * Sets the environment properties of this setup model.
	 * 
	 * @param environmentProperties the environment properties of this setup model
	 */
	public void setEnvironmentProperties(Properties<Environment> environmentProperties) {
		this.environmentProperties = environmentProperties;
	}
	
	/**
	 * Gets the planner properties of this setup model.
	 * 
	 * @return the planner properties of this setup model
	 */
	public Properties<Planner> getPlannerProperties() {
		return this.plannerProperties;
	}
	
	/**
	 * Sets the planner properties of this setup model.
	 * 
	 * @param plannerProperties the planner properties of this setup model
	 */
	public void setPlannerProperties(Properties<Planner> plannerProperties) {
		this.plannerProperties = plannerProperties;
	}
	
	/**
	 * Gets the datalink properties of this setup model.
	 * 
	 * @return the datalink properties of this setup model
	 */
	public Properties<Datalink> getDatalinkProperties() {
		return this.datalinkProperties;
	}
	
	/**
	 * Sets the datalink properties of this setup model.
	 * 
	 * @param datalinkProperties the datalink properties of this setup model
	 */
	public void setDatalinkProperties(Properties<Datalink> datalinkProperties) {
		this.datalinkProperties = datalinkProperties;
	}
	
}
