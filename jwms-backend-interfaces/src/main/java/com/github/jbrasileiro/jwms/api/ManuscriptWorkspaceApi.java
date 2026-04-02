package com.github.jbrasileiro.jwms.api;

import java.nio.file.Path;

/**
 * Porta de entrada agregada: toda a orquestração de projecto e metadados Geral fica no backend;
 * a UI só depende desta interface.
 */
public interface ManuscriptWorkspaceApi {

    OpenProjectResult openProject(Path projectFile);

    CreateProjectResult createProject(Path projectFile, boolean overwrite);

    LoadGeneralResult loadGeneral(ProjectSnapshot snapshot);

    SaveResult saveGeneral(ProjectSnapshot snapshot, GeneralMetadataDto metadata);

    LoadSummaryResult loadSummary(ProjectSnapshot snapshot);

    SaveResult saveSummary(ProjectSnapshot snapshot, SummaryMetadataDto metadata);
}
