package com.github.jbrasileiro.jwms.infrastructure.persistence;

import java.util.List;

public record FormatDetection(int formatVersion, boolean zipLayout, List<String> errors) {

    public boolean isOk() {
        return errors.isEmpty();
    }
}
