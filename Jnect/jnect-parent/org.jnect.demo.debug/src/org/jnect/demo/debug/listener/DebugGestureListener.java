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
package org.jnect.demo.debug.listener;

import org.jnect.demo.debug.DebugHelper;
import org.jnect.gesture.Gesture;
import org.jnect.gesture.GestureListener;
import org.jnect.gesture.detectors.CrouchGestureDetector;
import org.jnect.gesture.detectors.JumpGestureDetector;
import org.jnect.gesture.detectors.RightHandAboveHeadGestureDetector;

public class DebugGestureListener extends GestureListener {

	@Override
	public void notifyGestureDetected(Class<? extends Gesture> gesture) {
		System.out.println("Gesture recognized: " + gesture.getSimpleName());
		
		if (RightHandAboveHeadGestureDetector.class.equals(gesture)) {
			DebugHelper.stepOver();
		} else if(CrouchGestureDetector.class.equals(gesture)) {
			DebugHelper.stepInto();
		}else if(JumpGestureDetector.class.equals(gesture)){
			DebugHelper.resume();
		}
		
	}
}
