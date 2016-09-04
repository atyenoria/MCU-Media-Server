/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.spi;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.MsControlFactory;
import javax.media.mscontrol.spi.Driver;
import javax.media.mscontrol.spi.PropertyInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.murillo.mscontrol.MSControlFactoryImpl;
import org.murillo.mscontrol.MediaServer;
import org.murillo.mscontrol.MediaSessionImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Sergio
 */
public class DriverImpl implements Driver {
    private static final Logger logger = Logger.getLogger(DriverImpl.class.getName());
    
    public static String DEFAULT_NAME = "MCUMediaMixerDriver";

    private final String name;
    private final HashMap<String,MediaServer> servers;
    private MediaServer mediaServerDefault;
    private final HashSet<HashMap<URI,MediaSessionImpl>> registry;
    private final ExecutorService threadPool;
    private final String configuration;

    public DriverImpl() {
        //Store driver
        this.name = DEFAULT_NAME;
        //Create driver map
        servers = new HashMap<String,MediaServer>();
        //Create session map
        registry = new HashSet<HashMap<URI,MediaSessionImpl>>();
        //NO default media server
        mediaServerDefault = null;
        //Create executor threads
        threadPool = null;//Executors.newFixedThreadPool(10); FIX!!!!
        //Get
        configuration = System.getProperty("org.murillo.mscontrol.configuration");
        //Load conf
        loadConfiguration(configuration);
    }

    public DriverImpl(String name) {
        //Store driver
        this.name = name;
        //Create driver map
        servers = new HashMap<String,MediaServer>();
        //Create session map
        registry = new HashSet<HashMap<URI,MediaSessionImpl>>();
        //NO default media server
        mediaServerDefault = null;
        //Create executor threads
        threadPool = Executors.newFixedThreadPool(10);
        //Get
        configuration = System.getProperty("org.murillo.mscontrol.configuration");
        //Load conf
        loadConfiguration(configuration);
    }

    public DriverImpl(String name,String configuration) {
        //Store driver
        this.name = name;
        //Create driver map
        servers = new HashMap<String,MediaServer>();
        //Create session map
        registry = new HashSet<HashMap<URI,MediaSessionImpl>>();
        //NO default media server
        mediaServerDefault = null;
        //Create executor threads
        threadPool = Executors.newFixedThreadPool(10);
        //Store configuration
        this.configuration = configuration;
        //Load configuration
        loadConfiguration(configuration);
    }

    public final void saveMixersConfiguration(String filename) {
        //Check if not null
        if (filename==null)
            //Exit
            return;

        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            //create the root element
            Element root = doc.createElement("root");
            //Add to document
            doc.appendChild(root);

            //Get mixer iterator
            for (MediaServer server : servers.values())
            {
                //create child element
                Element child = doc.createElement("MediaServer");
                //Set attributes
                child.setAttribute("name", server.getName());
                child.setAttribute("url", server.getUrl());
                child.setAttribute("ip", server.getIp());
                child.setAttribute("localNet", server.getLocalNet().toString());
                //If it is the default
                if (server.equals(mediaServerDefault))
                    //Set it
                    child.setAttribute("default", "true");
                //Append
                root.appendChild(child);
            }

            //Serialize to file
            XMLSerializer serializer = new XMLSerializer();
            serializer.setOutputCharStream(new java.io.FileWriter(filename));
            serializer.serialize(doc);

        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public final void loadConfiguration(String filename) {
        //Check if not null
        if (filename==null)
            //Exit
            return;
        
        //Clear servers
        servers.clear();

        //Load configurations
        try {
            //Create document builder
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            //Load mixer configuration
            try {
                //Parse document
                Document doc = builder.parse(filename);
                //Get all mixer nodes
                NodeList nodes = doc.getElementsByTagName("MediaServer");
                //Proccess each mixer
                for (int i = 0; i < nodes.getLength(); i++)
                    try {
                        //Get element attributes
                        NamedNodeMap attrs = nodes.item(i).getAttributes();
                        //Create mixer
                        MediaServer server = new MediaServer(attrs.getNamedItem("name").getNodeValue(), attrs.getNamedItem("url").getNodeValue(), attrs.getNamedItem("ip").getNodeValue(), attrs.getNamedItem("localNet").getNodeValue());
                        //Append mixer
                        servers.put(server.getUID(),server);
                        //Get node
                        Node def = attrs.getNamedItem("default");
                        //If it is the default
                        if (def!=null && def.getNodeValue().equals("true"))
                                //Set it
                                mediaServerDefault = server;
                    } catch (MalformedURLException ex) {
                         logger.log(Level.SEVERE, null, ex);
                    }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }

        } catch (ParserConfigurationException ex) {
                logger.log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public MsControlFactory getFactory(Properties properties) throws MsControlException {

        if (properties==null)
            //Return default
            return new MSControlFactoryImpl("default",mediaServerDefault,threadPool,this);

        //Check if we have the server parameters
        if (properties.containsKey("MediaServerURL")
                && properties.containsKey("MediaServerName")
                && properties.containsKey("MediaServerMediaIP")
                && properties.containsKey("MediaServerPublicIP")
            )
        {
            //Ger media server parameters
            String url          = properties.getProperty("MediaServerURL");
            String serverName   = properties.getProperty("MediaServerName");
            String mediaIp      = properties.getProperty("MediaServerMediaIP");
            String publicIp     = properties.getProperty("MediaServerPublicIP");

            try {
                //Create client
                MediaServer mediaServer = new MediaServer(serverName,url,mediaIp,publicIp);
                //Return it
                return new MSControlFactoryImpl(serverName,mediaServer,threadPool,this);
            } catch (MalformedURLException ex) {
                //Log
                logger.log(Level.SEVERE, null, ex);
                //Throw exception
                throw new MsControlException("Wrong Media Mixer URL", ex);
            }
        }

        //Check if it has a media server UID
        if (properties.containsKey("MediaServerUID")) {
            //Ger media server parameters
            String uid          = properties.getProperty("MediaServerUID");
            //Get media server
            MediaServer mediaServer = servers.get(uid);
            //Return it
            return new MSControlFactoryImpl(uid,mediaServer,threadPool,this);
        }
        
        //Return default
        return new MSControlFactoryImpl("default",mediaServerDefault,threadPool,this);
    }

    @Override
    public String getName() {
        //Our name
        return name;
    }

    @Override
    public PropertyInfo[] getFactoryPropertyInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addMediaServer(MediaServer server) {
        //Append it
        servers.put(server.getUID(),server);
        //Save it
        saveMixersConfiguration(configuration);
    }

    public void removeMediaServer(String uid) {
        //remove it
        servers.remove(uid);
        //Save it
        saveMixersConfiguration(configuration);
    }

    public void setDefaultMediaServer(String uid) {
        //Set it
        mediaServerDefault = servers.get(uid);
        //Save it
        saveMixersConfiguration(configuration);
    }

    public void registerMediaSessionList(HashMap<URI, MediaSessionImpl> sessions) {
        //Add to registry
        synchronized(registry) {
            registry.add(sessions);
        }
    }

    public void unregisterMediaSessionList(HashMap<URI, MediaSessionImpl> sessions) {
        //Remove from registry
        synchronized(registry) {
            registry.remove(sessions);
        }
    }

    public Collection<MediaSession> getMediaSessions() {
        //Create collection
        Collection<MediaSession> all = new ArrayList<MediaSession>();
        //Get all sessions
        synchronized(registry)
        {
            //For each session list
            for (HashMap<URI, MediaSessionImpl> sessions : registry)
                //Add sessions
                all.addAll(sessions.values());
        }
        //return all media session
        return all;
    }

    public Collection<MediaServer> getMediaServers() {
        //Get servers
        return servers.values();
    }

    public MediaServer getDefaultMediaServer() {
        //return the default media server
        return mediaServerDefault;
    }
}
