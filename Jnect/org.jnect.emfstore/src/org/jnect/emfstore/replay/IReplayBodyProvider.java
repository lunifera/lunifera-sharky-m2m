package org.jnect.emfstore.replay;

import org.jnect.bodymodel.Body;

public interface IReplayBodyProvider {
	public Body getReplayBody();

	public void replay(int from);

	public void setReplayToState(int state) throws IndexOutOfBoundsException;

	/**
	 * @return The number of distinct body states this replay provider offers.
	 */
	public int getReplayStatesCount();

	/**
	 * Stops the replay if it is in progress.
	 */
	public void stopReplay();

}
