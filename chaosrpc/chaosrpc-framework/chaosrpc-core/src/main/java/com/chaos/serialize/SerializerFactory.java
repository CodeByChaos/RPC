package com.chaos.serialize;

import com.chaos.serialize.impl.HessianSerializer;
import com.chaos.serialize.impl.JdkSerializer;
import com.chaos.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SerializerFactory {

    private final static Map<String, SerializeWrapper> SERIALIZER_CACHE = new ConcurrentHashMap<>();
    private final static Map<Byte, SerializeWrapper> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>();

    static {
        SerializeWrapper jdk = new SerializeWrapper((byte) 1, "jdk", new JdkSerializer());
        SerializeWrapper json = new SerializeWrapper((byte) 2, "json", new JsonSerializer());
        SerializeWrapper hessian = new SerializeWrapper((byte) 3, "hessian", new HessianSerializer());
        SERIALIZER_CACHE.put("jdk", jdk);
        SERIALIZER_CACHE.put("json", json);
        SERIALIZER_CACHE.put("hessian", hessian);
        SERIALIZER_CACHE_CODE.put((byte) 1, jdk);
        SERIALIZER_CACHE_CODE.put((byte) 2, json);
        SERIALIZER_CACHE_CODE.put((byte) 3, hessian);
    }

    /**
     * 使用工厂方法获取一个SerializeWrapper
     * @param serializeType 序列化的类型
     * @return 包装类
     */
    public static SerializeWrapper getSerializer(String serializeType) {
        SerializeWrapper serializeWrapper = SERIALIZER_CACHE.get(serializeType);
        if(serializeWrapper == null) {
            log.error("未找到您配置的{}序列化策略，将使用默认序列化策略.", serializeType);
            return SERIALIZER_CACHE.get("jdk");
        }
        return SERIALIZER_CACHE.get(serializeType);
    }

    public static SerializeWrapper getSerializer(byte serializeCode) {
        SerializeWrapper serializeWrapper = SERIALIZER_CACHE_CODE.get(serializeCode);
        if(serializeWrapper == null) {
            log.error("未找到您配置的{}序列化策略，将使用默认序列化策略.", serializeCode);
            return SERIALIZER_CACHE.get("jdk");
        }
        return SERIALIZER_CACHE_CODE.get(serializeCode);
    }
}
