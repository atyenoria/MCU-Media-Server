/*
 * JBoss, Home of Professional Open Source
 * Copyright XXXX, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */

package org.murillo.mscontrol.networkconnection;

import java.util.HashSet;
import java.util.Set;
import javax.media.mscontrol.Configuration;
import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaConfig;
import javax.media.mscontrol.MediaObject;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.Qualifier;
import javax.media.mscontrol.Value;
import javax.media.mscontrol.join.JoinableStream.StreamType;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.resource.Action;
import javax.media.mscontrol.resource.Trigger;
import org.murillo.mscontrol.ParametersImpl;
import org.murillo.mscontrol.SupportedFeaturesImpl;
import org.murillo.mscontrol.MediaConfigImpl;
import org.murillo.mscontrol.ext.codecs.rtp;

/**
 *
 * @author kulikov
 */
public class NetworkConnectionBasicConfigImpl implements Configuration {
    private final static Set<StreamType> AUDIO			= new HashSet<StreamType>();
    private final static Set<StreamType> AUDIO_VIDEO		= new HashSet<StreamType>();
    private final static Set<StreamType> AUDIO_VIDEO_TEXT	= new HashSet<StreamType>();
    
    static {
	    AUDIO.add(StreamType.audio);
	    AUDIO_VIDEO.add(StreamType.audio);
	    AUDIO_VIDEO.add(StreamType.video);
	    AUDIO_VIDEO_TEXT.add(StreamType.audio);
	    AUDIO_VIDEO_TEXT.add(StreamType.video);
	    AUDIO_VIDEO_TEXT.add(StreamType.message);
    }    
    
    public static MediaConfig getConfig() {
        //Create params
        Parameters params = new ParametersImpl();
        //specify parameter
        Set<Parameter> parameters = new HashSet();
        parameters.add(MediaObject.MEDIAOBJECT_ID);

        //specify event types
        Set<EventType> eventTypes = new HashSet();
        eventTypes.add(SdpPortManagerEvent.OFFER_GENERATED);
        eventTypes.add(SdpPortManagerEvent.ANSWER_GENERATED);
        eventTypes.add(SdpPortManagerEvent.ANSWER_PROCESSED);
        eventTypes.add(SdpPortManagerEvent.NETWORK_STREAM_FAILURE);

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
    
    public static MediaConfig getAudioConfig() {
        //Create params
        Parameters params = new ParametersImpl();
        //specify parameters
        Set<Parameter> parameters = new HashSet();
        parameters.add(MediaObject.MEDIAOBJECT_ID);

        //specify event types
        Set<EventType> eventTypes = new HashSet();
        eventTypes.add(SdpPortManagerEvent.OFFER_GENERATED);
        eventTypes.add(SdpPortManagerEvent.ANSWER_GENERATED);
        eventTypes.add(SdpPortManagerEvent.ANSWER_PROCESSED);
        eventTypes.add(SdpPortManagerEvent.NETWORK_STREAM_FAILURE);

        //Define actions
        Set<Action> actions = new HashSet();

        //Define qualifiers
        Set<Qualifier> qualifiers = new HashSet();

        //Define triggers
        Set<Trigger> triggers = new HashSet();

        //Define values
        Set<Value> values = new HashSet();

        SupportedFeaturesImpl features = new SupportedFeaturesImpl(parameters, actions, eventTypes, qualifiers, triggers, values);
        return new MediaConfigImpl(features, params, AUDIO);
    }
}
