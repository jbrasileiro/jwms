package com.github.jbrasileiro.jwms.infrastructure.persistence;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.ZipFile;

public final class ZipEntryReader {

    public Optional<byte[]> readEntry(Path zipFile, String relativePath) throws IOException {
        String normalized = relativePath.replace('\\', '/');
        try (ZipFile zf = new ZipFile(zipFile.toFile())) {
            var e = zf.getEntry(normalized);
            if (e == null || e.isDirectory()) {
                return Optional.empty();
            }
            return Optional.of(zf.getInputStream(e).readAllBytes());
        }
    }
}
