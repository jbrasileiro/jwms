package com.github.jbrasileiro.jwms.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectFormatDetectorTest {

    @TempDir Path tempDir;

    @Test
    void legacyPointerFile_version1_notZip() throws IOException {
        Path msk = tempDir.resolve("book.msk");
        Files.writeString(msk, "1", StandardCharsets.UTF_8);
        FormatDetection d = new ProjectFormatDetector().detect(msk);
        assertTrue(d.isOk());
        assertEquals(1, d.formatVersion());
        assertFalse(d.zipLayout());
    }

    @Test
    void zipWithoutVersionMarker_isVersion0() throws IOException {
        Path msk = tempDir.resolve("old.msk");
        writeZip(msk, new Entry("plots.xml", "<root/>"));
        FormatDetection d = new ProjectFormatDetector().detect(msk);
        assertTrue(d.isOk());
        assertEquals(0, d.formatVersion());
        assertTrue(d.zipLayout());
    }

    @Test
    void zipWithManuskriptMarker_readsVersion() throws IOException {
        Path msk = tempDir.resolve("v1.msk");
        writeZip(msk, new Entry("MANUSKRIPT", "1"), new Entry("plots.xml", "<root/>"));
        FormatDetection d = new ProjectFormatDetector().detect(msk);
        assertTrue(d.isOk());
        assertEquals(1, d.formatVersion());
        assertTrue(d.zipLayout());
    }

    @Test
    void zipWithVersionMarker_readsVersion() throws IOException {
        Path msk = tempDir.resolve("v2.msk");
        writeZip(msk, new Entry("VERSION", "2"));
        FormatDetection d = new ProjectFormatDetector().detect(msk);
        assertTrue(d.isOk());
        assertEquals(2, d.formatVersion());
        assertTrue(d.zipLayout());
    }

    @Test
    void nonZipNonDigit_reportsError() throws IOException {
        Path msk = tempDir.resolve("bad.msk");
        Files.writeString(msk, "x", StandardCharsets.UTF_8);
        FormatDetection d = new ProjectFormatDetector().detect(msk);
        assertFalse(d.isOk());
        assertTrue(d.errors().contains(msk.toString()));
    }

    private record Entry(String name, String utf8Content) {}

    private static void writeZip(Path zipFile, Entry... entries) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            for (Entry e : entries) {
                zos.putNextEntry(new ZipEntry(e.name()));
                zos.write(e.utf8Content().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
    }
}
