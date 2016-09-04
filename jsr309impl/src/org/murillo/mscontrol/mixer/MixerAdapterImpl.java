/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.mixer;

import java.net.URI;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.mscontrol.MediaConfig;
import javax.media.mscontrol.MediaObject;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.join.JoinableStream;
import javax.media.mscontrol.join.JoinableStream.StreamType;
import javax.media.mscontrol.mixer.MixerAdapter;
import javax.media.mscontrol.resource.Action;
import org.murillo.mscontrol.resource.ContainerImpl;
import org.murillo.mscontrol.MediaSessionImpl;
import org.murillo.mscontrol.ParametersImpl;

/**
 *
 * @author Sergio
 */
public class MixerAdapterImpl extends ContainerImpl implements MixerAdapter {

    public MixerAdapterImpl(MediaSessionImpl sess, MediaMixerImpl mixer, URI uri,ParametersImpl params) throws MsControlException {
        //Call parent
        super(sess,uri,params);
        //Get supported streasm
        for (StreamType type : mixer.getSupportedTypes())
        {
            switch(type)
            {
                case audio:
                    //Create stream
                    MixerAdapterJoinableStreamAudio audioStream = new MixerAdapterJoinableStreamAudio(sess,mixer,uri);
                    //Create and add it
                    AddStream(type,audioStream);
                    break;
                case video:
                    //Create stream
                    MixerAdapterJoinableStreamVideo videoStream = new MixerAdapterJoinableStreamVideo(sess,mixer,uri);
                    //Add to default mosaic
                    videoStream.addToMosaic(0);
                    //Create and add it
                    AddStream(type,videoStream);
                    break;
                default:
            }
        }
        
    }

    @Override
    public void triggerAction(Action action) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <R> R getResource(Class<R> type) throws MsControlException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MediaConfig getConfig() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void release() {
        //Free joins
        releaseJoins();
        try {
            //Release all streams
            for (JoinableStream stream : getJoinableStreams()) {
                //It is an audio one
                if (stream instanceof MixerAdapterJoinableStreamAudio )
                    //Release it
                    ((MixerAdapterJoinableStreamAudio)stream).release();
                //It is an audio one
                if (stream instanceof MixerAdapterJoinableStreamVideo )
                    //Release it
                    ((MixerAdapterJoinableStreamVideo)stream).release();
            }
        } catch (MsControlException ex) {
            Logger.getLogger(MixerAdapterImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Iterator<MediaObject> getMediaObjects() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T extends MediaObject> Iterator<T> getMediaObjects(Class<T> type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
