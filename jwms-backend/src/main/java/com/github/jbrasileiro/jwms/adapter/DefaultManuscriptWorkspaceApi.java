package com.github.jbrasileiro.jwms.adapter;

import com.github.jbrasileiro.jwms.api.CreateProjectResult;
import com.github.jbrasileiro.jwms.api.GeneralMetadataDto;
import com.github.jbrasileiro.jwms.api.LoadGeneralResult;
import com.github.jbrasileiro.jwms.api.LoadSummaryResult;
import com.github.jbrasileiro.jwms.api.ManuscriptWorkspaceApi;
import com.github.jbrasileiro.jwms.api.OpenProjectResult;
import com.github.jbrasileiro.jwms.api.ProjectSnapshot;
import com.github.jbrasileiro.jwms.api.SaveResult;
import com.github.jbrasileiro.jwms.api.SummaryMetadataDto;
import com.github.jbrasileiro.jwms.application.CreateManuscriptProjectUseCase;
import com.github.jbrasileiro.jwms.application.OpenManuscriptProjectUseCase;
import com.github.jbrasileiro.jwms.infrastructure.persistence.GeneralMetadataPersistence;
import com.github.jbrasileiro.jwms.infrastructure.persistence.SummaryMetadataPersistence;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Implementação da API exposta ao frontend: delega nos casos de uso e na persistência Geral. */
public final class DefaultManuscriptWorkspaceApi implements ManuscriptWorkspaceApi {

    private final OpenManuscriptProjectUseCase openUseCase = new OpenManuscriptProjectUseCase();
    private final CreateManuscriptProjectUseCase createUseCase = new CreateManuscriptProjectUseCase();
    private final GeneralMetadataPersistence generalPersistence = new GeneralMetadataPersistence();
    private final SummaryMetadataPersistence summaryPersistence = new SummaryMetadataPersistence();

    @Override
    public OpenProjectResult openProject(Path projectFile) {
        var r = openUseCase.open(projectFile);
        if (r instanceof OpenManuscriptProjectUseCase.OpenManuscriptProjectResult.Failure f) {
            return new OpenProjectResult.Failure(f.errors());
        }
        var s = (OpenManuscriptProjectUseCase.OpenManuscriptProjectResult.Success) r;
        var p = s.project();
        return new OpenProjectResult.Success(
                new ProjectSnapshot(
                        p.getProjectFile(),
                        p.isZipContainer(),
                        p.getFormatVersion(),
                        p.getRelativeEntryNames(),
                        p.getSampleXmlRootLocalName()));
    }

    @Override
    public CreateProjectResult createProject(Path projectFile, boolean overwrite) {
        var r = createUseCase.create(projectFile, overwrite);
        if (r instanceof CreateManuscriptProjectUseCase.CreateManuscriptProjectResult.Failure f) {
            return new CreateProjectResult.Failure(f.errors());
        }
        var ok = (CreateManuscriptProjectUseCase.CreateManuscriptProjectResult.Success) r;
        return new CreateProjectResult.Success(ok.path());
    }

    @Override
    public LoadGeneralResult loadGeneral(ProjectSnapshot snapshot) {
        try {
            var data = generalPersistence.load(snapshot.projectFile(), snapshot.zipContainer());
            return new LoadGeneralResult.Success(GeneralMetadataMapping.toDto(data));
        } catch (IOException e) {
            return new LoadGeneralResult.Failure(List.of(e.getMessage()));
        }
    }

    @Override
    public SaveResult saveGeneral(ProjectSnapshot snapshot, GeneralMetadataDto metadata) {
        try {
            generalPersistence.save(
                    snapshot.projectFile(),
                    snapshot.zipContainer(),
                    GeneralMetadataMapping.fromDto(metadata));
            return new SaveResult.Ok();
        } catch (IOException e) {
            return new SaveResult.Failure(List.of(e.getMessage()));
        }
    }

    @Override
    public LoadSummaryResult loadSummary(ProjectSnapshot snapshot) {
        try {
            var data = summaryPersistence.load(snapshot.projectFile(), snapshot.zipContainer());
            return new LoadSummaryResult.Success(SummaryMetadataMapping.toDto(data));
        } catch (IOException e) {
            return new LoadSummaryResult.Failure(List.of(e.getMessage()));
        }
    }

    @Override
    public SaveResult saveSummary(ProjectSnapshot snapshot, SummaryMetadataDto metadata) {
        try {
            summaryPersistence.save(
                    snapshot.projectFile(),
                    snapshot.zipContainer(),
                    SummaryMetadataMapping.fromDto(metadata));
            return new SaveResult.Ok();
        } catch (IOException e) {
            return new SaveResult.Failure(List.of(e.getMessage()));
        }
    }
}
