package org.jnect.emfstore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jnect.bodymodel.Body;

public class MockBodyBuffer extends BodyBuffer {

	private List<float[]> loadedBody;

	public MockBodyBuffer(String pathToHDBuffer) {
		super();
		File buffFile = new File(pathToHDBuffer);
		if (!buffFile.exists()) {
			printNoBuffMessage();
			return;
		}

		loadedBody = new ArrayList<float[]>();
		BufferedReader floatArrayReader = null;
		try {
			floatArrayReader = new BufferedReader(new FileReader(buffFile));
			String line;
			while ((line = floatArrayReader.readLine()) != null) {
				String[] stringVals = line.split(" ");
				float[] vals = new float[stringVals.length];
				assert stringVals.length == 0 || stringVals.length == NEEDED_CHANGES; // 0 for empty lines
				for (int i = 0; i < stringVals.length; i++) {
					vals[i] = Float.valueOf(stringVals[i]);
				}
				if (vals.length == NEEDED_CHANGES)
					loadedBody.add(vals);
			}
		} catch (FileNotFoundException e) {
			printNoBuffMessage();
			e.printStackTrace();
		} catch (IOException e) {
			printNoBuffMessage();
			e.printStackTrace();
		} finally {
			try {
				if (floatArrayReader != null)
					floatArrayReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		buffer.addAll(loadedBody);
	}

	@Override
	public void flushToBody(Body flushBody, ICommitter committer, int commitResolution, IProgressMonitor monitor) {
		super.flushToBody(flushBody, committer, commitResolution, monitor);
		// for easier testing, the body buffer is never cleared
		buffer.addAll(loadedBody);

	}

	private void printNoBuffMessage() {
		System.out.println("Could not load the hard disk buffer.");
	}

}
