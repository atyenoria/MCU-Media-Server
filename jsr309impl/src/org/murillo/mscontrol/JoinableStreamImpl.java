/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol;

import org.murillo.mscontrol.resource.DirectionTools;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.Joinable;
import javax.media.mscontrol.join.JoinableContainer;
import javax.media.mscontrol.join.JoinableStream;
import org.murillo.mscontrol.mediagroup.PlayerJoinableStream;
import org.murillo.mscontrol.mixer.MixerAdapterJoinableStreamAudio;
import org.murillo.mscontrol.mixer.MixerAdapterJoinableStreamVideo;
import org.murillo.mscontrol.networkconnection.NetworkConnectionJoinableStream;


public abstract class JoinableStreamImpl extends JoinableImpl implements JoinableStream{
    private final JoinableContainerImpl parent;
    private final StreamType type;
    private Set<JoinableStreamImpl> sendingTo;
    private JoinableStreamImpl receivingFrom;


    public JoinableStreamImpl(MediaSessionImpl session,JoinableContainerImpl parent,StreamType type) {
        //Store session
        super(session);
        //Store valus
        this.parent = parent;
        this.type = type;
        //Create set
        sendingTo = new HashSet<JoinableStreamImpl>();
        //NOt reiving
        receivingFrom = null;
    }

    @Override
    public StreamType getType() {
        return type;
    }

    @Override
    public JoinableContainer getContainer() {
        return parent;
    }

    @Override
    public void doJoinStream(Direction dir, JoinableStreamImpl stream) throws MsControlException
    {
        //Add joinables
        AddJoinStream(dir,stream);
        //Also add joinable in reverse direcction
        stream.AddJoinStream(DirectionTools.reverse(dir),this);
        //If it is sending
        if (dir.equals(Direction.SEND) || dir.equals(Direction.DUPLEX))
            //Send
            stream.attach(this);
        //If it is receiving
        if (dir.equals(Direction.RECV) || dir.equals(Direction.DUPLEX))
            //Receive
            attach(stream);
    }

    @Override
    public void doJoinStreams(Direction dir, JoinableStreamImpl[] streams) throws MsControlException
    {
        //Check all strems
        for(JoinableStreamImpl stream : streams)
        {
            //If it is like us
            if(stream.getType().equals(type))
                //Join it
                doJoinStream(dir, stream);
        }
    }

    @Override
    public void doUnjoinStream(JoinableStreamImpl stream) throws MsControlException
    {
        //Check if it is the one we are receiving
        if (receivingFrom!=null && receivingFrom.equals(stream))
            //Detach
            dettach();
        //Check if we were sending
        if (sendingTo.contains(stream))
            //Dettach it from us
            stream.dettach();
        //Remove joinables
        RemoveJoinStream(stream);
        //Also remove joinable in reverse direcction
        stream.RemoveJoinStream(this);
        
    }

    private void AddJoinStream(Direction direction, JoinableStreamImpl stream) {
        //Check direction
        switch(direction)
        {
            case DUPLEX:
                //Add to sending
                sendingTo.add(stream);
                //Set receiving
                receivingFrom = stream;
                break;
            case SEND:
                //Add to sending
                sendingTo.add(stream);
                break;
            case RECV:
                //Set receiving
                receivingFrom = stream;
                break;
        }
    }

    private void RemoveJoinStream(JoinableStreamImpl stream) {
        //Remove from sending
        sendingTo.remove(stream);
        //Check if it was the receiving one
        if (receivingFrom!=null && receivingFrom.equals(stream))
            //Not receiving
            receivingFrom = null;
    }



    public void attach(JoinableStreamImpl stream)  throws MsControlException {
        //Network conentcion
        if (stream instanceof NetworkConnectionJoinableStream)
            //Attach
            attachEndpoint((NetworkConnectionJoinableStream)stream);
        //Player
        else if (stream instanceof PlayerJoinableStream)
            //Attach
             attachPlayer((PlayerJoinableStream)stream);
        //Audio mixer
        else if (stream instanceof MixerAdapterJoinableStreamAudio)
            //Attach
             attachMixer((MixerAdapterJoinableStreamAudio)stream);
        //Video mixer
        else if (stream instanceof MixerAdapterJoinableStreamVideo)
            //Attach
             attachMixer((MixerAdapterJoinableStreamVideo)stream);
    }

    @Override
    protected void fireJoineEvent(Joinable who, Direction direction, Joinable join, Serializable context) {
        //Call parent
        parent.fireJoineEvent(who, direction, join, context);
    }

    @Override
    protected void fireUnJoineEvent(Joinable who, Joinable join, Serializable context) {
        //Call parent
        parent.fireUnJoineEvent(who, join, context);
    }

    public int requestVideoCodec(Integer videoCodec) throws MsControlException {
        //Check all that are sending to me
        if (receivingFrom!=null)
            return receivingFrom.setVideoCodec(videoCodec);
        return 0;
    }

    public int requestAudioCodec(Integer audioCodec)  throws MsControlException{
        //Check all that are sending to me
        if (receivingFrom!=null)
            return receivingFrom.setAudioCodec(audioCodec);
        return 0;
    }

    public int setVideoCodec(Integer videoCodec) throws MsControlException{
        return 0;
    }

    public int setAudioCodec(Integer audioCodec) throws MsControlException {
        return 0;
    }

    protected abstract void attachEndpoint(NetworkConnectionJoinableStream networkConnectionJoinableStream) throws MsControlException;
    protected abstract void attachPlayer(PlayerJoinableStream playerJoinableStream) throws MsControlException;
    protected abstract void attachMixer(MixerAdapterJoinableStreamAudio mixerAdapterJoinableStreamAudio) throws MsControlException;
    protected abstract void attachMixer(MixerAdapterJoinableStreamVideo mixerAdapterJoinableStreamVideo) throws MsControlException;
    protected abstract void dettach()                         throws MsControlException;


}
