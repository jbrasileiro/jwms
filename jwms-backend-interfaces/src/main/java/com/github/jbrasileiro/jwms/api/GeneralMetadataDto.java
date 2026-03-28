package com.github.jbrasileiro.jwms.api;

/** Metadados do ecrã Geral persistidos em {@code jwms/main/General.json}. */
public record GeneralMetadataDto(
        String title,
        String subtitle,
        String series,
        String volume,
        String genre,
        String license,
        String authorName,
        String authorEmail) {

    public static GeneralMetadataDto empty() {
        return new GeneralMetadataDto("", "", "", "", "", "", "", "");
    }

    public GeneralMetadataDto {
        title = nullToEmpty(title);
        subtitle = nullToEmpty(subtitle);
        series = nullToEmpty(series);
        volume = nullToEmpty(volume);
        genre = nullToEmpty(genre);
        license = nullToEmpty(license);
        authorName = nullToEmpty(authorName);
        authorEmail = nullToEmpty(authorEmail);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
