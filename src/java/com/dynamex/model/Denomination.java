package com.dynamex.model;

public class Denomination {

    private final int valueCentavos;
    private final String label;
    private final boolean isBill;
    private boolean available;

    public Denomination(int valueCentavos, String label, boolean isBill, boolean available) {
        this.valueCentavos = valueCentavos;
        this.label = label;
        this.isBill = isBill;
        this.available = available;
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

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"denom\":").append(valueCentavos).append(",");
        sb.append("\"label\":\"").append(label).append("\",");
        sb.append("\"isBill\":").append(isBill).append(",");
        sb.append("\"available\":").append(available);
        sb.append("}");
        return sb.toString();
    }
}
