package com.github.kaklakariada.fritzbox.iot;

import com.amazonaws.services.iot.client.AWSIotDevice;

public class FritzDectDevice extends AWSIotDevice {

	public FritzDectDevice(String thingName) {
		super(thingName);
	}

}
