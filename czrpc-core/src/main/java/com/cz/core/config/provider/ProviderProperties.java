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
    public Map<ProviderPropertiesMeta, String> getMetas() {
        Map<ProviderPropertiesMeta, String> metas = new HashMap<>();
        metas.put(ProviderPropertiesMeta.ONLINE, String.valueOf(online));
        metas.put(ProviderPropertiesMeta.GRAY, String.valueOf(gray));
        metas.put(ProviderPropertiesMeta.DC, dc);
        metas.put(ProviderPropertiesMeta.UNIT, unit);
        metas.put(ProviderPropertiesMeta.TRAFFIC_CONTROL, String.valueOf(trafficControl));
        return metas;
    }

}
