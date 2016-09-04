<%@page contentType="text/html"%>
<%@page pageEncoding="ISO-8859-1"%>
<%@page import="java.util.UUID"%>
<%@page import="org.murillo.mcuWeb.ConferenceMngr"%>
<%
    //Get conference manager
    ConferenceMngr confMngr = (ConferenceMngr) getServletContext().getAttribute("confMngr");
    //Get the conference id
    String uid = request.getParameter("uid");
    //Add token and get RTMPUrl
    org.murillo.mcuWeb.RTMPUrl rtmpUrl = confMngr.addConferenceToken(uid);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>View Broadcast</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/swfobject/2.2/swfobject_src.js"></script>
<script type="text/javascript">
    var player = null;
        var flashvars = {};
        flashvars.serverUrl = "<%=rtmpUrl.getUrl()%>";
        flashvars.streamName = "<%=rtmpUrl.getStream()%>";

    var params = {};
        params.wmode = "window";
        params.menu = "false";
	params.allowFullScreen = "true";
	params.allowScriptAccess = "always";
	params.swliveconnect = "true";

        swfobject.embedSWF("video.swf", "movieContainer", "640", "480", "9.0.0","expressInstall.swf", flashvars, params, null, function(result){
        player = result.ref;
       });
</script>
</head>
<body>
<div id="movieContainer">
</div>
</body>
</html>