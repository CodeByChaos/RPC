package com.chaos.serialize;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class SerializeUtils {
    public static byte[] serialize(Object object) {
        // 针对不同的消息类型需要做不同的处理，心跳的请求，没有playload
        if(object == null) {
            return null;
        }
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
            outputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
