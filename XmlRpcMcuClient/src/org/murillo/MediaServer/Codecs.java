/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.MediaServer;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * @author Sergio
 */
public class Codecs {

   public static enum MediaType {
        AUDIO("audio",0),
        VIDEO("video",1),
        TEXT("text",2);
        
        public final String name;
        public final Integer value;
        
        MediaType(String name,Integer value){
            this.name = name;
            this.value = value;
        }

        public Integer valueOf() {
            return value;
        }

        public String getName() {
                return name;
        }
    };

    public static enum Direction {
	INACTIVE("inactive"),
        SENDRECV("sendrecv"),
        SENDONLY("sendonly"),
        RECVONLY("recvonly");

        public final String value;

        Direction(String value){
            this.value = value;
        }

        public String valueOf() {
            return value;
        }
	public boolean isSending() {
	    return equals(SENDRECV) || equals(SENDONLY);
	}

	public boolean isReceving() {
	    return equals(SENDRECV) || equals(RECVONLY);
	}

	public boolean equals(Direction dir) {
	    return value.equals(dir.value);
	}

	public Direction reverse() {
	    if (equals(RECVONLY))
		return SENDONLY;
	    else if (equals(SENDONLY))
		return RECVONLY;
	    else
		return this;
	}

    };

    public static enum Setup {
	ACTIVE("active"),
        PASSIVE("passive"),
        ACTPASS("actpass"),
        INACTIVE("inactive");

	public static Setup byValue(String value) {
	    if (ACTIVE.equals(value))
		return ACTIVE;
	    else if (PASSIVE.equals(value))
		return PASSIVE;
	    else if (ACTPASS.equals(value))
		return ACTPASS;
	    return INACTIVE;
	}

        public final String value;

        Setup(String value){
            this.value = value;
        }

        public String valueOf() {
            return value;
        }
	
	public boolean equals(Setup setup) {
	    return value.equals(setup.value);
	}

	public boolean equals(String setup) {
	    return value.equals(setup);
	}

	public Setup reverse() {
	    if (equals(ACTIVE))
		return PASSIVE;
	    else if (equals(PASSIVE))
		return ACTIVE;
	    else if (equals(ACTPASS))
		return PASSIVE;
	    else
		return INACTIVE;
	}

    };

    public static final Integer PCMU    = 0;
    public static final Integer PCMA    = 8;
    public static final Integer GSM     = 3;
    public static final Integer AMR     = 118;
    public static final Integer AMR_WB  = 119;
    public static final Integer G726    = 120;
    public static final Integer G722    = 9;
    public static final Integer NELLY11 = 131;
    public static final Integer SPEEX16 = 117;
    public static final Integer OPUS    = 98;
    public static final Integer TELEFONE_EVENT  = 100;

    public static final Integer H263_1996    = 34;
    public static final Integer H263_1998    = 103;
    public static final Integer MPEG4        = 104;
    public static final Integer H264         = 99;
    public static final Integer SORENSON     = 100;
    public static final Integer VP8          = 107;

    public static final Integer ULPFEC       = 108;
    public static final Integer RED          = 109;
    public static final Integer RTX          = 110;

    public static final Integer T140RED      = 105;
    public static final Integer T140         = 106;

    public static final Integer MaxH264SupportedProfile = 77;

    public static Integer getCodecForName(String media,String name)
    {
        if (media.equals("audio"))
        {
            if (name.equalsIgnoreCase("PCMU"))        return PCMU;
            if (name.equalsIgnoreCase("PCMA"))        return PCMA;
            if (name.equalsIgnoreCase("GSM"))         return GSM;
            if (name.equalsIgnoreCase("SPEEX"))       return SPEEX16;
            if (name.equalsIgnoreCase("AMR"))         return AMR;
            if (name.equalsIgnoreCase("AMR-WB"))      return AMR_WB;
            if (name.equalsIgnoreCase("G726"))        return G726;
            if (name.equalsIgnoreCase("G722"))        return G722;
            if (name.equalsIgnoreCase("NELLY11"))     return NELLY11;
            if (name.equalsIgnoreCase("opus"))        return OPUS;
            if (name.equalsIgnoreCase("telephone-event"))       return TELEFONE_EVENT;
        }
        else if (media.equals("video"))
        {
            if (name.equalsIgnoreCase("H263"))        return H263_1996;
            if (name.equalsIgnoreCase("H263-1998"))   return H263_1998;
            if (name.equalsIgnoreCase("H263-2000"))   return H263_1998;
            if (name.equalsIgnoreCase("MP4V-ES"))     return MPEG4;
            if (name.equalsIgnoreCase("H264"))        return H264;
            if (name.equalsIgnoreCase("SORENSON"))    return SORENSON;
            if (name.equalsIgnoreCase("VP8"))         return VP8;
            if (name.equalsIgnoreCase("RED"))         return RED;
            if (name.equalsIgnoreCase("ULPFEC"))      return ULPFEC;
	    if (name.equalsIgnoreCase("RTX"))	      return RTX;
        }
        else if (media.equals("text"))
        {
            if (name.equalsIgnoreCase("RED"))         return T140RED;
            if (name.equalsIgnoreCase("T140"))        return T140;
         }
        return -1;
    }

    public static String getNameForCodec(String media,Integer codec)
    {
        if (media.equals("audio"))
        {
            if (codec==PCMU)       return "PCMU";
            if (codec==PCMA)       return "PCMA";
            if (codec==GSM)        return "GSM";
            if (codec==SPEEX16)    return "SPEEX";
            if (codec==AMR)        return "AMR";
            if (codec==AMR_WB)     return "AMR-WB";
            if (codec==G726)       return "G726";
            if (codec==G722)       return "G722";
            if (codec==NELLY11)    return "NELLY11";
            if (codec==OPUS)       return "opus";
            if (codec==TELEFONE_EVENT)    return "telephone-event";
        }
        else if (media.equals("video"))
        {
            if (codec==H263_1996)  return "H263";
            if (codec==H263_1998)  return "H263-1998";
            if (codec==MPEG4)      return "MP4V-ES";
            if (codec==H264)       return "H264";
            if (codec==SORENSON)   return "SORENSON";
            if (codec==VP8)        return "VP8";
            if (codec==RED)        return "red";
            if (codec==ULPFEC)     return "ulpfec";
	    if (codec==RTX)        return "rtx";
        }
        else if (media.equals("text"))
        {
            if (codec==T140RED)    return "RED";
            if (codec==T140)       return "T140";
        }
        return "";
    }

    public static Integer getRateForCodec(String media,Integer codec)
    {
        if (media.equals("audio"))
        {
            if (codec==PCMU)            return 8000;
            if (codec==PCMA)            return 8000;
            if (codec==GSM)             return 8000;
            if (codec==SPEEX16)         return 16000;
            if (codec==AMR)             return 8000;
            if (codec==AMR_WB)          return 16000;
            if (codec==G726)            return 8000;
            if (codec==G722)            return 8000;
            if (codec==NELLY11)         return 11000;
            if (codec==OPUS)            return 48000;
            if (codec==TELEFONE_EVENT)  return 8000;
        }
        else if (media.equals("video"))
            return 90000;
        else if (media.equals("text"))
            return 1000;
        return 0;
    }

     public static Vector<Integer> getCodecsFromList(String media,String list) {
        //Create vector list of codecs
        Vector<Integer> codecs = new Vector<Integer>();
        //Split
        StringTokenizer token = new StringTokenizer(list);
        //While it has more
        while (token.hasMoreTokens())
        {
            //Get codec
            Integer codec = getCodecForName(media, token.nextToken());
            //If found
            if (codec!=-1)
                //add it
                codecs.add(codec);
         }
        //retrun list
        return codecs;
    }


     public static class CodecInfo {
	public CodecInfo(MediaType media,Integer codec,Integer rate) {
	    this.media = media;
	    this.codec = codec;
	    this.rate  = rate;
	}
	private MediaType media;
	private Integer codec;
	private Integer rate;

	public Integer getCodec() {
	    return codec;
	}

	public MediaType getMedia() {
	    return media;
	}

	public Integer getRate() {
	    return rate;
	}

    }

    public static CodecInfo getCodecInfoForname(String name)
    {
         if (name.equalsIgnoreCase("PCMU"))		return new CodecInfo(MediaType.AUDIO,PCMU,8000);
         if (name.equalsIgnoreCase("PCMA"))		return new CodecInfo(MediaType.AUDIO,PCMA,8000);
         if (name.equalsIgnoreCase("GSM"))		return new CodecInfo(MediaType.AUDIO,GSM,8000);
         if (name.equalsIgnoreCase("SPEEX"))		return new CodecInfo(MediaType.AUDIO,SPEEX16,16000);
         if (name.equalsIgnoreCase("AMR"))		return new CodecInfo(MediaType.AUDIO,AMR,8000);
         if (name.equalsIgnoreCase("AMR-WB"))		return new CodecInfo(MediaType.AUDIO,AMR_WB,16000);
         if (name.equalsIgnoreCase("G726"))		return new CodecInfo(MediaType.AUDIO,G726,8000);
         if (name.equalsIgnoreCase("G722"))		return new CodecInfo(MediaType.AUDIO,G722,8000);
         if (name.equalsIgnoreCase("NELLY11"))		return new CodecInfo(MediaType.AUDIO,NELLY11,11000);
         if (name.equalsIgnoreCase("telephone-event"))  return new CodecInfo(MediaType.AUDIO,TELEFONE_EVENT,1000);
         if (name.equalsIgnoreCase("H263"))		return new CodecInfo(MediaType.VIDEO,H263_1996,90000);
         if (name.equalsIgnoreCase("H263-1998"))	return new CodecInfo(MediaType.VIDEO,H263_1998,90000);
         if (name.equalsIgnoreCase("H263-2000"))	return new CodecInfo(MediaType.VIDEO,H263_1998,90000);
         if (name.equalsIgnoreCase("MP4V"))		return new CodecInfo(MediaType.VIDEO,MPEG4,90000);
         if (name.equalsIgnoreCase("H264"))		return new CodecInfo(MediaType.VIDEO,H264,90000);
         if (name.equalsIgnoreCase("SORENSON"))		return new CodecInfo(MediaType.VIDEO,SORENSON,90000);
         if (name.equalsIgnoreCase("VP8"))		return new CodecInfo(MediaType.VIDEO,VP8,90000);
         if (name.equalsIgnoreCase("RED"))		return new CodecInfo(MediaType.TEXT,T140RED,90000);
         if (name.equalsIgnoreCase("T140"))		return new CodecInfo(MediaType.TEXT,T140,90000);
         return null;
    }
}
