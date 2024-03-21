package com.cz.demo.api.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * demo - user pojo
 *
 * @author Zjianru
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    Integer id;
    String name;
    Long ident;
}
