package com.github.kaklakariada.fritzbox.iot;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.amazonaws.services.iot.client.AWSIotException;

public class Main {
	public static void main(String[] args) throws AWSIotException {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		final FritzBoxIoTConnector connector = FritzBoxIoTConnector.create();
		connector.connect();
	}

}
