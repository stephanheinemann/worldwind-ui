package com.cfar.swim.worldwind.ui.world;

public class WorldModel {

	private WorldMode mode = WorldMode.VIEW;
	
	public WorldModel() {
		// TODO Auto-generated constructor stub
	}
	
	public void setMode(WorldMode mode) {
		this.mode = mode;
	}
	
	public WorldMode getMode() {
		return this.mode;
	}

}
