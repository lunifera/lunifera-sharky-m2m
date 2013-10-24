package org.jnect.demo.m2m.gesture;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EAttribute;
import org.jnect.bodymodel.PositionedElement;
import org.jnect.gesture.Gesture;

public abstract class SharkyGesture extends Gesture {

	private float initial = Constants.INITIAL;
	private boolean gesture = false;
	private boolean alreadyNotified = false;

	public SharkyGesture() {
		super();
	}

	@Override
	public boolean isGestureDetected(Notification notification) {
		if (notification.getEventType() == Notification.SET
				&& notification.wasSet()) {
			EAttribute feature = (EAttribute) notification.getFeature();
			PositionedElement humanBodyPart = (PositionedElement) notification
					.getNotifier();

			if (getFeature().equals(feature.getName())) {
				float sensorValue = notification.getNewFloatValue();

				if (getJointType().isInstance(humanBodyPart)) {
					if (initial == Constants.INITIAL) {
						initial = sensorValue;
					}

					gesture = (initial + getMovementFactorLowerBound()
							* Constants.MOVEMENT_STEP) <= sensorValue
							&& (initial + getMovementFactorUpperBound()
									* Constants.MOVEMENT_STEP) > sensorValue;
					if (!gesture) {
						this.alreadyNotified = false;
					}
				}

				if (gesture) {
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

	protected abstract int getMovementFactorLowerBound();

	protected abstract int getMovementFactorUpperBound();

	protected abstract Class<? extends PositionedElement> getJointType();

	protected abstract String getFeature();

}