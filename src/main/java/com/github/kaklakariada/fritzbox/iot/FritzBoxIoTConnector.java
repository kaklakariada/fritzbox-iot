package com.github.kaklakariada.fritzbox.iot;

import java.time.Duration;

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
		final Duration updateDelay = config.getReportInterval().minusMillis(100);
		final FritzDectDevice device = new FritzDectDevice(fritzDect, config.getThingName(), config.getFritzDectAin(),
				updateDelay);

		return new FritzBoxIoTConnector(config, client, device);
	}

	private static HomeAutomation createFritzBoxConnection(Config config) {
		final HttpTemplate template = new HttpTemplate(config.getFritzBoxUrl());
		final FritzBoxSession session = new FritzBoxSession(template);
		session.login(config.getFritzBoxUsername(), config.getFritzBoxPassword());
		return new HomeAutomation(session);
	}

	public void connect() {
		device.setReportInterval(config.getReportInterval().toMillis());
		connectIoT();
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