package com.zeta.model.enums;

public enum PaymentCategory {
    SALARY("Salary"), VENDOR_PAYMENT("Vendor Payment"), CLIENT_PAYMENT("Client Payment");

    private final String displayName;

    PaymentCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}