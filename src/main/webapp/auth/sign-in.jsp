<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Login - HostelGuard™</title>
    <!-- Tailwind CSS (via CDN) and Bootstrap Icons -->
    <script src="https://cdn.tailwindcss.com" type="text/javascript"></script>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
</head>
<body class="flex min-h-screen items-center justify-center p-4 sm:p-6 bg-gray-50">
<!-- Back button in the top-left corner -->
<div class="absolute top-0 left-0 m-3">
    <a href="<%= request.getContextPath() %>/index.jsp"
       class="flex items-center justify-center w-10 h-10 rounded-lg bg-blue-600 hover:bg-blue-700">
        <i class="bi bi-arrow-left text-white"></i>
    </a>
</div>

<!-- Main card container -->
<div class="flex w-full flex-col items-start sm:max-w-sm">
    <!-- Header text -->
    <div class="mt-6 flex flex-col w-full">
        <h2 class="text-xl font-semibold text-gray-900">
            Welcome to HostelGuard™
        </h2>
        <p class="mt-1 text-sm text-gray-700">
            Don't have an account?
            <a href="#" class="text-blue-600 hover:text-blue-700">
                Contact your administrator
            </a>
        </p>
    </div>

    <div class="mt-10 w-full">
        <!-- Login form -->
        <!-- NOTE: The action below posts to our SignInServlet at /auth/signIn -->
        <form action="<c:out value='${pageContext.request.contextPath}/auth/signIn'/>" method="post" onsubmit="handleSignIn(event)">
            <!-- Email/Username field -->
            <div class="flex flex-col gap-y-2 mb-4">
                <label for="identifier" class="font-medium">Email or Username</label>
                <input type="text" id="identifier" name="identifier" placeholder="Email or username"
                       required class="w-full p-2 rounded border" />
            </div>

            <!-- Password field -->
            <div class="flex flex-col gap-y-2 mb-4">
                <label for="password" class="font-medium">Password</label>
                <input type="password" id="password" name="password" placeholder="Password"
                       required class="w-full p-2 rounded border" />
            </div>

            <!-- Dynamic error messages -->
            <c:if test="${not empty errorMessage}">
                <div class="text-sm text-red-600 mb-4 p-3 bg-red-50 rounded-md">
                    ${errorMessage}
                </div>
            </c:if>

            <!-- Continue button -->
            <button type="submit" id="submitBtn"
                    class="w-full bg-blue-600 p-2 rounded text-white hover:bg-blue-700 mb-4">
                Continue
            </button>

            <!-- Messages placeholder -->
            <div id="messages"></div>
        </form>

        <!-- Forgot password link -->
        <p class="text-sm text-gray-700">
            Forgot your password?
            <a href="#" class="text-blue-600 hover:text-blue-700">
                Reset password
            </a>
        </p>
    </div>
</div>

<script>
function handleSignIn(e) {
    const submitBtn = document.getElementById('submitBtn');
    submitBtn.disabled = true;
    submitBtn.innerHTML = `Processing`;
}
</script>
</body>
</html>
