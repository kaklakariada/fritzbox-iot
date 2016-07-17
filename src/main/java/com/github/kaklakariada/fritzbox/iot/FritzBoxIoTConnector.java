package com.github.kaklakariada.fritzbox.iot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.github.kaklakariada.fritzbox.FritzBoxSession;
import com.github.kaklakariada.fritzbox.HomeAutomation;
import com.github.kaklakariada.fritzbox.http.HttpTemplate;
import com.github.kaklakariada.fritzbox.iot.util.KeyStorePasswordPair;

public class FritzBoxIoTConnector {

	private final static Logger LOG = LoggerFactory.getLogger(FritzBoxIoTConnector.class);

	private final Config config;
	private final AWSIotMqttClient client;
	private final FritzDectDevice device;

	private FritzBoxIoTConnector(Config config, AWSIotMqttClient client, FritzDectDevice device) {
		this.config = config;
		this.client = client;
		this.device = device;
	}

	public static FritzBoxIoTConnector create() {
		return create(Config.get());
	}

	public static FritzBoxIoTConnector create(Config config) {
		LOG.debug("Creating new IoT connector");
		final KeyStorePasswordPair pair = config.getKeyStorePasswordPair();
		final AWSIotMqttClient client = new AWSIotMqttClient(config.getClientEndpoint(), config.getClientId(),
				pair.keyStore, pair.keyPassword);

		final HomeAutomation fritzDect = createFritzBoxConnection(config);
		final FritzDectDevice device = new FritzDectDevice(fritzDect, config.getThingName(), config.getFritzDectAin());

		return new FritzBoxIoTConnector(config, client, device);
	}

	private static HomeAutomation createFritzBoxConnection(Config config) {
		final HttpTemplate template = new HttpTemplate(config.getFritzBoxUrl());
		final FritzBoxSession session = new FritzBoxSession(template);
		session.login(config.getFritzBoxUsername(), config.getFritzBoxPassword());
		final HomeAutomation homeAutomation = new HomeAutomation(session);
		return homeAutomation;
	}

	public void connect() {
		device.setReportInterval(5000);
		connectIoT();
		try {
			LOG.debug("Delete current state of device '{}'...", device.getThingName());
			device.delete();
			LOG.debug("State deleted");
		} catch (final AWSIotException e) {
			throw new RuntimeException("Error deleting state", e);
		}
	}

	private void connectIoT() {
		try {
			LOG.debug("Attaching device with thing name '{}'...", device.getThingName());
			client.attach(device);
			LOG.debug("Connecting IoT client with id '{}'...", client.getClientId());
			client.connect();
			LOG.debug("IoT client connected");
		} catch (final AWSIotException e) {
			throw new RuntimeException("Error connecting to IoT", e);
		}
	}

}