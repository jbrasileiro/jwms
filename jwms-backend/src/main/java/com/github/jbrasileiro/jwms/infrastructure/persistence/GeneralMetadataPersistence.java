package com.github.jbrasileiro.jwms.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jbrasileiro.jwms.domain.manuscript.GeneralMetadata;
import com.github.jbrasileiro.jwms.domain.manuscript.ManuscriptProjectPaths;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** Lê/grava {@link ManuscriptProjectPaths#GENERAL_JSON_ENTRY} em ZIP ou pasta de conteúdo. */
public final class GeneralMetadataPersistence {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ZipEntryReader zipReader = new ZipEntryReader();

    public GeneralMetadata load(Path projectFile, boolean zipLayout) throws IOException {
        if (zipLayout) {
            return zipReader
                    .readEntry(projectFile, ManuscriptProjectPaths.GENERAL_JSON_ENTRY)
                    .map(this::parseOrEmpty)
                    .orElseGet(GeneralMetadata::new);
        }
        Path file = generalFileInContentRoot(projectFile);
        if (!Files.isRegularFile(file)) {
            return new GeneralMetadata();
        }
        return parseOrEmpty(Files.readAllBytes(file));
    }

    public void save(Path projectFile, boolean zipLayout, GeneralMetadata data) throws IOException {
        byte[] json = MAPPER.writeValueAsBytes(data);
        if (zipLayout) {
            saveZip(projectFile, json);
        } else {
            saveFolder(projectFile, json);
        }
    }

    private GeneralMetadata parseOrEmpty(byte[] bytes) {
        try {
            return MAPPER.readValue(bytes, GeneralMetadata.class);
        } catch (IOException e) {
            return new GeneralMetadata();
        }
    }

    private static Path generalFileInContentRoot(Path projectFile) {
        return ManuscriptProjectPaths.legacyContentFolder(projectFile)
                .resolve(Path.of("jwms", "main", "General.json"));
    }

    private void saveFolder(Path projectFile, byte[] json) throws IOException {
        Path target = generalFileInContentRoot(projectFile);
        Files.createDirectories(target.getParent());
        Files.write(target, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void saveZip(Path projectFile, byte[] json) throws IOException {
        Path parent = projectFile.getParent();
        if (parent == null) {
            parent = Path.of(".");
        }
        Path temp = Files.createTempFile(parent, "jwms-general-", ".tmp");
        try {
            try (ZipFile zf = new ZipFile(projectFile.toFile());
                    OutputStream raw =
                            Files.newOutputStream(
                                    temp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    ZipOutputStream zos = new ZipOutputStream(raw)) {
                boolean replaced = false;
                for (var stream = zf.stream().iterator(); stream.hasNext(); ) {
                    ZipEntry ent = stream.next();
                    if (ent.isDirectory()) {
                        continue;
                    }
                    String name = ent.getName().replace('\\', '/');
                    if (name.equals(ManuscriptProjectPaths.GENERAL_JSON_ENTRY)) {
                        if (!replaced) {
                            putGeneralEntry(zos, json);
                            replaced = true;
                        }
                        continue;
                    }
                    zos.putNextEntry(new ZipEntry(ent.getName()));
                    zf.getInputStream(ent).transferTo(zos);
                    zos.closeEntry();
                }
                if (!replaced) {
                    putGeneralEntry(zos, json);
                }
            }
            Files.move(temp, projectFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (Throwable t) {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
                // ignore
            }
            throw t;
        }
    }

    private static void putGeneralEntry(ZipOutputStream zos, byte[] json) throws IOException {
        zos.putNextEntry(new ZipEntry(ManuscriptProjectPaths.GENERAL_JSON_ENTRY));
        zos.write(json);
        zos.closeEntry();
    }

}
