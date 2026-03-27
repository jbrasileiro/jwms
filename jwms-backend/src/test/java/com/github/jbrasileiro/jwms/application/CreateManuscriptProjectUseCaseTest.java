package com.github.jbrasileiro.jwms.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.jbrasileiro.jwms.application.CreateManuscriptProjectUseCase.CreateManuscriptProjectResult;
import com.github.jbrasileiro.jwms.application.OpenManuscriptProjectUseCase.OpenManuscriptProjectResult;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CreateManuscriptProjectUseCaseTest {

    @TempDir Path tempDir;

    private final CreateManuscriptProjectUseCase create = new CreateManuscriptProjectUseCase();
    private final OpenManuscriptProjectUseCase open = new OpenManuscriptProjectUseCase();

    @Test
    void createsMinimalV2ZipOpenableByOpenUseCase() {
        Path jwms = tempDir.resolve("new.jwms");
        CreateManuscriptProjectResult c = create.create(jwms, false);
        var ok = assertInstanceOf(CreateManuscriptProjectResult.Success.class, c);
        assertEquals(jwms, ok.path());
        assertTrue(Files.isRegularFile(jwms));

        OpenManuscriptProjectResult r = open.open(jwms);
        var success = assertInstanceOf(OpenManuscriptProjectResult.Success.class, r);
        assertEquals(2, success.project().getFormatVersion());
        assertTrue(success.project().isZipContainer());
        assertTrue(success.project().getRelativeEntryNames().contains("plots.xml"));
    }

    @Test
    void failsWhenExistsWithoutOverwrite() throws Exception {
        Path msk = tempDir.resolve("x.jwms");
        Files.writeString(msk, "x");
        CreateManuscriptProjectResult c = create.create(msk, false);
        assertInstanceOf(CreateManuscriptProjectResult.Failure.class, c);
    }

    @Test
    void overwritesWhenRequested() {
        Path msk = tempDir.resolve("y.jwms");
        assertInstanceOf(CreateManuscriptProjectResult.Success.class, create.create(msk, false));
        assertInstanceOf(CreateManuscriptProjectResult.Success.class, create.create(msk, true));
        assertInstanceOf(OpenManuscriptProjectResult.Success.class, open.open(msk));
    }
}
