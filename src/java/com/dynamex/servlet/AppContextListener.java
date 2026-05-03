package com.dynamex.servlet;

import com.dynamex.model.Product;
import com.dynamex.service.AppState;
import com.dynamex.service.CSVLoader;
import com.dynamex.service.InventoryManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.InputStream;
import java.util.List;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        String csvPath = "/data/products.csv";
        ctx.log("AlgoPOS: Trying to load CSV from " + csvPath + " ...");

        try (InputStream in = ctx.getResourceAsStream(csvPath)) {

            if (in == null) {
                throw new RuntimeException("products.csv NOT FOUND at " + csvPath);
            }

            ctx.log("CSV FOUND. Loading products...");

            long start = System.nanoTime();

            // 🔥 Load CSV
            List<Product> products = CSVLoader.load(in);

            long durationMs = (System.nanoTime() - start) / 1_000_000;

            // 🔥 Initialize inventory manager
            InventoryManager inventoryManager = new InventoryManager(products);

            AppState.getInstance().setInventory(inventoryManager);

            ctx.log("✅ AlgoPOS: Loaded " + products.size() + " products in "
                    + durationMs + " ms (sorted for binary search)");

        } catch (Exception e) {
            ctx.log("❌ AlgoPOS: FAILED to load inventory", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().log("🛑 AlgoPOS shutting down...");
    }
}
