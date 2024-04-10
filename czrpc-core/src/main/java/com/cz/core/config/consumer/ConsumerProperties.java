package com.cz.core.config.consumer;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * for ha and governance
 *
 * @author Zjianru
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "czrpc.consumer")
public class ConsumerProperties {

    // 重试次数
    private int retries = 1;
    // 重试阈值 - 超时达到此阈值，即进行重试
    private int retryTimeout = 1000;
    // 请求 30 s 内错误阈值 - 超过此限制即进行故障隔离
    private int faultLimit = 10;
    // 半开探活初始化时间
    private int halfOpenInitialDelay = 10_000;
    // 半开探活时间
    private int halfOpenDelay = 60_000;
    // 灰度流量调拨比重 [0-100]
    private int grayRatio = 0;

}
