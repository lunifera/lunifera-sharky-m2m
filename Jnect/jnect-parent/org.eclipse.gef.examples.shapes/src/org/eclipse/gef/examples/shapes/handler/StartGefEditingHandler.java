package org.eclipse.gef.examples.shapes.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.gef.examples.shapes.helper.GefEditingHelper;
import org.eclipse.gef.examples.shapes.listener.GefEditingGestureListener;
import org.jnect.core.KinectManager;
import org.jnect.gesture.GestureProxy;
import org.jnect.gesture.detectors.RightHandAboveHeadGestureDetector;

public class StartGefEditingHandler extends AbstractHandler {

	private static boolean addedGestures = false;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (!addedGestures) {
			GestureProxy.INSTANCE.addGestureListener(new GefEditingGestureListener());
			GestureProxy.INSTANCE.addGestureDetector(new RightHandAboveHeadGestureDetector());
			addedGestures = true;
		}
		KinectManager.INSTANCE.startSkeletonTracking();
		GefEditingHelper.INSTANCE.startGefEditing();

		return null;
	}
}
