package com.example.app.databaseConfig.DynamicDataSourceSettings;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.example.app.databaseConfig.Context.TenantContext;

public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getCurrentTenant();
    }
    
}
