package com.chaos.utils;

import com.chaos.exceptions.NetworkException;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

@Slf4j
public class NetUtils {

    public static String getIpAddress() {
        String ipAddress = null;
        try {
            // 获取所有的网卡信息
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // 过滤非回环接口和虚拟接口
                if (iface.isLoopback() || iface.isVirtual() || !iface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    // 过滤ipv6地址和回环地址
                    if(address instanceof Inet6Address || address.isLoopbackAddress()) {
                        continue;
                    }
                    ipAddress = address.getHostAddress();
                    if(log.isDebugEnabled()) {
                        log.debug("局域网IP地址：{}", ipAddress);
                    }
                }
            }
        } catch (SocketException e) {
            log.error("获取局域网ip时发生异常。", e);
            throw new NetworkException();
        }
        return ipAddress;
    }
}
