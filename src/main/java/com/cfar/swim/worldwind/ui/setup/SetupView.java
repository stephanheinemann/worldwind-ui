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

import com.airhacks.afterburner.views.FXMLView;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TabPane;

/**
 * Realizes a setup view.
 * 
 * @author Stephan Heinemann
 *
 */
public class SetupView extends FXMLView {
	
	/**
	 * Gets the view of this setup view.
	 * 
	 * @return the view of this setup view
	 * 
	 * @see FXMLView#getView()
	 */
	@Override
	public TabPane getView() {
		return (TabPane) super.getView();
	}
	
	/**
	 * Gets the aircraft selector of this setup view.
	 * 
	 * @return the aircraft selector of this setup view
	 */
	@SuppressWarnings("unchecked")
	public ComboBox<String> getAircraft() {
		return (ComboBox<String>) this.getView().lookup("#aircraft");
	}
	
	/**
	 * Gets the environment selector of this setup view.
	 * 
	 * @return the environment selector of this setup view
	 */
	@SuppressWarnings("unchecked")
	public ComboBox<String> getEnvironment() {
		return (ComboBox<String>) this.getView().lookup("#environment");
	}
	
	/**
	 * Gets the planner family selector of this setup view.
	 * 
	 * @return the planner family selector of this setup view
	 */
	@SuppressWarnings("unchecked")
	public ComboBox<String> getPlannerFamily() {
		return (ComboBox<String>) this.getView().lookup("#plannerFamily");
	}
	
	/**
	 * Gets the planner selector of this setup view.
	 * 
	 * @return the planner selector of this setup view
	 */
	@SuppressWarnings("unchecked")
	public ComboBox<String> getPlanner() {
		return (ComboBox<String>) this.getView().lookup("#planner");
	}
	
	/**
	 * Gets the datalink selector of this setup view.
	 * 
	 * @return the datalink selector of this setup view
	 */
	@SuppressWarnings("unchecked")
	public ComboBox<String> getDatalink() {
		return (ComboBox<String>) this.getView().lookup("#datalink");
	}
	
}
