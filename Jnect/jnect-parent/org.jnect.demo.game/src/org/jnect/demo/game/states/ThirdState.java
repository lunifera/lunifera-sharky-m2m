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
package org.jnect.demo.game.states;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.jnect.demo.game.GameHelper;
import org.jnect.gesture.Gesture;
import org.jnect.gesture.detectors.CrouchGestureDetector;


public class ThirdState implements GameState {

	@Override
	public Class<? extends Gesture> getRequiredGesture() {
		return CrouchGestureDetector.class;
	}

	@Override
	public void performAction() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				GameHelper
						.runCommand("org.eclipse.debug.ui.commands.StepInto");
			}
		});
	}

	@Override
	public void paintScreen(final Label label) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				label.setText("Crouch to Step Into");
			}
		});
	}

	@Override
	public String getRequiredSpeechString() {
		return null;
	}

	@Override
	public boolean isGestureEnabled() {
		return true;
	}

	@Override
	public boolean isSpeechEnabled() {
		return false;
	}

}
