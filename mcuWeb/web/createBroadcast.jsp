<%@page contentType="text/html"%>
<%@page pageEncoding="ISO-8859-1"%>
<%@page import="java.util.Iterator"%> 
<%@page import="java.util.HashMap"%> 
<%@page import="org.murillo.mcuWeb.ConferenceMngr"%>
<%
    //Get conference manager
    ConferenceMngr confMngr = (ConferenceMngr) getServletContext().getAttribute("confMngr");
%>
<fieldset>
    <legend><img src="icons/film_add.png"> Broadcast</legend>
    <form method="POST" action="controller/createBroadcast">
        <table class="form">
            <tr>
                <td>Name:</td>
                <td><input type="text" name="name"></td>
            </tr>
            <tr>
                <td>Tag:</td>
                <td><input type="text" name="tag"></td>
            </tr>
            <tr>
                <td>MediaMixer:</td>
                <td><select name="mixerId">
                    <%
                        //Get mixers
                        Iterator<org.murillo.mcuWeb.MediaMixer> it = confMngr.getMediaMixers().values().iterator();
                        //Loop 
                        while(it.hasNext()) {
                            // Get mixer
                            org.murillo.mcuWeb.MediaMixer mixer = it.next();
                            %><option value="<%=mixer.getUID()%>"><%=mixer.getName()%><%
                        }
                    %>
                </select></td>
            </tr>
        </table>
        <input class="accept" type="submit" value="Create">
        <input class="cancel" type="submit" onClick="document.location.href='index.jsp';return false;" value="Cancel">
    </form>
</fieldset>