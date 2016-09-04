<%@page contentType="text/html"%>
<%@page pageEncoding="ISO-8859-1"%>
<%@page import="java.util.Vector"%> 
<%@page import="org.murillo.mcuWeb.ConferenceMngr"%>
<%
    //Get conference manager
    ConferenceMngr confMngr = (ConferenceMngr) getServletContext().getAttribute("confMngr");
%>
<fieldset>
    <legend><img src="icons/brick.png"> Media Mixer Data</legend>
    <form method="POST" action="controller/addMixer">
        <table class="form">
            <tr>
                <td>Id:</td>
                <td><input type="text" name="uid"></td>
            </tr>
            <tr>
                <td>Name:</td>
                <td><input type="text" name="name"></td>
            </tr>
            <tr>
                <td>Url:</td>
                <td><input type="text" name="url"></td>
            </tr>
            <tr>
                <td>Media Ip:</td>
                <td><input type="text" name="ip"></td>
            </tr>
	    <tr>
                <td>Public Ip:</td>
                <td><input type="text" name="publicIp"></td>
            </tr>
	    <tr>
                <td>Local Net:</td>
                <td><input type="text" name="localNet"></td>
            </tr>
        </table>
        <input class="accept" type="submit" value="Create">
        <input class="cancel" type="submit" onClick="document.location.href='index.jsp';return false;" value="Cancel">
    </form>
</fieldset>