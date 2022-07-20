package com.linkall.vance.common.store;

import com.google.protobuf.ByteString;
import com.ibm.etcd.api.KeyValue;
import com.ibm.etcd.client.EtcdClient;
import com.ibm.etcd.client.KvStoreClient;
import com.ibm.etcd.client.kv.KvClient;
import com.linkall.vance.common.constant.ConfigConstant;
import com.linkall.vance.common.env.ConfigUtil;
import com.linkall.vance.core.KVStore;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class EtcdKVStoreImpl implements KVStore {
    private static KvStoreClient client;
    private static KvClient kvClient;
    static {
        String etcdEP = ConfigUtil.getEnvOrConfigOrDefault(ConfigConstant.ETCD_URL);
        client = EtcdClient.forEndpoints(etcdEP).withPlainText().build();
        kvClient = client.getKvClient();
    }

    private ByteString toByteString(String s){
        return ByteString.copyFrom(s.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void put(String key, String value) {
        kvClient.put(toByteString(key),toByteString(value)).sync();
    }

    @Override
    public String get(String key) {
        List<KeyValue> list= kvClient.get(toByteString(key)).sync().getKvsList();

        if(list.size()!=0){
            String bs = list.get(0).getValue().toString();
            return bs.substring(bs.indexOf("\"")+1,bs.lastIndexOf("\""));
        }
        else return null;
    }
}
