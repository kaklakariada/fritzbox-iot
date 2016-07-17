package com.github.kaklakariada.fritzbox.iot.util;

import java.security.KeyStore;

public class KeyStorePasswordPair {
	public KeyStore keyStore;
	public String keyPassword;

	public KeyStorePasswordPair(KeyStore keyStore, String keyPassword) {
		this.keyStore = keyStore;
		this.keyPassword = keyPassword;
	}
}