package com.chaos.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用来描述请求调用方所请求的接口方法的描述
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestPlayload implements Serializable {

    // 1.接口的名字
    private String interfaceName;

    // 2.方法的名字
    private String methodName;

    // 3.参数列表，参数分为参数类型和具体的参数
    // 参数类型用来确定重载方法，具体的参数用来截停方法调用
    private Class<?>[] parametersType; // -- {java.lang.String}
    private Object[] parametersValue; // -- "???"

    // 4.返回值的封装
    private Class<?> returnType;

}
