package org.lunifera.sharky.m2m.commander;

import java.net.URISyntaxException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class TestSender {

	private static final String TOPIC = "sharky_sensors";
	private static final String PAYLOAD = "speed:%d";

	public void shouldReceiveMqttClientAfterSendingOne() throws Throwable {
		MqttClient publishClient = createClient();
		send(publishClient);
	}

	private MqttClient createClient() throws URISyntaxException, MqttException {
		MqttClient mqtt = new MqttClient("tcp://192.168.178.20:1883", MqttClient.generateClientId());
		mqtt.connect();
		return mqtt;
	}

	private void send(MqttClient client) throws Exception {
		for (int i = 0; i < 100; i++) {
			client.publish(TOPIC, String.format(PAYLOAD, i).getBytes(), 0, true);
		}
	}

	public static void main(String[] args) throws Exception {
		TestSender main = new TestSender();
		MqttClient publishClient = main.createClient();
		main.send(publishClient);

	}
}
