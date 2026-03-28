package com.github.jbrasileiro.jwms.api;

import java.util.List;

public sealed interface LoadGeneralResult permits LoadGeneralResult.Success, LoadGeneralResult.Failure {

    record Success(GeneralMetadataDto data) implements LoadGeneralResult {}

    record Failure(List<String> errors) implements LoadGeneralResult {
        public Failure {
            errors = List.copyOf(errors);
        }
    }
}
