package com.github.kaklakariada.fritzbox.iot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotDeviceProperty;
import com.github.kaklakariada.fritzbox.HomeAutomation;

public class FritzDectDevice extends AWSIotDevice {
	private final static Logger LOG = LoggerFactory.getLogger(FritzDectDevice.class);

	private final String deviceAin;
	private final HomeAutomation fritzDect;

	@AWSIotDeviceProperty
	private boolean powerState;
	@AWSIotDeviceProperty(allowUpdate = false)
	private boolean present;
	@AWSIotDeviceProperty(allowUpdate = false)
	private String name;
	@AWSIotDeviceProperty(allowUpdate = false)
	private float temperature;
	@AWSIotDeviceProperty(allowUpdate = false)
	private float powerWatt;
	@AWSIotDeviceProperty(allowUpdate = false)
	private int energyWattHour;

	public FritzDectDevice(HomeAutomation fritzDect, String thingName, String deviceAin) {
		super(thingName);
		this.fritzDect = fritzDect;
		this.deviceAin = deviceAin;
	}

	public boolean getPowerState() {
		powerState = fritzDect.getSwitchState(deviceAin);
		LOG.debug("Power state to {}", powerState);
		return powerState;
	}

	public void setPowerState(boolean powerState) {
		this.powerState = powerState;
		LOG.debug("Set power state to {}", this.powerState);
		fritzDect.switchPowerState(deviceAin, this.powerState);
	}

	public boolean getPresent() {
		this.present = fritzDect.getSwitchPresent(deviceAin);
		LOG.debug("Current present state is {}", powerState);
		return present;
	}

	public String getName() {
		this.name = fritzDect.getSwitchName(deviceAin);
		LOG.debug("Current name is {}", name);
		return name;
	}

	public float getTemperature() {
		temperature = fritzDect.getTemperature(deviceAin);
		LOG.debug("Current temperature is {}", temperature);
		return temperature;
	}

	public float getPowerWatt() {
		powerWatt = fritzDect.getSwitchPowerWatt(deviceAin);
		LOG.debug("Current power is {}W", powerWatt);
		return powerWatt;
	}

	public int getEnergyWattHour() {
		energyWattHour = fritzDect.getSwitchEnergyWattHour(deviceAin);
		LOG.debug("Current energy is {}Wh", energyWattHour);
		return energyWattHour;
	}
}
