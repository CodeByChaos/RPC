package com.chaosrpc.serialize;

import com.chaos.exceptions.SerializeException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class JdkSerializer implements Serializer{
    @Override
    public byte[] serialize(Object object) {
        if(object == null) {
            return null;
        }
        try(
                // 将流的定义写在这可以会自动关流
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream)
        )
        {
            outputStream.writeObject(object);
            if(log.isDebugEnabled()) {
                log.debug("对象{}已经完成了序列化.", object);
            }
            return byteArrayOutputStream.toByteArray();
        }catch (IOException e) {
            log.error("序列化对象{}时发生异常.", object);
            throw new SerializeException();
        }

    }

    @Override
    public <T> T disSerialize(byte[] bytes, Class<T> clazz) {
        if(bytes == null || clazz == null) {
            return null;
        }
        try(
                // 将流的定义写在这可以会自动关流
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream)
        )
        {
            Object object = inputStream.readObject();
            if(log.isDebugEnabled()) {
                log.debug("类{}已经完成了反序列化.", clazz);
            }
            return (T)object;
        }catch (IOException | ClassNotFoundException e) {
            log.error("反序列化对象{}时发生异常.", bytes);
            throw new SerializeException();
        }
    }
}
