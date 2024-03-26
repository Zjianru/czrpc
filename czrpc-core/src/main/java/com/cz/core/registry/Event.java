package com.cz.core.registry;

import com.cz.core.meta.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 注册中心事件
 *
 * @author Zjianru
 */
@Data
@AllArgsConstructor
public class Event {
    List<InstanceMeta> data;
}
