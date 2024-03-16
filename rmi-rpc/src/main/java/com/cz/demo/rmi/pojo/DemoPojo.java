package com.cz.demo.rmi.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * RPC demo by RMI
 * buss pojo
 *
 * @author Zjianru
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DemoPojo implements Serializable {
    /**
     * String 示例属性
     */
    public String bussName;
    /**
     * boolean 示例属性
     */
    public boolean bussSuccess;
    /**
     * int 示例属性
     */
    public int bussCount;

}
