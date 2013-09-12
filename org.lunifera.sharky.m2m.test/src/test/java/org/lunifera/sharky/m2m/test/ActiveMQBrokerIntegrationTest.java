package org.lunifera.sharky.m2m.test;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
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
	private Throwable error;

	@Test
	public void shouldReceiveMQTTAfterSendingOne() throws Throwable {
		// see https://github.com/fusesource/mqtt-client/tree/mqtt-client-project-1.3
		MQTT publishClient = createClient();
		MQTT receiveClient = createClient();

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

	private MQTT createClient() throws URISyntaxException {
		MQTT mqtt = new MQTT();
		mqtt.setHost("127.0.0.1", 1883);
		return mqtt;
	}

	private void subscribe(MQTT client) throws Exception {
		final CallbackConnection readConnection = client.callbackConnection();
		readConnection.listener(new Listener() {
			public void onConnected() {
			}

			public void onPublish(UTF8Buffer topic, Buffer payload, Runnable ack) {
				setResponse(new String(payload.data, payload.offset, payload.length));
				ack.run();
			}

			public void onDisconnected() {
			}

			public void onFailure(Throwable value) {
				setError(value);
			}
		});

		readConnection.connect(new Callback<Void>() {

			// Once we connect..
			public void onSuccess(Void v) {

				// Subscribe to a topic
				Topic[] topics = { new Topic(TOPIC, QoS.AT_LEAST_ONCE) };
				readConnection.subscribe(topics, new Callback<byte[]>() {
					public void onSuccess(byte[] qoses) {
					}

					public void onFailure(Throwable value) {
						setError(value);
						readConnection.disconnect(null); // subscribe failed.
					}
				});
			}

			public void onFailure(Throwable value) {
				setError(value);
			}
		});
	}

	private synchronized void setResponse(String value) {
		answer = value;
	}

	private synchronized void setError(Throwable value) {
		error = value;
	}

	private void send(MQTT client) throws Exception {
		final CallbackConnection publishConnection = client.callbackConnection();
		publishConnection.connect(new Callback<Void>() {
			@Override
			public void onSuccess(Void value) {
			}

			@Override
			public void onFailure(Throwable value) {
				setError(value);
			}
		});

		publishConnection.publish(TOPIC, PAYLOAD.getBytes(), QoS.AT_LEAST_ONCE, false, new Callback<Void>() {
			public void onSuccess(Void v) {
				// the pubish operation completed successfully.
			}

			public void onFailure(Throwable value) {
				setError(value);
				publishConnection.disconnect(null); // publish failed.
			}
		});
	}

}
