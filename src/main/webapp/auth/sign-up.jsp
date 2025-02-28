<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Sign Up - HostelGuard™</title>
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
<div class="flex w-full flex-col items-start sm:max-w-md">
    <!-- Header text -->
    <div class="mt-6 flex flex-col w-full">
        <h2 class="text-xl font-semibold text-gray-900">
            Request a HostelGuard™ Account
        </h2>
        <p class="mt-1 text-sm text-gray-700">
            Already have an account?
            <a href="<c:out value='${pageContext.request.contextPath}/auth/sign-in.jsp'/>" class="text-blue-600 hover:text-blue-700">
                Sign in here
            </a>
        </p>
    </div>

    <div class="mt-10 w-full">
        <!-- Registration form -->
        <!-- The action below posts to our SignUpServlet at /auth/signUp -->
        <form action="<c:out value='${pageContext.request.contextPath}/auth/signUp'/>" method="post" onsubmit="handleSignUp(event)">
            <!-- Personal Information Section -->
            <div class="mb-6">
                <h3 class="text-md font-semibold text-gray-800 mb-4">Personal Information</h3>
                
                <!-- Username field -->
                <div class="flex flex-col gap-y-2 mb-4">
                    <label for="username" class="font-medium">Username</label>
                    <input type="text" id="username" name="username" placeholder="Choose a username"
                        required class="w-full p-2 rounded border" />
                </div>

                <!-- First Name field -->
                <div class="flex flex-col gap-y-2 mb-4">
                    <label for="firstName" class="font-medium">First Name</label>
                    <input type="text" id="firstName" name="firstName" placeholder="First name"
                        required class="w-full p-2 rounded border" />
                </div>

                <!-- Last Name field -->
                <div class="flex flex-col gap-y-2 mb-4">
                    <label for="lastName" class="font-medium">Last Name</label>
                    <input type="text" id="lastName" name="lastName" placeholder="Last name"
                        required class="w-full p-2 rounded border" />
                </div>

                <!-- Email field -->
                <div class="flex flex-col gap-y-2 mb-4">
                    <label for="email" class="font-medium">Email</label>
                    <input type="email" id="email" name="email" placeholder="Your email address"
                        required class="w-full p-2 rounded border" />
                </div>

                <!-- Phone Number field -->
                <div class="flex flex-col gap-y-2 mb-4">
                    <label for="phoneNumber" class="font-medium">Phone Number</label>
                    <input type="tel" id="phoneNumber" name="phoneNumber" placeholder="Your phone number"
                        required class="w-full p-2 rounded border" />
                </div>

                <!-- Identity Number field -->
                <div class="flex flex-col gap-y-2 mb-4">
                    <label for="identityNumber" class="font-medium">Identity Number</label>
                    <input type="text" id="identityNumber" name="identityNumber" placeholder="ID number"
                        required class="w-full p-2 rounded border" />
                </div>

                <!-- Address field -->
                <div class="flex flex-col gap-y-2 mb-4">
                    <label for="address" class="font-medium">Address</label>
                    <textarea id="address" name="address" placeholder="Your address" rows="2"
                        class="w-full p-2 rounded border"></textarea>
                </div>

                <!-- Gender field -->
                <div class="flex flex-col gap-y-2 mb-4">
                    <label for="gender" class="font-medium">Gender</label>
                    <select id="gender" name="gender" class="w-full p-2 rounded border">
                        <option value="MALE">Male</option>
                        <option value="FEMALE">Female</option>
                        <option value="OTHER">Other</option>
                    </select>
                </div>
            </div>

            <!-- Account Type Section -->
            <div class="mb-6">
                <h3 class="text-md font-semibold text-gray-800 mb-4">Account Type</h3>
                
                <!-- Role field -->
                <div class="flex flex-col gap-y-2 mb-4">
                    <label for="role" class="font-medium">Requested Role</label>
                    <select id="role" name="role" class="w-full p-2 rounded border">
                        <option value="RESIDENT">Resident</option>
                        <option value="SECURITY_STAFF">Security Staff</option>
                        <option value="MANAGING_STAFF">Managing Staff</option>
                    </select>
                </div>
            </div>

            <!-- Additional Information -->
            <div class="mb-6">
                <h3 class="text-md font-semibold text-gray-800 mb-4">Additional Information</h3>
                
                <!-- Additional information field -->
                <div class="flex flex-col gap-y-2 mb-4">
                    <label for="additionalInfo" class="font-medium">Remarks</label>
                    <textarea id="additionalInfo" name="additionalInfo" 
                        placeholder="Add any additional information for administrators to review" 
                        rows="3" class="w-full p-2 rounded border"></textarea>
                </div>
            </div>

            <!-- Dynamic error messages -->
            <c:if test="${not empty errorMessage}">
                <div class="text-sm text-red-600 mb-4 p-3 bg-red-50 rounded-md">
                    ${errorMessage}
                </div>
            </c:if>

            <!-- Success message -->
            <c:if test="${not empty successMessage}">
                <div class="text-sm text-green-600 mb-4 p-3 bg-green-50 rounded-md">
                    ${successMessage}
                </div>
            </c:if>

            <!-- Submit button -->
            <button type="submit" id="submitBtn"
                    class="w-full bg-blue-600 p-2 rounded text-white hover:bg-blue-700 mb-4">
                Submit Account Request
            </button>

            <!-- Messages placeholder -->
            <div id="messages"></div>
        </form>
    </div>
</div>

<script>
function handleSignUp(e) {
    const submitBtn = document.getElementById('submitBtn');
    submitBtn.disabled = true;
    submitBtn.innerHTML = `Processing`;
}
</script>
</body>
</html> 