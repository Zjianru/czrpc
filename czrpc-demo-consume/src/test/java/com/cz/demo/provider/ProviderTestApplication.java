package com.cz.demo.provider;

import com.cz.core.protocol.RpcRequest;
import com.cz.core.protocol.RpcResponse;
import com.cz.core.provider.ProviderConfig;
import com.cz.core.provider.ProviderInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * code desc
 *
 * @author Zjianru
 */
@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
public class ProviderTestApplication {
    @Autowired
    ProviderInvoker providerInvoker;

    @RequestMapping("/")
    public RpcResponse<Object> invoke(@RequestBody RpcRequest request) {
        return providerInvoker.invoke(request);
    }

}
