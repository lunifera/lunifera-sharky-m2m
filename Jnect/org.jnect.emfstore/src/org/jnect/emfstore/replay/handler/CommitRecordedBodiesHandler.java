package org.jnect.emfstore.replay.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.jnect.emfstore.EMFStorage;

public class CommitRecordedBodiesHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		EMFStorage.getInstance().commit();
		return null;
	}

}
