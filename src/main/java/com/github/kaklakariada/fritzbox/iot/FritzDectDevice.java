package com.github.kaklakariada.fritzbox.iot;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotDeviceProperty;
import com.github.kaklakariada.fritzbox.HomeAutomation;
import com.github.kaklakariada.fritzbox.model.homeautomation.Device;
import com.github.kaklakariada.fritzbox.model.homeautomation.DeviceList;
import com.github.kaklakariada.fritzbox.model.homeautomation.SwitchState;
import com.github.kaklakariada.fritzbox.model.homeautomation.SwitchState.SwitchMode;

public class FritzDectDevice extends AWSIotDevice {
	private final static Logger LOG = LoggerFactory.getLogger(FritzDectDevice.class);

	private final String deviceAin;
	private final HomeAutomation fritzDect;

	private final Duration updateDelay;
	private final Clock clock;
	private Instant lastUpdate;

	@AWSIotDeviceProperty(allowUpdate = true)
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
	@AWSIotDeviceProperty(allowUpdate = false)
	private boolean locked;
	@AWSIotDeviceProperty(allowUpdate = false)
	private SwitchMode mode;
	@AWSIotDeviceProperty(allowUpdate = false)
	private String firmwareVersion;

	public FritzDectDevice(HomeAutomation fritzDect, String thingName, String deviceAin, Duration updateDelay) {
		this(fritzDect, thingName, deviceAin, updateDelay, Clock.systemUTC());
	}

	FritzDectDevice(HomeAutomation fritzDect, String thingName, String deviceAin, Duration updateDelay, Clock clock) {
		super(thingName);
		this.fritzDect = fritzDect;
		this.deviceAin = deviceAin;
		this.updateDelay = updateDelay;
		this.clock = clock;
	}

	public boolean getPowerState() {
		return powerState;
	}

	public void setPowerState(boolean desiredPowerState) {
		LOG.debug("Set power state to {}", desiredPowerState);
		switchPowerState(desiredPowerState);
	}

	private void switchPowerState(boolean desiredPowerState) {
		try {
			fritzDect.switchPowerState(deviceAin, desiredPowerState);
		} catch (final RuntimeException e) {
			LOG.error("Error switching power state to " + desiredPowerState, e);
			throw e;
		}
	}

	public boolean getPresent() {
		updateIfNecessary();
		return present;
	}

	public String getName() {
		updateIfNecessary();
		return name;
	}

	public float getTemperature() {
		updateIfNecessary();
		return temperature;
	}

	public float getPowerWatt() {
		updateIfNecessary();
		return powerWatt;
	}

	public int getEnergyWattHour() {
		updateIfNecessary();
		return energyWattHour;
	}

	public boolean isLocked() {
		updateIfNecessary();
		return locked;
	}

	public SwitchMode getMode() {
		updateIfNecessary();
		return mode;
	}

	public String getFirmwareVersion() {
		updateIfNecessary();
		return firmwareVersion;
	}

	private void updateIfNecessary() {
		final Instant now = clock.instant();
		if (lastUpdate == null) {
			LOG.trace("Request initial update");
			update();
			lastUpdate = now;
			return;
		}
		final Duration timeSinceLastUpdate = Duration.between(lastUpdate, now);
		if (timeSinceLastUpdate.compareTo(updateDelay) > 0) {
			LOG.trace("Last update was {} > {} ago, update now", timeSinceLastUpdate, updateDelay);
			update();
			lastUpdate = now;
		}
	}

	private void update() {
		final DeviceList deviceList = getDeviceList();
		final Device deviceInfo = deviceList.getDeviceByIdentifier(deviceAin);
		if (deviceInfo == null) {
			LOG.debug("Device {} not found. Set present = false. Available device ids: {}", deviceAin,
					deviceList.getDeviceIdentifiers());
			this.present = false;
			return;
		}
		final SwitchState switchState = deviceInfo.getSwitchState();
		if (switchState != null) {
			this.present = true;
			this.powerState = switchState.isOn();
			this.locked = switchState.isLocked();
			this.mode = switchState.getMode();
		} else {
			this.present = false;
			this.powerState = false;
			this.locked = false;
			this.mode = null;
		}
		this.name = deviceInfo.getName();
		this.firmwareVersion = deviceInfo.getFirmwareVersion();
		this.energyWattHour = deviceInfo.getPowerMeter().getEnergyWattHours();
		this.powerWatt = deviceInfo.getPowerMeter().getPowerWatt();
		this.temperature = deviceInfo.getTemperature().getCelsius();
	}

	private DeviceList getDeviceList() {
		try {
			return fritzDect.getDeviceListInfos();
		} catch (final Exception e) {
			LOG.error("Error getting device list", e);
			throw new RuntimeException("Error getting device list", e);
		}
	}
}
