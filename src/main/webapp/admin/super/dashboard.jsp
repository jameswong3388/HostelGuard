<%--
  Created by IntelliJ IDEA.
  User: jameswong
  Date: 12/12/2024
  Time: 1:33 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Admin Dashboard</title>
    <jsp:include page="/components/common_css.jsp" />
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <!-- Sidebar using Bootstrap classes -->
        <div class="col-md-3 col-lg-2 d-md-block bg-dark text-white vh-100 p-0">
            <div class="sticky-top">
                <div class="py-3 text-center">
                    <i class="bi bi-shield-lock"></i>
                    <span class="fs-5">HVVS Super Admin</span>
                </div>
                <hr class="text-white">
                <ul class="nav flex-column">
                    <li class="nav-item">
                        <a class="nav-link active text-white" href="#">
                            <i class="bi bi-speedometer2 me-2"></i>
                            Dashboard
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link text-white" href="#">
                            <i class="bi bi-people me-2"></i>
                            Users Management
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link text-white" href="#">
                            <i class="bi bi-gear me-2"></i>
                            Settings
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link text-white" href="#">
                            <i class="bi bi-bar-chart me-2"></i>
                            Reports
                        </a>
                    </li>
                    <li class="nav-item mt-3">
                        <a class="nav-link text-danger" href="http://localhost:8080/HVVS-1.0-SNAPSHOT/signout">
                            <i class="bi bi-box-arrow-right me-2"></i>
                            Logout
                        </a>
                    </li>
                </ul>
            </div>
        </div>

        <!-- Main Content -->
        <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h1 class="h2">Dashboard</h1>
                <div class="btn-toolbar mb-2 mb-md-0">
                    <div class="btn-group me-2">
                        <button type="button" class="btn btn-sm btn-outline-secondary">Share</button>
                        <button type="button" class="btn btn-sm btn-outline-secondary">Export</button>
                    </div>
                </div>
            </div>

            <!-- Dashboard content -->
            <div>
                Welcome to the admin dashboard
            </div>
        </main>
    </div>
</div>
</body>
</html>
