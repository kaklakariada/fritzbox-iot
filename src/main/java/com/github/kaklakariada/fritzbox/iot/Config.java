package com.github.kaklakariada.fritzbox.iot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kaklakariada.fritzbox.iot.util.KeyStorePasswordPair;
import com.github.kaklakariada.fritzbox.iot.util.Util;

public class Config {
	private static final String CONFIG_FILE = "config/config.properties";

	private final static Logger LOG = LoggerFactory.getLogger(Config.class);

	private final Properties config;
	private static Config instance;

	private Config(Properties config) {
		this.config = config;
	}

	public static Config get() {
		if (instance == null) {
			final Properties properties = readConfigFile(CONFIG_FILE);
			instance = new Config(properties);
		}
		return instance;
	}

	private static Properties readConfigFile(String fileName) {
		final Path path = Paths.get(fileName).toAbsolutePath();
		LOG.info("Reading config from {}", path);
		final Properties config = new Properties();
		try (InputStream stream = Files.newInputStream(path)) {
			config.load(stream);
		} catch (final IOException e) {
			throw new RuntimeException("Could not load properties " + path, e);
		}
		return config;
	}

	public String getClientEndpoint() {
		return getProperty("aws.iot.client_endpoint");
	}

	public String getClientId() {
		return getProperty("aws.iot.client_id");
	}

	public String getThingName() {
		return getProperty("aws.iot.thing_name");
	}

	public Duration getReportInterval() {
		return Duration.ofMillis(getLongProperty("aws.iot.report_interval_millis"));
	}

	public String getFritzBoxUrl() {
		return getProperty("fritzbox.url");
	}

	public String getFritzBoxUsername() {
		return getProperty("fritzbox.username");
	}

	public String getFritzBoxPassword() {
		return getProperty("fritzbox.password");
	}

	public String getFritzDectAin() {
		return getProperty("fritzbox.fritzdect.ain");
	}

	private Path getCertificateFile() {
		return getPathProperty("aws.iot.certificate_file");
	}

	private Path getPrivateKeyFile() {
		return getPathProperty("aws.iot.private_key_file");
	}

	public KeyStorePasswordPair getKeyStorePasswordPair() {
		return Util.getKeyStorePasswordPair(getCertificateFile(), getPrivateKeyFile());
	}

	private String getProperty(String propertyKey) {
		final String value = config.getProperty(propertyKey);
		if (value != null) {
			return value;
		}
		throw new AssertionError("Property '" + propertyKey + "' not found in " + CONFIG_FILE);
	}

	private Path getPathProperty(String propertyKey) {
		final Path path = Paths.get(getProperty(propertyKey)).toAbsolutePath();
		if (!Files.isReadable(path)) {
			throw new RuntimeException("Error reading file " + path);
		}
		return path;
	}

	private long getLongProperty(String propertyKey) {
		return Long.parseLong(getProperty(propertyKey));
	}

}
