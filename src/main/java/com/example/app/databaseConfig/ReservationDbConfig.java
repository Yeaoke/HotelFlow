package com.example.app.databaseConfig;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.example.app.repos.main",
    entityManagerFactoryRef = "reservationEntityManagerFactory",
    transactionManagerRef = "reservationTransactionManager"
)
public class ReservationDbConfig {

    @Value("${spring.dynamic.datasources.tenant-reservation.url}")
    private String dbUrl;

    @Value("${spring.dynamic.datasources.tenant-reservation.username}")
    private String dbUsername;

    @Value("${spring.dynamic.datasources.tenant-reservation.password}")
    private String dbPassword;

    @Bean(name = "reservationDataSource")
    public DataSource tokenDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(dbUrl);
        ds.setUsername(dbUsername);
        ds.setPassword(dbPassword);
        ds.setDriverClassName("org.postgresql.Driver");
        return ds;
    }

    @Bean(name = "reservationEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean reservationEntityManagerFactory(
            @Qualifier("reservationDataSource") DataSource dataSource) {
        var em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.app.models.main");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "update");
        em.setJpaPropertyMap(props);
        return em;
    }

    @Bean(name = "reservationTransactionManager")
    public PlatformTransactionManager reservationTransactionManager(
            @Qualifier("reservationEntityManagerFactory") LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(emf.getObject());
    }
}
