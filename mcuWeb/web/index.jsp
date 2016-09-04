<%@page contentType="text/html"%>
<%@page pageEncoding="ISO-8859-1"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="org.murillo.mcuWeb.ConferenceMngr"%>
<%
    //Get conference manager
    ConferenceMngr confMngr = (ConferenceMngr) getServletContext().getAttribute("confMngr");
%>
<script>
function removeConf(uid)
{
    var param = {uid:uid};
    return callController("removeConference",param);
}
function removeBroadcast(uid)
{
    var param = {uid:uid};
    return callController("removeBroadcast",param);
}
function removeMixer(uid)
{
    var param = {uid:uid};
    return callController("removeMixer",param);
}
function removeProfile(uid)
{
    var param = {uid:uid};
    return callController("removeProfile",param);
}
function removeConfTemplate(uid)
{
    var param = {uid:uid};
    return callController("removeConferenceAdHocTemplate",param);
}
</script>    
<fieldset>
    <legend><img src="icons/bricks.png"> Media Mixers</legend>
    <table class="list">
        <tr>
            <th>Name</th>
            <th>Url</th>
	    <th>Media IP</th>
	    <th>Local Net</th>
	    <th>Public IP</th>
            <th>Actions</th>
        </tr>
        <%
        //Get mixer iterator
        Iterator<org.murillo.mcuWeb.MediaMixer> it = confMngr.getMediaMixers().values().iterator();
        //Loop 
        while(it.hasNext()) {
            // Get mixer
           org.murillo.mcuWeb.MediaMixer mm = it.next();
            //Print values
            %>
        <tr>
            <td><%=mm.getName()%></td>
            <td><%=mm.getUrl()%></td>
	    <td><%=mm.getIp()%></td>
	    <td><%=mm.getLocalNet()%></td>
	    <td><%=mm.getPublicIp()%></td>
            <td><a href="#" onClick="removeMixer('<%=mm.getUID()%>');return false;"><img src="icons/bin_closed.png"><span>Remove mixer</span></a></td>
        </tr><%
        }
        %>
    </table>
    <form><input type="submit" class="add" onClick="document.location.href='addMixer.jsp';return false;" value="Add"></form>    
</fieldset>
<fieldset>
    <legend><img src="icons/table_multiple.png"> Profile List</legend>
        <table class="list">
        <tr>
	    <th>Id</th>
            <th>Name</th>
            <th>Parameters</th>
            <th>Actions</th>
        </tr>
        <%
        //Get profile
	for( org.murillo.mcuWeb.Profile profile : confMngr.getProfiles().values())
	{
            //Print values
            %>
        <tr>
	    <td><%=profile.getUID()%></td>
            <td><%=profile.getName()%></td>
            <td><%=org.murillo.mcuWeb.MediaMixer.getSizes().get(profile.getVideoSize())%>-<%=profile.getVideoBitrate()%>Kbs <%=profile.getVideoFPS()%>fps  <%=profile.getIntraPeriod()%></td>
            <td>
<a href="#" onClick="removeProfile('<%=profile.getUID()%>');return false;"><img src="icons/bin_closed.png"><span>Remove profile</span></a>
            </td>
        </tr><%
        }
        %>
    </table>
    <form><input class="add" type="button" onClick="document.location.href='addProfile.jsp';return false;" value="Create"></form>
</fieldset>
<%
    //Only if there are available mixers and profiles
    if (confMngr.getMediaMixers().size()>0 && confMngr.getProfiles().size()>0) {
%>
<fieldset>
    <legend><img src="icons/application_view_list.png"> AdHoc Conference Templates</legend>
        <table class="list">
        <tr>
            <th>DID pattern</th>
            <th>Name</th>
            <th>Profile</th>
            <th>Mixer</th>
            <th>Actions</th>
        </tr>
        <%
        //Get mixer iterator
        Iterator<org.murillo.mcuWeb.ConferenceTemplate> itConfTemp = confMngr.getTemplates().values().iterator();
        //Loop
        while(itConfTemp.hasNext()) {
            // Get mixer
           org.murillo.mcuWeb.ConferenceTemplate temp = itConfTemp.next();
            //Print values
            %>
        <tr>
            <td><%=temp.getDID()%></td>
            <td><%=temp.getName()%></td>
            <td><%=temp.getProfile().getName()%></td>
            <td><%=temp.getMixer().getName()%></td>
            <td>
<a href="#" onClick="removeConfTemplate('<%=temp.getUID()%>');return false;"><img src="icons/bin_closed.png"><span>Remove Template</span></a>
            </td>
        </tr><%
        }
        %>
    </table>
    <form><input class="add" type="button" onClick="document.location.href='addConferenceAdHocTemplate.jsp';return false;" value="Create"></form>
</fieldset>
<fieldset>
    <legend><img src="icons/images.png"> Conference List</legend>
        <table class="list">
        <tr>
            <th>UID</th>
	    <th>Timestamp</th>
	    <th>ConfId</th>
            <th>DID</th>
            <th>#</th>
            <th>Name</th>
            <th>Mixer</th>
            <th>Actions</th>
        </tr>
        <%
        //Get mixer iterator
        Iterator<org.murillo.mcuWeb.Conference> itConf = confMngr.getConferences().values().iterator();
        //Loop 
        while(itConf.hasNext()) {
            // Get mixer
           org.murillo.mcuWeb.Conference conf = itConf.next();
            //Print values
            %>
        <tr>
            <td><%=conf.getUID()%></td>
	    <td><%=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(conf.getTimestamp())%></td>
            <td><%=conf.getId()%></td>
            <td><%=conf.getDID()%></td>
            <td><%=conf.getNumParcitipants()%></td>
            <td><%=conf.getName()%></td>
            <td><%=conf.getMixer().getName()%></td>
            <td>
<a href="viewConference.jsp?uid=<%=conf.getUID()%>"><img src="icons/eye.png"><span>Watch conference</span></a>
<a href="conference.jsp?uid=<%=conf.getUID()%>"><img src="icons/zoom.png"><span>Conference details</span></a>
<a href="#" onClick="removeConf('<%=conf.getUID()%>');return false;"><img src="icons/bin_closed.png"><span>Remove conference</span></a>
            </td>
        </tr><%
        }
        %>
    </table>
    <form><input class="add" type="button" onClick="document.location.href='createConference.jsp';return false;" value="Create"></form>
</fieldset>
<fieldset>
    <legend><img src="icons/film.png"> Broadcast List</legend>
        <table class="list">
        <tr>
            <th>ID</th>
            <th>Tag</th>
            <th>Name</th>
            <th>Mixer</th>
            <th>Actions</th>
        </tr>
        <%
        //Get mixer iterator
        Iterator<org.murillo.mcuWeb.Broadcast> itBcast = confMngr.getBroadcasts().values().iterator();
        //Loop
        while(itBcast.hasNext()) {
            // Get mixer
           org.murillo.mcuWeb.Broadcast bcast = itBcast.next();
            //Print values
            %>
        <tr>
            <td><%=bcast.getId()%></td>
            <td><%=bcast.getTag()%></td>
            <td><%=bcast.getName()%></td>
            <td><%=bcast.getMixer().getName()%></td>
            <td>
<a href="viewBroadcast.jsp?uid=<%=bcast.getUID()%>"><img src="icons/eye.png"><span>Watch broadcast</span></a>
<a href="#" onClick="removeBroadcast('<%=bcast.getUID()%>');return false;"><img src="icons/bin_closed.png"><span>Remove broadcast</span></a>
            </td>
        </tr><%
        }
        %>
    </table>
    <form><input class="add" type="button" onClick="document.location.href='createBroadcast.jsp';return false;" value="Create"></form>
</fieldset>
<%
    }
%>
