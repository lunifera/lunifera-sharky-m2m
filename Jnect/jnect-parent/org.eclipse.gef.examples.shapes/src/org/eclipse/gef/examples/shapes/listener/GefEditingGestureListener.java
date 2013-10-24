package org.eclipse.gef.examples.shapes.listener;

import org.eclipse.gef.examples.shapes.helper.GefEditingHelper;
import org.jnect.gesture.Gesture;
import org.jnect.gesture.GestureListener;
import org.jnect.gesture.detectors.RightHandAboveHeadGestureDetector;

public class GefEditingGestureListener extends GestureListener {

	@Override
	public void notifyGestureDetected(Class<? extends Gesture> gesture) {
		if (RightHandAboveHeadGestureDetector.class.equals(gesture)) {
			GefEditingHelper.INSTANCE.switchGefEditingMode();
		}
	}

}
