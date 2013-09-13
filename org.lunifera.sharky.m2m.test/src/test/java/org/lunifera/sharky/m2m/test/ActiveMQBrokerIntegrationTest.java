package org.lunifera.sharky.m2m.test;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ActiveMQBrokerIntegrationTest {

	private static final String TOPIC = "foo";
	private static final String PAYLOAD = "Hello";

	private String answer;
	private Throwable error;

	@Test
	public void shouldReceiveMqttClientAfterSendingOne() throws Throwable {
		MqttClient publishClient = createClient();
		MqttClient receiveClient = createClient();

		subscribe(receiveClient);
		Thread.sleep(100);

		send(publishClient);
		Thread.sleep(500);

		assertResult();
	}

	private synchronized void assertResult() throws Throwable {
		assertEquals(PAYLOAD, answer);
		if (error != null) {
			throw error;
		}
	}

	private MqttClient createClient() throws URISyntaxException, MqttException {
		MqttClient mqtt = new MqttClient("tcp://127.0.0.1:1883", MqttClient.generateClientId());
		mqtt.connect();
		return mqtt;
	}

	private void subscribe(MqttClient client) throws Exception {
		client.subscribe(TOPIC);
		client.setCallback(new MqttCallback() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				setResponse(new String(message.getPayload()));
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
			}

			@Override
			public void connectionLost(Throwable ex) {
				setError(ex);
			}
		});
	}

	private synchronized void setResponse(String value) {
		answer = value;
	}

	private synchronized void setError(Throwable value) {
		error = value;
	}

	private void send(MqttClient client) throws Exception {
		client.publish(TOPIC, PAYLOAD.getBytes(), 0, false);
	}
}
