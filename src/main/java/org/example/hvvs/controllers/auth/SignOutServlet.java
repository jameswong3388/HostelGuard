package org.example.hvvs.controllers.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.example.hvvs.util.CookieSessionParam;

import java.io.IOException;

@WebServlet("/signout")
public class SignOutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 设置session过期
        HttpSession session = req.getSession(false);
        if(session != null) {
            session.invalidate();
        }

        // 将自动登录相关的Cookie设置过期
        Cookie cookie = new Cookie(CookieSessionParam.COOKIE_AUTO_LOGIN, "every-thing-is-ok");
        cookie.setMaxAge(0);
        resp.addCookie(cookie);

        // 将页面重定向置登录界面
        resp.sendRedirect(getServletContext().getContextPath() + "/auth");
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        this.doPost(req, resp);
    }
}
