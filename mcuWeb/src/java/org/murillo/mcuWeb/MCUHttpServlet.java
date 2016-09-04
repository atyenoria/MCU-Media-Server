/*
 * MCUHttpServlet.java
 *
 * Copyright (C) 2007  Sergio Garcia Murillo
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.murillo.mcuWeb;

import java.io.*;
import java.util.HashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import org.murillo.mcu.exceptions.ConferenceNotFoundExcetpion;
import org.murillo.mcu.exceptions.ParticipantNotFoundException;


/**
 *
 * @author Sergio Garcia Murillo
 */
public class MCUHttpServlet extends HttpServlet {
    

    private SipFactory sf;
    private ConferenceMngr confMngr;
    private String path;
    
    @Override
    public void init() throws ServletException {
        //Retreive the servlet context
        ServletContext context = getServletContext();
        //Get path
        path = context.getContextPath();
        //Get the sf
        sf = (SipFactory) context.getAttribute(SipServlet.SIP_FACTORY);
        //Create conference manager
        confMngr = new ConferenceMngr(context);
        //Set csip factory
        confMngr.setSipFactory(sf);
        //Set it
        context.setAttribute("confMngr", confMngr);
        try {
            //Get the input stream
            InputStream inputStream = context.getResourceAsStream("/META-INF/MANIFEST.MF");
            //Read manifest
            Manifest manifest = new Manifest(inputStream);
            //Gett attributes
            Attributes attr = manifest.getMainAttributes();
            //Put them in the application
            context.setAttribute("BuiltDate"       ,attr.getValue("Built-Date"));
            context.setAttribute("SubversionInfo"  ,attr.getValue("Subversion-Info"));
        } catch (IOException ex) {
            Logger.getLogger(MCUHttpServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet MCUHttpServlet</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Servlet MCUHttpServlet at " + request.getContextPath () + "</h1>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Get uri of the request
        String uri =request.getRequestURI();
        //Get method
        String method = uri.substring(uri.lastIndexOf("/")+1,uri.length());

        try {
            //Depending on the method
            if (method.equals("createConference")) {
                //Get parameters
                String name         = request.getParameter("name");
                String did          = request.getParameter("did");
                String mixerId      = request.getParameter("mixerId");
                String profileId    = request.getParameter("profileId");
                Integer compType    = Integer.parseInt(request.getParameter("compType"));
                Integer vad         = Integer.parseInt(request.getParameter("vad"));
                Integer size        = Integer.parseInt(request.getParameter("size"));
                String audioCodecs  = request.getParameter("audioCodecs");
                String videoCodecs  = request.getParameter("videoCodecs");
                String textCodecs   = request.getParameter("textCodecs");
                //Call
                Conference conf = confMngr.createConference(name,did,mixerId,size,compType,vad,profileId,audioCodecs,videoCodecs,textCodecs);
                //Redirect
                response.sendRedirect(path+"/conference.jsp?uid=" + conf.getUID());
            } else if (method.equals("removeConference")) {
                //Get parameters
                String  uid  = request.getParameter("uid");
                //Call
                confMngr.removeConference(uid);
                //Redirect
                response.sendRedirect(path);
            } else if (method.equals("removeAllConferencesByDID")) {
                //Get parameters
                String  did  = request.getParameter("did");
                //Get conferences
                HashMap<String, Conference> conferences = confMngr.getConferences();
                //For each one
                for (Conference conf : conferences.values())
                    //If the DID is matched
                    if (conf.getDID().equals(did))
                        //Call
                        conf.destroy();
                //Redirect
                response.sendRedirect(path);
            } else if (method.equals("createBroadcast")) {
                //Get parameters
                String name      = request.getParameter("name");
                String tag       = request.getParameter("tag");
                String mixerId   = request.getParameter("mixerId");
                //Call
                Broadcast bcast = confMngr.createBroadcast(name, tag, mixerId);
                //Redirect
                response.sendRedirect(path);
            } else if (method.equals("removeBroadcast")) {
                //Get parameters
                String  uid  = request.getParameter("uid");
                //Call
                confMngr.removeBroadcast(uid);
                //Redirect
                response.sendRedirect(path);
            } else if (method.equals("callParticipant")) {
                //Get parameters
                String  uid  = request.getParameter("uid");
                String  dest = request.getParameter("dest");
                //Call
                Participant part = confMngr.callParticipant(uid,dest);
                //Redirect
                response.sendRedirect(path+"/conference.jsp?uid=" + uid);
            } else if(method.equals("removeParticipant")){
                //Get parameters
                String  uid  = request.getParameter("uid");
                Integer partId  = Integer.parseInt(request.getParameter("partId"));
                //Remove participant
                confMngr.removeParticipant(uid, partId);
                //Redirect
                response.sendRedirect(path+"/conference.jsp?uid=" + uid);
             } else if(method.equals("setVideoMute")){
                //Get parameters
                String  uid = request.getParameter("uid");
                Integer partId = Integer.parseInt(request.getParameter("partId"));
                Boolean flag = Boolean.parseBoolean(request.getParameter("flag"));
                //Call
                confMngr.setVideoMute(uid, partId, flag);
                //Redirect
                response.sendRedirect(path+"/conference.jsp?uid=" + uid);
            }else if(method.equals("setAudioMute")){
                //Get parameters
                String  uid = request.getParameter("uid");
                Integer partId = Integer.parseInt(request.getParameter("partId"));
                Boolean flag = Boolean.parseBoolean(request.getParameter("flag"));
                //Call
                confMngr.setAudioMute(uid, partId, flag);
                //Redirect
                response.sendRedirect(path+"/conference.jsp?uid=" + uid);
            } else if (method.equals("addProfile")) {
                //Get parameters
                String  uid  = request.getParameter("uid");
                String  name  = request.getParameter("name");
                Integer videoSize = Integer.parseInt(request.getParameter("videoSize"));
                Integer videoBitrate = Integer.parseInt(request.getParameter("videoBitrate"));
                Integer videoFPS = Integer.parseInt(request.getParameter("videoFPS"));
                Integer intraPeriod = Integer.parseInt(request.getParameter("intraPeriod"));
                //Call
                confMngr.addProfile(uid,name,videoSize,videoBitrate,videoFPS,intraPeriod,0,8000);
                //Redirect
                response.sendRedirect(path);
            } else if (method.equals("removeProfile")) {
                //Get parameters
                String  uid  = request.getParameter("uid");
                //Call
                confMngr.removeProfile(uid);
                //Redirect
                response.sendRedirect(path);
            } else if (method.equals("addMixer")) {
                //Get parameters
                String  name  = request.getParameter("name");
                String  url = request.getParameter("url");
                String  ip = request.getParameter("ip");
                String  publicIp = request.getParameter("publicIp");
                    String  localNet = request.getParameter("localNet");
                //Call
                    confMngr.addMixer(name,url,ip,publicIp,localNet);
                //Redirect
                response.sendRedirect(path);
            } else if (method.equals("removeMixer")) {
                //Get parameters
                String  uid  = request.getParameter("uid");
                //Call
                confMngr.removeMixer(uid);
                //Redirect
                response.sendRedirect(path);
              } else if (method.equals("addConferenceAdHocTemplate")) {
		//Get parameters
                String name         = request.getParameter("name");
                String did          = request.getParameter("did");
                String mixerId      = request.getParameter("mixerId");
                String profileId    = request.getParameter("profileId");
                Integer compType    = Integer.parseInt(request.getParameter("compType"));
                Integer vad         = Integer.parseInt(request.getParameter("vad"));
                Integer size        = Integer.parseInt(request.getParameter("size"));
                //Get codecs
                String audioCodecs = request.getParameter("audioCodecs");
                String videoCodecs = request.getParameter("videoCodecs");
                String textCodecs = request.getParameter("textCodecs");
                //Call
                confMngr.addConferenceAdHocTemplate(name,did,mixerId,size,compType,vad,profileId,true,audioCodecs,videoCodecs,textCodecs,"");
                //Redirect
                response.sendRedirect(path);
            } else if (method.equals("removeConferenceAdHocTemplate")) {
                //Get parameters
                String  uid  = request.getParameter("uid");
                //Call
                confMngr.removeConferenceAdHocTemplate(uid);
                //Redirect
                response.sendRedirect(path);
             } else if (method.equals("setCompositionType")) {
                //Get parameters
                String  uid = request.getParameter("uid");
                Integer compType  = Integer.parseInt(request.getParameter("compType"));
                Integer size = Integer.parseInt(request.getParameter("size"));
                String  profileId = request.getParameter("profileId");
                //Call
                confMngr.setCompositionType(uid,compType,size,profileId);
                //Redirect
                response.sendRedirect(path+"/conference.jsp?uid=" + uid);
              } else if (method.equals("changeParticipantProfile")) {
                //Get parameters
                String  uid = request.getParameter("uid");
                Integer partId = Integer.parseInt(request.getParameter("partId"));
                String  profileId = request.getParameter("profileId");
                //Call
                confMngr.changeParticipantProfile(uid, partId, profileId);
                //Redirect
                response.sendRedirect(path+"/conference.jsp?uid=" + uid);
             } else if (method.equals("setMosaicSlot")) {
                //Get parameters
                String  uid = request.getParameter("uid");
                Integer num  = Integer.parseInt(request.getParameter("num"));
                Integer id = Integer.parseInt(request.getParameter("id"));
                //Call
                confMngr.setMosaicSlot(uid,num,id);
                //Set xml response
                response.getOutputStream().print("<result>1</result>");
            } else {
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Servlet MCUHttpServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Unknown request [" + method + "]</h1>");
                out.println("</body>");
                out.println("</html>");
                out.close();
            }
        } catch (ConferenceNotFoundExcetpion cex) {
            //Log
            Logger.getLogger(MCUHttpServlet.class.getName()).log(Level.SEVERE, "failed to run" + method, cex);
            //Redirect home
            response.sendRedirect(path);
        } catch (ParticipantNotFoundException pex) {
            //Log
            Logger.getLogger(MCUHttpServlet.class.getName()).log(Level.SEVERE, "failed to run" + method, pex);
            //Redirect home
            response.sendRedirect(path);
        }
    }
    
    /** Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "MCU HTTP Servlet";
    }
}
