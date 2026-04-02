package com.github.jbrasileiro.jwms.api;

import java.util.List;

public sealed interface LoadSummaryResult permits LoadSummaryResult.Success, LoadSummaryResult.Failure {

    record Success(SummaryMetadataDto data) implements LoadSummaryResult {}

    record Failure(List<String> errors) implements LoadSummaryResult {
        public Failure {
            errors = List.copyOf(errors);
        }
    }
}
