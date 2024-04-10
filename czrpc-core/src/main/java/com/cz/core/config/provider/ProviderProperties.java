package com.cz.core.config.provider;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * for provider
 *
 * @author Zjianru
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "czrpc.provider")
public class ProviderProperties {

    boolean online = true;
    boolean gray = false;
    String dc = "bj";
    String unit = "001";
    int trafficControl;
    /**
     * get metas
     *
     * @return metas
     */
    public Map<String, String> getMetas() {
        Map<String, String> metas = new HashMap<>();
        metas.put("online", String.valueOf(online));
        metas.put("gray", String.valueOf(gray));
        metas.put("dc", dc);
        metas.put("unit", unit);
        metas.put("trafficControl", String.valueOf(trafficControl));
        return metas;
    }

}
