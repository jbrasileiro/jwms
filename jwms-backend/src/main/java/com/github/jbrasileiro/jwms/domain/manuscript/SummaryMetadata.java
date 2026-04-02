package com.github.jbrasileiro.jwms.domain.manuscript;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class SummaryMetadata {

    private String situation = "";
    private String lengthMode = "SENTENCE";
    private String sentenceText = "";
    private String paragraphText = "";
    private String pageText = "";
    private String fullText = "";

    public SummaryMetadata() {}

    public String getSituation() {
        return situation;
    }

    public void setSituation(String situation) {
        this.situation = situation == null ? "" : situation;
    }

    public String getLengthMode() {
        return lengthMode;
    }

    public void setLengthMode(String lengthMode) {
        this.lengthMode =
                lengthMode == null || lengthMode.isBlank() ? "SENTENCE" : lengthMode.trim();
    }

    public String getSentenceText() {
        return sentenceText;
    }

    public void setSentenceText(String sentenceText) {
        this.sentenceText = sentenceText == null ? "" : sentenceText;
    }

    public String getParagraphText() {
        return paragraphText;
    }

    public void setParagraphText(String paragraphText) {
        this.paragraphText = paragraphText == null ? "" : paragraphText;
    }

    public String getPageText() {
        return pageText;
    }

    public void setPageText(String pageText) {
        this.pageText = pageText == null ? "" : pageText;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText == null ? "" : fullText;
    }
}
