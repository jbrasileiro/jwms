package com.github.jbrasileiro.jwms.adapter;

import com.github.jbrasileiro.jwms.api.GeneralMetadataDto;
import com.github.jbrasileiro.jwms.domain.manuscript.GeneralMetadata;

final class GeneralMetadataMapping {

    private GeneralMetadataMapping() {}

    static GeneralMetadata fromDto(GeneralMetadataDto dto) {
        GeneralMetadata m = new GeneralMetadata();
        m.setTitle(dto.title());
        m.setSubtitle(dto.subtitle());
        m.setSeries(dto.series());
        m.setVolume(dto.volume());
        m.setGenre(dto.genre());
        m.setLicense(dto.license());
        m.setAuthorName(dto.authorName());
        m.setAuthorEmail(dto.authorEmail());
        return m;
    }

    static GeneralMetadataDto toDto(GeneralMetadata m) {
        return new GeneralMetadataDto(
                m.getTitle(),
                m.getSubtitle(),
                m.getSeries(),
                m.getVolume(),
                m.getGenre(),
                m.getLicense(),
                m.getAuthorName(),
                m.getAuthorEmail());
    }
}
