/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.murillo.mscontrol.resource;

import java.net.URI;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameter;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.resource.AllocationEvent;
import javax.media.mscontrol.resource.AllocationEventListener;
import javax.media.mscontrol.resource.ResourceContainer;
import org.murillo.mscontrol.JoinableContainerImpl;
import org.murillo.mscontrol.MediaSessionImpl;
import org.murillo.mscontrol.ParametersImpl;

/**
 *
 * @author Sergio
 */
public abstract class ContainerImpl extends JoinableContainerImpl implements ResourceContainer {
    private final URI uri;
    private Parameters params;
    private final ConcurrentLinkedQueue<AllocationEventListener> listenersAllocation;

    public ContainerImpl (MediaSessionImpl sess,URI uri,ParametersImpl params) {
        //Call parent
        super(sess);
        //Store
        this.uri = uri;
        //Store params
        this.params = params;
        //Create
        listenersAllocation = new ConcurrentLinkedQueue<AllocationEventListener>();
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public void setParameters(Parameters params) {
        //Store new
        this.params = params;
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
    
    @Override
    public void confirm() throws MsControlException {
        //Launch event
        fireEventAsync(new AllocationEventImpl(this,AllocationEvent.ALLOCATION_CONFIRMED));
    }

    private void fireEventAsync(final AllocationEventImpl event) {
        //Check if we have listeners
        if (listenersAllocation.isEmpty())
            //Exit
            return;
        //Execute
        session.Exec(new Runnable() {

            @Override
            public void run() {
                //For each listener
                for (AllocationEventListener listener : listenersAllocation)
                    //call it
                    listener.onEvent(event);
            }
        });
    }

    @Override
    public void addListener(AllocationEventListener al) {
        //Add it
        listenersAllocation.add(al);
    }

    @Override
    public void removeListener(AllocationEventListener al) {
        //Remove
        listenersAllocation.remove(al);
    }
}
