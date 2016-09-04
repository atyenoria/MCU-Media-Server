/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.MediaServer;


import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.parser.XmlRpcResponseParser;
import org.apache.xmlrpc.util.SAXParsers;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author Sergio
 */
public class XmlRpcEventManager {

    XmlRpcTimedClient controller;
    private InputStream is;
    private Socket socket;
    private PrintWriter writer;
    private boolean canceled;
    private boolean connected;



    public interface Listener {
        void onConnect();
        void onError();
        void onDisconnect();

        public void onEvent(Object result);
    }

    public XmlRpcEventManager()
    {
        //Create controller for retreiving default config
        controller = new XmlRpcTimedClient();
	//not connected yet
	connected = false;
    }

    public void Connect(String endpoint,Listener listener) throws MalformedURLException
    {
        int len;
        byte[] buffer = new byte[4096];
        //Not canceled
        canceled = false;
       //Create url connection with the event endpoing
        URL url = new URL(endpoint);
        //Check protocol
        if (!url.getProtocol().equalsIgnoreCase("http")) {
            //Error
            throw new MalformedURLException("Unsuported protocol");
        }
        //Create new socekt
        socket = new Socket();
        //Get host
        String host = url.getHost();
        //Get por
        int port = url.getPort();
        //Check if it is set
        if (port == -1) {
            //Default
            port = 80;
        }
        
       try {
            socket.connect(new InetSocketAddress(host, port));
            //Create request
            String req = "POST " + url.getPath() + " HTTP/1.1\r\n\r\n";
            //Get input stream
            is = socket.getInputStream();
            //Get output stream
            writer = new PrintWriter(socket.getOutputStream());
            //Write request
            writer.print(req);
            //Send it
            writer.flush();
            int count = 0;
            int chunkSize = 0;
            int chunkLen = 0;
            boolean parsingResponseCode = true;
            boolean parsingHeaders = true;
            boolean parsingChunkHeader = false;
            boolean parsingChunk = false;
            StringBuffer line = new StringBuffer();
            StringBuffer strLen = new StringBuffer();
            String chunk = new String();
            //Read each chunk
            while ((len = is.read(buffer, 0, 4096)) > 0) {
                //Start parsing from the begining
                int ini = 0;
                //Consume all input
                while (ini < len) {
                    //If parsing headers
                    if (parsingHeaders) {
                        //Find end of headers
                        for (; ini < len; ini++) {
                            //Increase line length
                            count++;
                            //check if it is a \n
                            if (buffer[ini] == 10) {
                                //Remove \r
                                String header = line.substring(0, line.length() - 1);
                                //If we have not parsed the response code
                                if (parsingResponseCode) {
                                    //Not parsing it anymore
                                    parsingResponseCode = false;
                                    //Get response code
                                    Integer code = Integer.parseInt(header.substring(9, 12));
                                    //Check it
                                    if (code >= 200 && code < 300) {
                                        //We are connected
					connected = true;
					//Launch listener
                                        listener.onConnect();
                                    } else {
                                        //Not connected
					connected = false;
					//Launch listener
                                        listener.onError();
                                        //exit
                                        return;
                                    }
                                }
                                line = new StringBuffer();
                                //Empty line??
                                if (count == 2) {
                                    //Skip it
                                    ini++;
                                    //Found header
                                    parsingHeaders = false;
                                    //Set the start of the chunk
                                    parsingChunkHeader = true;
                                    //We are connected
				    connected = true;
				    //Launch listener
				    listener.onConnect();
                                    //Exit loop
                                    break;
                                } else {
                                    //new line
                                    count = 0;
                                }
                            } else {
                                //Append to lengt
                                line.append((char) buffer[ini]);
                            }
                        }
                    }
                    if (parsingChunkHeader) {
                        //Find end of headers
                        for (; ini < len; ini++) {
                            //check if it is a \n
                            if (buffer[ini] == 10) {
                                //Remove \r
                                String str = strLen.substring(0, strLen.length() - 1);
                                //Skip it
                                ini++;
                                //Stop parsing chunk header
                                parsingChunkHeader = false;
                                //Parse chunk
                                parsingChunk = true;
                                //Clear chunk
                                chunk = new String();
                                //Exit loop
                                chunkSize = Integer.parseInt(str, 16) + 2;
                                //And start parsing it
                                chunkLen = 0;
                                //Exti loop
                                break;
                            } else {
                                //Append to lengt
                                strLen.append((char) buffer[ini]);
                            }
                        }
                    }
                    if (parsingChunk) {
                        //Amount to copy
                        int copy = len - ini;
                        //Check remaining in the buffer
                        if (chunkLen + copy > chunkSize) {
                            //Copy only what is needed
                            copy = chunkSize - chunkLen;
                        }
                        chunk += new String(buffer, ini, copy);
                        //Increase ini
                        ini += copy;
                        //Increase length
                        chunkLen += copy;
                        //Check if we have a full chunk
                        if (chunkLen == chunkSize) {
                            //Get event
                            String event = chunk.substring(0, chunkLen - 2);
                            //If not keep alive
                            if (!event.isEmpty() && !event.equals("\r\n")) {
                                //Parse chunk
                                ParseChunk(event, listener);
                            }
                            parsingChunkHeader = true;
                            //Not parse chunk
                            parsingChunk = false;
                            //Empty header
                            strLen = new StringBuffer();
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(XmlRpcEventManager.class.getName()).log(Level.SEVERE,"Exception when reading event http connection:{0}", ex.getMessage());
        }
        //trye to close everything anyway
        Close();
	//We are not connected anymore
	connected = false;
	//Check if we have been canceled
        if (canceled)
            //We are disconnected
            listener.onDisconnect();
        else
            //Error
            listener.onError();
    }


    private void ParseChunk(String chunk,Listener listener)
    {
        try {
            //Get the input
            InputSource isource = new InputSource(new StringReader(chunk));
            //Create reader
            XMLReader xr = SAXParsers.newXMLReader();
            //Create response parser
            XmlRpcResponseParser xp = new XmlRpcResponseParser( new XmlRpcStreamRequestConfig() {
                public boolean isGzipCompressing() {
                    return false;
                }
                public boolean isGzipRequesting() {
                    return false;
                }
                public boolean isEnabledForExceptions() {
                    return false;
                }
                public String getEncoding() {
                    return "UTF-8";
                }
                public boolean isEnabledForExtensions() {
                     return false;
                }
                public TimeZone getTimeZone() {
                     return TimeZone.getDefault();
                }
            },controller.getTypeFactory());
            //Set the cntent handler
            xr.setContentHandler(xp);
            //parse source
            xr.parse(isource);
            //If it is a success event
            if (xp.isSuccess())
                //Call event
                listener.onEvent(xp.getResult());
        } catch (SAXException e) {
            Logger.getLogger(XmlRpcEventManager.class.getName()).log(Level.SEVERE, "Chunk: "+chunk, e);
        } catch (IOException e) {
            Logger.getLogger(XmlRpcEventManager.class.getName()).log(Level.SEVERE, null, e);
        } catch (XmlRpcException e) {
            Logger.getLogger(XmlRpcEventManager.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void Cancel()
    {
        //We have been canceled
        canceled = true;
        //Close
        Close();
    }

    public void Close()
    {
        //Close input stream
        try { is.close();       } catch (Exception e) {}
        //Close writter
        try { writer.close();   } catch (Exception e) {}
        //Close socket
        try { socket.close();   } catch (Exception e) {}
        //Nullify
        is = null;
        writer = null;
        socket = null;
    }

    public boolean isConnected() {
	return connected;
    }

}
