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
import org.jnect.gesture.Gesture;
import org.jnect.gesture.util.MovingAverageCalculator;

/**
 * This is an example class for demonstrating a {@link Gesture}. This class
 * detects a crouch of the user. For doing so it checks whether the current
 * value of the head is lower then the average position minus a threshold.
 * 
 * @author Philip Achenbach
 * @author Eugen Neufeld
 * 
 */
public class CrouchGestureDetector extends Gesture {

	private static final int NUM_PERIODS = 15;
	private static final float THRESHOLD = 0.5f;

	private MovingAverageCalculator yMovingAvgHead;

	private boolean alreadyNotified = false;

	public CrouchGestureDetector() {
		this.yMovingAvgHead = new MovingAverageCalculator(NUM_PERIODS);
	}

	@Override
	public boolean isGestureDetected(Notification notification) {
		if (notification.getEventType() == Notification.SET
				&& notification.wasSet()) {
			EAttribute feature = (EAttribute) notification.getFeature();
			PositionedElement humanBodyPart = (PositionedElement) notification
					.getNotifier();

			if ("y".equals(feature.getName())
					&& Head.class.isInstance(humanBodyPart)) {
				float sensorValue = notification.getNewFloatValue();
				this.yMovingAvgHead.addValue(sensorValue);
				float avgHeadValue = this.yMovingAvgHead.getMovingAvg();
				
				if (sensorValue < avgHeadValue - THRESHOLD) {
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
