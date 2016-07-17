package com.github.kaklakariada.fritzbox.iot.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/**
 * This is a helper class to facilitate reading of the configurations and
 * certificate from the resource files.
 */
public class Util {

	public static KeyStorePasswordPair getKeyStorePasswordPair(Path certificateFile, Path privateKeyFile) {
		return getKeyStorePasswordPair(certificateFile, privateKeyFile, null);
	}

	public static KeyStorePasswordPair getKeyStorePasswordPair(Path certificateFile, Path privateKeyFile,
			String keyAlgorithm) {
		if (certificateFile == null || privateKeyFile == null) {
			throw new IllegalArgumentException("Certificate or private key file missing");
		}

		final Certificate certificate = loadCertificateFromFile(certificateFile);
		final PrivateKey privateKey = loadPrivateKeyFromFile(privateKeyFile, keyAlgorithm);
		return getKeyStorePasswordPair(certificate, privateKey);
	}

	public static KeyStorePasswordPair getKeyStorePasswordPair(Certificate certificate, PrivateKey privateKey) {
		final KeyStore keyStore;
		final String keyPassword;
		try {
			keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null);
			keyStore.setCertificateEntry("alias", certificate);

			// randomly generated key password for the key in the KeyStore
			keyPassword = new BigInteger(128, new SecureRandom()).toString(32);
			keyStore.setKeyEntry("alias", privateKey, keyPassword.toCharArray(), new Certificate[] { certificate });
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new RuntimeException("Failed to create key store", e);
		}

		return new KeyStorePasswordPair(keyStore, keyPassword);
	}

	private static Certificate loadCertificateFromFile(Path file) {
		if (!Files.isReadable(file)) {
			throw new RuntimeException("Certificate file not found: " + file);
		}
		try (BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(file))) {
			final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			return certFactory.generateCertificate(stream);
		} catch (IOException | CertificateException e) {
			throw new RuntimeException("Failed to load certificate file " + file, e);
		}
	}

	private static PrivateKey loadPrivateKeyFromFile(Path file, String algorithm) {
		if (!Files.isReadable(file)) {
			throw new RuntimeException("Private key file not found: " + file);
		}
		try (DataInputStream stream = new DataInputStream(Files.newInputStream(file))) {
			return PrivateKeyReader.getPrivateKey(stream, algorithm);
		} catch (IOException | GeneralSecurityException e) {
			throw new RuntimeException("Failed to load private key from file " + file, e);
		}
	}
}