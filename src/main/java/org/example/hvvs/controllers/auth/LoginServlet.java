package org.example.hvvs.controllers.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.hvvs.db.DBConnection;
import org.example.hvvs.model.User;
import org.example.hvvs.services.AuthServices;
import org.example.hvvs.services.AuthServicesImpl;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        req.setAttribute("error", "Email and password are required");
        req.getRequestDispatcher("index.jsp").forward(req, resp);

//        return;

//        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
//            req.setAttribute("error", "Email and password are required");
//            req.getRequestDispatcher("index.jsp").forward(req, resp);
//            return;
//        }
//
//        try {
//            AuthServices authServices = new AuthServicesImpl(DBConnection.getConn());
//            User user = authServices.signIn(email, password);
//
//            if (user != null) {
//                // User is authenticated, proceed to the next step
//                HttpSession session = req.getSession();
//                session.setAttribute("user", user);
//                resp.sendRedirect("dashboard.jsp"); // Redirect to the dashboard
//            } else {
//                // Authentication failed, show an error message
//                req.setAttribute("error", "Invalid email or password");
//                req.getRequestDispatcher("index.jsp").forward(req, resp);
//            }
//
//        } catch (Exception e) {
//            // Handle any exceptions that occur during sign in
//            req.setAttribute("error", "An error occurred during login: " + e.getMessage());
//            req.getRequestDispatcher("index.jsp").forward(req, resp);
//        }
    }
}
