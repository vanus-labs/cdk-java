package com.linkall.vance.common.constant;

public enum KVImpl {
    LOCAL_KV("file"),
    ETCD_KV("etcd");
    private final String value;
    KVImpl(String v){
        value = v;
    }
    public String getValue() {
        return value;
    }
}
