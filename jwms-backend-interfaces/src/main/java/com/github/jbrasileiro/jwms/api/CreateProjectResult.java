package com.github.jbrasileiro.jwms.api;

import java.nio.file.Path;
import java.util.List;

public sealed interface CreateProjectResult permits CreateProjectResult.Success, CreateProjectResult.Failure {

    record Success(Path path) implements CreateProjectResult {}

    record Failure(List<String> errors) implements CreateProjectResult {
        public Failure {
            errors = List.copyOf(errors);
        }
    }
}
