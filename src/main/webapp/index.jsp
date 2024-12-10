<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>JSP - User Authentication</title>
    <%@include file="components/common_css.jsp" %>
</head>
<body>
<div class="container h-screen flex flex-col items-center justify-center">
    <div class="card">
        <div class="card-body">
            <h5 class="card-title font-medium text-lg">Welcome to Hostel Visitor Verification System</h5>
            <form class="row g-3">
                <div class="col-md-12">
                    <label for="email" class="form-label">Email</label>
                    <input type="email" class="form-control" id="email" placeholder="Your email">
                </div>
                <div class="col-md-12">
                    <label for="password" class="form-label">Password</label>
                    <input type="password" class="form-control" id="password" placeholder="Your password">
                </div>
                <div class="col-md-12">
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