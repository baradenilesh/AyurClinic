package com.ayurclinic.document.storage;

import com.ayurclinic.document.config.DocumentStorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LocalDocumentStorageService
        implements DocumentStorageService {

    private final Path baseDirectory;

    public LocalDocumentStorageService(
            DocumentStorageProperties properties
    ) {
        this.baseDirectory =
                Paths.get(properties.getLocalBaseDir())
                        .toAbsolutePath()
                        .normalize();

        try {
            Files.createDirectories(baseDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Unable to initialize document storage",
                    ex
            );
        }
    }

    @Override
    public String store(
            MultipartFile file,
            String storageKey
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File is required"
            );
        }

        Path target =
                resolveStoragePath(storageKey);

        Path parent = target.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (InputStream inputStream =
                     file.getInputStream()) {

            Files.copy(
                    inputStream,
                    target,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
        }

        return storageKey;
    }

    @Override
    public InputStream load(
            String storageKey
    ) throws IOException {

        Path path =
                resolveStoragePath(storageKey);

        if (!Files.exists(path)) {
            throw new java.io.FileNotFoundException(
                    "Document file not found"
            );
        }

        return Files.newInputStream(path);
    }

    @Override
    public void delete(
            String storageKey
    ) throws IOException {

        Path path =
                resolveStoragePath(storageKey);

        Files.deleteIfExists(path);
    }

    private Path resolveStoragePath(
            String storageKey
    ) {

        if (storageKey == null ||
                storageKey.isBlank()) {

            throw new IllegalArgumentException(
                    "Storage key is required"
            );
        }

        Path resolved =
                baseDirectory
                        .resolve(storageKey)
                        .normalize();

        if (!resolved.startsWith(baseDirectory)) {
            throw new IllegalArgumentException(
                    "Invalid storage key"
            );
        }

        return resolved;
    }
}
