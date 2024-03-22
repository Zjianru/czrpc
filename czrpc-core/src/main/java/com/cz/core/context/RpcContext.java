package com.cz.core.context;

import com.cz.core.connect.Filter;
import com.cz.core.connect.LoadBalancer;
import com.cz.core.connect.Router;
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
