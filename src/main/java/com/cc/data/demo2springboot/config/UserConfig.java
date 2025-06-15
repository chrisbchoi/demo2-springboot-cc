package com.cc.data.demo2springboot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for User-related settings.
 */
@Configuration
@ConfigurationProperties(prefix = "app.user")
public class UserConfig {

    /**
     * Maximum number of users that can be created in a batch operation.
     * Default value is 10.
     */
    private int maxBatchSize = 10;

    /**
     * Default page number for pagination (zero-based).
     * Default value is 0.
     */
    private int defaultPage = 0;

    /**
     * Default page size for pagination.
     * Default value is 10.
     */
    private int defaultPageSize = 10;

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    public int getDefaultPage() {
        return defaultPage;
    }

    public void setDefaultPage(int defaultPage) {
        this.defaultPage = defaultPage;
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }
}
