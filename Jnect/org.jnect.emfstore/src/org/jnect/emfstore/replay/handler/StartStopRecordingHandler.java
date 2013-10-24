package org.jnect.emfstore.replay.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.jnect.core.KinectManager;
import org.jnect.emfstore.EMFStorage;
import org.jnect.emfstore.KinectBodyPresentationManager;

public class StartStopRecordingHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		boolean wasRecording = EMFStorage.getInstance().isRecording();
		EMFStorage.getInstance().startStopRecording(!wasRecording);
		if (!wasRecording && !KinectManager.INSTANCE.isSkeletonTrackingStarted()) {
			// show the tracked skeleton if record start is requested and not yet tracked
			KinectManager.INSTANCE.startSkeletonTracking();
		}
		KinectBodyPresentationManager.showRecordingBody();
		return null;
	}

}
