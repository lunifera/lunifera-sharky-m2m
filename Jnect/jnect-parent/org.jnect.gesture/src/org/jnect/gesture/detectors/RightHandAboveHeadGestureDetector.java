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
package org.jnect.gesture.detectors;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EAttribute;
import org.jnect.bodymodel.Head;
import org.jnect.bodymodel.PositionedElement;
import org.jnect.bodymodel.RightHand;
import org.jnect.gesture.Gesture;
import org.jnect.gesture.util.MovingAverageCalculator;

/**
 * This is an example class for demonstrating a {@link Gesture}. This class
 * detects when the right hand is above the head of the user. For doing so it
 * compares the average position of the right hand with the average position of
 * the head.
 * 
 * @author Eugen Neufeld
 * 
 */
public class RightHandAboveHeadGestureDetector extends Gesture {

	private static final int NUM_PERIODS = 10;

	private MovingAverageCalculator yMovingAvgHead;
	private MovingAverageCalculator yMovingAvgRightHand;

	private boolean alreadyNotified = false;

	public RightHandAboveHeadGestureDetector() {
		this.yMovingAvgHead = new MovingAverageCalculator(NUM_PERIODS);
		this.yMovingAvgRightHand = new MovingAverageCalculator(NUM_PERIODS);
	}

	@Override
	public boolean isGestureDetected(Notification notification) {
		if (notification.getEventType() == Notification.SET
				&& notification.wasSet()) {
			EAttribute feature = (EAttribute) notification.getFeature();
			PositionedElement humanBodyPart = (PositionedElement) notification
					.getNotifier();

			if ("y".equals(feature.getName())) {
				float sensorValue = notification.getNewFloatValue();

				if (Head.class.isInstance(humanBodyPart)) {
					this.yMovingAvgHead.addValue(sensorValue);
				} else if (RightHand.class.isInstance(humanBodyPart)) {
					this.yMovingAvgRightHand.addValue(sensorValue);
				}

				if (yMovingAvgRightHand.getMovingAvg() > yMovingAvgHead
						.getMovingAvg()) {
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
