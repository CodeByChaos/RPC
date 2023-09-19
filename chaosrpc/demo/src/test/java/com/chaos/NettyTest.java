package com.chaos;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

public class NettyTest {

    @Test
    public void testByteBuf() {
        ByteBuf header = Unpooled.buffer(); // 模拟http请求头
        ByteBuf body = Unpooled.buffer(); // 模拟http请求体
        // compositeBuffer()实现了透明的零拷贝，将物理上的多个 Buffer 组合成一个逻辑上完整的 CompositeByteBuf.
        // 通过逻辑组装而不是物理拷贝，实现在JVM中的零拷贝
        CompositeByteBuf httpBuf = Unpooled.compositeBuffer();
        // 这一步，不需要进行header和body的额外复制，httpBuf只是持有了header和body的引用
        // 接下来就可以正常操作完整httpBuf了
        httpBuf.addComponents(header, body);
    }

    @Test
    public void testWrapper() {
        byte[] bytes1 = new byte[1024];
        byte[] bytes2 = new byte[1024];
        // 共享byte数组的内容而不是拷贝，这也算零拷贝
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes1, bytes2);
    }

    @Test
    public void testSlice() {
        byte[] bytes1 = new byte[1024];
        byte[] bytes2 = new byte[1024];
        // 共享byte数组的内容而不是拷贝，这也算零拷贝
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes1, bytes2);

        // 同样将一个byteBuf，分割成多个，使用共享地址，而非拷贝
        ByteBuf buf1 = byteBuf.slice(1, 5);
        ByteBuf buf2 = byteBuf.slice(6, 15);
    }
}
