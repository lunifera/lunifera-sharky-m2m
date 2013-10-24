package org.eclipse.gef.examples.shapes.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.gef.examples.shapes.helper.GefEditingHelper;
import org.jnect.core.KinectManager;

public class StopGefEditingHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		GefEditingHelper.INSTANCE.stopGefEditing();
		KinectManager.INSTANCE.stopSkeletonTracking();
		return null;
	}

}
