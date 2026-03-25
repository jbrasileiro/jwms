package com.github.jbrasileiro.jwms.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.jbrasileiro.jwms.application.OpenManuscriptProjectUseCase.OpenManuscriptProjectResult;
import com.github.jbrasileiro.jwms.domain.manuscript.ManuscriptProject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OpenManuscriptProjectUseCaseTest {

    @TempDir Path tempDir;
    private final OpenManuscriptProjectUseCase open = new OpenManuscriptProjectUseCase();

    @Test
    void opensZipWithPlotsXml() throws IOException {
        Path msk = tempDir.resolve("z.msk");
        writeZip(
                msk,
                zipEntry("MANUSKRIPT", "1"),
                zipEntry(
                        "plots.xml",
                        "<?xml version='1.0' encoding='UTF-8'?><root><plot name=\"a\"/></root>"));

        OpenManuscriptProjectResult r = open.open(msk);
        var success = assertInstanceOf(OpenManuscriptProjectResult.Success.class, r);
        ManuscriptProject p = success.project();
        assertTrue(p.isZipContainer());
        assertEquals(1, p.getFormatVersion());
        assertTrue(p.getRelativeEntryNames().contains("plots.xml"));
        assertEquals("root", p.getSampleXmlRootLocalName().orElseThrow());
    }

    @Test
    void opensLegacyFolderLayout_nextToMsk() throws IOException {
        Path msk = tempDir.resolve("demo.msk");
        Files.writeString(msk, "1", StandardCharsets.UTF_8);
        Path folder = tempDir.resolve("demo");
        Files.createDirectories(folder);
        Files.writeString(
                folder.resolve("plots.xml"),
                "<?xml version='1.0' encoding='UTF-8'?><root/>",
                StandardCharsets.UTF_8);

        OpenManuscriptProjectResult r = open.open(msk);
        var success = assertInstanceOf(OpenManuscriptProjectResult.Success.class, r);
        ManuscriptProject p = success.project();
        assertTrue(!p.isZipContainer());
        assertEquals(1, p.getFormatVersion());
        assertTrue(p.getRelativeEntryNames().contains("plots.xml"));
        assertEquals("root", p.getSampleXmlRootLocalName().orElseThrow());
    }

    @Test
    void failsWhenLegacyFolderMissing() throws IOException {
        Path msk = tempDir.resolve("orphan.msk");
        Files.writeString(msk, "1", StandardCharsets.UTF_8);

        OpenManuscriptProjectResult r = open.open(msk);
        var failure = assertInstanceOf(OpenManuscriptProjectResult.Failure.class, r);
        assertTrue(failure.errors().getFirst().toLowerCase().contains("esperada a pasta"));
    }

    private record ZE(String name, String utf8) {}

    private static ZE zipEntry(String name, String content) {
        return new ZE(name, content);
    }

    private static void writeZip(Path zipFile, ZE... entries) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            for (ZE e : entries) {
                zos.putNextEntry(new ZipEntry(e.name()));
                zos.write(e.utf8().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
    }
}
