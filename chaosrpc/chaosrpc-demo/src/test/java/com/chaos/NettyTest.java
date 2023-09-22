package com.chaos;

import com.chaos.netty.AppClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class NettyTest {

    /**
     * 零拷贝
     */
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

    /**
     * 封装报文
     * @throws IOException
     */
    @Test
    public void testMessage() throws IOException {
        ByteBuf message = Unpooled.buffer();
        message.writeBytes("chaos".getBytes(StandardCharsets.UTF_8));
        message.writeByte(1);
        message.writeShort(125);
        message.writeInt(256);
        message.writeByte(1);
        message.writeByte(0);
        message.writeByte(2);
        message.writeLong(251455L);
        // 用对象流转换为字节数组
        AppClient appClient = new AppClient();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(appClient);
        byte[] bytes = outputStream.toByteArray();
        message.writeBytes(bytes);

        printAsBinary(message);
    }

    public static void printAsBinary(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(), bytes);

        String binaryString = ByteBufUtil.hexDump(bytes);
        StringBuilder formattedBinary = new StringBuilder();
        for (int i = 0; i < binaryString.length(); i += 2) {
            formattedBinary .append(binaryString.substring(i, i + 2)).append(" ");
        }
        System.out.println("Binary representation: " + formattedBinary.toString());
    }

    /**
     * 压缩
     */
    @Test
    public void testCompress() throws IOException {
        byte[] buf = new byte[]{12, 12, 12, 12, 12,  25, 34, 23, 25, 14,
                12, 12, 12, 12, 12,  25, 34, 23, 25, 14,
                12, 12, 12, 12, 12,  25, 34, 23, 25, 14,
                12, 12, 12, 12, 12,  25, 34, 23, 25, 14};

        // 本质就是，将buf作为输入，将结果输出到另一个字节数组当中。
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write(buf);
        gzipOutputStream.finish();

        byte[] bytes = byteArrayOutputStream.toByteArray();

        System.out.println(buf.length + "---->" + bytes.length);
        System.out.println(Arrays.toString(bytes));
    }
    /**
     * 解压
     */
    @Test
    public void testDeCompress() throws IOException {
        byte[] buf = new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, 0, -29, -31, 1, 2, 73, 37, 113, 73, 62, 30, -68, 44, 0, 35, -57, 10, 64, 40, 0, 0, 0};

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buf);
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);

        byte[] bytes = gzipInputStream.readAllBytes();

        System.out.println(buf.length + "---->" + bytes.length);
        System.out.println(Arrays.toString(bytes));
    }

}
