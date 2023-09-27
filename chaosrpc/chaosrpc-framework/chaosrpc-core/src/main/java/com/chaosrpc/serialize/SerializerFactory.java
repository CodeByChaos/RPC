package com.chaosrpc.serialize;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SerializerFactory {

    private final static Map<String, SerializeWrapper> SERIALIZER_CACHE = new ConcurrentHashMap<>();
    private final static Map<Byte, SerializeWrapper> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>();

    static {
        SerializeWrapper jdk = new SerializeWrapper((byte) 1, "jdk", new JdkSerializer());
        SerializeWrapper json = new SerializeWrapper((byte) 2, "json", new JsonSerializer());
        SerializeWrapper hessian = new SerializeWrapper((byte) 3, "hessian", new HessianSerailizer());
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
        return SERIALIZER_CACHE.get(serializeType);
    }

    public static SerializeWrapper getSerializer(byte serializeCode) {
        return SERIALIZER_CACHE_CODE.get(serializeCode);
    }
}
