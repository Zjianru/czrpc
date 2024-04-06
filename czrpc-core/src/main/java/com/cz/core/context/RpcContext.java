package com.cz.core.context;

import com.cz.core.enhance.Router;
import com.cz.core.filter.Filter;
import com.cz.core.loadBalance.LoadBalancer;
import com.cz.core.meta.InstanceMeta;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * code desc
 *
 * @author Zjianru
 */
@Data
@Builder
public class RpcContext {
    Router<InstanceMeta> router;
    LoadBalancer<InstanceMeta> loadBalancer;
    List<Filter> filters;
    Map<String, String> params;
}
