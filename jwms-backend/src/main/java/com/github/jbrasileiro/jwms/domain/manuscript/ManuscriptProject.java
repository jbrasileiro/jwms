package com.github.jbrasileiro.jwms.domain.manuscript;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ManuscriptProject {

    private final Path projectFile;
    private final int formatVersion;
    private final boolean zipContainer;
    private final List<String> relativeEntryNames;
    private final Optional<String> sampleXmlRootLocalName;

    public ManuscriptProject(
            Path projectFile,
            int formatVersion,
            boolean zipContainer,
            List<String> relativeEntryNames,
            Optional<String> sampleXmlRootLocalName) {
        this.projectFile = Objects.requireNonNull(projectFile);
        this.formatVersion = formatVersion;
        this.zipContainer = zipContainer;
        this.relativeEntryNames = List.copyOf(relativeEntryNames);
        this.sampleXmlRootLocalName = Objects.requireNonNull(sampleXmlRootLocalName);
    }

    public Path getProjectFile() {
        return projectFile;
    }

    public int getFormatVersion() {
        return formatVersion;
    }

    public boolean isZipContainer() {
        return zipContainer;
    }

    public List<String> getRelativeEntryNames() {
        return relativeEntryNames;
    }

    public Optional<String> getSampleXmlRootLocalName() {
        return sampleXmlRootLocalName;
    }
}
