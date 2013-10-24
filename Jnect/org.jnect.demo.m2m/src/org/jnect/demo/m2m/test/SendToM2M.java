package org.jnect.demo.m2m.test;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jnect.core.m2m.M2MProtocolConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SendToM2M {

	public static void main(String[] args) {
		try {
			MqttClient client = new MqttClient(
					M2MProtocolConstants.DEFAULT_HOST,
					MqttClient.generateClientId());
			client.connect();
			client.publish(M2MProtocolConstants.SKELETON_TOPIC, createMessage());
			client.disconnect();
			client.close();
		} catch (MqttException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	private static MqttMessage createMessage() throws TransformerException,
			ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();

		Element header = doc.createElement("header");
		doc.appendChild(header);

		Element frame = doc.createElement("frameNumber");
		header.appendChild(frame);

		DateFormat df = new SimpleDateFormat("HHmmss");
		frame.setTextContent(df.format(new Date()));

		Element skeleton = doc.createElement("skeletonData");
		header.appendChild(skeleton);

		Element joint = doc.createElement("joint");
		skeleton.appendChild(joint);

		Element jointId = doc.createElement("jointId");
		jointId.setTextContent("HandLeft");
		joint.appendChild(jointId);

		Element jointX = doc.createElement("positionX");
		jointX.setTextContent(Float.toString(11.0f));
		joint.appendChild(jointX);
		Element jointY = doc.createElement("positionY");
		jointY.setTextContent(Float.toString(14.0f));
		joint.appendChild(jointY);
		Element jointZ = doc.createElement("positionZ");
		jointZ.setTextContent(Float.toString(9.0f));
		joint.appendChild(jointZ);

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(stream);

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);

		transformer.transform(source, result);

		return new MqttMessage(String.format("SKELETON: %s", stream.toString())
				.getBytes());
	}
}
