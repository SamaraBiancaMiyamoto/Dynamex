package com.dynamex.model;

public class CartItem {

    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getLineTotal() {
        return product.getPrice() * quantity;
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(product.getId()).append(",");
        sb.append("\"name\":\"").append(Product.escape(product.getName())).append("\",");
        sb.append("\"price\":").append(product.getPrice()).append(",");
        sb.append("\"stock\":").append(product.getStock()).append(",");
        sb.append("\"quantity\":").append(quantity).append(",");
        sb.append("\"lineTotal\":").append(getLineTotal());
        sb.append("}");
        return sb.toString();
    }
}