package org.jnect.demo.m2m.gesture;

import org.jnect.bodymodel.LeftHand;
import org.jnect.bodymodel.PositionedElement;
import org.jnect.bodymodel.RightHand;
import org.jnect.gesture.Gesture;

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
public class SharkyLHDown4Detector extends SharkyGesture {

	public SharkyLHDown4Detector() {
	}

	protected String getFeature() {
		return "y";
	}

	protected Class<? extends PositionedElement> getJointType() {
		return LeftHand.class;
	}

	protected int getMovementFactorLowerBound() {
		return 4;
	}

	protected int getMovementFactorUpperBound() {
		return 5;
	}
}
