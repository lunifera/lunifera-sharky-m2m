package org.jnect.demo.m2m.gesture;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EAttribute;
import org.jnect.bodymodel.PositionedElement;
import org.jnect.bodymodel.RightHand;
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
public class SharkyRHRight5Detector extends SharkyGesture {

	protected String getFeature() {
		return "x";
	}

	protected Class<? extends PositionedElement> getJointType() {
		return RightHand.class;
	}

	protected int getMovementFactorLowerBound() {
		return 5;
	}

	protected int getMovementFactorUpperBound() {
		return 10;
	}
}