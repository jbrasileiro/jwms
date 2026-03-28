package com.github.jbrasileiro.jwms.api;

import java.nio.file.Path;
import java.util.Locale;

/** Convenções de ficheiro de projecto expostas à UI (sem depender do módulo backend). */
public final class ProjectFileHints {

    public static final String EXTENSION = ".jwms";

    private ProjectFileHints() {}

    public static boolean endsWithProjectExtension(String fileName) {
        return fileName.toLowerCase(Locale.ROOT).endsWith(EXTENSION);
    }

    public static Path ensureProjectExtension(Path path) {
        String name = path.getFileName().toString();
        if (!endsWithProjectExtension(name)) {
            return path.resolveSibling(name + EXTENSION);
        }
        return path;
    }
}
