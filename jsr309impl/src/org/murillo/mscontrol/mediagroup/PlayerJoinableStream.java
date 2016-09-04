/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.mediagroup;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.join.JoinableStream;
import javax.media.mscontrol.join.JoinableStream.StreamType;
import org.apache.xmlrpc.XmlRpcException;
import org.murillo.MediaServer.Codecs;
import org.murillo.MediaServer.Codecs.MediaType;
import org.murillo.mscontrol.JoinableContainerImpl;
import org.murillo.mscontrol.JoinableStreamImpl;
import org.murillo.mscontrol.MediaServer;
import org.murillo.mscontrol.MediaSessionImpl;
import org.murillo.mscontrol.mixer.MixerAdapterJoinableStreamAudio;
import org.murillo.mscontrol.mixer.MixerAdapterJoinableStreamVideo;
import org.murillo.mscontrol.networkconnection.NetworkConnectionJoinableStream;

/**
 *
 * @author Sergio
 */
public class PlayerJoinableStream extends JoinableStreamImpl implements JoinableStream {
    private final PlayerImpl player;
    private final StreamType type;
    private final MediaType media;
    private Set<Joinable> sending;

    PlayerJoinableStream(MediaSessionImpl session, PlayerImpl player, StreamType type)
    {
        //Call parent
        super(session, (JoinableContainerImpl)player.getContainer(),type);
        //Store values
        this.player = player;
        this.type = type;
        //Get mapping
        if (type.equals(StreamType.audio))
            //Audio
            media = Codecs.MediaType.AUDIO;
        else if(type.equals(StreamType.video))
            //Video
            media = Codecs.MediaType.VIDEO;
        else if(type.equals(StreamType.message))
            //Text
            media = Codecs.MediaType.TEXT;
        else
            //uuh??
            media = null;
        //Create set
        sending = new HashSet<Joinable>();
    }

    public PlayerImpl getPlayer() {
        //Return parent object
        return player;
    }

    @Override
    public void dettach() throws MsControlException {
        //Nothing
    }

    @Override
    protected void attachEndpoint(NetworkConnectionJoinableStream networkConnectionJoinableStream) throws MsControlException {
        ///NOthing
    }

    @Override
    protected void attachPlayer(PlayerJoinableStream playerJoinableStream) throws MsControlException {
        //Nothing
    }

    @Override
    protected void attachMixer(MixerAdapterJoinableStreamAudio mixerAdapterJoinableStreamAudio) throws MsControlException {
        //Nothing
    }

    @Override
    protected void attachMixer(MixerAdapterJoinableStreamVideo mixerAdapterJoinableStreamVideo) throws MsControlException {
    }
}
