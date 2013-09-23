package org.lunifera.m2m.standalone.sharky.webclient;

import com.vaadin.ui.ProgressBar;

@SuppressWarnings("serial")
public class IntensityBar extends ProgressBar {

	public IntensityBar() {
		setStyleName("sharky-inensitybar");
	}

	@Override
	public void setValue(Float newValue) {
		super.setValue(1 - newValue);
	}

}
