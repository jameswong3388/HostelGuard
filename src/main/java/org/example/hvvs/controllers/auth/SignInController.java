package org.example.hvvs.controllers.auth;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.example.hvvs.commonClasses.QueryWrapper;
import org.example.hvvs.dao.GenericDao;
import org.example.hvvs.model.User;
import org.example.hvvs.services.AuthServices;
import org.example.hvvs.util.CookieSessionParam;

import java.io.IOException;
import java.util.List;

@WebServlet("/login")
public class SignInController extends HttpServlet {

    @EJB
    private AuthServices authServices;

    @EJB
    private GenericDao dao;


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
            String result = authServices.signIn(email, password);
            if (result != null) {
                req.setAttribute("error", result);
                req.getRequestDispatcher("auth.jsp").forward(req, res);
                return;
            }

            QueryWrapper queryWrapper = new QueryWrapper("User.findByEmail").setParameter("email", email);
            List<User>  users = dao.findWithNamedQuery(queryWrapper);
            User user = users.getFirst();

            HttpSession session = req.getSession();
            session.setAttribute(CookieSessionParam.SESSION_ROLE, user.getRole());
            session.setAttribute(CookieSessionParam.SESSION_SELF, user);

            if (rememberMe) {
                String digest = authServices.getAutoLoginCookieValue(user.getId());

                if (digest != null) {
                    Cookie cookie = new Cookie(CookieSessionParam.COOKIE_AUTO_LOGIN, digest);
                    int timeInSecond = 60 * 60; // Adjust as needed
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
            req.setAttribute("error", "An error occurred during login: " + e.getMessage());
            req.getRequestDispatcher("auth.jsp").forward(req, res);
        }
    }
}
