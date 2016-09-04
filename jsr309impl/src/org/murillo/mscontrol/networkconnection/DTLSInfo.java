package org.murillo.mscontrol.networkconnection;

import org.murillo.MediaServer.Codecs.Setup;

class DTLSInfo
{
	Setup setup;
	String hash;
	String fingerprint;

	public DTLSInfo(Setup setup,String hash, String fingerprint) {
   		this.setup = setup;
		this.hash = hash;
		this.fingerprint = fingerprint;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public String getHash() {
		return hash;
	}

	public Setup getSetup() {
		return setup;
	}
}