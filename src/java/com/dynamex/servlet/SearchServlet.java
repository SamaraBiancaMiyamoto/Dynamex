package com.dynamex.servlet;

import com.dynamex.model.Product;
import com.dynamex.service.AppState;
import com.dynamex.service.InventoryManager;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/search")
public class SearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            JsonUtil.error(resp, 400, "Missing 'id' parameter");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            JsonUtil.error(resp, 400, "Product ID must be a number");
            return;
        }

        InventoryManager im = AppState.getInstance().getInventory();
        if (im == null) {
            JsonUtil.error(resp, 503, "Inventory not loaded");
            return;
        }

        Product p = im.binarySearch(id);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"success\":").append(p != null).append(",");
        sb.append("\"comparisons\":").append(im.getLastSearchSteps()).append(",");
        sb.append("\"timeNs\":").append(im.getLastSearchNanos()).append(",");
        sb.append("\"datasetSize\":").append(im.size());
        if (p != null) {
            sb.append(",\"product\":").append(p.toJson());
        }
        sb.append("}");

        JsonUtil.write(resp, sb.toString());
    }
}
