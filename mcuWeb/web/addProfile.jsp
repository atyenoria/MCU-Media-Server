<%@page contentType="text/html"%>
<%@page pageEncoding="ISO-8859-1"%>
<%@page import="java.util.Iterator"%> 
<%@page import="org.murillo.mcuWeb.ConferenceMngr"%>
<%
    //Get conference manager
    ConferenceMngr confMngr = (ConferenceMngr) getServletContext().getAttribute("confMngr");
%>
<fieldset>
    <legend><img src="icons/image.png"> Conference</legend>
    <form method="POST" action="controller/addProfile">
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
                <td>Size:</td>
                <td><select name="videoSize">
                    <%
                        //Get sizes
                        java.util.HashMap<Integer,String> sizes = org.murillo.mcuWeb.MediaMixer.getSizes();
                        //Get iterator
                        Iterator<java.lang.Integer> itSizes = sizes.keySet().iterator();
                        //Loop 
                        while(itSizes.hasNext()) {
                            //Get key and value
                            Integer k = itSizes.next();
                            String v = sizes.get(k);
                            %><option value="<%=k%>"><%=v%><%
                        }
                    %>
                    </select>
            </tr>
            <tr>
                <td>Video bitrate:</td>
                <td><input type="text" name="videoBitrate"></td>
            </tr>
            <tr>
                <td>Video FPS:</td>
                <td><input type="text" name="videoFPS"></td>
            </tr>
	    <tr>
                <td>Intra Period:</td>
                <td><input type="text" name="intraPeriod"></td>
            </tr>
        </table>
        <input class="accept" type="submit" value="Create">
        <input class="cancel" type="submit" onClick="document.location.href='index.jsp';return false;" value="Cancel">
    </form>
</fieldset>