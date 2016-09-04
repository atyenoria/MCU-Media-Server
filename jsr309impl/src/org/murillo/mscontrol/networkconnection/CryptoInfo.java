package org.murillo.mscontrol.networkconnection;

import java.security.SecureRandom;
import javax.xml.bind.DatatypeConverter;

public class CryptoInfo 
{
		String suite;
		String key;

		public static CryptoInfo Generate()
		{
			//Create crypto info for media
			CryptoInfo info = new CryptoInfo();
			//Set suite
			info.suite = "AES_CM_128_HMAC_SHA1_80";
			//Get random
			SecureRandom random = new SecureRandom();
			//Create key bytes
			byte[] key = new byte[30];
			//Generate it
			random.nextBytes(key);
			//Encode to base 64
			info.key = DatatypeConverter.printBase64Binary(key);
			//return it
			return info;
		}

		public CryptoInfo() {

		}

		public CryptoInfo(String suite, String key) {
			this.suite = suite;
			this.key = key;
		}
	}
