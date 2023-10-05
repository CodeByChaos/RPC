package com.chaos.compress.impl;

import com.chaos.exceptions.CompressException;
import com.chaos.compress.Compressor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class GZIPCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] data) {
        try(
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)
        ) {
            gzipOutputStream.write(data);
            gzipOutputStream.finish();
            byte[] bytes = byteArrayOutputStream.toByteArray();
            if(log.isDebugEnabled()) {
                log.debug("对字节数组进行压缩，长度由{}压缩至{}.", data.length, bytes.length);
            }
            return bytes;
        } catch (IOException e) {
            log.error("对字节数组进行压缩时发生异常.", e);
            throw new CompressException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] data) {
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
                GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)
        ){
            byte[] bytes = gzipInputStream.readAllBytes();
            if(log.isDebugEnabled()) {
                log.debug("对字节数组进行解压，长度由{}解压至{}.", data.length, bytes.length);
            }
            return bytes;
        } catch (IOException e) {
            log.error("对字节数组进行解压时发生异常.", e);
            throw new CompressException(e);
        }
    }
}
