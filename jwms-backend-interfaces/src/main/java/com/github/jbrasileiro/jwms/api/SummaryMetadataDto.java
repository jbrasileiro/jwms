package com.github.jbrasileiro.jwms.api;

/**
 * Metadados do ecrã Resumo persistidos em {@code jwms/main/Summary.json}. Quatro textos, um por
 * estado do combo: frase, parágrafo, página, completo.
 */
public record SummaryMetadataDto(
        String situation,
        String lengthMode,
        String sentenceText,
        String paragraphText,
        String pageText,
        String fullText) {

    public static SummaryMetadataDto empty() {
        return new SummaryMetadataDto("", "SENTENCE", "", "", "", "");
    }

    public SummaryMetadataDto {
        situation = nullToEmpty(situation);
        lengthMode = lengthMode == null || lengthMode.isBlank() ? "SENTENCE" : lengthMode.trim();
        sentenceText = nullToEmpty(sentenceText);
        paragraphText = nullToEmpty(paragraphText);
        pageText = nullToEmpty(pageText);
        fullText = nullToEmpty(fullText);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
