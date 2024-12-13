<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <title>Hostel Visitor Verification System</title>
    <%@include file="components/common_css.jsp" %>
</head>
<body>
<div class="container h-screen flex flex-col items-center justify-center">
    <div class="text-center">
        <h1 class="text-4xl font-bold mb-4">Welcome to Hostel Visitor Verification System</h1>
        <p class="text-xl text-gray-600 mb-8">
            A secure and efficient way to manage hostel visitors
        </p>
        <div class="space-y-4">
            <div>
                <a href="auth" class="btn btn-primary btn-lg px-8">
                    Login
                    <i class="bi bi-arrow-right-circle ms-2"></i>
                </a>
            </div>
            <div class="text-gray-500">
                Need help? Contact system administrator
            </div>
        </div>
    </div>
</div>
</body>
</html>