package com.dynamex.servlet;

import com.dynamex.model.Denomination;
import com.dynamex.service.AppState;
import com.dynamex.service.InventoryManager;
import com.dynamex.service.TransactionEngine;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/metrics")
public class MetricsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AppState state = AppState.getInstance();
        InventoryManager im = state.getInventory();
        if (im == null) {
            JsonUtil.error(resp, 503, "Inventory not loaded");
            return;
        }

        int n = im.size();
        
        // Sample probe IDs from across the inventory range so the benchmarks
        // reflect realistic worst/average behavior.
        int[] probes = new int[] {
                1,
                Math.max(1, n / 4),
                Math.max(1, n / 2),
                Math.max(1, (3 * n) / 4),
                n,
                n + 999_999    
        };

        long binaryTotal = 0, linearTotal = 0;
        int  binarySteps = 0, linearSteps = 0;
        for (int probe : probes) {
            im.binarySearch(probe);
            binaryTotal += im.getLastSearchNanos();
            binarySteps += im.getLastSearchSteps();
            im.linearSearch(probe);
            linearTotal += im.getLastLinearNanos();
            linearSteps += im.getLastLinearSteps();
        }
        long binaryAvgNs = binaryTotal / probes.length;
        long linearAvgNs = linearTotal / probes.length;
        int  binaryAvgComp = binarySteps / probes.length;
        int  theoreticalMax = (int) Math.ceil(Math.log(n) / Math.log(2));

        TransactionEngine.TransactionResult tr =
                state.getEngine().computeChange(1432.50, 2000.00, state.getDenominations());

        int dpUnits     = tr == null ? -1 : tr.dpUnits;
        int greedyUnits = tr == null ? -1 : tr.greedyUnits;

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"datasetSize\":").append(n).append(",");
        sb.append("\"binaryTimeNs\":").append(binaryAvgNs).append(",");
        sb.append("\"binaryComparisons\":").append(binaryAvgComp).append(",");
        sb.append("\"theoreticalMaxComparisons\":").append(theoreticalMax).append(",");
        sb.append("\"linearTimeNs\":").append(linearAvgNs).append(",");
        sb.append("\"linearComparisons\":").append(linearSteps / probes.length).append(",");
        sb.append("\"dpUnits\":").append(dpUnits).append(",");
        sb.append("\"greedyUnits\":").append(greedyUnits);
        sb.append("}");

        JsonUtil.write(resp, sb.toString());
    }
}
