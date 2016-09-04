/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.ext;

import javax.media.mscontrol.Parameter;

/**
 *
 * @author Sergio
 */
public class codecs 
{
	public static final class rtp	{
		public static final Parameter ice				= new Parameter(){};
		public static final Parameter dtls				= new Parameter(){};
		public static final Parameter secure				= new Parameter(){};

		public static class ext {
			public static final Parameter ssrc_audio_level		= new Parameter(){};
			public static final Parameter toffset			= new Parameter(){};
			public static final Parameter abs_send_time		= new Parameter(){};
		}
	}
	public static final class rtcp	{
		public static final Parameter disabled				= new Parameter(){};
		public static final Parameter feedback				= new Parameter(){};
		public static final Parameter rtx				= new Parameter(){};
		public static final Parameter cname				= new Parameter(){};
		public static final Parameter msid				= new Parameter(){};
	}
	public static final class h264	{
		public static final Parameter max_mbps				= new Parameter(){};
		public static final Parameter max_fs				= new Parameter(){};
		public static final Parameter max_br				= new Parameter(){};
		public static final Parameter max_smbps				= new Parameter(){};
		public static final Parameter max_fps				= new Parameter(){};
		
	}
	public static final class opus {
		public static final Parameter ptime				= new Parameter(){};
		public static final Parameter maxptime				= new Parameter(){};
		public static final Parameter minptime				= new Parameter(){};
		public static final Parameter useinbandfec			= new Parameter(){};
		public static final Parameter usedtx				= new Parameter(){};
		public static final Parameter stereo				= new Parameter(){};
		public static final Parameter maxaveragebitrate			= new Parameter(){};
	}
}
