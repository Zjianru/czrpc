package com.cz.core.meta;

import com.alibaba.fastjson2.JSON;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 服务提供者元信息
 *
 * @author Zjianru
 */
@Data
@Builder
public class ProviderMeta {
    /**
     * 能力方法
     */
    private Method method;
    /**
     * 方法签名
     */
    private String methodSign;
    /**
     * 实例 bean
     */
    private Object targetService;
    /**
     * 附加参数
     * 目前包含
     * [打标]-> gray/unit/dc
     * [蓝绿]-> online
     */
    private Map<String, String> params;

    /**
     * 将附加参数列表转换为 json 串
     *
     * @return json string
     */
    public String metasTransfer() {
        return JSON.toJSONString(getParams());
    }
}
