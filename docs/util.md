## JsonMapper
JsonMapper provides the `public static JsonObject wrapCloudEvent(CloudEvent event)` method to wrap data of CloudEvents
into a JsonObject.

## GenericFileUtil
Methods provided by GenericFileUtil:
```java
public static String readResource(String resourceName);
public static String readFile(String fileName);
```
Both methods provide reading of files and resources and return results in String format.

## URITool
Methods provided by URITool:
```java
public static URI getURI(String uriStr);
public static String getHost(URI uri);
public static boolean isIP(String uri);
```
1. The `getURI` method will wrap a uri name in String format into a URI format.
2. The `getHost` method will resolve the host ip from a uri.
3. The `isIP` method will judge whether a String is an IP address.