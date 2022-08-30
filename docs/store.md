## KVStore

`KVStore` interface provides Key-Value Storage implementation in Vance Connectors. The cdk-java now supported two modes of KV Storage implementation include `EtcdKVStore` and `FileKVStore`. `EtcdKVStore` uses etcd to storage KV data. And if you don't want to use etcd, you can just store the data in a local file by using `FileKVStore`. 

![KVStore](images/KVStore.png)

We can use `KVStoreFactory.class` to create KVStore of etcd type or file type. The method provided to create KVStore is as follows:

| Number         | Method          | Description |
|:-------------|:------------------|:------|
| 1 | public static KVStore createKVStore() | A factory method to build KVStore of different types  |

This method loads the KVStore type name from config.json in which you configure the `v_store` attribute like `"v_store": "file"` or `"v_store": "etcd"`. If the `v_store` attribute is not configured in config.json, `KVStoreFactory.class` will use the default `FileKVStore` mode to realize KV Storage.
While using `FileKVStore` mode, you should set `v_store_file` attribute in config.json. And when you use `EtcdKVStore` mode, you should set `etcd_url` attribute in config.json. The `v_store_file` indicates the path name of the storage file, while the `etcd_url` indicates the endpoints of etcd. Here is an example of configuration of the two attributes:
``` Json 
"v_store_file": "/vance/data/data.file"
"etcd_url": "http://localhost:2379"
``` 
The following code shows the application  of `KVStore`:
``` java
public static void main(String[] args) {
        KVStore kv = KVStoreFactory.createKVStore();
        kv.put("abc","ccc");
        System.out.println(kv.get("abc"));
        kv.put("abc","ddd");
        System.out.println(kv.get("abc"));
        kv.put("abc","eee");
        System.out.println(kv.get("abc"));
    }
```