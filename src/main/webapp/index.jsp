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
    <div class="card">
        <div class="card-body">
            <h5 class="card-title font-medium text-lg text-center">Welcome to Hostel Visitor Verification System</h5>
            <c:if test="${not empty requestScope.error}">
                <div class="alert alert-danger" role="alert">
                    ${requestScope.error}
                </div>
            </c:if>

            <form class="row g-3" action="login" method="post">
                <div class="col-md-12">
                    <label for="email" class="form-label">Email</label>
                    <input type="email" name="email" class="form-control" id="email" placeholder="Your email">
                </div>
                <div class="col-md-12">
                    <label for="password" class="form-label">Password</label>
                    <input type="password" name="password" class="form-control" id="password"
                           placeholder="Your password">
                </div>
                <div class="col-md-12">
                    <div class="form-check mb-3">
                        <input type="checkbox" class="form-check-input" id="rememberMe" name="rememberMe">
                        <label class="form-check-label" for="rememberMe">Remember me</label>
                    </div>
                    <button type="submit" class="btn btn-primary">Sign in with Email</button>
                </div>
                <div class="col-md-12 mt-3">
                    <a href="forgot-password.jsp" class="btn btn-link">Forgot Password?</a>
                </div>
            </form>
        </div>
    </div>
</div>
</body>
</html>