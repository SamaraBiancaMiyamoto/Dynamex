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

/**
 * Atomically finalises a transaction:
 *   1. Re-runs the bounded DP using the current drawer state.
 *   2. Deducts the optimal breakdown from the drawer.
 *   3. Clears the cart.
 *
 * Re-running on the server (instead of trusting a client-supplied
 * breakdown) prevents tampering and races against denomination edits.
 */
@WebServlet("/api/finalize")
public class FinalizeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AppState state = AppState.getInstance();
        List<Denomination> denoms = state.getDenominations();

        String totalStr = req.getParameter("total");
        String cashStr  = req.getParameter("cash");
        if (totalStr == null || cashStr == null) {
            JsonUtil.error(resp, 400, "Missing total or cash parameter");
            return;
        }

        long totalCent;
        long cashCent;
        try {
            totalCent = Long.parseLong(totalStr.trim());
            cashCent  = Long.parseLong(cashStr.trim());
        } catch (NumberFormatException nfe) {
            JsonUtil.error(resp, 400, "total and cash must be integer centavos");
            return;
        }

        if (cashCent < totalCent) {
            JsonUtil.error(resp, 400, "Cash tendered is less than total due");
            return;
        }

        synchronized (state) {
            TransactionEngine.TransactionResult result =
                    state.getEngine().computeChange(totalCent / 100.0, cashCent / 100.0, denoms);

            if (result == null || !result.feasible) {
                JsonUtil.error(resp, 409,
                        result != null && result.message != null
                                ? result.message
                                : "Cannot finalise: drawer cannot make change");
                return;
            }

            boolean ok = state.getEngine().applyBreakdown(result.dpBreakdown, denoms);
            if (!ok) {
                JsonUtil.error(resp, 409,
                        "Drawer state changed mid-transaction; please retry");
                return;
            }

            state.clearCart();

            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\":true,");
            sb.append("\"changeCentavos\":").append(result.changeCentavos).append(",");
            sb.append("\"totalUnits\":").append(result.dpUnits).append(",");
            sb.append("\"deducted\":[");
            boolean first = true;
            for (Map.Entry<Integer,Integer> e : result.dpBreakdown.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("{\"denom\":").append(e.getKey())
                  .append(",\"count\":").append(e.getValue()).append("}");
            }
            sb.append("]}");
            JsonUtil.write(resp, sb.toString());
        }
    }
}
