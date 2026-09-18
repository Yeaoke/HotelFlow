package com.example.app.databaseConfig.DynamicDataSourceSettings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.activation.DataSource;

@Configuration
public class DynamicDataSourceConfig {
    
    private DynamicDataSourceProperties properties;

    private Map<Object, Object> dataSourceMap = new ConcurrentHashMap<>();
    
    public DynamicDataSourceConfig(Map<Object, Object> dataSourceMap) {
        this.dataSourceMap = dataSourceMap;
    }

    @Bean 
    public DataSource dataSource() {
        DynamicRoutingDataSource routingDataSource = new DynamicRoutingDataSource();

        properties.getDataSources().forEach(
            (key, prop) -> {
                DataSource ds = createDataSource(prop);
                dataSourceMap.put(key, ds);
            }
        );

        routingDataSource.setTargetDataSources(dataSourceMap);
        if (!dataSourceMap.isEmpty()) {
            routingDataSource.setDefaultTargetDataSource(dataSourceMap.values().iterator().next());
        }

        routingDataSource.afterPropertiesSet();
        return (DataSource) routingDataSource;
    }

    public void addDataSource(String key, DataSourceProperties props) {
        DataSource ds = createDataSource(props);
        dataSourceMap.put(key, ds);

        ((DynamicRoutingDataSource) dataSource()).setTargetDataSources(dataSourceMap);
        ((DynamicRoutingDataSource) dataSource()).afterPropertiesSet();
    }

    private DataSource createDataSource(DataSourceProperties prop) {
        return (DataSource) prop.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }
}
