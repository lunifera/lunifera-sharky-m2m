/*******************************************************************************
 * Copyright (c) 2012 jnect.org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugen Neufeld - initial API and implementation
 *******************************************************************************/
package org.jnect.demo.game.timer;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * The Game Timer
 * 
 * @author Stephan K�hler
 * 
 */
public class GameTimer {

	public static final String ID = "gameclipse.timer.GameTimer";

	private Runnable timer;

	private final int invocationTime = 1000;

	private int startTime = 0;

	private Display display = null;

	private Label label = null;

	public GameTimer(final Display display, final Label label) {
		this.label = label;
		this.display = display;
		timer = new Runnable() {
			public void run() {
				if (label.isDisposed())
					return;
				label.setText("" + startTime);
				startTime++;
				display.timerExec(invocationTime, this);
			}
		};
	}

	public void startTimer() {
		startTime = 0;
		display.timerExec(invocationTime, timer);
	}

	public void stopTimer() {
		display.timerExec(-1, timer);
	}

	public void reset() {
		startTime = 0;
		label.setText("" + startTime);
	}

	public int getTime() {
		return startTime;
	}

}
