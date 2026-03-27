package com.github.jbrasileiro.jwms.domain.manuscript;

import java.util.Locale;

/** Convenções de nome de ficheiro de projeto na UI JWMS. */
public final class ManuscriptProjectPaths {

    public static final String EXTENSION = ".jwms";

    private ManuscriptProjectPaths() {}

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
