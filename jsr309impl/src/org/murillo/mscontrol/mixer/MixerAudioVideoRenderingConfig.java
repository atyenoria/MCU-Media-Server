/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.mixer;

import java.util.HashSet;
import java.util.Set;
import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaConfig;
import javax.media.mscontrol.MediaObject;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.Qualifier;
import javax.media.mscontrol.Value;
import javax.media.mscontrol.join.JoinableStream;
import javax.media.mscontrol.mixer.MediaMixer;
import javax.media.mscontrol.mixer.MixerEvent;
import javax.media.mscontrol.resource.Action;
import javax.media.mscontrol.resource.Trigger;
import org.murillo.mscontrol.MediaConfigImpl;
import org.murillo.mscontrol.ParametersImpl;
import org.murillo.mscontrol.SupportedFeaturesImpl;
/**
 *
 * @author Sergio
 */
class MixerAudioVideoRenderingConfig {
    private final static Set<JoinableStream.StreamType> AUDIO			= new HashSet<JoinableStream.StreamType>();
    private final static Set<JoinableStream.StreamType> AUDIO_VIDEO		= new HashSet<JoinableStream.StreamType>();
    private final static Set<JoinableStream.StreamType> AUDIO_VIDEO_TEXT	= new HashSet<JoinableStream.StreamType>();
    
    static {
	    AUDIO.add(JoinableStream.StreamType.audio);
	    AUDIO_VIDEO.add(JoinableStream.StreamType.audio);
	    AUDIO_VIDEO.add(JoinableStream.StreamType.video);
	    AUDIO_VIDEO_TEXT.add(JoinableStream.StreamType.audio);
	    AUDIO_VIDEO_TEXT.add(JoinableStream.StreamType.video);
	    AUDIO_VIDEO_TEXT.add(JoinableStream.StreamType.message);
    }    
    public static MediaConfig getConfig() {
        Parameters params = new ParametersImpl();
        //specify parameters
        Set<Parameter> parameters = new HashSet();
        parameters.add(MediaObject.MEDIAOBJECT_ID);
        parameters.add(MediaMixer.ENABLED_EVENTS);
        parameters.add(MediaMixer.MAX_ACTIVE_INPUTS);
        parameters.add(MediaMixer.MAX_PORTS);

        //specify event types
        Set<EventType> eventTypes = new HashSet();
        eventTypes.add(MixerEvent.ACTIVE_INPUTS_CHANGED);
        eventTypes.add(MixerEvent.MOST_ACTIVE_INPUT_CHANGED);

        //Define actions
        Set<Action> actions = new HashSet();

        //Define qualifiers
        Set<Qualifier> qualifiers = new HashSet();

        //Define triggers
        Set<Trigger> triggers = new HashSet();

        //Define values
        Set<Value> values = new HashSet();

        SupportedFeaturesImpl features = new SupportedFeaturesImpl(parameters, actions, eventTypes, qualifiers, triggers, values);
        return new MediaConfigImpl(features, params, AUDIO_VIDEO_TEXT);
    }
}
