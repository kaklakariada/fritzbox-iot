package com.github.kaklakariada.fritzbox.iot;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.github.kaklakariada.fritzbox.iot.util.KeyStorePasswordPair;

public class Main {
	public static void main(String[] args) throws AWSIotException {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		final Config config = Config.get();
		final KeyStorePasswordPair pair = config.getKeyStorePasswordPair();
		final AWSIotMqttClient client = new AWSIotMqttClient(config.getClientEndpoint(), config.getClientId(),
				pair.keyStore, pair.keyPassword);

		final FritzDectDevice device = new FritzDectDevice(config.getThingName());
		client.attach(device);
		client.connect();
	}
}
