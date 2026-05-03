package com.dynamex.servlet;

import com.dynamex.service.AppState;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/finalize")
public class FinalizeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AppState.getInstance().clearCart();
        JsonUtil.write(resp, "{\"success\":true}");
    }
}
