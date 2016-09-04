/*
 * MCUSipServlet.java
 *
 * Copyright (C) 2007  Sergio Garcia Murillo
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope t73hat it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.murillo.mcuWeb;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;

/**
 *
 * @author Sergio Garcia Murillo
 */
public class MCUSipServlet extends SipServlet implements SipSessionListener,SipApplicationSessionListener {

    private static final long serialVersionUID = 3978425801979081269L;

    @Override
    protected void doResponse(SipServletResponse resp) throws ServletException, IOException
    {
        //Super processing
        super.doResponse(resp);
        //Get session
        SipSession session = resp.getSession();
        //Get Participant
        RTPParticipant part = (RTPParticipant) session.getAttribute("user");
        //If not found
        if (part==null)
        {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "doResponse without participant [idSession:{0},method:{1},from:{2}]",new Object[]{session.getId(),resp.getMethod(),session.getRemoteParty().toString()});
            //Try from the application session
            part = (RTPParticipant) session.getApplicationSession().getAttribute("user");
            //If not found
            if (part==null)
            {
                //Log
                 Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "doResponse without participant [idAppSession:{0}]",new Object[]{session.getApplicationSession().getId()});
            //exit
            return;
        }
            //Set it to the session also
            session.setAttribute("user", part);
        }
        //Check participant
        if (part==null)
            //Exit
            return;
        //Check methods
        if (resp.getMethod().equals("INFO"))
            part.onInfoResponse(resp);
        else if (resp.getMethod().equals("INVITE"))
            part.onInviteResponse(resp);
        else if (resp.getMethod().equals("BYE"))
            part.onByeResponse(resp);
        else if (resp.getMethod().equals("CANCEL"))
            part.onCancelResponse(resp);
    }

    @Override
    protected void doOptions(SipServletRequest request) throws IOException
    {
        //Get Participant
        RTPParticipant part = (RTPParticipant) request.getSession().getAttribute("user");
        //Check participant
        if (part!=null)
        {
            //Handle it
            part.onOptionsRequest(request);
        } else  {
            //Create response
            SipServletResponse response = request.createResponse(200);
            //Add allowed header
            response.addHeader("Allow", RTPParticipant.ALLOWED);
            //Send it
            response.send();
    }
    }

    @Override
    protected void doInvite(SipServletRequest request) throws IOException
    {
        if (request.isInitial())
        {
            //Retreive the servlet context
            ServletContext context = getServletContext();
            //Get Manager
            ConferenceMngr confMngr = (ConferenceMngr) context.getAttribute("confMngr");
            //Handle it
            confMngr.onInviteRequest(request);
        } else {
            //Get Participant
            RTPParticipant part = (RTPParticipant) request.getSession().getAttribute("user");
            //Check participant
            if (part!=null)
                //Handle it
                part.onUpdatesRequest(request);
        }
    }

   @Override
    protected void doUpdate(SipServletRequest request) throws ServletException, IOException {
        //Get Participant
        RTPParticipant part = (RTPParticipant) request.getSession().getAttribute("user");
        //Check participant
        if (part!=null)
            //Handle it
            part.onUpdatesRequest(request);
    }

    @Override
    protected void doBye(SipServletRequest request) throws ServletException, IOException
    {
        //Get Participant
        RTPParticipant part = (RTPParticipant) request.getSession().getAttribute("user");
        //Check participant
        if (part!=null)
            //Handle it
            part.onByeRequest(request);
    }

    @Override
    protected void doRegister(SipServletRequest request) throws ServletException, IOException
    {
        //Handle dummy registration for demoing without sip proxy
        SipServletResponse resp = request.createResponse(200);
        //Set expire
        resp.setExpires(3600);
        //Send
        resp.send();
    }

    @Override
    protected void doAck(SipServletRequest request) throws ServletException, IOException {
        //Get Participant
        RTPParticipant part = (RTPParticipant) request.getSession().getAttribute("user");
        //Check participant
        if (part!=null)
            //Handle it
            part.onAckRequest(request);
    }

    @Override
    protected void doInfo(SipServletRequest request) throws ServletException, IOException {
        //Get Participant
        RTPParticipant part = (RTPParticipant) request.getSession().getAttribute("user");
        //Check participant
        if (part!=null)
            //Handle it
            part.onInfoRequest(request);
    }

    @Override
    protected void doCancel(SipServletRequest request) throws ServletException, IOException {
        //Get Participant
        RTPParticipant part = (RTPParticipant) request.getSession().getAttribute("user");
        //Check participant
        if (part!=null)
            //Handle it
            part.onCancelRequest(request);
    }

    public void sessionCreated(SipSessionEvent event) {
        Logger.getLogger(this.getClass().getName()).log(java.util.logging.Level.FINEST, "sessionCreated! {0}", event.getSession().getId());
    }

    public void sessionDestroyed(SipSessionEvent event) {
        //Log it       
        Logger.getLogger(this.getClass().getName()).log(Level.FINEST, "sessionDestroyed! {0}", event.getSession().getId());
   }

    public void sessionReadyToInvalidate(SipSessionEvent event)
    {
       //Log it
        Logger.getLogger(this.getClass().getName()).log(Level.FINEST, "sessionReadyToInvalidate! {0}", event.getSession().getId());
    }

    public void sessionCreated(SipApplicationSessionEvent sase) {
        Logger.getLogger(this.getClass().getName()).log(java.util.logging.Level.FINEST, "appSessionCreated! {0}", sase.getApplicationSession().getId());
    }

    public void sessionDestroyed(SipApplicationSessionEvent sase) {
        Logger.getLogger(this.getClass().getName()).log(java.util.logging.Level.FINEST, "appSessionDestroyed! {0}", sase.getApplicationSession().getId());
    }

    public void sessionExpired(SipApplicationSessionEvent sase) {
        Logger.getLogger(this.getClass().getName()).log(java.util.logging.Level.FINEST, "appSessionExpired! {0}", sase.getApplicationSession().getId());
        //Get application session
        SipApplicationSession applicationSession = sase.getApplicationSession();
        //Get user
        RTPParticipant part = (RTPParticipant) applicationSession.getAttribute("user");
        //Check if we have participant
        if (part!=null)
            //Timeout
            part.onTimeout();
    }

    public void sessionReadyToInvalidate(SipApplicationSessionEvent sase) {
        Logger.getLogger(this.getClass().getName()).log(java.util.logging.Level.FINEST, "sessionReadyToInvalidate! {0}", sase.getApplicationSession().getId());
        //Get application session
        SipApplicationSession applicationSession = sase.getApplicationSession();
        //Get user
        RTPParticipant part = (RTPParticipant) applicationSession.getAttribute("user");
        //Check if we have participant
        if (part!=null)
            //Timeout
            part.onTimeout();
    }
}
