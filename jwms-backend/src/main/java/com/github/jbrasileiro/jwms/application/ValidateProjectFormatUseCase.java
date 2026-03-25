package com.github.jbrasileiro.jwms.application;

import com.github.jbrasileiro.jwms.infrastructure.persistence.FormatDetection;
import com.github.jbrasileiro.jwms.infrastructure.persistence.ProjectFormatDetector;
import java.nio.file.Path;

public final class ValidateProjectFormatUseCase {

    private final ProjectFormatDetector detector = new ProjectFormatDetector();

    public FormatDetection validate(Path projectFile) {
        return detector.detect(projectFile);
    }
}
