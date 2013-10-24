package org.jnect.emfstore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.jnect.bodymodel.Body;
import org.jnect.bodymodel.PositionedElement;

public class BodyBuffer {
	public enum Coordinate {
		X, Y, Z

	}

	Body body;
	final int NEEDED_CHANGES;
	List<float[]> buffer = Collections.synchronizedList(new LinkedList<float[]>());

	public BodyBuffer() {
		this.body = EMFStorage.createAndFillBody();
		body.eAdapters().add(new CommitBodyChangesAdapter());
		// 3 changes (x, y, z) in every body element
		NEEDED_CHANGES = body.eContents().size() * 3;
	}

	public Body getBufferBody() {
		return body;
	}

	private class CommitBodyChangesAdapter extends EContentAdapter {
		private int currChanges = 0;

		@Override
		public void notifyChanged(Notification notification) {
			if (++currChanges % NEEDED_CHANGES == 0) {
				nextBody();
			}

		}
	}

	void nextBody() {
		float[] state = new float[NEEDED_CHANGES];
		assert NEEDED_CHANGES / 3 == body.eContents().size();
		for (int i = 0; i < NEEDED_CHANGES / 3; i++) {
			EObject elem = body.eContents().get(i);
			if (!(elem instanceof PositionedElement))
				continue;
			PositionedElement pos = (PositionedElement) elem;
			state[i * 3] = pos.getX();
			state[i * 3 + 1] = pos.getY();
			state[i * 3 + 2] = pos.getZ();
		}
		buffer.add(state);
	}

	public void flushToBody(Body flushBody, ICommitter committer, int commitResolution, IProgressMonitor monitor) {
		final int BODY_PART_COUNT = NEEDED_CHANGES / 3;
		assert flushBody.eContents().size() == BODY_PART_COUNT;
		int commitCount = buffer.size() / commitResolution;
		int roundUp = buffer.size() % commitResolution == 0 ? 0 : 1;
		commitCount += roundUp;
		monitor.beginTask("Saving to EMFStore Server", buffer.size() + commitCount);
		EList<EObject> bodyContents = flushBody.eContents();

		long timeBefore = Calendar.getInstance().getTimeInMillis();
		synchronized (buffer) {
			org.eclipse.emf.emfstore.client.model.Configuration.setAutoSave(false);
			Iterator<float[]> bufferIt = buffer.iterator();
			int collectedBodyChanges = 0;
			while (bufferIt.hasNext() && !monitor.isCanceled()) {
				monitor.subTask("Writing to EMFStore...");
				float[] values = bufferIt.next();
				for (int i = 0; i < BODY_PART_COUNT/* - 1 */; i++) {
					EObject elem = bodyContents.get(i);
					if (!(elem instanceof PositionedElement))
						continue;
					PositionedElement pos = (PositionedElement) elem;
					setAndForceModification(pos, Coordinate.X, values[i * 3]);
					setAndForceModification(pos, Coordinate.Y, values[i * 3 + 1]);
					setAndForceModification(pos, Coordinate.Z, values[i * 3 + 2]);
				}
				collectedBodyChanges++;
				if (collectedBodyChanges == commitResolution) {
					monitor.subTask("Committing...");
					committer.commit();
					collectedBodyChanges = 0;
					monitor.worked(1);
				}
				monitor.worked(1);
			}
			if (collectedBodyChanges != 0) {
				assert roundUp == 1 : "Only when the number of changes is not dividable by the commit resolution there should be changes left...";
				committer.commit();
				monitor.worked(1);
			}
			buffer.clear();
			org.eclipse.emf.emfstore.client.model.Configuration.setAutoSave(true);
		}
		long timeAfter = Calendar.getInstance().getTimeInMillis();
		System.out.println("Saving took: " + (timeAfter - timeBefore) / 1000 + " seconds.");

	}

	/**
	 * Sets the positioned element to a new value, and adds a dummy change epsilon if the value did not change, so that
	 * all change packages are fully filled.
	 * 
	 * @param pos
	 * @param coord
	 * @param newValue
	 */
	private void setAndForceModification(PositionedElement pos, Coordinate coord, float newValue) {
		float oldVal;
		// change the smallest amount possible
		// Note: Math.nextAfter() is Java 1.6
		// Use Float.floatToIntBits(arg0), increment it by one and reassign it if java 1.5 should be a requirement in
		// the future

		switch (coord) {
		case X:
			oldVal = pos.getX();
			if (oldVal == newValue)
				newValue = Math.nextAfter(newValue, newValue + 1);
			pos.setX(newValue);
			break;
		case Y:
			oldVal = pos.getY();
			if (oldVal == newValue)
				newValue = Math.nextAfter(newValue, newValue + 1);
			pos.setY(newValue);
			break;
		case Z:
			oldVal = pos.getZ();
			if (oldVal == newValue)
				newValue = Math.nextAfter(newValue, newValue + 1);
			pos.setZ(newValue);
			break;
		default:
			assert false;
			oldVal = Float.NaN; // disable compiler not-init warning below
		}
		assert oldVal != newValue;

	}

	public void storeToFile(String string) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(string)));
			StringBuilder currLine = new StringBuilder();
			for (float[] vals : buffer) {

				for (float value : vals) {
					currLine.append(value + " ");
				}
				writer.write(currLine.substring(0, currLine.length() - 1) + "\n");
				currLine.setLength(0);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
