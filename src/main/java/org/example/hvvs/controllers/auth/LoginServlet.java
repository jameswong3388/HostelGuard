package org.example.hvvs.controllers.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.hvvs.dao.UserDao;
import org.example.hvvs.dao.UserDaoImpl;
import org.example.hvvs.model.User;
import org.example.hvvs.services.AuthServices;
import org.example.hvvs.services.AuthServicesImpl;

import java.io.IOException;
import java.util.Random;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        boolean rememberMe = req.getParameter("rememberMe") != null;

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "Email and password are required");
            req.getRequestDispatcher("index.jsp").forward(req, res);
            return;
        }

        try {
            AuthServices authServices = new AuthServicesImpl();
            String result = authServices.signIn(email, password);
            if (result != null) {
                req.setAttribute("error", result);
                req.getRequestDispatcher("index.jsp").forward(req, res);
                return;
            }

            UserDao userDao = new UserDaoImpl();
            User user = userDao.findRange(email, UserDao.Type.EMAIL, false, 0, 1).getFirst();

            HttpSession session = req.getSession();
            session.setAttribute("user", user);
            res.sendRedirect("dashboard");

        } catch (Exception e) {
            // Handle any exceptions that occur during sign in
            req.setAttribute("error", "An error occurred during login: " + e.getMessage());
            req.getRequestDispatcher("index.jsp").forward(req, res);
        }
    }


    private String randomString(int n) {
        String base = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ~!@#$%^&*()_+=<>/,./;'[]{}|\\\\";
        int length = base.length();

        char[] buff = new char[n];
        Random r = new Random();
        for(int i = 0; i < n; i++) {
            buff[i] = base.charAt(r.nextInt(length));
        }

        return new String(buff);
    }
}
