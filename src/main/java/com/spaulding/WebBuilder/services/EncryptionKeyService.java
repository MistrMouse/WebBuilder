package com.spaulding.WebBuilder.services;

import com.spaulding.tools.Archive.Archive;
import com.spaulding.tools.Archive.services.DBEncryptionKeyService;
import com.spaulding.tools.Cypher.objects.FileEncryptionKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

@Service
public class EncryptionKeyService {
    @Autowired
    private DBEncryptionKeyService dbEncryptionKeyService;

    private Map<String, String> keys;

    public void refresh() throws SQLException {
        keys = new HashMap<>();
        List<Archive.Row> rows = dbEncryptionKeyService.getAllKeys();
        for (Archive.Row row : rows) {
            keys.put((String) row.getResult(1), (String) row.getResult(2));
        }

        List<String> fileNames = Stream.of(Objects.requireNonNull(new File(".").listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .toList();

        for (String fileName : fileNames) {
            fileName = fileName.replace(FileEncryptionKey.FILE_SUFFIX, "");
            if (keys.containsKey(fileName)) {
                throw new RuntimeException();
            }

            FileEncryptionKey fileEncryptionKey = new FileEncryptionKey(fileName);
            keys.put(fileName, fileEncryptionKey.value);
        }
    }

    public String getKey(String keyName) {
        return keys.get(keyName);
    }

    public Set<String> getKeyNames() {
        return keys.keySet();
    }
}
