package com.dynamex.service;

import com.dynamex.model.CartItem;
import com.dynamex.model.Denomination;
import com.dynamex.model.Product;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppState {

    private static AppState INSTANCE;

    public static synchronized AppState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppState();
        }
        return INSTANCE;
    }

    private InventoryManager inventory;
    private final TransactionEngine engine = new TransactionEngine();

    private final Map<Integer, CartItem> cart = new LinkedHashMap<>();

    private final List<Denomination> denominations = new ArrayList<>();

    public static final double TAX_RATE = 0.12;

    private AppState() {
        seedDenominations();
    }

    private void seedDenominations() {
        // Bills (label, available, starting quantity in drawer)
        denominations.add(new Denomination(100000, "P1000 Bill", true,  true, 10));
        denominations.add(new Denomination(50000,  "P500 Bill",  true,  true, 20));
        denominations.add(new Denomination(20000,  "P200 Bill",  true,  true, 25));
        denominations.add(new Denomination(10000,  "P100 Bill",  true,  true, 40));
        denominations.add(new Denomination(5000,   "P50 Bill",   true,  true, 40));
        // Coins
        denominations.add(new Denomination(2000,   "P20 Coin",   false, true, 50));
        denominations.add(new Denomination(1000,   "P10 Coin",   false, true, 60));
        denominations.add(new Denomination(500,    "P5 Coin",    false, true, 80));
        denominations.add(new Denomination(100,    "P1 Coin",    false, true, 100));
        denominations.add(new Denomination(25,     "P0.25 Coin", false, true, 100));
        denominations.add(new Denomination(10,     "P0.10 Coin", false, true, 120));
        denominations.add(new Denomination(5,      "P0.05 Coin", false, true, 150));
        denominations.add(new Denomination(1,      "P0.01 Coin", false, true, 200));
    }

    public void setInventory(InventoryManager im) {
        this.inventory = im;
    }

    public InventoryManager getInventory() {
        return inventory;
    }

    public TransactionEngine getEngine() {
        return engine;
    }

    public List<Denomination> getDenominations() {
        return denominations;
    }

    public Denomination findDenomination(int value) {
        for (Denomination d : denominations) {
            if (d.getValue() == value) return d;
        }
        return null;
    }

    // ---------------- Cart ----------------

    public synchronized List<CartItem> cartItems() {
        return new ArrayList<>(cart.values());
    }

    public synchronized CartItem addToCart(Product p, int qty) {
        if (qty <= 0) qty = 1;
        CartItem existing = cart.get(p.getId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + qty);
            return existing;
        }
        CartItem ci = new CartItem(p, qty);
        cart.put(p.getId(), ci);
        return ci;
    }

    public synchronized void removeFromCart(int id) {
        cart.remove(id);
    }

    public synchronized void updateQuantity(int id, int qty) {
        CartItem ci = cart.get(id);
        if (ci == null) return;
        if (qty <= 0) {
            cart.remove(id);
        } else {
            ci.setQuantity(qty);
        }
    }

    public synchronized void clearCart() {
        cart.clear();
    }

    public synchronized double subtotal() {
        double s = 0.0;
        for (CartItem ci : cart.values()) s += ci.getLineTotal();
        return round2(s);
    }

    public double tax(double subtotal) {
        return round2(subtotal * TAX_RATE);
    }

    public double total(double subtotal) {
        return round2(subtotal + tax(subtotal));
    }

    public static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}