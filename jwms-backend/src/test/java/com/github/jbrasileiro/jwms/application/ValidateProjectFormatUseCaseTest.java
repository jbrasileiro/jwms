package com.github.jbrasileiro.jwms.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.jbrasileiro.jwms.infrastructure.persistence.FormatDetection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ValidateProjectFormatUseCaseTest {

    @TempDir Path tempDir;

    @Test
    void validateMatchesOpenDetection() throws Exception {
        Path msk = tempDir.resolve("x.msk");
        Files.writeString(msk, "1", StandardCharsets.UTF_8);
        var useCase = new ValidateProjectFormatUseCase();
        FormatDetection d = useCase.validate(msk);
        assertTrue(d.isOk());
        assertEquals(1, d.formatVersion());
        assertTrue(!d.zipLayout());
    }
}
