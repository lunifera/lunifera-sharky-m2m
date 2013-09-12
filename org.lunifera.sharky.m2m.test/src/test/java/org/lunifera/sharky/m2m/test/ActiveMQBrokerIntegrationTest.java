package org.lunifera.sharky.m2m.test;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
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
	private Exception error;

	@Test
	public void shouldReceiveMQTTAfterSendingOne() throws Exception {
		// see https://github.com/fusesource/mqtt-client/tree/mqtt-client-project-1.3
		final MQTT client = createClient();

		Thread senderThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					receive(client);
				} catch (Exception ex) {
					error = ex;
				}
			}
		});
		senderThread.start();

		Thread.sleep(1000);

		send(client);
		senderThread.join();

		assertEquals(PAYLOAD, answer);
		if (error != null) {
			throw error;
		}
	}

	private MQTT createClient() throws URISyntaxException {
		MQTT mqtt = new MQTT();
		mqtt.setHost("localhost", 1883);
		return mqtt;
	}

	private void receive(MQTT client) throws Exception {
		BlockingConnection readConnection = subscribe(client, TOPIC);
		try {
			Message message = readConnection.receive();
			byte[] payload = message.getPayload();
			answer = new String(payload);
			message.ack();
		} finally {
			readConnection.disconnect();
		}
	}

	private BlockingConnection subscribe(MQTT client, String topicName) throws Exception {
		BlockingConnection readConnection = client.blockingConnection();
		Topic[] topics = { new Topic(topicName, QoS.AT_LEAST_ONCE) };
		readConnection.subscribe(topics);
		return readConnection;
	}

	private void send(MQTT client) throws Exception {
		BlockingConnection publishConnection = client.blockingConnection();
		publishConnection.connect();
		try {
			publish(publishConnection, TOPIC);
		} finally {
			publishConnection.disconnect();
		}
	}

	private void publish(BlockingConnection publishConnection, String topicName) throws Exception {
		publishConnection.publish(topicName, PAYLOAD.getBytes(), QoS.AT_LEAST_ONCE, false);
	}

}
