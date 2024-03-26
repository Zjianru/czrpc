package com.cz.core.meta;

import lombok.Builder;
import lombok.Data;

/**
 * 服务元数据
 *
 * @author Zjianru
 */
@Data
@Builder
public class ServiceMeta {
    private String applicationId;
    private String nameSpace;
    private String env;
    private String serviceName;

    /**
     * 转换为注册中心的路径
     *
     * @return path
     */
    public String toPath() {
        return String.format("%s_%s_%s_%s", applicationId, nameSpace, env, serviceName);
    }
}
