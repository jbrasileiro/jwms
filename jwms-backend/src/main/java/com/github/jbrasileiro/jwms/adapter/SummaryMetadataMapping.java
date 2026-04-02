package com.github.jbrasileiro.jwms.adapter;

import com.github.jbrasileiro.jwms.api.SummaryMetadataDto;
import com.github.jbrasileiro.jwms.domain.manuscript.SummaryMetadata;

final class SummaryMetadataMapping {

    private SummaryMetadataMapping() {}

    static SummaryMetadata fromDto(SummaryMetadataDto dto) {
        SummaryMetadata m = new SummaryMetadata();
        m.setSituation(dto.situation());
        m.setLengthMode(dto.lengthMode());
        m.setSentenceText(dto.sentenceText());
        m.setParagraphText(dto.paragraphText());
        m.setPageText(dto.pageText());
        m.setFullText(dto.fullText());
        return m;
    }

    static SummaryMetadataDto toDto(SummaryMetadata m) {
        return new SummaryMetadataDto(
                m.getSituation(),
                m.getLengthMode(),
                m.getSentenceText(),
                m.getParagraphText(),
                m.getPageText(),
                m.getFullText());
    }
}
