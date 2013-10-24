package org.jnect.emfstore;

import org.jnect.bodymodel.Body;
import org.jnect.core.IBodyProvider;

public class RecordingBodyProvider implements IBodyProvider {
	Body nonRecordingBody;

	@Override
	public Body getBody() {
		return EMFStorage.getInstance().getRecordingBody();
	}

	@Override
	public void startStopRecoring(boolean on) {
		EMFStorage.getInstance().startStopRecording(on);
	}

	@Override
	public boolean isRecording() {
		return EMFStorage.getInstance().isRecording();
	}

}
