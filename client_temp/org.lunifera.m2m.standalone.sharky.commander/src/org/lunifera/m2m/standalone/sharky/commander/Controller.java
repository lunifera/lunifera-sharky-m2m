package org.lunifera.m2m.standalone.sharky.commander;

import java.net.URISyntaxException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.lunifera.m2m.standalone.sharky.commander.api.ISharkyController;
import org.osgi.service.component.ComponentContext;

public class Controller implements ISharkyController {

	private static final String M2M_SERVER_URL = "tcp://192.168.178.28:1883";
	private static final String PITCH = "pitch:%d";
	private static final String ROTATION = "rotation:%d";
	private static final String SPEED = "speed:%d";
	private static final String STOP = "stop:0";
	private MqttClient mqtt;

	private int pitch;
	private int rotation;
	private int speed;

	public Controller() {
		try {
			createClient();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	protected void activate(ComponentContext context) {
		System.out.println("Sharky Controller started");
	}

	@Override
	public int pitch(int value, CommandRecorder recorder) {
		if (value > 0 && pitch == 5 || value < 0 && pitch == -5) {
			recorder.record(COMMANDS_TOPIC, String.format(PITCH, pitch), false);
			return pitch;
		}

		pitch += value;

		try {
			sendCommand(String.format(PITCH, pitch), recorder);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return pitch;
	}

	@Override
	public int rotation(int value, CommandRecorder recorder) {

		// for gliding different handling is required
		if (speed != 0) {
			if (value > 0 && rotation == 5 || value < 0 && rotation == -5) {
				recorder.record(COMMANDS_TOPIC, String.format(ROTATION, value),
						false);
				return rotation;
			}

			rotation += value;
		}

		try {
			sendCommand(String.format(ROTATION, value), recorder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rotation;
	}

	@Override
	public int speed(int value, CommandRecorder recorder) {
		speed = value;

		if (speed == 0) {
			rotation = 0;
		}

		try {
			sendCommand(String.format(SPEED, value), recorder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return speed;
	}

	@Override
	public void stop(CommandRecorder recorder) {
		pitch = 0;
		rotation = 0;
		speed = 0;

		try {
			sendCommand(STOP, recorder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sharkAlarmDistance(Integer distance, CommandRecorder recorder) {
		try {
			sendAlarmDistance(distance, recorder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private MqttClient createClient() throws URISyntaxException, MqttException {
		mqtt = new MqttClient(M2M_SERVER_URL, MqttClient.generateClientId());
		mqtt.connect();

		mqtt.subscribe(SENSORS_TOPIC);
		mqtt.setCallback(new MqttCallback() {

			@Override
			public void messageArrived(String topic, MqttMessage message)
					throws Exception {
				System.out.println(new String(message.getPayload()));
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken arg0) {

			}

			@Override
			public void connectionLost(Throwable arg0) {

			}
		});
		return mqtt;
	}

	private void sendCommand(String command, CommandRecorder recorder)
			throws Exception {
		sendCommand(COMMANDS_TOPIC, command, false, recorder);
	}

	private void sendAlarmDistance(int value, CommandRecorder recorder)
			throws Exception {
		sendCommand(ALARM_TOPIC, String.format("distance:%d", value), true,
				recorder);
	}

	@Override
	public void sendCommand(String topic, String command, boolean retain,
			CommandRecorder recorder) throws Exception {
		recorder.record(topic, command, retain);
		mqtt.publish(topic, command.getBytes(), 0, retain);
	}

}
