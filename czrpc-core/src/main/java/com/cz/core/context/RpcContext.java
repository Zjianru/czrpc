package com.cz.core.context;

import com.cz.core.enhance.Router;
import com.cz.core.filter.Filter;
import com.cz.core.loadBalance.LoadBalancer;
import com.cz.core.meta.InstanceMeta;
import lombok.Builder;
import lombok.Data;

import java.util.List;

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

}
