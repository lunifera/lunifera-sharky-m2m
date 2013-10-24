/*******************************************************************************
 * Copyright (c) 2012 jnect.org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugen Neufeld - initial API and implementation
 *******************************************************************************/
package org.jnect.core.impl.connection.m2m;

import java.io.IOException;

import org.jnect.core.impl.ConnectionDataHandler;
import org.jnect.core.impl.ConnectionManager;
import org.jnect.core.impl.KinectDataHandler;
import org.jnect.core.m2m.M2MProtocolConstants;


public class M2MConnectionManager extends ConnectionDataHandler implements ConnectionManager {

	// The socket connection is not yet fully implemented!
	
	
	private M2MProcessor socketProcessor;
	private String host;
	
	public M2MConnectionManager() {
		this.host = M2MProtocolConstants.DEFAULT_HOST;
	}
	
	public M2MConnectionManager(String host, int port) {
		this.host = host;
	}
	
	@Override
	public void openConnection() throws IOException {
		this.socketProcessor = new M2MProcessor(this.host);
		this.socketProcessor.setConnectionDataHandler(this);
		this.socketProcessor.run();
	}

	@Override
	public void closeConnection() throws IOException {
		this.socketProcessor.stop();
	}

	@Override
	public void startSkeletonTracking() {
		socketProcessor.startSkeletonTracking();
	}

	@Override
	public void stopSkeletonTracking() {
		socketProcessor.stopSkeletonTracking();
	}

	@Override
	public void startSpeechRecognition(String[] keywords) {
		socketProcessor.startSpeechRecognition();
	}

	@Override
	public void stopSpeechRecognition() {
		socketProcessor.stopSpeechRecognition();
	}

	@Override
	public void setDataHandler(KinectDataHandler dataHandler) {
		this.dataHandler = dataHandler;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public boolean isSkeletonTrackingStarted() {
		return false;
	}

	@Override
	public boolean isSpeechRecognitionStarted() {
		return false;
	}

	@Override
	public void stopKinect() {
		
	}

}
