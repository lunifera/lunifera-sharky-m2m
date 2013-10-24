package org.jnect.demo.m2m.gesture;

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
public class SharkyRHUp2Detector extends SharkyGesture {

	public SharkyRHUp2Detector() {
	}

	protected String getFeature() {
		return "y";
	}

	protected Class<? extends PositionedElement> getJointType() {
		return RightHand.class;
	}

	protected int getMovementFactorLowerBound() {
		return -3;
	}

	protected int getMovementFactorUpperBound() {
		return -2;
	}
}
