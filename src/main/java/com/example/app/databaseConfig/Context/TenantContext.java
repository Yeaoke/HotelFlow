package com.example.app.databaseConfig.Context;

public class TenantContext {
    
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void setCurrentTenant(String tenantId) {
        CONTEXT.set(tenantId);
    }

    public static String getCurrentTenant() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
