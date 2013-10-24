package org.jnect.demo.m2m;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.jnect.core.KinectManager;
import org.jnect.core.impl.connection.m2m.M2MManagerImpl;
import org.jnect.core.m2m.M2MProtocolConstants;
import org.jnect.demo.m2m.gesture.SharkyLHUp1Detector;
import org.jnect.demo.m2m.gesture.SharkyLHUp2Detector;
import org.jnect.demo.m2m.gesture.SharkyLHUp3Detector;
import org.jnect.demo.m2m.gesture.SharkyLHUp4Detector;
import org.jnect.demo.m2m.gesture.SharkyLHUp5Detector;
import org.jnect.demo.m2m.gesture.SharkyLHUp0Detector;
import org.jnect.demo.m2m.gesture.SharkyLHDown1Detector;
import org.jnect.demo.m2m.gesture.SharkyLHDown2Detector;
import org.jnect.demo.m2m.gesture.SharkyLHDown3Detector;
import org.jnect.demo.m2m.gesture.SharkyLHDown4Detector;
import org.jnect.demo.m2m.gesture.SharkyLHDown5Detector;
import org.jnect.demo.m2m.gesture.SharkyRHUp1Detector;
import org.jnect.demo.m2m.gesture.SharkyRHUp2Detector;
import org.jnect.demo.m2m.gesture.SharkyRHUp3Detector;
import org.jnect.demo.m2m.gesture.SharkyRHUp4Detector;
import org.jnect.demo.m2m.gesture.SharkyRHUp5Detector;
import org.jnect.demo.m2m.gesture.SharkyRHLeft1Detector;
import org.jnect.demo.m2m.gesture.SharkyRHLeft2Detector;
import org.jnect.demo.m2m.gesture.SharkyRHLeft3Detector;
import org.jnect.demo.m2m.gesture.SharkyRHLeft4Detector;
import org.jnect.demo.m2m.gesture.SharkyRHLeft5Detector;
import org.jnect.demo.m2m.gesture.SharkyRHRight0Detector;
import org.jnect.demo.m2m.gesture.SharkyRHRight1Detector;
import org.jnect.demo.m2m.gesture.SharkyRHRight2Detector;
import org.jnect.demo.m2m.gesture.SharkyRHRight3Detector;
import org.jnect.demo.m2m.gesture.SharkyRHRight4Detector;
import org.jnect.demo.m2m.gesture.SharkyRHRight5Detector;
import org.jnect.demo.m2m.gesture.SharkyRHUp0Detector;
import org.jnect.demo.m2m.gesture.SharkyRHDown1Detector;
import org.jnect.demo.m2m.gesture.SharkyRHDown2Detector;
import org.jnect.demo.m2m.gesture.SharkyRHDown3Detector;
import org.jnect.demo.m2m.gesture.SharkyRHDown4Detector;
import org.jnect.demo.m2m.gesture.SharkyRHDown5Detector;
import org.jnect.gesture.Gesture;
import org.jnect.gesture.GestureListener;
import org.jnect.gesture.GestureProxy;
import org.jnect.gesture.M2MGestureProxy;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends GestureListener implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jnect.demo.m2m"; //$NON-NLS-1$
	private static final String TOPIC = "gesture";
	private static final String COMMAND_TOPIC = "sharky_commands";

	// The shared instance
	private static Activator plugin;

	private KinectManager manager;
	private MqttClient client;

	private Set<Gesture> gestures = new HashSet<Gesture>();
	private int i;

	/**
	 * The constructor
	 */
	public Activator() {
		// gestures.add(new LHLeftDetector());
		// gestures.add(new LHRightDetector());
		// gestures.add(new LHUpDetector());
		// gestures.add(new LHDownDetector());
		// gestures.add(new LHToSensorDetector());
		// gestures.add(new LHAwayFromSensorDetector());
		//
		// gestures.add(new RHLeftDetector());
		// gestures.add(new RHRightDetector());
		// gestures.add(new RHUpDetector());
		// gestures.add(new RHDownDetector());
		// gestures.add(new RHToSensorDetector());
		// gestures.add(new RHAwayFromSensorDetector());
		// gestures.add(new SharkyLHUpDetector());
		// gestures.add(new SharkyLHDownDetector());
		// gestures.add(new SharkyLHLeftDetector());
		// gestures.add(new SharkyRHUpDetector());
		// gestures.add(new SharkyRHDownDetector());
		// gestures.add(new SharkyRHLeftDetector());
		gestures.add(new SharkyRHRight1Detector());
		gestures.add(new SharkyRHRight2Detector());
		gestures.add(new SharkyRHRight3Detector());
		gestures.add(new SharkyRHRight4Detector());
		gestures.add(new SharkyRHRight5Detector());
		gestures.add(new SharkyRHRight0Detector());
		gestures.add(new SharkyRHLeft1Detector());
		gestures.add(new SharkyRHLeft2Detector());
		gestures.add(new SharkyRHLeft3Detector());
		gestures.add(new SharkyRHLeft4Detector());
		gestures.add(new SharkyRHLeft5Detector());
		gestures.add(new SharkyRHUp1Detector());
		gestures.add(new SharkyRHUp2Detector());
		gestures.add(new SharkyRHUp3Detector());
		gestures.add(new SharkyRHUp4Detector());
		gestures.add(new SharkyRHUp5Detector());
		gestures.add(new SharkyRHUp0Detector());
		gestures.add(new SharkyRHDown1Detector());
		gestures.add(new SharkyRHDown2Detector());
		gestures.add(new SharkyRHDown3Detector());
		gestures.add(new SharkyRHDown4Detector());
		gestures.add(new SharkyRHDown5Detector());
		gestures.add(new SharkyLHUp0Detector());
		gestures.add(new SharkyLHDown1Detector());
		gestures.add(new SharkyLHDown2Detector());
		gestures.add(new SharkyLHDown3Detector());
		gestures.add(new SharkyLHDown4Detector());
		gestures.add(new SharkyLHDown5Detector());
		gestures.add(new SharkyLHUp1Detector());
		gestures.add(new SharkyLHUp2Detector());
		gestures.add(new SharkyLHUp3Detector());
		gestures.add(new SharkyLHUp4Detector());
		gestures.add(new SharkyLHUp5Detector());
	}

	/**
	 * Sends the given gesture to the M2M server.
	 * 
	 * @param gesture
	 * @throws MqttPersistenceException
	 * @throws MqttException
	 */

	private void sendGesture(byte[] gesture) {
		try {
			client.publish(TOPIC, new MqttMessage(gesture));
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends the recognized command to the Sharky controller via M2M.
	 * 
	 * @param command
	 * @throws MqttPersistenceException
	 * @throws MqttException
	 */

	private void sendCommand(String command) {
		try {
			client.publish(COMMAND_TOPIC, new MqttMessage(command.getBytes()));
			System.out.println("########## Sent MQTT: " + command);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void start(BundleContext context) throws Exception {
		plugin = this;

		client = new MqttClient(M2MProtocolConstants.DEFAULT_HOST,
				MqttClient.generateClientId());
		client.connect();

		manager = M2MManagerImpl.getInstance();
		manager.startKinect();

		M2MGestureProxy.INSTANCE.addGestureListener(this);
		manager.startSkeletonTracking();
		for (Gesture gesture : gestures) {
			M2MGestureProxy.INSTANCE.addGestureDetector(gesture);
		}

	}

	public void stop(BundleContext context) throws Exception {

		GestureProxy.INSTANCE.removeGestureListener(this);
		for (Gesture gesture : gestures) {
			GestureProxy.INSTANCE.removeGestureDetector(gesture);
		}

		manager.stopKinect();

		client.disconnect();
		client.close();
		client = null;

		manager = null;
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	@Override
	public void notifyGestureDetected(Class<? extends Gesture> gesture) {
		if (gesture == SharkyRHRight1Detector.class) {
			i++;
			System.out.println(i + ":            Right hand right 1");
			sendCommand("direction:-1");
		} else if (gesture == SharkyRHRight2Detector.class) {
			i++;
			System.out.println(i + ":              Right hand right 2");
			sendCommand("direction:-2");
		} else if (gesture == SharkyRHRight3Detector.class) {
			i++;
			System.out.println(i + ":                 Right hand right 3");
			sendCommand("direction:-3");
		} else if (gesture == SharkyRHRight4Detector.class) {
			i++;
			System.out.println(i + ":                   Right hand right 4");
			sendCommand("direction:-4");
		} else if (gesture == SharkyRHRight5Detector.class) {
			i++;
			System.out.println(i + ":                     Right hand right 5");
			sendCommand("direction:-5");
		} else if (gesture == SharkyRHRight0Detector.class) {
			i++;
			System.out.println(i + ":          Right hand NEUTRAL");
			sendCommand("direction:0");
		} else if (gesture == SharkyRHLeft1Detector.class) {
			i++;
			System.out.println(i + ":        Right hand left 1");
			sendCommand("direction:1");
		} else if (gesture == SharkyRHLeft2Detector.class) {
			i++;
			System.out.println(i + ":      Right hand left 2");
			sendCommand("direction:2");
		} else if (gesture == SharkyRHLeft3Detector.class) {
			i++;
			System.out.println(i + ":    Right hand left 3");
			sendCommand("direction:3");
		} else if (gesture == SharkyRHLeft4Detector.class) {
			i++;
			System.out.println(i + ":  Right hand left 4");
			sendCommand("direction:4");
		} else if (gesture == SharkyRHLeft5Detector.class) {
			i++;
			System.out.println(i + ":Right hand left 5");
			sendCommand("direction:5");
		} else if (gesture == SharkyRHDown1Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t Right hand down 1 V");
			sendCommand("pitch:1");
		} else if (gesture == SharkyRHDown2Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t Right hand down 2 VV");
			sendCommand("pitch:2");
		} else if (gesture == SharkyRHDown3Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t Right hand down 3 VVV");
			sendCommand("pitch:3");
		} else if (gesture == SharkyRHDown4Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t Right hand down 4 VVVV");
			sendCommand("pitch:4");
		} else if (gesture == SharkyRHDown5Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t Right hand down 5 VVVVV");
			sendCommand("pitch:5");
		} else if (gesture == SharkyRHUp0Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t Right hand down 0 -----");
			sendCommand("pitch:0");
		} else if (gesture == SharkyRHUp1Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t Right hand up 1   T");
			sendCommand("pitch:-1");
		} else if (gesture == SharkyRHUp2Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t Right hand up 2   TT");
			sendCommand("pitch:-2");
		} else if (gesture == SharkyRHUp3Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t Right hand up 3   TTT");
			sendCommand("pitch:-3");
		} else if (gesture == SharkyRHUp4Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t Right hand up 4   TTTT");
			sendCommand("pitch:-4");
		} else if (gesture == SharkyRHUp5Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t Right hand up 5   TTTTT");
			sendCommand("pitch:-5");
		} else if (gesture == SharkyLHDown1Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t\t\t Left hand down 1 V");
			sendCommand("speed:1");
		} else if (gesture == SharkyLHDown2Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t\t\t Left hand down 2 VV");
			sendCommand("speed:1");
		} else if (gesture == SharkyLHDown3Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t\t\t Left hand down 3 VVV");
			sendCommand("speed:0");
		} else if (gesture == SharkyLHDown4Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t\t\t Left hand down 4 VVVV");
			sendCommand("speed:0");
		} else if (gesture == SharkyLHDown5Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t\t\t Left hand down 5 VVVVV");
			sendCommand("speed:0");
		} else if (gesture == SharkyLHUp0Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t\t\t Left hand up 0 -----");
			sendCommand("speed:2");
		} else if (gesture == SharkyLHUp1Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t\t\t Left hand up 1   T");
			sendCommand("speed:3");
		} else if (gesture == SharkyLHUp2Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t\t\t Left hand up 2   TT");
			sendCommand("speed:4");
		} else if (gesture == SharkyLHUp3Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t\t\t Left hand up 3   TTT");
			sendCommand("speed:4");
		} else if (gesture == SharkyLHUp4Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t\t\t Left hand up 4   TTTT");
			sendCommand("speed:5");
		} else if (gesture == SharkyLHUp5Detector.class) {
			i++;
			System.out.println(i + ":\t\t\t\t\t Left hand up 5   TTTTT");
			sendCommand("speed:5");
		}

	}
}
