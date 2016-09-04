/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.MediaServer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.TimingOutCallback;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

/**
 *
 * @author Sergio
 */
public class XmlRpcTimedClient extends XmlRpcClient {
    private final static int XML_RPC_TIMEOUT = 10000;
    private int timeout = XML_RPC_TIMEOUT;
    private static final Logger logger = Logger.getLogger("XMLRPCMCU");
    private static final Level level = Level.FINE;
    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public Object execute(String pMethodName, Object[] pParams) throws XmlRpcException {
        try {
	    long ini = System.currentTimeMillis();
            //Create timed out callback
            TimingOutCallback callback = new TimingOutCallback(timeout);
            //Execute async
            executeAsync(pMethodName, pParams, callback);
	    //Wait for response
	    Object res = callback.waitForResponse();
	    //Log time
	    logger.log(level,"executed " + pMethodName + " method in "+(System.currentTimeMillis()-ini) + " ms ["+((XmlRpcClientConfigImpl)this.getClientConfig()).getServerURL()+"]");
            //Return obcjet
            return res;
        } catch (Throwable ex) {
            //Launc exception
            throw new XmlRpcException("Async execution error " +ex.getMessage(), ex);
        }
    }
}
