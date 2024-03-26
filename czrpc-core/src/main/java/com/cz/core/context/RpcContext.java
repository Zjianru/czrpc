package com.cz.core.context;

import com.cz.core.enhance.Filter;
import com.cz.core.enhance.LoadBalancer;
import com.cz.core.enhance.Router;
import com.cz.core.meta.InstanceMeta;
import lombok.Data;

import java.util.List;

/**
 * code desc
 *
 * @author Zjianru
 */
@Data
public class RpcContext {
    Router<InstanceMeta> router;
    LoadBalancer<InstanceMeta> loadBalancer;
    List<Filter> filters;

}
