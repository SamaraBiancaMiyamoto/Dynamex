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
        if (valueStr == null) {
            JsonUtil.error(resp, 400, "Missing denom");
            return;
        }

        int value;
        try {
            value = Integer.parseInt(valueStr);
        } catch (NumberFormatException nfe) {
            JsonUtil.error(resp, 400, "Bad denom value");
            return;
        }

        Denomination d = AppState.getInstance().findDenomination(value);
        if (d == null) {
            JsonUtil.error(resp, 404, "Unknown denomination");
            return;
        }

        String availStr = req.getParameter("available");
        String qtyStr   = req.getParameter("quantity");
        String deltaStr = req.getParameter("delta");

        if (availStr == null && qtyStr == null && deltaStr == null) {
            JsonUtil.error(resp, 400, "Provide at least one of: available, quantity, delta");
            return;
        }

        if (availStr != null) {
            d.setAvailable(Boolean.parseBoolean(availStr));
        }

        try {
            if (qtyStr != null) {
                int q = Integer.parseInt(qtyStr);
                if (q < 0) {
                    JsonUtil.error(resp, 400, "quantity must be >= 0");
                    return;
                }
                d.setQuantity(q);
            }
            if (deltaStr != null) {
                int delta = Integer.parseInt(deltaStr);
                int next = d.getQuantity() + delta;
                if (next < 0) {
                    JsonUtil.error(resp, 400, "delta would drive quantity below zero");
                    return;
                }
                d.setQuantity(next);
            }
        } catch (NumberFormatException nfe) {
            JsonUtil.error(resp, 400, "Bad numeric parameter");
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
