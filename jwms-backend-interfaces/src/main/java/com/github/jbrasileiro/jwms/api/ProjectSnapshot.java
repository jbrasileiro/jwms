package com.github.jbrasileiro.jwms.api;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/** Vista estável do projecto aberto para a UI (sem tipos de domínio internos). */
public record ProjectSnapshot(
        Path projectFile,
        boolean zipContainer,
        int formatVersion,
        List<String> relativeEntryNames,
        Optional<String> sampleXmlRootLocalName) {

    public ProjectSnapshot {
        relativeEntryNames = List.copyOf(relativeEntryNames);
    }
}
