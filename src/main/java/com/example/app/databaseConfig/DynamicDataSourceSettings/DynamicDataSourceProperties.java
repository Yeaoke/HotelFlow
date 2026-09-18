package com.example.app.databaseConfig.DynamicDataSourceSettings;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.dynamic")
public class DynamicDataSourceProperties {

    private Map<String, DataSourceProperties> datasources = new HashMap<>();

    public Map<String, DataSourceProperties> getDataSources() {
        return datasources;
    }

    public void setDataSources(Map<String, DataSourceProperties> dataSources) {
        this.datasources = dataSources;
    }
}