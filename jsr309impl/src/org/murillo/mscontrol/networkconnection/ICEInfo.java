package org.murillo.mscontrol.networkconnection;

import java.security.SecureRandom;
import javax.xml.bind.DatatypeConverter;

class ICEInfo
{
	String ufrag;
	String pwd;

	public static ICEInfo Generate()
	{
		//Create ICE info for media
		ICEInfo info = new ICEInfo();
		 //Get random
		SecureRandom random = new SecureRandom();
		//Create key bytes
		byte[] frag = new byte[8];
		byte[] pwd = new byte[22];
		//Generate them
		random.nextBytes(frag);
		random.nextBytes(pwd);
		//Create ramdom pwd
		info.ufrag = DatatypeConverter.printHexBinary(frag);
		info.pwd   =  DatatypeConverter.printHexBinary(pwd);
		//return it
		return info;
	}

	private ICEInfo() {

	}

	public ICEInfo(String ufrag, String pwd) {
		this.ufrag = ufrag;
		this.pwd = pwd;
	}
}
