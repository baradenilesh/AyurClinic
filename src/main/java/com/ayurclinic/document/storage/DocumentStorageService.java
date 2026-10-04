package com.ayurclinic.document.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface DocumentStorageService {

    String store(
            MultipartFile file,
            String storageKey
    ) throws IOException;

    InputStream load(
            String storageKey
    ) throws IOException;

    void delete(
            String storageKey
    ) throws IOException;
}