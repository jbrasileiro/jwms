package com.github.jbrasileiro.jwms.prefs;

import java.util.Optional;
import java.util.prefs.Preferences;

/** Preferências de utilizador (persistidas no armazenamento nativo do SO). */
public final class JwmsPreferences {

    private static final String NODE = "com/github/jbrasileiro/jwms";
    private static final String KEY_LOCALE = "locale.languageTag";
    private static final String KEY_LAST_PROJECT_PATH = "project.lastPath";

    private JwmsPreferences() {}

    private static Preferences node() {
        return Preferences.userRoot().node(NODE);
    }

    /** Tag BCP 47 (ex.: pt-PT) ou vazio se o utilizador escolheu “idioma do sistema”. */
    public static Optional<String> getLocaleTag() {
        String v = node().get(KEY_LOCALE, null);
        if (v == null || v.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(v.trim());
    }

    public static void setLocaleTag(String languageTag) {
        if (languageTag == null || languageTag.isBlank()) {
            node().remove(KEY_LOCALE);
        } else {
            node().put(KEY_LOCALE, languageTag.trim());
        }
    }

    public static void clearLocaleTag() {
        node().remove(KEY_LOCALE);
    }

    /** Caminho absoluto normalizado do último projeto .jwms (Abrir / Novo). */
    public static Optional<String> getLastProjectPath() {
        String v = node().get(KEY_LAST_PROJECT_PATH, null);
        if (v == null || v.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(v.trim());
    }

    public static void setLastProjectPath(String absoluteNormalizedPath) {
        if (absoluteNormalizedPath == null || absoluteNormalizedPath.isBlank()) {
            node().remove(KEY_LAST_PROJECT_PATH);
        } else {
            node().put(KEY_LAST_PROJECT_PATH, absoluteNormalizedPath.trim());
        }
    }
}
