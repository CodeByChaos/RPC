package com.chaosrpc.serialize;

import java.io.IOException;

public class JsonSerializer implements Serializer{
    @Override
    public byte[] serialize(Object object) throws IOException {
        return new byte[0];
    }

    @Override
    public <T> T disSerialize(byte[] bytes, Class<T> clazz) {
        return null;
    }
}
