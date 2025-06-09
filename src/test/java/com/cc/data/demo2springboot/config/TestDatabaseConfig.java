package com.cc.data.demo2springboot.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Test configuration for database connection that doesn't rely on environment variables
 */
@TestConfiguration
public class TestDatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        // Create a test-specific data source with hardcoded credentials for tests
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("sa");
        dataSource.setPassword("password");

        return dataSource;
    }
}
