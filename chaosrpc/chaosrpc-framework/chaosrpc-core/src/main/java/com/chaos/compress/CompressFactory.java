package com.chaos.compress;

import com.chaos.compress.impl.GZIPCompressor;
import com.chaos.config.ObjectWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CompressFactory {

    private final static Map<String, ObjectWrapper<Compressor>> COMPRESS_CACHE = new ConcurrentHashMap<>();
    private final static Map<Byte, ObjectWrapper<Compressor>> COMPRESS_CACHE_CODE = new ConcurrentHashMap<>();

    static {
        ObjectWrapper<Compressor> gzip = new ObjectWrapper<>((byte) 1, "jdk", new GZIPCompressor());
//        CompressWrapper json = new CompressWrapper((byte) 2, "json", new JsonSerializer());
//        CompressWrapper hessian = new CompressWrapper((byte) 3, "hessian", new HessianSerailizer());
        COMPRESS_CACHE.put("gzip", gzip);
//        COMPRESS_CACHE.put("json", json);
//        COMPRESS_CACHE.put("hessian", hessian);
        COMPRESS_CACHE_CODE.put((byte) 1, gzip);
//        COMPRESS_CACHE_CODE.put((byte) 2, json);
//        COMPRESS_CACHE_CODE.put((byte) 3, hessian);
    }

    /**
     * 使用工厂方法获取一个CompressWrapper
     * @param compressType 压缩的类型
     * @return 包装类
     */
    public static ObjectWrapper<Compressor> getCompress(String compressType) {
        ObjectWrapper<Compressor> compressorObjectWrapper = COMPRESS_CACHE.get(compressType);
        if(compressorObjectWrapper == null) {
            log.error("未找到您配置的{}压缩策略，将使用默认压缩策略.", compressType);
            return COMPRESS_CACHE.get("gzip");
        }
        // return compressorObjectWrapper;
        return COMPRESS_CACHE.get(compressType);
    }

    public static ObjectWrapper<Compressor> getCompress(byte compressCode) {
        ObjectWrapper<Compressor> compressorObjectWrapper = COMPRESS_CACHE_CODE.get(compressCode);
        if(compressorObjectWrapper == null) {
            log.error("未找到您配置的{}压缩策略，将使用默认压缩策略.", compressCode);
            return COMPRESS_CACHE.get("gzip");
        }
        return COMPRESS_CACHE_CODE.get(compressCode);
    }

    /**
     * 给工厂中新增一个压缩方式
     * @param compressorObjectWrapper 压缩类型的包装
     */
    public static void addCompressor(ObjectWrapper<Compressor> compressorObjectWrapper) {
        COMPRESS_CACHE.put(compressorObjectWrapper.getName(), compressorObjectWrapper);
        COMPRESS_CACHE_CODE.put(compressorObjectWrapper.getCode(), compressorObjectWrapper);
    }
}
