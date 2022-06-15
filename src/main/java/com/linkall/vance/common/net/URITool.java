package com.linkall.vance.common.net;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

public class URITool {
    private static final String IP_REGEX = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
    private static final Pattern PATTERN = Pattern.compile(IP_REGEX);
    public static URI getURI(String uriStr){
        URI uri = URI.create(uriStr);
        URI effectiveURI = null;
        try {
            effectiveURI = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
        } catch (URISyntaxException e) {
            System.out.println("---");
            e.printStackTrace();
            effectiveURI = null;
        }
        return effectiveURI;
    }

    public static String getHost(URI uri){
        String uriStr = uri.toString();
        if(uriStr.contains("http://")|| uriStr.contains("https://")){
            uriStr = uriStr.substring(uriStr.indexOf("//")+2);
        }
        if(uriStr.contains(":")){
            uriStr = uriStr.substring(0,uriStr.indexOf(":"));
        }
        return uriStr;
    }

    public static boolean isIP(String uri){
        return PATTERN.matcher(uri).matches();
    }
}
