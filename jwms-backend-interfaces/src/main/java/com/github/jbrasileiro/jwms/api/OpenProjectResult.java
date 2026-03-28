package com.github.jbrasileiro.jwms.api;

import java.util.List;

public sealed interface OpenProjectResult permits OpenProjectResult.Success, OpenProjectResult.Failure {

    record Success(ProjectSnapshot snapshot) implements OpenProjectResult {}

    record Failure(List<String> errors) implements OpenProjectResult {
        public Failure {
            errors = List.copyOf(errors);
        }
    }
}
