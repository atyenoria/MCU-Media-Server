/*
 * XmlRpcMcuClient.java
 *
 * Copyright (C) 2007  Sergio Garcia Murillo
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.murillo.MediaServer;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.murillo.MediaServer.Codecs.MediaType;
import org.murillo.MediaServer.Codecs.Setup;

/**
 *
 * @author Sergio Garcia Murillo
 */
public class XmlRpcMcuClient {

	

    @XmlType()
    @XmlAccessorType(XmlAccessType.NONE)
    public static class MediaStatistics {
	@XmlElement
	public boolean	isSending = false;
	@XmlElement
	public boolean	isReceiving = false;
	@XmlElement
        public Integer	lostRecvPackets = 0;
	@XmlElement
	public Integer	numRecvPackets = 0;
	@XmlElement
	public Integer	numSendPackets = 0;
	@XmlElement
	public Integer	totalRecvBytes = 0;
	@XmlElement
	public Integer	totalSendBytes = 0;

    };

    public static class ConferenceInfo {
        public Integer id;
        public String name;
        public Integer numPart;
    }
    
    public static final Integer QCIF	= 0;  // 176  x 144 	AR:	1,222222222
    public static final Integer CIF	= 1;  // 352  x 288	AR:	1,222222222
    public static final Integer VGA	= 2;  // 640  x 480	AR:	1,333333333
    public static final Integer PAL	= 3;  // 768  x 576	AR:	1,333333333
    public static final Integer HVGA	= 4;  // 480  x 320	AR:	1,5
    public static final Integer QVGA	= 5;  // 320  x 240	AR:	1,333333333
    public static final Integer HD720P	= 6;  // 1280 x 720	AR:	1,777777778
    public static final Integer WQVGA	= 7;  // 400  x 240	AR:	1,666666667
    public static final Integer W448P	= 8;  // 768  x 448	AR:	1,714285714
    public static final Integer SD448P	= 9;  // 576  x 448	AR:	1,285714286
    public static final Integer W288P	= 10; // 512  x 288	AR:	1,777777778
    public static final Integer W576	= 11; // 1024 x 576	AR:	1,777777778
    public static final Integer FOURCIF	= 12; // 704  x 576	AR:	1,222222222
    public static final Integer FOURSIF	= 13; // 704  x 480	AR:	1,466666667
    public static final Integer XGA	= 14; // 1024 x 768	AR:	1,333333333
    public static final Integer WVGA	= 15; // 800  x 480	AR:	1,666666667
    public static final Integer DCIF	= 16; // 528  x 384	AR:	1,375
    public static final Integer SIF	= 17; // 352  x 240	AR:	1,466666667
    public static final Integer QSIF	= 18; // 176  x 120	AR:	1,466666667
    public static final Integer SD480P	= 19; // 480  x 360	AR:	1,333333333
    public static final Integer SQCIF	= 20; // 128  x 96	AR:	1,333333333
    public static final Integer SCIF	= 21; // 256  x 192	AR:	1,333333333
    public static final Integer HD1080P = 22; // 1920 x 1080    AR:     1,777777778
   
    
    public static final Integer MOSAIC1x1      = 0;
    public static final Integer MOSAIC2x2      = 1;
    public static final Integer MOSAIC3x3      = 2;
    public static final Integer MOSAIC3p4      = 3;
    public static final Integer MOSAIC1p7      = 4;
    public static final Integer MOSAIC1p5      = 5;
    public static final Integer MOSAIC1p1      = 6;
    public static final Integer MOSAICPIP1     = 7;
    public static final Integer MOSAICPIP3     = 8;
    public static final Integer MOSAIC4x4      = 9;
    public static final Integer MOSAIC1p4A     = 10;
    public static final Integer MOSAIC1p2A     = 11;
    public static final Integer MOSAIC1p2x2A   = 12;
    public static final Integer MOSAIC1p6A     = 13;
    public static final Integer MOSAIC1p12     = 14;
    public static final Integer MOSAIC1p16A    = 15;
    public static final Integer MOSAIC4x5A     = 16;
    public static final Integer MOSAIC5x5      = 17;
    public static final Integer MOSAIC1p1A     = 18;
    public static final Integer MOSAIC1p2      = 19;
    public static final Integer MOSAIC1p2x6A   = 20;
    public static final Integer MOSAIC1p1p2x4A = 21;
    public static final Integer MOSAIC1p3A     = 22;

    public static final Integer DefaultMosaic = 0;
    public static final Integer AppMixerMosaic = -1;
    public static final Integer DefaultSidebar = 0;
    public static final Integer AppMixerId = 1;

    public static final Integer RTP = 0;
    public static final Integer RTMP = 1;

    public static final Integer VADNONE = 0;
    public static final Integer VADBASIC = 1;
    public static final Integer VADFULL = 2;

    public static final Integer SLOTFREE = 0;
    public static final Integer SLOTLOCK = -1;
    public static final Integer SLOTVAD = -2;

    public static final int getMosaicNumSlots(Integer type) 
    {
        switch(type) 
        {
            case 0:
                return 1;
            case 1:
                return 4;
            case 2:
                return 9;
            case 3:
                return 7;
            case 4:
                return 8;
            case 5:
                return 6;
            case 6:
                return 2;
            case 7:
                return 2;
            case 8:
                return 4;
            case 9:
                return 16;
            case 10:
                return 5;
	    case 11:
		return 3;
	    case 12:
		return 5;
	    case 13:
		return 7;
	    case 14:
		return 13;
	    case 15:
		return 17;
	    case 16:
		return 20;
	    case 17:
		return 25;
	    case 18:
		return 2;
	    case 19:
		return 3;
	    case 20:
		return 13;
	    case 21:
		return 10;
	    case 22:
		return 4;
        }
        
        return -1;
    }

    
    private XmlRpcTimedClient client;
    private XmlRpcClientConfigImpl config;
    private static final Logger logger = Logger.getLogger("XMLRPCMCU");
    private static final Level level = Level.INFO;
    
    /** Creates a new instance of XmlRpcMcuClient */
    public XmlRpcMcuClient(String  url) throws MalformedURLException
    {
        config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(url));
        client = new XmlRpcTimedClient();
        client.setConfig(config);
    }

    public Map<Integer,ConferenceInfo> getConferences() throws XmlRpcException {
        //Create request
        Object[] request = new Object[]{};
        //Execute
        HashMap response = (HashMap) client.execute("GetConferences", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Create map
        HashMap<Integer,ConferenceInfo> conferences = new HashMap<Integer, ConferenceInfo>(returnVal.length);
        //For each value in array
        for (int i=0;i<returnVal.length;i++)
        {
            //Get array
             Object[] arr = (Object[]) returnVal[i];
             //Get id
             Integer id = (Integer)arr[0];
             //Create info
             ConferenceInfo info = new ConferenceInfo();
             //Fill values
             info.id      = (Integer)arr[0];
             info.name    = (String)arr[1];
             info.numPart = (Integer)arr[2];
             //Add it
             conferences.put(id, info);
        }
        //Return conference list
        return conferences;
    }

     public Integer CreateConference(String tag,Integer queueId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{tag,queueId};
        //Log
        logger.log(level,"CreateConference({0},{1})",request);
        //Execute
        HashMap response = (HashMap) client.execute("CreateConference", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        
        //Return conference id
        return (Integer)returnVal[0];
    }
     
    public Integer CreateConference(String tag,Integer vad,Integer queueId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{tag,vad,queueId};
        //Log
        logger.log(level,"CreateConference({0},{1},{2})",request);
        //Execute
        HashMap response = (HashMap) client.execute("CreateConference", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return conference id
        return (Integer)returnVal[0];
    }
    
    public Integer CreateConference(String tag,Integer vad,Integer rate,Integer queueId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{tag,vad,rate,queueId};
        //Log
        logger.log(level,"CreateConference({0},{1},{2},{3})",request);
        //Execute 
        HashMap response = (HashMap) client.execute("CreateConference", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return conference id
        return (Integer)returnVal[0];
    }

    public boolean InitConference(Integer confId, HashMap<String,String> properties) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,properties};
        //Log
        logger.log(level,"InitConference({0},{1})",request);
        //Execute
        HashMap response = (HashMap) client.execute("InitConference", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    
    public Integer CreateMosaic(Integer confId,Integer comp,Integer size) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,comp,size};
        //Log
        logger.log(level,"CreateMosaic({0},{1},{2})",request);
        //Execute
        HashMap response = (HashMap) client.execute("CreateMosaic", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return conference id
        return (Integer)returnVal[0];
    }
    
    public Boolean SetMosaicOverlayImage(Integer confId,Integer mosaicId,String filename) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,mosaicId,filename};
        //Log
        logger.log(level,"SetMosaicOverlayImage({0},{1},{2})",request);
        //Execute
        HashMap response = (HashMap) client.execute("SetMosaicOverlayImage", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    
    public Boolean ResetMosaicOverlay(Integer confId,Integer mosaicId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,mosaicId};
        //Log
        logger.log(level,"ResetMosaicOverlay({0},{1})",request);
        //Execute
        HashMap response = (HashMap) client.execute("ResetMosaicOverlay", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public Boolean DeleteMosaic(Integer confId,Integer mosaicId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,mosaicId};
        //Log
        logger.log(level,"DeleteMosaic({0},{1})",request);
        //Execute
        HashMap response = (HashMap) client.execute("DeleteMosaic", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public Integer CreateSidebar(Integer confId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId};
        //Log
        logger.log(level,"CreateSidebar({0})",request);
        //Execute
        HashMap response = (HashMap) client.execute("CreateSidebar", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return conference id
        return (Integer)returnVal[0];
    }

    public Boolean DeleteSidebar(Integer confId,Integer sidebarId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,sidebarId};
        //Log
        logger.log(level,"DeleteSidebar({0},{1})",request);
        //Execute
        HashMap response = (HashMap) client.execute("DeleteSidebar", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public Integer CreateParticipant(Integer confId,String name,String token,Integer type,Integer mosaicId,Integer sidebarId) throws XmlRpcException
    {
         //Create request
        Object[] request = new Object[]{confId,name,token,type,mosaicId,sidebarId};
        //Log
        logger.log(level,"CreateParticipant({0},{1},{2},{3},{4},{5})",request);
        //Execute 
        HashMap response = (HashMap) client.execute("CreateParticipant", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return part id
        return (Integer)returnVal[0];
    }
    
    public boolean SetCompositionType(Integer confId,Integer mosaicId,Integer comp,Integer size) throws XmlRpcException
    {
         //Create request
        Object[] request = new Object[]{confId,mosaicId,comp,size};
        //Log
        logger.log(level,"SetCompositionType({0},{1},{2},{3})",request);
        //Execute 
        HashMap response = (HashMap) client.execute("SetCompositionType", request);
        //Return 
        return (((Integer)response.get("returnCode"))==1);
    }
    
    public boolean SetMosaicSlot(Integer confId,Integer mosaicId,Integer num,Integer id) throws XmlRpcException
    {
         //Create request
        Object[] request = new Object[]{confId,mosaicId,num,id};
         //Log
        logger.log(level,"SetMosaicSlot({0},{1},{2},{3})",request);
        //Execute 
        HashMap response = (HashMap) client.execute("SetMosaicSlot", request);
        //Return 
        return (((Integer)response.get("returnCode"))==1);
    }

    public List<Integer> GetMosaicPositions(Integer confId, Integer mosaicId) throws XmlRpcException {
        //Create request
        Object[] request = new Object[]{confId,mosaicId};
        //Log
        logger.log(level,"GetMosaicPositions({0},{1})",request);
        //Execute
        HashMap response = (HashMap) client.execute("GetMosaicPositions", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Create list
        LinkedList<Integer> positions = new LinkedList<Integer>();
        //For each value in array
        for (int i=0;i<returnVal.length;i++)
            //Get position
            positions.add((Integer)returnVal[i]);
        //Return conference id
        return positions;
    }

    public boolean AddMosaicParticipant(Integer confId,Integer mosaicId,Integer partId) throws XmlRpcException
    {
         //Create request
        Object[] request = new Object[]{confId,mosaicId,partId};
        //Log
        logger.log(level,"AddMosaicParticipant({0},{1},{2},{3})",request);
        //Execute
        HashMap response = (HashMap) client.execute("AddMosaicParticipant", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean RemoveMosaicParticipant(Integer confId,Integer mosaicId,Integer partId) throws XmlRpcException
    {
         //Create request
        Object[] request = new Object[]{confId,mosaicId,partId};
        //Log
        logger.log(level,"RemoveMosaicParticipant({0},{1},{2})",request);
        //Execute
        HashMap response = (HashMap) client.execute("RemoveMosaicParticipant", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean AddSidebarParticipant(Integer confId,Integer sidebarId,Integer partId) throws XmlRpcException
    {
         //Create request
        Object[] request = new Object[]{confId,sidebarId,partId};
        //Log
        logger.log(level,"AddSidebarParticipant({0},{1},{2})",request);
        //Execute
        HashMap response = (HashMap) client.execute("AddSidebarParticipant", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean RemoveSidebarParticipant(Integer confId,Integer sidebarId,Integer partId) throws XmlRpcException
    {
         //Create request
        Object[] request = new Object[]{confId,sidebarId,partId};
        //Log
        logger.log(level,"RemoveSidebarParticipant({0},{1},{2},{3})",request);
        //Execute
        HashMap response = (HashMap) client.execute("RemoveSidebarParticipant", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean SetLocalSTUNCredentials(Integer confId,Integer partId,MediaType media,String username,String pwd) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,partId,media.valueOf(),username,pwd};
        //Log
        logger.log(level,"SetLocalSTUNCredentials({0},{1},{2},{3},{4})",request);
        //Execute
        HashMap response = (HashMap) client.execute("SetLocalSTUNCredentials", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
     
    public boolean SetRemoteSTUNCredentials(Integer confId,Integer partId,MediaType media,String username,String pwd) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,partId,media.valueOf(),username,pwd};
        //Log
        logger.log(level,"SetRemoteSTUNCredentials({0},{1},{2},{3},{4})",request);
        //Execute
        HashMap response = (HashMap) client.execute("SetRemoteSTUNCredentials", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }


    public boolean SetLocalCryptoSDES(Integer confId,Integer partId,MediaType media,String suite,String key) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,partId,media.valueOf(),suite,key};
        //Log
        logger.log(level,"SetLocalCryptoSDES({0},{1},{2},{3},{4})",request);
        //Execute
        HashMap response = (HashMap) client.execute("SetLocalCryptoSDES", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean SetRemoteCryptoSDES(Integer confId,Integer partId,MediaType media,String suite,String key) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,partId,media.valueOf(),suite,key};
        //Log
        logger.log(level,"SetRemoteCryptoSDES({0},{1},{2},{3},{4})",request);
        //Execute
        HashMap response = (HashMap) client.execute("SetRemoteCryptoSDES", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public String GetLocalCryptoDTLSFingerprint(String hash) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{hash};
        //Log
        logger.log(level,"GetLocalCryptoDTLSFingerprint({0})",request);
        //Execute
        HashMap response = (HashMap) client.execute("GetLocalCryptoDTLSFingerprint", request);
	//Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
	//Get result
        return (String) returnVal[0];
    }

    public boolean SetRemoteCryptoDTLS(Integer confId,Integer partId,MediaType media,Setup setup,String hash,String fingerprint) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,partId,media.valueOf(),setup.valueOf(),hash,fingerprint};
        //Log
        logger.log(level,"SetRemoteCryptoDTLS({0},{1},{2},{3},{4},{5})",request);
        //Execute
        HashMap response = (HashMap) client.execute("SetRemoteCryptoDTLS", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean SetRTPProperties(Integer confId,Integer partId,MediaType media, HashMap<String,String> properties) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,partId,media.valueOf(),properties};
        //Log
        logger.log(level,"SetRTPProperties({0},{1},{2},{3})",request);
        //Execute
        HashMap response = (HashMap) client.execute("SetRTPProperties", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean StartSending(Integer confId,Integer partId,MediaType media,String sendIp,Integer sendPort,HashMap<Integer,Integer> rtpMap) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,partId,media.valueOf(),sendIp,sendPort,rtpMap};
        //Log
        logger.log(level,"StartSending({0},{1},{2},{3},{4},{5})",request);
        //Execute
        HashMap response = (HashMap) client.execute("StartSending", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean StopSending(Integer confId,Integer partId,MediaType media) throws XmlRpcException
    {
       //Create request
        Object[] request = new Object[]{confId,partId,media.valueOf()};
        //Log
        logger.log(level,"StopSending({0},{1},{2})",request);
        //Execute
        HashMap response = (HashMap) client.execute("StopSending", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public Integer StartReceiving(Integer confId,Integer partId,MediaType media,HashMap<Integer,Integer> rtpMap) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,partId,media.valueOf(),rtpMap};
        //Log
        logger.log(level,"StartReceiving({0},{1},{2},{3},{4})",request);
        //Execute
        HashMap response = (HashMap) client.execute("StartReceiving", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return port
        return (Integer)returnVal[0];
    }

    public boolean StopReceiving(Integer confId,Integer partId,MediaType media) throws XmlRpcException
    {
       //Create request
        Object[] request = new Object[]{confId,partId,media.valueOf()};
        //Log
        logger.log(level,"StopReceiving({0},{1},{2},{3})",request);
        //Execute
        HashMap response = (HashMap) client.execute("StopReceiving", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    //Video
    @Deprecated
    public boolean SetVideoCodec(Integer confId,Integer partId,Integer codec,Integer mode,Integer fps,Integer bitrate,Integer quality, Integer fillLevel, Integer intraPeriod) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,partId,codec,mode,fps,bitrate,quality,fillLevel,intraPeriod};
        //Log
        logger.log(level,"SetVideoCodec({0},{1},{2},{3},{4},{5},{6},{7},{8})",request);
        //Execute 
        HashMap response = (HashMap) client.execute("SetVideoCodec", request);
        //Return 
        return (((Integer)response.get("returnCode"))==1);
    }

     //Video
    @Deprecated
    public boolean SetVideoCodec(Integer confId,Integer partId,Integer codec,Integer mode,Integer fps,Integer bitrate,Integer intraPeriod, HashMap<String,String> params) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,partId,codec,mode,fps,bitrate,intraPeriod,params};
        //Log
        logger.log(level,"SetVideoCodec({0},{1},{2},{3},{4},{5},{6},{7})",request);
        //Execute
        HashMap response = (HashMap) client.execute("SetVideoCodec", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    //Audio
    public boolean SetAudioCodec(Integer confId,Integer partId,Integer codec) throws XmlRpcException
    {
       //Create request
        Object[] request = new Object[]{confId,partId,codec};
        //Log
        logger.log(level,"SetAudioCodec({0},{1},{2})",request);
        //Execute 
        HashMap response = (HashMap) client.execute("SetAudioCodec", request);
        //Return 
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean SetAudioCodec(Integer confId,Integer partId,Integer codec,HashMap<String,String> params) throws XmlRpcException
    {
       //Create request
        Object[] request = new Object[]{confId,partId,codec,params};
        //Log
        logger.log(level,"SetAudioCodec({0},{1},{2},{3})",request);
        //Execute
        HashMap response = (HashMap) client.execute("SetAudioCodec", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
            
    //Text
    public boolean SetTextCodec(Integer confId,Integer partId,Integer codec) throws XmlRpcException
    {
       //Create request
        Object[] request = new Object[]{confId,partId,codec};
        //Log
        logger.log(level,"SetTextCodec({0},{1},{2})",request);
        //Execute
        HashMap response = (HashMap) client.execute("SetTextCodec", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean DeleteParticipant(Integer confId,Integer partId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,partId};
        //Log
        logger.log(level,"DeleteParticipant({0},{1})",request);
        //Execute 
        HashMap response = (HashMap) client.execute("DeleteParticipant", request);
        //Return 
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean StartBroadcaster(Integer confId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId};
        //Log
        logger.log(level,"StartBroadcaster({0})",request);
        //Execute
        HashMap response = (HashMap) client.execute("StartBroadcaster", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    
    public boolean StartBroadcaster(Integer confId, HashMap<String,String> properties) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,properties};
        //Log
        logger.log(level,"StartBroadcaster({0},{1})",request);
        //Execute
        HashMap response = (HashMap) client.execute("StartBroadcaster", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean StopBroadcaster(Integer confId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId};
        //Log
        logger.log(level,"StopBroadcaster({0})",request);
        //Execute 
        HashMap response = (HashMap) client.execute("StopBroadcaster", request);
        //Return 
        return (((Integer)response.get("returnCode"))==1);
    }

    public Integer StartPublishing(Integer confId,String server,Integer port,String app,String stream) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,server,port,app,stream};
        //Log
        logger.log(level,"StartPublishing({0},{1},{2},{3},{4})",request);
        //Execute
        HashMap response = (HashMap) client.execute("StartPublishing", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return port
        return (Integer)returnVal[0];
    }

    public boolean StopPublishing(Integer confId,Integer id) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,id};
        //Log
        logger.log(level,"StopPublishing({0},{1})",request);
        //Execute
        HashMap response = (HashMap) client.execute("StopPublishing", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    
	public boolean StartBrowsing(Integer confId, String url) throws XmlRpcException
	{
		//Create request
		Object[] request = new Object[]{confId,url};
		//Log
		logger.log(level,"StartBrowsing({0},{1}})",request);
		//Execute
		HashMap response = (HashMap) client.execute("StartBrowsing", request);
		//Return port
		return (((Integer)response.get("returnCode"))==1);
	}

	public boolean StopBrowsing(Integer confId) throws XmlRpcException {
		//Create request
		Object[] request = new Object[]{confId};
		//Log
		logger.log(level,"StopBrowsing({0}})",request);
		//Execute
		HashMap response = (HashMap) client.execute("StopBrowsing", request);
		//Return port
		return (((Integer)response.get("returnCode"))==1);
	}
    
    public boolean EndConference(Integer confId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId};
        //Log
        logger.log(level,"EndConference({0})",request);
        //Execute 
        HashMap response = (HashMap) client.execute("EndConference", request);
        //Return 
        return (((Integer)response.get("returnCode"))==1);
    }
    
    public boolean DeleteConference(Integer confId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId};
        //Log
        logger.log(level,"DeleteConference({0})",request);
        //Execute 
        HashMap response = (HashMap) client.execute("DeleteConference", request);
        //Return 
        return (((Integer)response.get("returnCode"))==1);
    }

    public void AddConferencetToken(Integer confId,String token) throws XmlRpcException
    {
         //Create request
        Object[] request = new Object[]{confId,token};
        //Log
        logger.log(level,"AddConferenceToken({0},{1})",request);
        //Execute
        HashMap response = (HashMap) client.execute("AddConferenceToken", request);
    }

    public void AddParticipantInputToken(Integer confId,Integer partId,String token) throws XmlRpcException
    {
         //Create request
        Object[] request = new Object[]{confId,partId,token};
        //Log
        logger.log(level,"AddParticipantInputToken({0},{1},{2})",request);
        //Execute
        HashMap response = (HashMap) client.execute("AddParticipantInputToken", request);
    }

    public void AddParticipantOutputToken(Integer confId,Integer partId,String token) throws XmlRpcException
    {
         //Create request
        Object[] request = new Object[]{confId,partId,token};
        //Log
        logger.log(level,"AddParticipantOutputToken({0},{1},{2})",request);
        //Execute
        HashMap response = (HashMap) client.execute("AddParticipantOutputToken", request);
    }

    public int CreatePlayer(Integer confId,Integer privateId,String name) throws XmlRpcException
    {
         //Create request
        Object[] request = new Object[]{confId,privateId,name};
        //Log
        logger.log(level,"CreatePlayer({0},{1},{2})",request);
        //Execute
        HashMap response = (HashMap) client.execute("CreatePlayer", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return part id
        return (Integer)returnVal[0];
    }

    public boolean DeletePlayer(Integer confId,int playerId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,playerId};
        //Log
        logger.log(level,"DeletePlayer({0},{1})",request);
        //Execute
        HashMap response = (HashMap) client.execute("DeletePlayer", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    
    public boolean StartPlaying(Integer confId,int playerId,String filename,int loop) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,playerId,filename,loop};
        //Log
        logger.log(level,"StartPlaying({0},{1},{2},{3})",request);
        //Execute
        HashMap response = (HashMap) client.execute("StartPlaying", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    public boolean StopPlaying(Integer confId,int playerId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,playerId};
        //Log
        logger.log(level,"StopPlaying({0},{1})",request);
        //Execute
        HashMap response = (HashMap) client.execute("StopPlaying", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean StartRecordingBroadcaster(Integer confId,String filename) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,filename};
        //Log
        logger.log(level,"StartRecordingBroadcaster({0},{1})",request);
        //Execute
        HashMap response = (HashMap) client.execute("StartRecordingBroadcaster", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean StopRecordingBroadcaster(Integer confId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId};
        //Log
        logger.log(level,"StopRecordingBroadcaster({0})",request);
        //Execute
        HashMap response = (HashMap) client.execute("StopRecordingBroadcaster", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean StartRecordingParticipant(Integer confId,int playerId,String filename) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,playerId,filename};
        //Log
        logger.log(level,"StartRecordingParticipant({0},{1},{2})",request);
        //Execute
        HashMap response = (HashMap) client.execute("StartRecordingParticipant", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean StopRecordingParticipant(Integer confId,int playerId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,playerId};
        //Log
        logger.log(level,"StopRecordingParticipant({0},{1})",request);
        //Execute
        HashMap response = (HashMap) client.execute("StopRecordingParticipant", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public void SetParticipantMosaic(Integer confId,Integer partId, Integer mosaicId) throws XmlRpcException {
        //Create request
        Object[] request = new Object[]{confId,partId,mosaicId};
        //Log
        logger.log(level,"SetParticipantMosaic({0},{1},{2})",request);
        //Execute
        HashMap response = (HashMap) client.execute("SetParticipantMosaic", request);
    }

    public void SetParticipantSidebar(Integer confId,Integer partId, Integer sidebarId) throws XmlRpcException {
        //Create request
        Object[] request = new Object[]{confId,partId,sidebarId};
        //Log
        logger.log(level,"SetParticipantSidebar({0},{1},{2})",request);
        //Execute
        HashMap response = (HashMap) client.execute("SetParticipantSidebar", request);
    }

    public boolean SetMute(Integer confId,int partId,Codecs.MediaType media,boolean isMuted) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,partId,media.valueOf(),isMuted?1:0};
        //Log
        logger.log(level,"SetMute({0},{1},{2},{3})",request);
        //Execute
        HashMap response = (HashMap) client.execute("SetMute", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

     public boolean SetChair(Integer confId,int partId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,partId};
        //Log
        logger.log(level,"SetChair({0},{1}})",request);
        //Execute
        HashMap response = (HashMap) client.execute("SetChair", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
     
    public boolean SetAppMixerViewer(Integer confId,Integer partId)  throws XmlRpcException {
        //Create request
        Object[] request = new Object[]{confId,partId};
        //Log
        logger.log(level,"SetAppMixerViewer({0},{1}})",request);
        //Execute
        HashMap response = (HashMap) client.execute("SetAppMixerViewer", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean SendFPU(Integer confId,int partId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{confId,partId};
        //Log
        logger.log(level,"SendFPU({0},{1})",request);
        //Execute
        HashMap response = (HashMap) client.execute("SendFPU", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public Map<String,MediaStatistics> getParticipantStatistics(Integer confId, Integer partId) throws XmlRpcException {
        //Create request
        Object[] request = new Object[]{confId,partId};
        //Log
        logger.log(level,"GetParticipantStatistics({0},{1})",request);
        //Execute
        HashMap response = (HashMap) client.execute("GetParticipantStatistics", request);
	//Check it is ok
	if (((Integer)response.get("returnCode"))!=1)
		//Error
		return null;
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Create map
        HashMap<String,MediaStatistics> partStats = new HashMap<String, MediaStatistics>();
        //For each value in array
        for (int i=0;i<returnVal.length;i++)
        {
            //Get array
             Object[] arr = (Object[]) returnVal[i];
             //Get media
             String media = (String)arr[0];
             //Create stats
             MediaStatistics stats = new MediaStatistics();
             //Fill values
             stats.isReceiving      = ((Integer)arr[1])==1;
             stats.isSending        = ((Integer)arr[2])==1;
             stats.lostRecvPackets  = (Integer)arr[3];
             stats.numRecvPackets   = (Integer)arr[4];
             stats.numSendPackets   = (Integer)arr[5];
             stats.totalRecvBytes   = (Integer)arr[6];
             stats.totalSendBytes   = (Integer)arr[7];
             //Add it
             partStats.put(media, stats);
        }
        //Return conference id
        return partStats;
    }
    
    public int EventQueueCreate() throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{};
        //Log
        logger.log(level,"EventQueueCreate()",request);
        //Execute
        HashMap response = (HashMap) client.execute("EventQueueCreate", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return part id
        return (Integer)returnVal[0];
    }

    public boolean EventQueueDelete(int queueId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{queueId};
        //Log
        logger.log(level,"EventQueueDelete({0})",request);
        //Execute
        HashMap response = (HashMap) client.execute("EventQueueDelete", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
}
