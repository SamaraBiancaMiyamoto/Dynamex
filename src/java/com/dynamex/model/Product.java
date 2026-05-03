package com.dynamex.model;

public class Product {

    private final int id;
    private final String name;
    private final double price;
    private final int stock;

    public Product(int id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(id).append(",");
        sb.append("\"name\":\"").append(escape(name)).append("\",");
        sb.append("\"price\":").append(price).append(",");
        sb.append("\"stock\":").append(stock);
        sb.append("}");
        return sb.toString();
    }

    static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
