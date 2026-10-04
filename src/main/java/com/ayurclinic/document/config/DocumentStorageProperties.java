package com.ayurclinic.document.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ayurclinic.document.storage")
public class DocumentStorageProperties {

    /**
     * Local directory used for document storage.
     */
    private String localBaseDir = "./storage/documents";

    /**
     * Maximum allowed file size in bytes.
     *
     * Default: 10 MB.
     */
    private long maxFileSize = 10 * 1024 * 1024;
}