package com.cz.demo.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * demo - user pojo
 *
 * @author Zjianru
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    Integer id;
    String name;
}
