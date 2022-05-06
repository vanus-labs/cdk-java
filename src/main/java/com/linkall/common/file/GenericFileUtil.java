/*
 * Copyright 2022-Present The Vance Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.linkall.common.file;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A util class to deal with files
 */
public class GenericFileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericFileUtil.class);
    /**
     * extract String-based content from a resource
     * @param resourceName
     * @return String-based content
     * @throws IOException
     */
    public static String readResource(String resourceName) {
        URL resource = null;
        try {
            resource =Resources.getResource(resourceName);
        }catch (IllegalArgumentException e){
            LOGGER.info("cannot find resource: "+resourceName);
        }
        String result = null;
        if(null !=resource){
            try {
                result = Resources.toString(resource, StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOGGER.info("READ local resource <config.json> failed");
            }
        }
        return result;
    }
    /**
     * extract String-based content from a file
     * @param fileName
     * @return
     * @throws IOException
     */
    public static String readFile(String fileName) throws IOException{
        List<String> temp = Files.readLines(new File(fileName),StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        temp.forEach(str->{stringBuilder.append(str);});
        return stringBuilder.toString();
    }
}
