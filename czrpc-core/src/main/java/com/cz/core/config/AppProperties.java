package com.cz.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * for app instance
 *
 * @author Zjianru
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "czrpc.app")
public class AppProperties {

    // app id
    private String applicationId = "app1";
    // app name spase
    private String nameSpace = "public";
    // app env tag
    private String env = "dev";

}
