package com.cz.core.context;

import com.cz.core.enhance.Filter;
import com.cz.core.enhance.LoadBalancer;
import com.cz.core.enhance.Router;
import lombok.Data;

import java.util.List;

/**
 * code desc
 *
 * @author Zjianru
 */
@Data
public class RpcContext {
    Router router;
    LoadBalancer loadBalancer;
    List<Filter> filters;

}
