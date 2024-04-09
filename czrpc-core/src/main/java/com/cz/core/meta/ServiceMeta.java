package com.cz.core.meta;

import com.alibaba.fastjson2.JSON;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

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
     * 附加参数
     * 目前包含
     * [打标]-> gray/unit/dc
     * [蓝绿]-> online
     */
    private Map<String, String> params;

    /**
     * 转换为注册中心的路径
     *
     * @return path
     */
    public String toPath() {
        return String.format("%s_%s_%s_%s", applicationId, nameSpace, env, serviceName);
    }

    /**
     * 将附加参数列表转换为 json 串
     *
     * @return json string
     */
    public String metasTransfer() {
        return JSON.toJSONString(getParams());
    }

}
