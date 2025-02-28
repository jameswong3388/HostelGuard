<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Reset Password - HostelGuard™</title>
    <!-- Tailwind CSS (via CDN) and Bootstrap Icons -->
    <script src="https://cdn.tailwindcss.com" type="text/javascript"></script>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
</head>
<body class="flex min-h-screen items-center justify-center p-4 sm:p-6 bg-gray-50">
<!-- Back button in the top-left corner -->
<div class="absolute top-0 left-0 m-3">
    <a href="<c:out value='${pageContext.request.contextPath}/auth/signIn'/>"
       class="flex items-center justify-center w-10 h-10 rounded-lg bg-blue-600 hover:bg-blue-700">
        <i class="bi bi-arrow-left text-white"></i>
    </a>
</div>

<!-- Main card container -->
<div class="flex w-full flex-col items-start sm:max-w-sm">
    <!-- Header text -->
    <div class="mt-6 flex flex-col w-full">
        <h2 class="text-xl font-semibold text-gray-900">
            Reset Your Password
        </h2>
        <p class="mt-1 text-sm text-gray-700">
            We'll send you a link to reset your password.
        </p>
    </div>

    <div class="mt-10 w-full">
        <!-- Error/Success messages -->
        <c:if test="${not empty errorMessage}">
            <div class="text-sm text-red-600 mb-4 p-3 bg-red-50 rounded-md">
                ${errorMessage}
            </div>
        </c:if>
        
        <c:if test="${not empty message}">
            <div class="text-sm text-green-600 mb-4 p-3 bg-green-50 rounded-md">
                ${message}
            </div>
        </c:if>
        
        <c:choose>
            <%-- Show password reset form when token is present in session --%>
            <c:when test="${not empty passwordResetToken}">
                <form action="<c:out value='${pageContext.request.contextPath}/auth/password-reset'/>" method="post" id="passwordResetForm">
                    <input type="hidden" name="action" value="reset">
                    
                    <!-- New password field -->
                    <div class="flex flex-col gap-y-2 mb-4">
                        <label for="password" class="font-medium">New Password</label>
                        <input type="password" id="password" name="password" placeholder="New password"
                               required class="w-full p-2 rounded border" />
                        <p class="text-xs text-gray-500">Password must be at least 8 characters long.</p>
                    </div>
                    
                    <!-- Confirm password field -->
                    <div class="flex flex-col gap-y-2 mb-4">
                        <label for="confirmPassword" class="font-medium">Confirm Password</label>
                        <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Confirm password"
                               required class="w-full p-2 rounded border" />
                    </div>
                    
                    <!-- Submit button -->
                    <button type="submit" id="resetBtn"
                            class="w-full bg-blue-600 p-2 rounded text-white hover:bg-blue-700 mb-4">
                        Reset Password
                    </button>
                </form>
            </c:when>
            
            <%-- Show email request form by default --%>
            <c:otherwise>
                <form action="<c:out value='${pageContext.request.contextPath}/auth/password-reset'/>" method="post">
                    <input type="hidden" name="action" value="request">
                    
                    <!-- Email field -->
                    <div class="flex flex-col gap-y-2 mb-4">
                        <label for="email" class="font-medium">Email Address</label>
                        <input type="email" id="email" name="email" placeholder="Your email address"
                               required class="w-full p-2 rounded border" />
                    </div>
                    
                    <!-- Submit button -->
                    <button type="submit" id="submitBtn"
                            class="w-full bg-blue-600 p-2 rounded text-white hover:bg-blue-700 mb-4">
                        Send Reset Link
                    </button>
                </form>
            </c:otherwise>
        </c:choose>
        
        <div class="text-center">
            <a href="<c:out value='${pageContext.request.contextPath}/auth/signIn'/>" class="text-sm text-blue-600 hover:text-blue-700">
                Back to Sign In
            </a>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    // For password reset form
    const passwordForm = document.getElementById('passwordResetForm');
    if (passwordForm) {
        passwordForm.addEventListener('submit', function(event) {
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            
            if (password.length < 8) {
                event.preventDefault();
                alert('Password must be at least 8 characters long.');
                return;
            }
            
            if (password !== confirmPassword) {
                event.preventDefault();
                alert('Passwords do not match.');
                return;
            }
            
            const resetBtn = document.getElementById('resetBtn');
            resetBtn.disabled = true;
            resetBtn.innerHTML = `Processing`;
        });
    }
    
    // For email request form
    const submitBtn = document.getElementById('submitBtn');
    if (submitBtn) {
        const form = submitBtn.closest('form');
        form.addEventListener('submit', function() {
            submitBtn.disabled = true;
            submitBtn.innerHTML = `Processing`;
        });
    }
});
</script>
</body>
</html> 