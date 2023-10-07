package com.chaos.serialize;

import com.chaos.config.ObjectWrapper;
import com.chaos.serialize.impl.HessianSerializer;
import com.chaos.serialize.impl.JdkSerializer;
import com.chaos.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SerializerFactory {

    private final static Map<String, ObjectWrapper<Serializer>> SERIALIZER_CACHE = new ConcurrentHashMap<>();
    private final static Map<Byte, ObjectWrapper<Serializer>> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>();

    static {
        ObjectWrapper<Serializer> jdk = new ObjectWrapper<>((byte) 1, "jdk", new JdkSerializer());
        ObjectWrapper<Serializer> json = new ObjectWrapper<>((byte) 2, "json", new JsonSerializer());
        ObjectWrapper<Serializer> hessian = new ObjectWrapper<>((byte) 3, "hessian", new HessianSerializer());
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
    public static ObjectWrapper<Serializer> getSerializer(String serializeType) {
        ObjectWrapper<Serializer> serializerObjectWrapper = SERIALIZER_CACHE.get(serializeType);
        if(serializerObjectWrapper == null) {
            log.error("未找到您配置的{}序列化策略，将使用默认序列化策略.", serializeType);
            return SERIALIZER_CACHE.get("jdk");
        }
        return SERIALIZER_CACHE.get(serializeType);
    }

    public static ObjectWrapper<Serializer> getSerializer(byte serializeCode) {
        ObjectWrapper<Serializer> serializerObjectWrapper = SERIALIZER_CACHE_CODE.get(serializeCode);
        if(serializerObjectWrapper == null) {
            log.error("未找到您配置的{}序列化策略，将使用默认序列化策略.", serializeCode);
            return SERIALIZER_CACHE.get("jdk");
        }
        return SERIALIZER_CACHE_CODE.get(serializeCode);
    }

    /**
     * 给工厂中新增一个序列化方式
     * @param serializerObjectWrapper 序列化类型的包装
     */
    public static void addSerializer(ObjectWrapper<Serializer> serializerObjectWrapper) {
        SERIALIZER_CACHE.put(serializerObjectWrapper.getName(), serializerObjectWrapper);
        SERIALIZER_CACHE_CODE.put(serializerObjectWrapper.getCode(), serializerObjectWrapper);
    }
}
