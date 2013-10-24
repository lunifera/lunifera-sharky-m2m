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
import org.jnect.gesture.Gesture;


public class FinalState implements GameState {

	@Override
	public Class<? extends Gesture> getRequiredGesture() {
		return null;
	}

	@Override
	public void performAction() {

	}

	@Override
	public void paintScreen(final Label label) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				label.setText("Thanks for playing the game..");
			}
		});
	}

	@Override
	public String getRequiredSpeechString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isGestureEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSpeechEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

}