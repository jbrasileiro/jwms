package com.github.jbrasileiro.jwms.api;

import java.util.List;

public sealed interface SaveResult permits SaveResult.Ok, SaveResult.Failure {

    record Ok() implements SaveResult {}

    record Failure(List<String> errors) implements SaveResult {
        public Failure {
            errors = List.copyOf(errors);
        }
    }
}
