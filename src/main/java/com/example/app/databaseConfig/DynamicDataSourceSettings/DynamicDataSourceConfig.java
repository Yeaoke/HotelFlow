package com.example.app.databaseConfig.DynamicDataSourceSettings;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DynamicDataSourceConfig {

    private final DynamicDataSourceProperties properties;

    public DynamicDataSourceConfig(DynamicDataSourceProperties properties) {
        this.properties = properties;
    }

    @Bean
    public DataSource dataSource() {
        DynamicRoutingDataSource routingDataSource = new DynamicRoutingDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();

        if (properties.getDataSources() != null) {
            properties.getDataSources().forEach((key, prop) -> {
                HikariDataSource ds = new HikariDataSource();

                ds.setJdbcUrl(prop.getUrl());
                ds.setUsername(prop.getUsername());
                ds.setPassword(prop.getPassword());
                ds.setDriverClassName(prop.getDriverClassName());
                ds.validate();

                dataSourceMap.put(key, ds);
            });
        }

        routingDataSource.setTargetDataSources(dataSourceMap);
        
        DataSource defaultDs = (DataSource) dataSourceMap.get("tenant-reservation");
        if (defaultDs != null) {
            routingDataSource.setDefaultTargetDataSource(defaultDs);
        } else if (!dataSourceMap.isEmpty()) {
            routingDataSource.setDefaultTargetDataSource(dataSourceMap.values().iterator().next());
        }

        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }
}