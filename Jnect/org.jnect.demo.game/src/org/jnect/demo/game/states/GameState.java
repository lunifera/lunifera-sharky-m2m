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

import org.eclipse.swt.widgets.Label;
import org.jnect.gesture.Gesture;


public interface GameState {

	public Class<? extends Gesture> getRequiredGesture();

	public String getRequiredSpeechString();

	public void performAction();

	public void paintScreen(Label label);

	public boolean isGestureEnabled();

	public boolean isSpeechEnabled();
}
