package com.chaosrpc;

public class ProtocolConfig {
    private String protocolName;

    public ProtocolConfig(String protocolName) {
        this.protocolName = protocolName;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }
}
