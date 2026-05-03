package com.dynamex.servlet;

import com.dynamex.model.Denomination;
import com.dynamex.service.AppState;
import com.dynamex.service.TransactionEngine;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/checkout")
public class CheckoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AppState state = AppState.getInstance();

        if (state.cartItems().isEmpty()) {
            JsonUtil.error(resp, 400, "Cart is empty");
            return;
        }

        String cashStr = req.getParameter("cash");
        if (cashStr == null || cashStr.trim().isEmpty()) {
            JsonUtil.error(resp, 400, "Missing cash tendered");
            return;
        }

        double cash;
        try {
            cash = Double.parseDouble(cashStr.trim());
        } catch (NumberFormatException e) {
            JsonUtil.error(resp, 400, "Cash must be a number");
            return;
        }

        double subtotal = state.subtotal();
        double tax      = state.tax(subtotal);
        double total    = state.total(subtotal);

        if (cash < total) {
            StringBuilder err = new StringBuilder();
            err.append("{\"error\":\"Insufficient cash tendered\",");
            err.append("\"subtotal\":").append(subtotal).append(",");
            err.append("\"tax\":").append(tax).append(",");
            err.append("\"total\":").append(total).append(",");
            err.append("\"cash\":").append(cash).append("}");
            resp.setStatus(400);
            JsonUtil.write(resp, err.toString());
            return;
        }

        List<Denomination> available = state.getDenominations();
        TransactionEngine.TransactionResult result =
                state.getEngine().computeChange(total, cash, available);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"subtotal\":").append(subtotal).append(",");
        sb.append("\"tax\":").append(tax).append(",");
        sb.append("\"taxRate\":").append(AppState.TAX_RATE).append(",");
        sb.append("\"total\":").append(total).append(",");
        sb.append("\"cash\":").append(cash).append(",");
        sb.append("\"change\":").append(result.changeCentavos / 100.0).append(",");
        sb.append("\"dpUnits\":").append(result.dpUnits).append(",");
        sb.append("\"greedyUnits\":").append(result.greedyUnits).append(",");
        sb.append("\"unitsSaved\":").append(result.unitsSaved).append(",");
        sb.append("\"runtimeNanos\":").append(result.dpRuntimeNanos).append(",");
        sb.append("\"success\":").append(result.dpUnits >= 0).append(",");

        sb.append("\"breakdown\":[");
        boolean first = true;
        for (Map.Entry<Integer,Integer> e : result.dpBreakdown.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            int valueCent  = e.getKey();
            Denomination d = state.findDenomination(valueCent);
            String label   = d != null ? d.getLabel() : ("P" + (valueCent / 100.0));
            boolean isBill = d != null && d.isBill();
            sb.append("{");
            sb.append("\"denom\":").append(valueCent).append(",");
            sb.append("\"label\":\"").append(JsonUtil.escape(label)).append("\",");
            sb.append("\"isBill\":").append(isBill).append(",");
            sb.append("\"count\":").append(e.getValue());
            sb.append("}");
        }
        sb.append("]");
        sb.append("}");

        JsonUtil.write(resp, sb.toString());
    }
}
