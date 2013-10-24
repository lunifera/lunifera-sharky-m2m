/*******************************************************************************
 * Copyright (c) 2012 jnect.org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Eugen Neufeld - initial API and implementation
 *******************************************************************************/
package org.jnect.core.impl.connection.m2m;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jnect.core.impl.ConnectionProcessor;
import org.jnect.core.m2m.M2MProtocolConstants;

public class M2MProcessor extends ConnectionProcessor implements MqttCallback {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private MqttClient client;
	private String host;

	public M2MProcessor(String host) {
		this.host = host;
	}

	@Override
	public void run() {
		try {
			this.client = new MqttClient(this.host, MqttClient.generateClientId());
			client.connect();
			this.client.setCallback(this);
		} catch (MqttException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	public void stop() {
		try {
			this.client.disconnect();
			this.client.close();
		} catch (MqttException ioe) {
			logger.log(Level.SEVERE, ioe.getLocalizedMessage(), ioe);
		}
		this.client = null;
	}

	private void processInput(String input) {
		if (input.startsWith(M2MProtocolConstants.SPEECH)) {
			String speech = input.substring(M2MProtocolConstants.SPEECH.length());
			this.connectionDataHandler.handleSpeechInput(speech);
		} else if (input.startsWith(M2MProtocolConstants.SKELETON)) {
			String xml = input.substring(M2MProtocolConstants.SKELETON.length());
			this.connectionDataHandler.handleSkeletonInput(xml);
		} else {
			System.out.println("Unrecognized: " + input);// throws IOException
		}
	}

	@Override
	public void connectionLost(Throwable e) {

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		processInput(new String(message.getPayload()));
	}

	void startSkeletonTracking() {
		try {
			this.client.subscribe(M2MProtocolConstants.SKELETON_TOPIC);
		} catch (MqttException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	void stopSkeletonTracking() {
		try {
			this.client.unsubscribe(M2MProtocolConstants.SKELETON_TOPIC);
		} catch (MqttException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	void startSpeechRecognition() {
		try {
			this.client.subscribe(M2MProtocolConstants.SPEECH_TOPIC);
		} catch (MqttException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	void stopSpeechRecognition() {
		try {
			this.client.unsubscribe(M2MProtocolConstants.SPEECH_TOPIC);
		} catch (MqttException e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
}
