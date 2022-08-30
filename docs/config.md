## Config

### ConfigUtil

`ConfigUtil` is a class which provides static methods to load user-specific configs. The following methods are supported by the `ConfigUtil.class`:
| Number         | Method          | Description |
|:-------------|:------------------|:------|
| 1 | public static String getString(String key) | Get a config value according to a specific key  |
| 2 | public static List<String> getStringArray(String key)   | Get a list of configs according to a specific key |
| 3 | public static String getEnvOrConfigOrDefault(String name) | Same as ConfigUtil.getString(String). This method retrieves data from env and config first.If this method cannot find value from above positions, it will try to get a default value in the SDK.  |
| 4 | public static String getConfigPath()   | Get config-path, it could either be a user-set env or default config-path value |
| 5 |     public static String getVanceSink() | Get the sink of vance, its default value is v_target value  |
| 6 | public static String getPort()   | Get the port the process needs to listen to. |
| 7 | public static String getKVStore() | Get the name of KVStore.                    |

### SecretUtil

`SecretUtil` provides static methods to load secrets of users for authentication or encryption. The secrets need to be encrypted and stored in the secret.json file. The following methods are supported by the `SecretUtil.class`:

| Number         | Method          | Description |
|:-------------|:------------------|:------|
| 1 | public static String getString(String key) | Get decoded value of users' secrets according to a specific key.  |