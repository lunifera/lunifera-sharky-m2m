package org.lunifera.sharky.m2m.commander;

import java.net.URISyntaxException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.lunifera.sharky.m2m.commander.api.ISharkyController;

public class Controller implements ISharkyController {

	private static final String M2M_SERVER_URL = "tcp://127.0.0.1:1883";
	private static final String PITCH = "pitch:%d";
	private static final String ROTATION = "rotation:%d";
	private static final String SPEED = "speed:%d";
	private static final String STOP = "stop";
	private MqttClient mqtt;

	public Controller() {
		try {
			createClient();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String pitch(int value) {
		String command = String.format(PITCH, value);
		try {
			send(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return command;
	}

	@Override
	public String rotation(int value) {
		String command = String.format(ROTATION, value);
		try {
			send(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return command;
	}

	@Override
	public String speed(int value) {
		String command = String.format(SPEED, value);
		try {
			send(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return command;
	}

	@Override
	public String stop() {
		String command = STOP;
		try {
			send(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return command;
	}

	private MqttClient createClient() throws URISyntaxException, MqttException {
		mqtt = new MqttClient(M2M_SERVER_URL, MqttClient.generateClientId());
		mqtt.connect();
		return mqtt;
	}

	private void send(String command) throws Exception {
		mqtt.publish(TOPIC, command.getBytes(), 0, false);
	}

}
