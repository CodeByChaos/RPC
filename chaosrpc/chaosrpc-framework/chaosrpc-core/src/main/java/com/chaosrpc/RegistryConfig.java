package com.chaosrpc;

public class RegistryConfig {
    private String connectString;

    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }
}
