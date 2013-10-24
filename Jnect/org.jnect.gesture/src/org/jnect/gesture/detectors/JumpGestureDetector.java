/*******************************************************************************
 * Copyright (c) 2012 jnect.org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Eugen Neufeld - initial API and implementation
 *******************************************************************************/
package org.jnect.gesture.detectors;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EAttribute;
import org.jnect.bodymodel.Head;
import org.jnect.bodymodel.LeftHand;
import org.jnect.bodymodel.PositionedElement;
import org.jnect.gesture.Gesture;
import org.jnect.gesture.util.MovingAverageCalculator;

/**
 * This is an example class for demonstrating a {@link Gesture}. This class
 * detects a jump of the user. For doing so it checks whether the current value
 * of both feet and the head is higher then the average position plus a
 * threshold.
 * 
 * @author Philip Achenbach
 * @author Eugen Neufeld
 * 
 */
public class JumpGestureDetector extends Gesture {

	private static final int NUM_PERIODS = 10;
	private static final float THRESHOLD_HEAD = 40.0f;
	private static final float THRESHOLD_FOOT = 0.1f;

	private MovingAverageCalculator yMovingAvgLeftHand;

	private boolean gestureHead = false;

	private boolean alreadyNotified = false;

	public JumpGestureDetector() {
		this.yMovingAvgLeftHand = new MovingAverageCalculator(NUM_PERIODS);
	}

	@Override
	public boolean isGestureDetected(Notification notification) {
		if (notification.getEventType() == Notification.SET && notification.wasSet()) {
			EAttribute feature = (EAttribute) notification.getFeature();
			PositionedElement humanBodyPart = (PositionedElement) notification.getNotifier();

			if ("y".equals(feature.getName())) {
				float sensorValue = notification.getNewFloatValue();

				if (LeftHand.class.isInstance(humanBodyPart)) {
					this.yMovingAvgLeftHand.addValue(sensorValue);
					float avgHeadValue = this.yMovingAvgLeftHand.getMovingAvg();
					gestureHead = (sensorValue > avgHeadValue +  10.0f);
				}

				if (gestureHead) {
					if (!this.alreadyNotified) {
						this.alreadyNotified = true;
						return true;
					}
				} else {
					this.alreadyNotified = false;
				}
			}
		}
		return false;
	}
}
