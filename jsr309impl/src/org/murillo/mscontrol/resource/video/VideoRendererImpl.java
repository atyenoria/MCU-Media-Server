/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.resource.video;

import java.io.StringReader;
import java.util.HashSet;
import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaErr;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.JoinableStream.StreamType;
import javax.media.mscontrol.resource.ResourceContainer;
import javax.media.mscontrol.resource.video.VideoLayout;
import javax.media.mscontrol.resource.video.VideoRenderer;
import javax.media.mscontrol.resource.video.VideoRendererEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.murillo.mscontrol.MediaServer;
import org.murillo.mscontrol.MediaSessionImpl;
import org.murillo.mscontrol.mixer.MediaMixerImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 *
 * @author Sergio
 */
public class VideoRendererImpl implements VideoRenderer {
    
    private final MediaMixerImpl mixer;
    private final int mosaicId;
    private int mosaicCompType;
    private final HashSet<MediaEventListener<VideoRendererEvent>> listeners;
    private final MediaSessionImpl sess;

    public VideoRendererImpl(MediaMixerImpl mixer, int mosaicId) {
        //Store values
        this.mixer = mixer;
        this.mosaicId = mosaicId;
        //No default mosaicType by default
        mosaicCompType = -1;
        //Get media session
        sess = (MediaSessionImpl)mixer.getMediaSession();
        //Create set
        listeners = new HashSet<MediaEventListener<VideoRendererEvent>>();
    }

    @Override
    public void setLayout(final VideoLayout vl) throws MsControlException {
        //Check type
        if (!vl.getType().equalsIgnoreCase("mcu;xml-layout"))
            //Throw exception
            throw new MsControlException("Type not supported");
        //Store value for async call
        final VideoRenderer renderer = this;
        //Execute it
        sess.Exec(new Runnable() {
            @Override
            public void run() {
                try {
                    //Get xml
                    String xml = vl.marshall();
                    //Get builder
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    //Parse
                    Document doc = builder.parse(new InputSource(new StringReader(xml)));
                    //Get root node
                    Element layout = doc.getDocumentElement();
                    //Get meida server
                    MediaServer mediaServer = sess.getMediaServer();
                    //Get session id
                    int sessId = sess.getSessionId();
                    //Get mixer id
                    int mixerId = mixer.getMixerId(StreamType.video);
                    //Get composition type
                    int comp = Integer.parseInt(layout.getAttribute("type"));
                    //Get size
                    int size = Integer.parseInt(layout.getAttribute("size"));
                    //Check if the composition type is different than the one we have to avoid flickering
                    if (comp!=mosaicCompType)
                        //Set
                        mediaServer.VideoMixerMosaicSetCompositionType(sessId, mixerId, mosaicId, comp, size);
                    //Store composition type
                    mosaicCompType = comp;
                    //Get overlay
                    String overlay = layout.getAttribute("overlay");
                    //If set
                    if (overlay!=null && !overlay.isEmpty())
                        //Set it
                        mediaServer.VideoMixerMosaicSetOverlayPNG(sessId, mixerId, mosaicId, overlay);
                    else
                        //Unset it
                        mediaServer.VideoMixerMosaicResetOverlay(sessId, mixerId, mosaicId);
                    //Everything ok
                    fireEvent(new VideoRendererEventImpl(renderer,VideoRendererEvent.RENDERING_COMPLETED));
                } catch (Exception ex) {
                    fireEvent(new VideoRendererEventImpl(renderer,MediaErr.BAD_ARG, ex.getMessage()));
                }
            }
        });
    }

    @Override
    public ResourceContainer getContainer() {
        return mixer;
    }

    @Override
    public void addListener(MediaEventListener<VideoRendererEvent> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(MediaEventListener<VideoRendererEvent> listener) {
        listeners.remove(listener);
    }

    @Override
    public MediaSession getMediaSession() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void fireEvent(VideoRendererEvent event) {
        //For each listener
        for (MediaEventListener<VideoRendererEvent> listener : listeners)
                    //call it
                    listener.onEvent(event);
    }
}
