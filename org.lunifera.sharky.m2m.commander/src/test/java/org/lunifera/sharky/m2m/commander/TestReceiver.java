package org.lunifera.sharky.m2m.commander;

import java.net.URISyntaxException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class TestReceiver {

	private static final String TOPIC = "sharky_sensors";

	public static void main(String[] args) throws Throwable {
		new TestReceiver().shouldReceiveMqttClientAfterSendingOne();
	}

	public void shouldReceiveMqttClientAfterSendingOne() throws Throwable {
		MqttClient receiveClient = createClient();

		subscribe(receiveClient);
		Thread.sleep(100);
	}

	private MqttClient createClient() throws URISyntaxException, MqttException {
		MqttClient mqtt = new MqttClient("tcp://192.168.178.20:1883", MqttClient.generateClientId());
		mqtt.connect();
		return mqtt;
	}

	private void subscribe(MqttClient client) throws Exception {
		client.subscribe(TOPIC);
		client.setCallback(new MqttCallback() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				System.out.println(new String(message.getPayload()));
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
			}

			@Override
			public void connectionLost(Throwable ex) {
				ex.printStackTrace();
			}
		});
	}

}
