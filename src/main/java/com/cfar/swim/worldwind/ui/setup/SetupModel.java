package com.cfar.swim.worldwind.ui.setup;

import com.cfar.swim.worldwind.ai.Planner;
import com.cfar.swim.worldwind.aircraft.Aircraft;
import com.cfar.swim.worldwind.planning.Environment;
import com.cfar.swim.worldwind.registries.Properties;

public class SetupModel {

	Properties<Aircraft> aircraftProperties;
	Properties<Environment> environmentProperties;
	Properties<Planner> plannerProperties;
	
	public Properties<Aircraft> getAircraftProperties() {
		return this.aircraftProperties;
	}

	public void setAircraftProperties(Properties<Aircraft> aircraftProperties) {
		this.aircraftProperties = aircraftProperties;
	}

	public Properties<Environment> getEnvironmentProperties() {
		return this.environmentProperties;
	}
	
	public void setEnvironmentProperties(Properties<Environment> environmentProperties) {
		this.environmentProperties = environmentProperties;
	}
	
	public Properties<Planner> getPlannerProperties() {
		return this.plannerProperties;
	}
	
	public void setPlannerProperties(Properties<Planner> plannerProperties) {
		this.plannerProperties = plannerProperties;
	}
	
}
