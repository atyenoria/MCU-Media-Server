/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.MediaServer;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sergio Garcia Murillo
 */
public class XmlRpcMediaGatewayClient {
    
    public static final Integer QCIF    = 0;
    public static final Integer CIF     = 1;
    public static final Integer VGA     = 2;
    public static final Integer PAL     = 3;
    public static final Integer HVGA    = 4;
    public static final Integer QVGA    = 5;
    public static final Integer HD720P  = 6;
    public static final Integer w448P   = 8;	// 768  x 448
    public static final Integer sd448P  = 9;	// 576  x 448
    public static final Integer w288P   = 10;	// 512  x 288
    public static final Integer w576    = 11;	// 1024 x 576
    public static final Integer FOURCIF = 12;	// 704  x 576
    public static final Integer FOURSIF = 13;	// 704  x 576
    public static final Integer XGA     = 14;	// 1024 x 768

    private XmlRpcTimedClient client;
    private XmlRpcClientConfigImpl config;

    /** Creates a new instance of XmlRpcMcuClient */
    public XmlRpcMediaGatewayClient(String  url) throws MalformedURLException
    {
        config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(url));
        client = new XmlRpcTimedClient();
        client.setConfig(config);
    }

    public Integer CreateMediaBridge(String name) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{name};
        //Execute
        HashMap response = (HashMap) client.execute("CreateMediaBridge", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return
        return (Integer)returnVal[0];
    }

    public void SetMediaBridgeInputToken(Integer sessionId,String token) throws XmlRpcException
    {
         //Create request
        Object[] request = new Object[]{sessionId,token};
        //Execute
        HashMap response = (HashMap) client.execute("SetMediaBridgeInputToken", request);
    }

    public void SetMediaBridgeOutputToken(Integer sessionId,String token) throws XmlRpcException
    {
         //Create request
        Object[] request = new Object[]{sessionId,token};
        //Execute
        HashMap response = (HashMap) client.execute("SetMediaBridgeOutputToken", request);
    }

    public boolean StartSendingVideo(Integer sessionId,String sendVideoIp,Integer sendVideoPort,HashMap<Integer,Integer> rtpMap) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessionId,sendVideoIp,sendVideoPort,rtpMap};
        //Execute
        HashMap response = (HashMap) client.execute("StartSendingVideo", request);
        //Return
        return true;
    }

    public boolean StopSendingVideo(Integer sessionId) throws XmlRpcException
    {
       //Create request
        Object[] request = new Object[]{sessionId};
        //Execute
        HashMap response = (HashMap) client.execute("StopSendingVideo", request);
        //Return
        return true;
    }

    public Integer StartReceivingVideo(Integer sessionId,HashMap<Integer,Integer> rtpMap) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessionId,rtpMap};
        //Execute
        HashMap response = (HashMap) client.execute("StartReceivingVideo", request);
        //Get result
        Object[] returnVal = (Object[]) response.get(new String("returnVal"));
        //Return port
        return (Integer)returnVal[0];
    }

    public boolean StopReceivingVideo(Integer sessionId) throws XmlRpcException
    {
       //Create request
        Object[] request = new Object[]{sessionId};
        //Execute
        HashMap response = (HashMap) client.execute("StopReceivingVideo", request);
        //Return
        return true;
    }

    public boolean StartSendingAudio(Integer sessionId,String sendAudioIp,Integer sendAudioPort,HashMap<Integer,Integer> rtpMap) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessionId,sendAudioIp,sendAudioPort,rtpMap};
        //Execute
        HashMap response = (HashMap) client.execute("StartSendingAudio", request);
        //Return
        return true;
    }

    public boolean StopSendingAudio(Integer sessionId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessionId};
        //Execute
        HashMap response = (HashMap) client.execute("StopSendingAudio", request);
        //Return
        return true;
    }

    public Integer StartReceivingAudio(Integer sessionId,Map<Integer,Integer> rtpMap) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessionId,rtpMap};
        //Execute
        HashMap response = (HashMap) client.execute("StartReceivingAudio", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return port
        return (Integer)returnVal[0];
    }

    public boolean StopReceivingAudio(Integer sessionId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessionId};
        //Execute
        HashMap response = (HashMap) client.execute("StopReceivingAudio", request);
        //Return,
        return true;
    }

     public boolean StartSendingText(Integer sessionId,String sendTextIp,Integer sendTextPort,HashMap<Integer,Integer> rtpMap) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessionId,sendTextIp,sendTextPort,rtpMap};
        //Execute
        HashMap response = (HashMap) client.execute("StartSendingText", request);
        //Return
        return true;
    }

    public boolean StopSendingText(Integer sessionId) throws XmlRpcException
    {
       //Create request
        Object[] request = new Object[]{sessionId};
        //Execute
        HashMap response = (HashMap) client.execute("StopSendingText", request);
        //Return
        return true;
    }

    public Integer StartReceivingText(Integer sessionId,HashMap<Integer,Integer> rtpMap) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessionId,rtpMap};
        //Execute
        HashMap response = (HashMap) client.execute("StartReceivingText", request);
        //Get result
        Object[] returnVal = (Object[]) response.get(new String("returnVal"));
        //Return port
        return (Integer)returnVal[0];
    }

    public boolean StopReceivingText(Integer sessionId) throws XmlRpcException
    {
       //Create request
        Object[] request = new Object[]{sessionId};
        //Execute
        HashMap response = (HashMap) client.execute("StopReceivingText", request);
        //Return
        return true;
    }

    public boolean SendFPU(Integer sessionId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessionId};
        //Execute
        HashMap response = (HashMap) client.execute("SendFPU", request);
        //Return
        return true;
    }

    public void DeleteMediaBridge(Integer sessionId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{sessionId};
        //Execute
        HashMap response = (HashMap) client.execute("DeleteMediaBridge", request);
    }
}
