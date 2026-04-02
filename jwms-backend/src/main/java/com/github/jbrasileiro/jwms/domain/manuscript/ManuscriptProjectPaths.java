package com.github.jbrasileiro.jwms.domain.manuscript;

import java.nio.file.Path;
import java.util.Locale;

/** Convenções de nome de ficheiro de projeto e caminhos internos JWMS. */
public final class ManuscriptProjectPaths {

    public static final String EXTENSION = ".jwms";

    /** Metadados do ecrã Geral dentro do ZIP ou da pasta de conteúdo. */
    public static final String GENERAL_JSON_ENTRY = "jwms/main/General.json";

    /** Metadados do ecrã Resumo dentro do ZIP ou da pasta de conteúdo. */
    public static final String SUMMARY_JSON_ENTRY = "jwms/main/Summary.json";

    private ManuscriptProjectPaths() {}

    /**
     * Pasta de conteúdo ao lado do ficheiro apontador (ex.: {@code livro.jwms} + pasta {@code livro/}).
     */
    public static Path legacyContentFolder(Path projectFile) {
        Path parent = projectFile.getParent();
        if (parent == null) {
            parent = Path.of(".");
        }
        String base = basenameForLegacyFolderLayout(projectFile.getFileName().toString());
        return parent.resolve(base);
    }

    /** Indica se o nome termina em {@link #EXTENSION} (comparação sem distinguir maiúsculas). */
    public static boolean endsWithProjectExtension(String fileName) {
        return fileName.toLowerCase(Locale.ROOT).endsWith(EXTENSION);
    }

    /**
     * Nome base para o layout legacy em pasta (ficheiro apontador ao lado da pasta do conteúdo).
     * Aceita {@code .jwms} (actual) e {@code .msk} (legado Manuskript).
     */
    public static String basenameForLegacyFolderLayout(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(EXTENSION)) {
            return fileName.substring(0, fileName.length() - EXTENSION.length());
        }
        if (lower.endsWith(".msk")) {
            return fileName.substring(0, fileName.length() - 4);
        }
        return fileName;
    }
}
