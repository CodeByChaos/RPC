package com.chaosrpc.serialize;

import com.alibaba.fastjson2.JSON;
import com.chaos.exceptions.SerializeException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
@Slf4j
public class JsonSerializer implements Serializer{
    @Override
    public byte[] serialize(Object object) throws IOException {
        if(object == null) {
            return null;
        }
        byte[] bytes = JSON.toJSONBytes(object);
        if(log.isDebugEnabled()) {
            log.debug("对象{}使用json已经完成了序列化.", object);
        }
        return bytes;
    }

    @Override
    public <T> T disSerialize(byte[] bytes, Class<T> clazz) {
        if(bytes == null || clazz == null) {
            return null;
        }
        T t = JSON.parseObject(bytes, clazz);
        if(log.isDebugEnabled()) {
            log.debug("类{}使用json已经完成了反序列化.", clazz);
        }
        return (T)t;
    }
}
