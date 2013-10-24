package org.jnect.emfstore;

import org.jnect.bodymodel.Body;
import org.jnect.emfstore.replay.IReplayBodyProvider;

public class ReplayBodyProvider implements IReplayBodyProvider {

	@Override
	public Body getReplayBody() {
		return EMFStorage.getInstance().getReplayingBody();
	}

	@Override
	public void replay(int from) {
		EMFStorage.getInstance().initReplay();
		EMFStorage.getInstance().replay(from);
	}

	@Override
	public void setReplayToState(int state) throws IndexOutOfBoundsException {
		EMFStorage.getInstance().initReplay();
		EMFStorage.getInstance().setReplayToState(state);
	}

	@Override
	public int getReplayStatesCount() {
		EMFStorage.getInstance().initReplay();
		return EMFStorage.getInstance().getReplayStatesCount();
	}

	@Override
	public void stopReplay() {
		EMFStorage.getInstance().stopReplay();
		KinectBodyPresentationManager.showRecordingBody();
	}

}
