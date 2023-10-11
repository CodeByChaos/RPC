package com.chaos;

import com.chaos.annotation.ChaosService;
import com.chaos.proxy.ChaosrpcProxyFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @author Chaos Wong
 */
@Component
public class ChaosrpcProxyBeanPostProcessor implements BeanPostProcessor {

    // 他会拦截所有的bean的创建，会在每一个bean初始化后被调用
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 想办法生成一个代理
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            ChaosService chaosService = field.getAnnotation(ChaosService.class);
            if (chaosService != null) {
                // 获取一个代理
                Class<?> type = field.getType();
                Object proxy = ChaosrpcProxyFactory.getProxy(type);
                field.setAccessible(true);
                try {
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }
}
