/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol;

import java.util.HashSet;
import java.util.Set;
import javax.media.mscontrol.EventType;
import javax.media.mscontrol.MediaConfig;
import javax.media.mscontrol.MediaConfigException;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.Qualifier;
import javax.media.mscontrol.SupportedFeatures;
import javax.media.mscontrol.Value;
import javax.media.mscontrol.join.JoinableStream.StreamType;
import javax.media.mscontrol.resource.Action;
import javax.media.mscontrol.resource.Trigger;


/**
 *
 * @author Sergio
 */
public class MediaConfigImpl implements MediaConfig {

    private SupportedFeaturesImpl features;
    private Parameters params = new ParametersImpl();
    private final Set<StreamType> streams;

    public MediaConfigImpl(SupportedFeaturesImpl features, Parameters params,Set<StreamType> streams) {
        this.features = features;
        this.params.putAll(params);
	this.streams = streams;
    }

    @Override
    public boolean hasStream(StreamType streamType) {
        return streams.contains(streamType);
    }

    @Override
    public MediaConfig createCustomizedClone(Parameters params) throws MediaConfigException {
        Parameters cust = new ParametersImpl();
        cust.putAll(this.params);

        //create new sets
        Set<Parameter> parameters = new HashSet();
        Set<Action> actions = new HashSet();
        Set<EventType> eventTypes = new HashSet();
        Set<Qualifier> qualifiers = new HashSet();
        Set<Trigger> triggers = new HashSet();
        Set<Value> values = new HashSet();

        //copy default values
        parameters.addAll(features.getSupportedParameters());
        actions.addAll(features.getSupportedActions());
        eventTypes.addAll(features.getSupportedEventTypes());
        qualifiers.addAll(features.getSupportedQualifiers());
        triggers.addAll(features.getSupportedTriggers());
        values.addAll(features.getSupportedValues());

        //add customized parameters and values
        if (params != null) {
            Set<Parameter> set = params.keySet();
            for (Parameter p : set) {
                parameters.add(p);
            }
        }

        SupportedFeaturesImpl f = new SupportedFeaturesImpl(
                parameters, actions, eventTypes, qualifiers, triggers, values);

        if (params != null) {
            cust.putAll(params);
        }
        return new MediaConfigImpl(f, cust, streams);
    }

    @Override
    public SupportedFeatures getSupportedFeatures() {
        return features;
    }

    public Object getValue(Parameter p) {
        return params.get(p);
    }

    public Parameters getParameters() {
        return params;
    }

    @Override
    public String marshall() {
        return "";
    }

    @Override
    public String toString() {
        return params.toString();
    }
}
