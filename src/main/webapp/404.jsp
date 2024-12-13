<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>404 - Page Not Found</title>
    <%@include file="components/common_css.jsp" %>
</head>
<body>
    <div class="container h-screen flex flex-col items-center justify-center">
        <div class="text-center">
            <h1 class="text-9xl font-bold text-primary">404</h1>
            <h2 class="text-4xl font-medium mt-4">Page Not Found</h2>
            <p class="text-gray-600 mt-2">The page you are looking for doesn't exist or has been moved.</p>
            <div class="mt-6">
                <a href="index.jsp" class="btn btn-primary">Go Home</a>
            </div>
        </div>
    </div>
</body>
</html>
