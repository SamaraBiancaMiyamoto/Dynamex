package com.dynamex.servlet;

import com.dynamex.model.CartItem;
import com.dynamex.model.Product;
import com.dynamex.service.AppState;
import com.dynamex.service.InventoryManager;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/cart")
public class CartServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        writeCart(resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AppState state = AppState.getInstance();
        String action = req.getParameter("action");
        if (action == null) {
            JsonUtil.error(resp, 400, "Missing action");
            return;
        }

        try {
            switch (action) {
                case "add": {
                    int id  = Integer.parseInt(req.getParameter("id"));
                    int qty = parseQty(req.getParameter("qty"), 1);
                    InventoryManager im = state.getInventory();
                    if (im == null) {
                        JsonUtil.error(resp, 503, "Inventory not loaded");
                        return;
                    }
                    Product p = im.binarySearch(id);
                    if (p == null) {
                        JsonUtil.error(resp, 404, "Product not found");
                        return;
                    }
                    state.addToCart(p, qty);
                    break;
                }
                case "update": {
                    int id  = Integer.parseInt(req.getParameter("id"));
                    int qty = parseQty(req.getParameter("qty"), 1);
                    state.updateQuantity(id, qty);
                    break;
                }
                case "remove": {
                    int id = Integer.parseInt(req.getParameter("id"));
                    state.removeFromCart(id);
                    break;
                }
                case "clear": {
                    state.clearCart();
                    break;
                }
                default:
                    JsonUtil.error(resp, 400, "Unknown action: " + action);
                    return;
            }
        } catch (NumberFormatException nfe) {
            JsonUtil.error(resp, 400, "Bad numeric parameter");
            return;
        }

        writeCart(resp);
    }

    private static int parseQty(String s, int dflt) {
        if (s == null || s.isEmpty()) return dflt;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return dflt; }
    }

    private void writeCart(HttpServletResponse resp) throws IOException {
        AppState state = AppState.getInstance();
        List<CartItem> items = state.cartItems();
        double subtotal = state.subtotal();
        double tax      = state.tax(subtotal);
        double total    = state.total(subtotal);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"items\":[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(items.get(i).toJson());
        }
        sb.append("],");
        sb.append("\"subtotal\":").append(subtotal).append(",");
        sb.append("\"tax\":").append(tax).append(",");
        sb.append("\"taxRate\":").append(AppState.TAX_RATE).append(",");
        sb.append("\"total\":").append(total);
        sb.append("}");

        JsonUtil.write(resp, sb.toString());
    }
}
