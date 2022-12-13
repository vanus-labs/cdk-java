package com.linkall.vance.store;

import java.io.EOFException;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;

public class FileKVKVStoreImpl extends MemoryKVStoreImpl {
    private final File file;

    public FileKVKVStoreImpl(String filepath) throws Exception {
        this.file = new File(filepath);
        load();
    }

    @SuppressWarnings("unchecked")
    public void load() throws Exception {
        try (ObjectInputStream inputStream = new ObjectInputStream(Files.newInputStream(file.toPath()))) {
            Object obj = inputStream.readObject();
            if (!(obj instanceof HashMap))
                throw new Exception("Expected HashMap but found " + obj.getClass());
            Map<byte[], byte[]> raw = (Map<byte[], byte[]>) obj;
            data = new HashMap<>();
            for (Map.Entry<byte[], byte[]> entry : raw.entrySet()) {
                data.put(new String(entry.getKey()), entry.getValue());
            }
        } catch (NoSuchFileException | EOFException e) {
        }
    }

    @Override
    public void save() throws Exception {
        try (ObjectOutputStream os = new ObjectOutputStream(Files.newOutputStream(file.toPath()))) {
            Map<byte[], byte[]> raw = new HashMap<>();
            for (Map.Entry<String, byte[]> entry : data.entrySet()) {
                raw.put(entry.getKey().getBytes(StandardCharsets.UTF_8), entry.getValue());
            }
            os.writeObject(raw);
        }
    }
}
