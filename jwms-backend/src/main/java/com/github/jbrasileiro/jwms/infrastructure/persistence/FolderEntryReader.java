package com.github.jbrasileiro.jwms.infrastructure.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class FolderEntryReader {

    public Optional<byte[]> readEntry(Path projectContentRoot, String relativePath) throws IOException {
        Path file = projectContentRoot.resolve(relativePath.replace('/', java.io.File.separatorChar)).normalize();
        if (!file.startsWith(projectContentRoot.toAbsolutePath().normalize())) {
            return Optional.empty();
        }
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        return Optional.of(Files.readAllBytes(file));
    }
}
