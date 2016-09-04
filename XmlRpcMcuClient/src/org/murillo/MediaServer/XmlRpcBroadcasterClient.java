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
import java.util.Map;
import org.murillo.MediaServer.XmlRpcMcuClient.ConferenceInfo;

/**
 *
 * @author Sergio Garcia Murillo
 */
public class XmlRpcBroadcasterClient {

    private XmlRpcTimedClient client;
    private XmlRpcClientConfigImpl config;

    public class BroadcastStreamInfo {
        public String name;
        public String url;
        public String publishedIp;
    }

    /** Creates a new instance of XmlRpcMcuClient */
    public XmlRpcBroadcasterClient(String  url) throws MalformedURLException
    {
        config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(url));
        client = new XmlRpcTimedClient();
        client.setConfig(config);
    }

    public Integer CreateBroadcast(String name,String tag,Integer maxTransfer,Integer maxConcurrent) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{name,tag,maxTransfer,maxConcurrent};
        //Execute
        HashMap response = (HashMap) client.execute("CreateBroadcast", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Return
        return (Integer)returnVal[0];
    }

    public void PublishBroadcast(Integer broadcastId,String pin) throws XmlRpcException
    {
         //Create request
        Object[] request = new Object[]{broadcastId,pin};
        //Execute
        HashMap response = (HashMap) client.execute("PublishBroadcast", request);
    }

    public void UnPublishBroadcast(Integer broadcastId) throws XmlRpcException
    {
         //Create request
        Object[] request = new Object[]{broadcastId};
        //Execute
        HashMap response = (HashMap) client.execute("UnPublishBroadcast", request);
    }

    public void AddBroadcastToken(Integer broadcastId,String token) throws XmlRpcException
    {
         //Create request
        Object[] request = new Object[]{broadcastId,token};
        //Execute
        HashMap response = (HashMap) client.execute("AddBroadcastToken", request);
    }

    public void DeleteBroadcast(Integer broadcastId) throws XmlRpcException
    {
        //Create request
        Object[] request = new Object[]{broadcastId};
        //Execute
        HashMap response = (HashMap) client.execute("DeleteBroadcast", request);
    }

    public Map<String,BroadcastStreamInfo> getBroadcastPublishedStreams(Integer broadcastId) throws XmlRpcException {
        //Create request
        Object[] request = new Object[]{broadcastId};
        //Execute
        HashMap response = (HashMap) client.execute("GetBroadcastPublishedStreams", request);
        //Get result
        Object[] returnVal = (Object[]) response.get("returnVal");
        //Create map
        HashMap<String,BroadcastStreamInfo> streams = new HashMap<String, BroadcastStreamInfo>(returnVal.length);
        //For each value in array
        for (int i=0;i<returnVal.length;i++)
        {
            //Get array
             Object[] arr = (Object[]) returnVal[i];
             //Get id
             String name = (String)arr[0];
             //Create info
             BroadcastStreamInfo info = new BroadcastStreamInfo();
             //Fill values
             info.name          = (String)arr[0];
             info.url           = (String)arr[1];
             info.publishedIp   = (String)arr[2];
             //Add it
             streams.put(name, info);
        }
        //Return conference list
        return streams;
    }

}
