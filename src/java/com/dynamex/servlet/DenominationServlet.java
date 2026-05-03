package com.dynamex.servlet;

import com.dynamex.model.Denomination;
import com.dynamex.service.AppState;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/denominations")
public class DenominationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        write(resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Accept both `denom` (front-end) and `value` (legacy) parameter names.
        String valueStr = req.getParameter("denom");
        if (valueStr == null) valueStr = req.getParameter("value");
        String availStr = req.getParameter("available");
        if (valueStr == null || availStr == null) {
            JsonUtil.error(resp, 400, "Missing denom or available");
            return;
        }
        try {
            int value = Integer.parseInt(valueStr);
            boolean avail = Boolean.parseBoolean(availStr);
            Denomination d = AppState.getInstance().findDenomination(value);
            if (d == null) {
                JsonUtil.error(resp, 404, "Unknown denomination");
                return;
            }
            d.setAvailable(avail);
        } catch (NumberFormatException nfe) {
            JsonUtil.error(resp, 400, "Bad denom value");
            return;
        }
        write(resp);
    }

    private void write(HttpServletResponse resp) throws IOException {
        List<Denomination> all = AppState.getInstance().getDenominations();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"denominations\":[");
        for (int i = 0; i < all.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(all.get(i).toJson());
        }
        sb.append("]}");
        JsonUtil.write(resp, sb.toString());
    }
}