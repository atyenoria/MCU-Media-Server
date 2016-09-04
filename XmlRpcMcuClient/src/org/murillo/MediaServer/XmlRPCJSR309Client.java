/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.MediaServer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.murillo.MediaServer.Codecs.MediaType;

/**
 *
 * @author Sergio
 */
public class XmlRPCJSR309Client {

    private XmlRpcTimedClient client;
    private XmlRpcClientConfigImpl config;

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

    /** Creates a new instance of XmlRpcMcuClient */
    public XmlRPCJSR309Client(String  url) throws MalformedURLException
    {
        config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(url));
        client = new XmlRpcTimedClient();
        client.setConfig(config);
    }

    public int EventQueueCreate() throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{};
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
        //Execute
        HashMap response = (HashMap) client.execute("EventQueueDelete", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public int MediaSessionCreate(String name,Integer queueId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{name,queueId};
        //Execute
        HashMap response = (HashMap) client.execute("MediaSessionCreate", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return part id
        return (Integer)returnVal[0];
    }

    public boolean MediaSessionDelete(int sessId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId};
        //Execute
        HashMap response = (HashMap) client.execute("MediaSessionDelete", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    //Player management
    public Integer PlayerCreate(int sessId,String name) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,name};
        //Execute
        HashMap response = (HashMap) client.execute("PlayerCreate", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return part id
        return (Integer)returnVal[0];
    }
    
    public boolean PlayerDelete(int sessId,int playerId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,playerId};
        //Execute
        HashMap response = (HashMap) client.execute("PlayerDelete", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    //Player functionality
    public boolean PlayerOpen(int sessId,int playerId,String filename) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,playerId,filename};
        //Execute
        HashMap response = (HashMap) client.execute("PlayerOpen", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    
    public boolean PlayerPlay(int sessId,int playerId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,playerId};
        //Execute
        HashMap response = (HashMap) client.execute("PlayerPlay", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    
    public boolean PlayerSeek(int sessId,int playerId,int time) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,playerId,time};
        //Execute
        HashMap response = (HashMap) client.execute("PlayerSeek", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    
    public boolean PlayerStop(int sessId,int playerId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,playerId};
        //Execute
        HashMap response = (HashMap) client.execute("PlayerStop", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    
    public boolean PlayerClose(int sessId,int playerId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,playerId};
        //Execute
        HashMap response = (HashMap) client.execute("PlayerClose", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    //Recorder management
    public Integer RecorderCreate(int sessId,String name) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,name};
        //Execute
        HashMap response = (HashMap) client.execute("RecorderCreate", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return part id
        return (Integer)returnVal[0];
    }

    public boolean RecorderDelete(int sessId,int recorderId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,recorderId};
        //Execute
        HashMap response = (HashMap) client.execute("RecorderDelete", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    //Recorder functionality
    public boolean RecorderRecord(int sessId,int recorderId,String filename) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,recorderId,filename};
        //Execute
        HashMap response = (HashMap) client.execute("RecorderRecord", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean RecorderStop(int sessId,int recorderId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,recorderId};
        //Execute
        HashMap response = (HashMap) client.execute("RecorderStop", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean RecorderAttachToEndpoint(int sessId,int recorderId,int endpointId,Codecs.MediaType media) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,recorderId,endpointId,media.valueOf()};
        //Execute
        HashMap response = (HashMap) client.execute("RecorderAttachToEndpoint", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean RecorderAttachToAudioMixerPort(int sessId,int recorderId,int mixerId,int portId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,recorderId,mixerId,portId};
        //Execute
        HashMap response = (HashMap) client.execute("RecorderAttachToAudioMixerPort", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean RecorderAttachToVideoMixerPort(int sessId,int recorderId,int mixerId,int portId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,recorderId,mixerId,portId};
        //Execute
        HashMap response = (HashMap) client.execute("RecorderAttachToVideoMixerPort", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean RecorderDettach(int sessId,int recorderId,Codecs.MediaType media) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,recorderId,media.valueOf()};
        //Execute
        HashMap response = (HashMap) client.execute("RecorderDettach", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    //Endpoint management
    public Integer EndpointCreate(int sessId,String name,boolean audioSupported,boolean videoSupported,boolean textSupported) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,name,audioSupported,videoSupported,textSupported};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointCreate", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return part id
        return (Integer)returnVal[0];
    }

    public boolean EndpointDelete(int sessId,int endpointId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,endpointId};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointDelete", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean EndpointSetLocalSTUNCredentials(Integer sessId,Integer endpointId,MediaType media,String username,String pwd) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,endpointId,media.valueOf(),username,pwd};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointSetLocalSTUNCredentials", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean EndpointSetRemoteSTUNCredentials(Integer sessId,Integer endpointId,MediaType media,String username,String pwd) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,endpointId,media.valueOf(),username,pwd};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointSetRemoteSTUNCredentials", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean EndpointSetLocalCryptoSDES(Integer sessId,Integer endpointId,MediaType media,String suite,String key) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,endpointId,media.valueOf(),suite,key};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointSetLocalCryptoSDES", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean EndpointSetRemoteCryptoSDES(Integer sessId,Integer endpointId,MediaType media,String suite,String key) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,endpointId,media.valueOf(),suite,key};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointSetRemoteCryptoSDES", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public String EndpointGetLocalCryptoDTLSFingerprint(String hash) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{hash};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointGetLocalCryptoDTLSFingerprint", request);
	//Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
	//Get result
        return (String) returnVal[0];
    }

    public boolean EndpointSetRemoteCryptoDTLS(Integer sessId,Integer endpointId,MediaType media,Codecs.Setup setup,String hash,String fingerprint) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,endpointId,media.valueOf(),setup.valueOf(),hash,fingerprint};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointSetRemoteCryptoDTLS", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    
    public boolean EndpointSetRTPProperties(Integer sessId,Integer endpointId,MediaType media, HashMap<String,String> properties) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,endpointId,media.valueOf(),properties};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointSetRTPProperties", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean EndpointStartSending(int sessId,int endpointId,Codecs.MediaType media,String sendIp,int sendPort,HashMap<Integer,Integer> rtpMap) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,endpointId,media.valueOf(),sendIp,sendPort,rtpMap};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointStartSending", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    
    public boolean EndpointStopSending(int sessId,int endpointId,Codecs.MediaType media) throws XmlRpcException
    {
       //Create request
        Object[] request = new Object[]{sessId,endpointId,media.valueOf()};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointStopSending", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    
    public Integer EndpointStartReceiving(int sessId,int endpointId,Codecs.MediaType media,HashMap<Integer,Integer> rtpMap) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,endpointId,media.valueOf(),rtpMap};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointStartReceiving", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return port
        return (Integer)returnVal[0];
    }
    
    public boolean EndpointStopReceiving(int sessId,int endpointId,Codecs.MediaType media) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,endpointId,media.valueOf()};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointStopReceiving", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
   
    public boolean EndpointRequestUpdate(int sessId,int endpointId,Codecs.MediaType media) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,endpointId,media.valueOf()};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointRequestUpdate", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    //Attach intput to
    public boolean EndpointAttachToPlayer(int sessId,int endpointId,int playerId,Codecs.MediaType media) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,endpointId,playerId,media.valueOf()};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointAttachToPlayer", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    public boolean EndpointAttachToEndpoint(int sessId,int endpointId,int sourceId,Codecs.MediaType media) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,endpointId,sourceId,media.valueOf()};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointAttachToEndpoint", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean EndpointAttachToAudioMixerPort(int sessId,int endpointId,int mixerId,int portId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,endpointId,mixerId,portId};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointAttachToAudioMixerPort", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean EndpointAttachToVideoMixerPort(int sessId,int endpointId,int mixerId,int portId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,endpointId,mixerId,portId};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointAttachToVideoMixerPort", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean EndpointAttachToVideoTranscoder(int sessId,int endpointId,int videoTranscodeId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,endpointId,videoTranscodeId};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointAttachToVideoTranscoder", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean EndpointDettach(int sessId,int endpointId,Codecs.MediaType media) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,endpointId,media.valueOf()};
        //Execute
        HashMap response = (HashMap) client.execute("EndpointDettach", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    
    public Integer AudioMixerCreate(int sessId,String name) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,name,};
        //Execute
        HashMap response = (HashMap) client.execute("AudioMixerCreate", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return part id
        return (Integer)returnVal[0];
    }

    public boolean AudioMixerDelete(int sessId,int mixerId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,mixerId};
        //Execute
        HashMap response = (HashMap) client.execute("AudioMixerDelete", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    //AudioMixer port management
    public Integer AudioMixerPortCreate(int sessId,int mixerId,String name) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,name};
        //Execute
        HashMap response = (HashMap) client.execute("AudioMixerPortCreate", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return part id
        return (Integer)returnVal[0];
    }
    
    public boolean AudioMixerPortSetCodec(int sessId,int mixerId,int portId,int codec) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,portId,codec};
        //Execute
        HashMap response = (HashMap) client.execute("AudioMixerPortSetCodec", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean AudioMixerPortDelete(int sessId,int mixerId,int portId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,portId};
        //Execute
        HashMap response = (HashMap) client.execute("AudioMixerPortDelete", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean AudioMixerPortAttachToEndpoint(int sessId,int mixerId,int portId,int endpointId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,portId,endpointId};
        //Execute
        HashMap response = (HashMap) client.execute("AudioMixerPortAttachToEndpoint", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean AudioMixerPortAttachToPlayer(int sessId,int mixerId,int portId,int playerId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,portId,playerId};
        //Execute
        HashMap response = (HashMap) client.execute("AudioMixerPortAttachToPlayer", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean AudioMixerPortDettach(int sessId,int mixerId,int portId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,portId};
        //Execute
        HashMap response = (HashMap) client.execute("AudioMixerPortDettach", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public Integer VideoMixerCreate(int sessId,String tag) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,tag};
        //Execute
        HashMap response = (HashMap) client.execute("VideoMixerCreate", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return part id
        return (Integer)returnVal[0];
    }
    
    public boolean VideoMixerDelete(int sessId,int mixerId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{mixerId};
        //Execute
        HashMap response = (HashMap) client.execute("VideoMixerDelete", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    
    public Integer VideoMixerPortCreate(int sessId,int mixerId,String tag,int mosiacId) throws XmlRpcException
        {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,tag,mosiacId};
        //Execute
        HashMap response = (HashMap) client.execute("VideoMixerPortCreate", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return part id
        return (Integer)returnVal[0];
    }

    public boolean VideoMixerPortSetCodec(int sessId,int mixerId,int portId,Integer codec,int size,int fps,int bitrate,int intraPeriod,HashMap<String,String> properties) throws XmlRpcException
        {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,portId,codec,size,fps,bitrate,intraPeriod,properties};
        //Execute
        HashMap response = (HashMap) client.execute("VideoMixerPortSetCodec", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean VideoMixerPortDelete(int sessId,int mixerId,int portId) throws XmlRpcException
            {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,portId};
        //Execute
        HashMap response = (HashMap) client.execute("VideoMixerPortDelete", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

   public boolean VideoMixerPortAttachToEndpoint(int sessId,int mixerId,int portId,int endpointId) throws XmlRpcException
           {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,portId,endpointId};
        //Execute
        HashMap response = (HashMap) client.execute("VideoMixerPortAttachToEndpoint", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    public boolean VideoMixerPortAttachToPlayer(int sessId,int mixerId,int portId,int playerId) throws XmlRpcException
           {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,portId,playerId};
        //Execute
        HashMap response = (HashMap) client.execute("VideoMixerPortAttachToPlayer", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean VideoMixerPortDettach(int sessId,int mixerId,int portId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,portId};
        //Execute
        HashMap response = (HashMap) client.execute("VideoMixerPortDettach", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public Integer VideoMixerMosaicCreate(int sessId,int mixerId,int comp,int size) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,comp,size};
        //Execute
        HashMap response = (HashMap) client.execute("VideoMixerMosaicCreate", request);
         //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return part id
        return (Integer)returnVal[0];
    }

    public boolean VideoMixerMosaicDelete(int sessId,int mixerId,int portId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,portId};
        //Execute
        HashMap response = (HashMap) client.execute("VideoMixerMosaicDelete", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean VideoMixerMosaicSetSlot(int sessId,int mixerId,int mosaicId,int num,int portId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,mosaicId,num,portId};
        //Execute
        HashMap response = (HashMap) client.execute("VideoMixerMosaicSetSlot", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean VideoMixerMosaicSetCompositionType(int sessId,int mixerId,int mosaicId,int comp,int size) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,mosaicId,comp,size};
        //Execute
        HashMap response = (HashMap) client.execute("VideoMixerMosaicSetCompositionType", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean VideoMixerMosaicSetOverlayPNG(int sessId,int mixerId,int mosaicId,String overlay) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,mosaicId,overlay};
        //Execute
        HashMap response = (HashMap) client.execute("VideoMixerMosaicSetOverlayPNG", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean VideoMixerMosaicResetOverlay(int sessId,int mixerId,int mosaicId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,mosaicId};
        //Execute
        HashMap response = (HashMap) client.execute("VideoMixerMosaicResetOverlay", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean VideoMixerMosaicAddPort(int sessId,int mixerId,int mosaicId,int portId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,mosaicId,portId};
        //Execute
        HashMap response = (HashMap) client.execute("VideoMixerMosaicAddPort", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean VideoMixerMosaicRemovePort(int sessId,int mixerId,int mosaicId,int portId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,mixerId,mosaicId,portId};
        //Execute
        HashMap response = (HashMap) client.execute("VideoMixerMosaicRemovePort", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public Integer VideoTranscoderCreate(int sessId,String tag) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,tag};
        //Execute
        HashMap response = (HashMap) client.execute("VideoTranscoderCreate", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return part id
        return (Integer)returnVal[0];
    }

    public boolean VideoTranscoderDelete(int sessId,int videoTranscoderId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,videoTranscoderId};
        //Execute
        HashMap response = (HashMap) client.execute("VideoTranscoderDelete", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean VideoTranscoderSetCodec(int sessId,int videoTranscoderId,Integer codec,int size,int fps,int bitrate,int intraPeriod,HashMap<String,String> properties) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,videoTranscoderId,codec,size,fps,bitrate,intraPeriod,properties};
        //Execute
        HashMap response = (HashMap) client.execute("VideoTranscoderSetCodec", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    
    public boolean VideoTranscoderFPU(Integer sessId, Integer videoTranscoderId) throws XmlRpcException {
        //Create request
        Object[] request = new Object[]{sessId,videoTranscoderId};
        //Execute
        HashMap response = (HashMap) client.execute("VideoTranscoderFPU", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }

    public boolean VideoTranscoderAttachToEndpoint(int sessId,int videoTranscoderId,int endpointId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,videoTranscoderId,endpointId};
        //Execute
        HashMap response = (HashMap) client.execute("VideoTranscoderAttachToEndpoint", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
    
    public boolean VideoTranscoderDettach(int sessId,int videoTranscoderId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessId,videoTranscoderId};
        //Execute
        HashMap response = (HashMap) client.execute("VideoTranscoderDettach", request);
        //Return
        return (((Integer)response.get("returnCode"))==1);
    }
}
