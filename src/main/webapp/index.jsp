<%--
  Created by IntelliJ IDEA.
  User: jameswong
  Date: 06/02/2025
  Time: 4:55 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>HostelGuard™ Portal</title>
    <meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">

    <link rel="stylesheet" type="text/css"  href="${pageContext.request.contextPath}/index.css?v=1" />
</head>
<body>
<div id="container">
    <span id="text1"></span>
    <span id="text2"></span>
</div>

<!-- The SVG filter used to create the merging effect -->
<svg id="filters">
    <defs>
        <filter id="threshold">
            <!-- Basically just a threshold effect - pixels with a high enough opacity are set to full opacity, and all other pixels are set to completely transparent. -->
            <feColorMatrix in="SourceGraphic"
                           type="matrix"
                           values="1 0 0 0 0
									0 1 0 0 0
									0 0 1 0 0
									0 0 0 255 -140" />
        </filter>
    </defs>
</svg>

<script src="index.js"></script>
</body>
</html>
