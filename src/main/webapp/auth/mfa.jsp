<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
  <title>MFA Verification - HostelGuard™</title>
  <!-- Tailwind CSS -->
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="flex min-h-screen items-center justify-center p-4 sm:p-6 bg-gray-50">

<div class="flex w-full flex-col items-start sm:max-w-sm">
  <div class="mt-6 flex flex-col w-full">
    <h2 class="text-xl font-semibold text-gray-900">
      Two-Factor Authentication
    </h2>
    <p class="mt-1 text-sm text-gray-700">
      <c:choose>
        <%-- If isRecoveryMode = "true", show text for recovery code --%>
        <c:when test="${param.isRecoveryMode == 'true'}">
          Enter your 8-digit recovery code
        </c:when>
        <%-- Otherwise, show text for verification code --%>
        <c:otherwise>
          Please enter the 6-digit code sent to your device
        </c:otherwise>
      </c:choose>
    </p>
  </div>

  <div class="mt-10 w-full">
    <form method="POST" action="<c:out value='${pageContext.request.contextPath}/auth/mfa'/>" class="flex w-full flex-col gap-y-6">
      <c:if test="${not empty errorMessage}">
        <div class="text-red-600 p-3 bg-red-50 rounded-md">
            ${errorMessage}
        </div>
      </c:if>

      <c:choose>
        <c:when test="${isBlocked}">
          <div class="text-red-600 font-medium p-3 bg-red-50 rounded-md">
            Account locked. Please try again in 15 minutes.
          </div>
        </c:when>
        <c:otherwise>
          <div class="flex flex-col gap-y-2">
            <!-- Choose appropriate label/input by isRecoveryMode -->
            <c:choose>
              <c:when test="${param.isRecoveryMode == 'true'}">
                <label class="font-medium">Recovery Code</label>
                <input type="text"
                       name="code"
                       placeholder="Enter 8-digit code"
                       pattern="\d{8}"
                       maxlength="8"
                       inputmode="numeric"
                       class="w-full p-2 border rounded-md text-center text-xl"
                       required />

                <!-- We might want to post `isRecoveryMode=true` if we want the server to know this is recovery mode. -->
                <input type="hidden" name="isRecoveryMode" value="true" />
              </c:when>
              <c:otherwise>
                <label class="font-medium">Verification Code</label>
                <input type="text"
                       name="code"
                       placeholder="Enter 6-digit code"
                       pattern="\d{6}"
                       maxlength="6"
                       inputmode="numeric"
                       class="w-full p-2 border rounded-md text-center text-xl"
                       required />
              </c:otherwise>
            </c:choose>
          </div>

          <button type="submit" class="w-full bg-blue-600 text-white p-2 rounded-md hover:bg-blue-700">
            Verify
          </button>

          <!-- Button that toggles between normal code and recovery code -->
          <div class="flex justify-center gap-2">
            <button type="button"
                    onclick="toggleRecoveryMode('${param.isRecoveryMode}')"
                    class="text-sm text-blue-600 hover:underline">
              <c:choose>
                <%-- If we're currently in Recovery mode, offer to go back to normal TOTP/email/etc. --%>
                <c:when test="${param.isRecoveryMode == 'true'}">
                  Use Verification Code
                </c:when>
                <%-- Else, offer to use Recovery mode --%>
                <c:otherwise>
                  Use Recovery Code
                </c:otherwise>
              </c:choose>
            </button>
          </div>
        </c:otherwise>
      </c:choose>
    </form>

    <!-- Help text -->
    <p class="text-sm text-gray-700 mt-4 text-center">
      Having trouble? Contact your administrator for assistance.
    </p>
  </div>
</div>

<!-- Toggle logic in JavaScript -->
<script>
  function toggleRecoveryMode(isRecoveryMode) {
    // isRecoveryMode is the string from param (e.g. "true" or "" or null if not present)
    const url = new URL(window.location.href);

    // If it's "true", remove param to go to normal code mode
    if (isRecoveryMode === 'true') {
      url.searchParams.delete('isRecoveryMode');
    }
    // Otherwise, set it to "true" to switch to recovery code mode
    else {
      url.searchParams.set('isRecoveryMode', 'true');
    }

    window.location.href = url.toString();
  }
</script>

</body>
</html>
