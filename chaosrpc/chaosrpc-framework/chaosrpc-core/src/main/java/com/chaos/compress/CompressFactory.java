package com.chaos.compress;

import com.chaos.compress.impl.GZIPCompressor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CompressFactory {

    private final static Map<String, CompressWrapper> COMPRESS_CACHE = new ConcurrentHashMap<>();
    private final static Map<Byte, CompressWrapper> COMPRESS_CACHE_CODE = new ConcurrentHashMap<>();

    static {
        CompressWrapper gzip = new CompressWrapper((byte) 1, "jdk", new GZIPCompressor());
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
    public static CompressWrapper getCompress(String compressType) {
        CompressWrapper compressWrapper = COMPRESS_CACHE.get(compressType);
        if(compressWrapper == null) {
            log.error("未找到您配置的{}压缩策略，将使用默认压缩策略.", compressType);
            return COMPRESS_CACHE.get("gzip");
        }
        return COMPRESS_CACHE.get(compressType);
    }

    public static CompressWrapper getCompress(byte compressCode) {
        CompressWrapper compressWrapper = COMPRESS_CACHE_CODE.get(compressCode);
        if(compressWrapper == null) {
            log.error("未找到您配置的{}压缩策略，将使用默认压缩策略.", compressCode);
            return COMPRESS_CACHE.get("gzip");
        }
        return COMPRESS_CACHE_CODE.get(compressCode);
    }
}
