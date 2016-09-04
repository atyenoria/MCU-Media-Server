/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol;

import java.net.URI;
import javax.media.mscontrol.MediaObject;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;

/**
 *
 * @author Sergio
 */
public abstract class MediaObjectImpl implements MediaObject {
    private final URI uri;
    private ParametersImpl params;
    
    public MediaObjectImpl(URI uri){
        //Store
        this.uri = uri;
        //Empty params
        params = new ParametersImpl();
    }
    
    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public void setParameters(Parameters params) {
        //Store new
        this.params = (ParametersImpl) params;
    }

    @Override
    public Parameters getParameters(Parameter[] requested) {
        //Cretate new params
        ParametersImpl returned = new ParametersImpl();
        //For each requested parameter
        for (Parameter p : requested)
            returned.put(p, params.get(p));
        return returned;
    }

    @Override
    public Parameters createParameters() {
        return new ParametersImpl();
    }

}
