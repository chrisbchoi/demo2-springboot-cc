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

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }
}
