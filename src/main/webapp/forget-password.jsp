<%--
  Created by IntelliJ IDEA.
  User: jameswong
  Date: 13/12/2024
  Time: 8:38 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Forgot Password - HVVS</title>
    <%@include file="components/common_css.jsp" %>
</head>
<body>
    <div class="container h-screen flex flex-col items-center justify-center">
        <div class="text-center">
            <h1 class="text-4xl font-bold mb-4">Forgot Password?</h1>
            <p class="text-xl text-gray-600 mb-8">
                Please contact the system administrator to reset your password.
            </p>
            <div class="space-y-4">
                <div>
                    <a href="auth.jsp" class="btn btn-primary">
                        Back to Login
                        <i class="bi bi-arrow-left-circle ms-2"></i>
                    </a>
                </div>
                <div class="text-gray-500">
                    Contact: admin@hvvs.com
                </div>
            </div>
        </div>
    </div>
</body>
</html>
