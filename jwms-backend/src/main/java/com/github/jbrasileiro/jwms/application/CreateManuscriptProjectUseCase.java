package com.github.jbrasileiro.jwms.application;

import com.github.jbrasileiro.jwms.domain.manuscript.ManuscriptProjectPaths;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Cria um ficheiro .jwms mínimo (ZIP Manuskript v2) no caminho indicado. */
public final class CreateManuscriptProjectUseCase {

    private static final String ENTRY_VERSION = "VERSION";
    private static final byte[] VERSION_2 = "2".getBytes(StandardCharsets.UTF_8);
    private static final String ENTRY_PLOTS = "plots.xml";
    private static final byte[] PLOTS_XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><plot/>".getBytes(StandardCharsets.UTF_8);

    public CreateManuscriptProjectResult create(Path projectFile, boolean overwrite) {
        if (!ManuscriptProjectPaths.endsWithProjectExtension(projectFile.getFileName().toString())) {
            return new CreateManuscriptProjectResult.Failure(
                    List.of("O ficheiro deve ter extensão " + ManuscriptProjectPaths.EXTENSION + ": " + projectFile));
        }
        if (Files.exists(projectFile) && !overwrite) {
            return new CreateManuscriptProjectResult.Failure(
                    List.of("Ficheiro já existe: " + projectFile));
        }
        try {
            Path parent = projectFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            writeMinimalZip(projectFile);
            return new CreateManuscriptProjectResult.Success(projectFile);
        } catch (IOException e) {
            return new CreateManuscriptProjectResult.Failure(
                    List.of("Erro de I/O: " + e.getMessage()));
        }
    }

    private static void writeMinimalZip(Path projectFile) throws IOException {
        try (OutputStream raw = Files.newOutputStream(projectFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                ZipOutputStream zos = new ZipOutputStream(raw)) {
            zos.putNextEntry(new ZipEntry(ENTRY_VERSION));
            zos.write(VERSION_2);
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry(ENTRY_PLOTS));
            zos.write(PLOTS_XML);
            zos.closeEntry();
        }
    }

    public sealed interface CreateManuscriptProjectResult
            permits CreateManuscriptProjectResult.Success, CreateManuscriptProjectResult.Failure {

        record Success(Path path) implements CreateManuscriptProjectResult {}

        record Failure(List<String> errors) implements CreateManuscriptProjectResult {}
    }
}
