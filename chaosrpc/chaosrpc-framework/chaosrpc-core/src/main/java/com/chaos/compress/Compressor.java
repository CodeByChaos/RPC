package com.chaos.compress;

public interface Compressor {
    /**
     * 压缩数据
     * @param data 需压缩的数据
     * @return 压缩后的数据
     */
    byte[] compress(byte[] data);

    /**
     * 解压数据
     * @param data 需解压的数据
     * @return 解压后的数据
     */
    byte[] decompress(byte[] data);
}
