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

@WebServlet("/api/payment")
public class PaymentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String totalStr = req.getParameter("total");
        String cashStr  = req.getParameter("cash");
        if (totalStr == null || cashStr == null) {
            writeFailure(resp, "Missing total or cash parameter");
            return;
        }

        long totalCent;
        long cashCent;
        try {
            totalCent = Long.parseLong(totalStr.trim());
            cashCent  = Long.parseLong(cashStr.trim());
        } catch (NumberFormatException e) {
            writeFailure(resp, "total and cash must be integer centavos");
            return;
        }

        if (cashCent < totalCent) {
            writeFailure(resp, "Cash tendered is less than the total due");
            return;
        }

        AppState state = AppState.getInstance();
        List<Denomination> denoms = state.getDenominations();

        TransactionEngine.TransactionResult result =
                state.getEngine().computeChange(totalCent / 100.0, cashCent / 100.0, denoms);

        if (result == null) {
            writeFailure(resp, "Invalid totals supplied");
            return;
        }
        if (!result.feasible || result.dpUnits < 0) {
            writeFailure(resp, result.message != null ? result.message
                    : "Cannot make exact change with the cash drawer's current quantities");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"success\":true,");
        sb.append("\"changeCentavos\":").append(result.changeCentavos).append(",");
        sb.append("\"totalUnits\":").append(result.dpUnits).append(",");
        sb.append("\"greedyUnits\":").append(result.greedyUnits).append(",");
        sb.append("\"unitsSaved\":").append(result.unitsSaved).append(",");
        sb.append("\"runtimeNanos\":").append(result.dpRuntimeNanos).append(",");

        sb.append("\"breakdown\":[");
        boolean first = true;
        for (Map.Entry<Integer,Integer> e : result.dpBreakdown.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            int denomCent  = e.getKey();
            Denomination d = state.findDenomination(denomCent);
            String label   = d != null ? d.getLabel() : ("P" + (denomCent / 100.0));
            boolean isBill = d != null && d.isBill();
            sb.append("{");
            sb.append("\"denom\":").append(denomCent).append(",");
            sb.append("\"label\":\"").append(JsonUtil.escape(label)).append("\",");
            sb.append("\"isBill\":").append(isBill).append(",");
            sb.append("\"count\":").append(e.getValue());
            sb.append("}");
        }
        sb.append("]");
        sb.append("}");

        JsonUtil.write(resp, sb.toString());
    }

    private void writeFailure(HttpServletResponse resp, String message) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"success\":false,\"message\":\"")
          .append(JsonUtil.escape(message))
          .append("\"}");
        JsonUtil.write(resp, sb.toString());
    }
}