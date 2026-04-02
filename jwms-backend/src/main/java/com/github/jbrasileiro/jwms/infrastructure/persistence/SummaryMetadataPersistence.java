package com.github.jbrasileiro.jwms.infrastructure.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jbrasileiro.jwms.domain.manuscript.ManuscriptProjectPaths;
import com.github.jbrasileiro.jwms.domain.manuscript.SummaryMetadata;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** Lê/grava {@link ManuscriptProjectPaths#SUMMARY_JSON_ENTRY} em ZIP ou pasta de conteúdo. */
public final class SummaryMetadataPersistence {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ZipEntryReader zipReader = new ZipEntryReader();

    public SummaryMetadata load(Path projectFile, boolean zipLayout) throws IOException {
        if (zipLayout) {
            return zipReader
                    .readEntry(projectFile, ManuscriptProjectPaths.SUMMARY_JSON_ENTRY)
                    .map(this::parseOrEmpty)
                    .orElseGet(SummaryMetadata::new);
        }
        Path file = summaryFileInContentRoot(projectFile);
        if (!Files.isRegularFile(file)) {
            return new SummaryMetadata();
        }
        return parseOrEmpty(Files.readAllBytes(file));
    }

    public void save(Path projectFile, boolean zipLayout, SummaryMetadata data) throws IOException {
        byte[] json = MAPPER.writeValueAsBytes(data);
        if (zipLayout) {
            saveZip(projectFile, json);
        } else {
            saveFolder(projectFile, json);
        }
    }

    private SummaryMetadata parseOrEmpty(byte[] bytes) {
        try {
            JsonNode node = MAPPER.readTree(bytes);
            SummaryMetadata m = MAPPER.convertValue(node, SummaryMetadata.class);
            migrateLegacyLeftRightColumns(node, m);
            return m;
        } catch (IOException e) {
            return new SummaryMetadata();
        }
    }

    /**
     * Formato antigo: {@code leftColumnText} / {@code rightColumnText} por painel; reparte para os
     * quatro campos consoante o {@code lengthMode} guardado.
     */
    private static void migrateLegacyLeftRightColumns(JsonNode node, SummaryMetadata m) {
        if (!m.getParagraphText().isEmpty()
                || !m.getPageText().isEmpty()
                || !m.getFullText().isEmpty()) {
            return;
        }
        if (!node.has("leftColumnText") && !node.has("rightColumnText")) {
            return;
        }
        String legL = node.path("leftColumnText").asText("");
        String legR = node.path("rightColumnText").asText("");
        String mode = node.path("lengthMode").asText("SENTENCE");
        switch (mode) {
            case "PARAGRAPH" -> {
                if (m.getParagraphText().isEmpty()) {
                    m.setParagraphText(legR);
                }
            }
            case "PAGE" -> {
                if (m.getParagraphText().isEmpty()) {
                    m.setParagraphText(legL);
                }
                if (m.getPageText().isEmpty()) {
                    m.setPageText(legR);
                }
            }
            case "FULL" -> {
                if (m.getPageText().isEmpty()) {
                    m.setPageText(legL);
                }
                if (m.getFullText().isEmpty()) {
                    m.setFullText(legR);
                }
            }
            default -> {
                if (m.getParagraphText().isEmpty()) {
                    m.setParagraphText(legL);
                }
                if (m.getPageText().isEmpty()) {
                    m.setPageText(legR);
                }
            }
        }
    }

    private static Path summaryFileInContentRoot(Path projectFile) {
        return ManuscriptProjectPaths.legacyContentFolder(projectFile)
                .resolve(Path.of("jwms", "main", "Summary.json"));
    }

    private void saveFolder(Path projectFile, byte[] json) throws IOException {
        Path target = summaryFileInContentRoot(projectFile);
        Files.createDirectories(target.getParent());
        Files.write(target, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void saveZip(Path projectFile, byte[] json) throws IOException {
        Path parent = projectFile.getParent();
        if (parent == null) {
            parent = Path.of(".");
        }
        Path temp = Files.createTempFile(parent, "jwms-summary-", ".tmp");
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
                    if (name.equals(ManuscriptProjectPaths.SUMMARY_JSON_ENTRY)) {
                        if (!replaced) {
                            putSummaryEntry(zos, json);
                            replaced = true;
                        }
                        continue;
                    }
                    zos.putNextEntry(new ZipEntry(ent.getName()));
                    zf.getInputStream(ent).transferTo(zos);
                    zos.closeEntry();
                }
                if (!replaced) {
                    putSummaryEntry(zos, json);
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

    private static void putSummaryEntry(ZipOutputStream zos, byte[] json) throws IOException {
        zos.putNextEntry(new ZipEntry(ManuscriptProjectPaths.SUMMARY_JSON_ENTRY));
        zos.write(json);
        zos.closeEntry();
    }
}
