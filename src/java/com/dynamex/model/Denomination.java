package com.dynamex.model;

public class Denomination {

    private final int valueCentavos;
    private final String label;
    private final boolean isBill;
    private boolean available;
    private int quantity;

    public Denomination(int valueCentavos, String label, boolean isBill, boolean available) {
        this(valueCentavos, label, isBill, available, 0);
    }

    public Denomination(int valueCentavos, String label, boolean isBill, boolean available, int quantity) {
        this.valueCentavos = valueCentavos;
        this.label = label;
        this.isBill = isBill;
        this.available = available;
        this.quantity = Math.max(0, quantity);
    }

    public int getValue() {
        return valueCentavos;
    }
    public int getValueCentavos() {
        return valueCentavos;
    }

    public String getLabel() {
        return label;
    }

    public boolean isBill() {
        return isBill;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
    }

    public synchronized boolean deduct(int n) {
        if (n < 0 || n > quantity) return false;
        quantity -= n;
        return true;
    }

    public synchronized void add(int n) {
        if (n > 0) quantity += n;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"denom\":").append(valueCentavos).append(",");
        sb.append("\"label\":\"").append(label).append("\",");
        sb.append("\"isBill\":").append(isBill).append(",");
        sb.append("\"available\":").append(available).append(",");
        sb.append("\"quantity\":").append(quantity);
        sb.append("}");
        return sb.toString();
    }
}
