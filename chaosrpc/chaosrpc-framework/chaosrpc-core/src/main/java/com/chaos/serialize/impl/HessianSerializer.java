package com.chaos.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.chaos.exceptions.SerializeException;
import com.chaos.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) throws IOException {
        if(object == null) {
            return null;
        }
        try(
                // 将流的定义写在这可以会自动关流
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ) {
            Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            if(log.isDebugEnabled()) {
                log.debug("对象{}使用Hessian已经完成了序列化.", object);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("使用Hessian序列化对象{}时发生异常.", object);
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
        )
        {
            Hessian2Input hessian2Input = new Hessian2Input(byteArrayInputStream);
            T t = (T) hessian2Input.readObject();
            if(log.isDebugEnabled()) {
                log.debug("类{}使用Hessian已经完成了反序列化.", clazz);
            }
            return t;
        }catch (IOException e) {
            log.error("使用Hessian反序列化对象{}时发生异常.", bytes);
            throw new SerializeException();
        }
    }
}
