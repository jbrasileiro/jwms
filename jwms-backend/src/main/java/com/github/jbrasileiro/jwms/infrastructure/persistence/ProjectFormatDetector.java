package com.github.jbrasileiro.jwms.infrastructure.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

public final class ProjectFormatDetector {

    private static final String ENTRY_VERSION = "VERSION";
    private static final String ENTRY_MANUSKRIPT = "MANUSKRIPT";

    public FormatDetection detect(Path projectFile) {
        List<String> errors = new ArrayList<>();
        boolean isZip = false;
        int version = 0;

        ZipFile zf = tryOpenZip(projectFile);
        if (zf != null) {
            isZip = true;
            try {
                if (hasEntry(zf, ENTRY_VERSION)) {
                    version = parseVersionFile(zf, ENTRY_VERSION, errors, projectFile);
                } else if (hasEntry(zf, ENTRY_MANUSKRIPT)) {
                    version = parseVersionFile(zf, ENTRY_MANUSKRIPT, errors, projectFile);
                } else {
                    version = 0;
                }
            } finally {
                try {
                    zf.close();
                } catch (IOException ignored) {
                    // ignore
                }
            }
        } else {
            try {
                String s = Files.readString(projectFile, StandardCharsets.UTF_8).trim();
                if (!s.isEmpty() && s.chars().allMatch(Character::isDigit)) {
                    version = Integer.parseInt(s);
                } else {
                    errors.add(projectFile.toString());
                }
            } catch (IOException e) {
                errors.add("Não foi possível ler: " + projectFile + " — " + e.getMessage());
            }
        }

        return new FormatDetection(version, isZip, List.copyOf(errors));
    }

    private static ZipFile tryOpenZip(Path projectFile) {
        try {
            return new ZipFile(projectFile.toFile());
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean hasEntry(ZipFile zf, String name) {
        return zf.getEntry(name) != null;
    }

    private static int parseVersionFile(ZipFile zf, String entryName, List<String> errors, Path projectFile) {
        try {
            byte[] raw = zf.getInputStream(zf.getEntry(entryName)).readAllBytes();
            String text = new String(raw, StandardCharsets.UTF_8).trim();
            if (!text.isEmpty() && text.chars().allMatch(Character::isDigit)) {
                return Integer.parseInt(text);
            }
            errors.add(projectFile.toString());
            errors.add(entryName);
        } catch (IOException e) {
            errors.add(entryName + ": " + e.getMessage());
        }
        return 0;
    }
}
