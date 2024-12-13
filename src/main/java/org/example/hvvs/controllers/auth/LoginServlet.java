package org.example.hvvs.controllers.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.example.hvvs.dao.UserDao;
import org.example.hvvs.dao.UserDaoImpl;
import org.example.hvvs.model.User;
import org.example.hvvs.services.AuthServices;
import org.example.hvvs.services.AuthServicesImpl;
import org.example.hvvs.util.CookieSessionParam;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        boolean rememberMe = req.getParameter("rememberMe") != null;

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "Email and password are required");
            req.getRequestDispatcher("auth.jsp").forward(req, res);
            return;
        }

        try {
            AuthServices authServices = new AuthServicesImpl();
            String result = authServices.signIn(email, password);
            if (result != null) {
                req.setAttribute("error", result);
                req.getRequestDispatcher("auth.jsp").forward(req, res);
                return;
            }

            UserDao userDao = new UserDaoImpl();
            User user = userDao.findRange(email, UserDao.Type.EMAIL, false, 0, 1).getFirst();

            HttpSession session = req.getSession();
            session.setAttribute(CookieSessionParam.SESSION_ROLE, user.getRole());
            session.setAttribute(CookieSessionParam.SESSION_SELF, user);

            if (rememberMe) {
                String digest = authServices.getAutoLoginCookieValue(user.getUser_id().trim());

                if (digest != null) {
                    Cookie cookie = new Cookie(CookieSessionParam.COOKIE_AUTO_LOGIN, digest);
                    int timeInSecond = 60 * 60; // todo: Integer.parseInt(getServletConfig().getInitParameter("auto_login_time"));
                    cookie.setMaxAge(timeInSecond);
                    res.addCookie(cookie);
                }
            }

            switch (user.getRole()) {
                case CookieSessionParam.SESSION_ROLE_RESIDENT:
                    res.sendRedirect("resident/dashboard.jsp");
                    break;
                case CookieSessionParam.SESSION_ROLE_SECURITY_STAFF:
                    res.sendRedirect("security/dashboard.jsp");
                    break;
                case CookieSessionParam.SESSION_ROLE_ADMIN:
                    res.sendRedirect("admin/dashboard.jsp");
                    break;
                case CookieSessionParam.SESSION_ROLE_SUPER_ADMIN:
                    res.sendRedirect("admin/super/dashboard.jsp");
                    break;
                default:
                    req.setAttribute("error", "Invalid user role");
                    req.getRequestDispatcher("auth.jsp").forward(req, res);
            }
        } catch (Exception e) {
            // Handle any exceptions that occur during sign in
            req.setAttribute("error", "An error occurred during login: " + e.getMessage());
            req.getRequestDispatcher("auth.jsp").forward(req, res);
        }
    }
}
