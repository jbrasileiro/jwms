package com.github.jbrasileiro.jwms.application;

import com.github.jbrasileiro.jwms.domain.manuscript.ManuscriptProject;
import com.github.jbrasileiro.jwms.domain.manuscript.ManuscriptProjectPaths;
import com.github.jbrasileiro.jwms.infrastructure.persistence.FolderEntryLister;
import com.github.jbrasileiro.jwms.infrastructure.persistence.FolderEntryReader;
import com.github.jbrasileiro.jwms.infrastructure.persistence.FormatDetection;
import com.github.jbrasileiro.jwms.infrastructure.persistence.MinimalXmlRootName;
import com.github.jbrasileiro.jwms.infrastructure.persistence.ProjectFormatDetector;
import com.github.jbrasileiro.jwms.infrastructure.persistence.ZipEntryLister;
import com.github.jbrasileiro.jwms.infrastructure.persistence.ZipEntryReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class OpenManuscriptProjectUseCase {

    private final ProjectFormatDetector detector = new ProjectFormatDetector();
    private final ZipEntryLister zipLister = new ZipEntryLister();
    private final ZipEntryReader zipReader = new ZipEntryReader();
    private final FolderEntryLister folderLister = new FolderEntryLister();
    private final FolderEntryReader folderReader = new FolderEntryReader();

    public OpenManuscriptProjectResult open(Path projectFile) {
        if (!Files.exists(projectFile)) {
            return new OpenManuscriptProjectResult.Failure(List.of("Ficheiro não existe: " + projectFile));
        }
        FormatDetection detection = detector.detect(projectFile);
        if (!detection.isOk()) {
            return new OpenManuscriptProjectResult.Failure(detection.errors());
        }
        try {
            List<String> names;
            Optional<byte[]> plotsXml;
            if (detection.zipLayout()) {
                names = zipLister.listRelativePaths(projectFile);
                plotsXml = zipReader.readEntry(projectFile, "plots.xml");
                if (plotsXml.isEmpty()) {
                    plotsXml = firstXmlInZip(projectFile, names);
                }
            } else {
                Path contentRoot = resolveContentFolder(projectFile);
                if (!Files.isDirectory(contentRoot)) {
                    return new OpenManuscriptProjectResult.Failure(
                            List.of("Formato em pasta: esperada a pasta do projecto em: " + contentRoot));
                }
                names = folderLister.listRelativePaths(contentRoot);
                plotsXml = folderReader.readEntry(contentRoot, "plots.xml");
                if (plotsXml.isEmpty()) {
                    plotsXml = firstXmlInFolder(contentRoot, names);
                }
            }
            Optional<String> rootName = plotsXml.flatMap(MinimalXmlRootName::parse);
            var project =
                    new ManuscriptProject(
                            projectFile,
                            detection.formatVersion(),
                            detection.zipLayout(),
                            names,
                            rootName);
            return new OpenManuscriptProjectResult.Success(project);
        } catch (IOException e) {
            return new OpenManuscriptProjectResult.Failure(List.of("Erro de I/O: " + e.getMessage()));
        }
    }

    private static Path resolveContentFolder(Path projectFile) {
        return ManuscriptProjectPaths.legacyContentFolder(projectFile);
    }

    private Optional<byte[]> firstXmlInZip(Path zipFile, List<String> relativeNames) throws IOException {
        for (String n : xmlPathsSorted(relativeNames)) {
            Optional<byte[]> data = zipReader.readEntry(zipFile, n);
            if (data.isPresent()) {
                return data;
            }
        }
        return Optional.empty();
    }

    private Optional<byte[]> firstXmlInFolder(Path contentRoot, List<String> relativeNames) throws IOException {
        for (String n : xmlPathsSorted(relativeNames)) {
            Optional<byte[]> data = folderReader.readEntry(contentRoot, n);
            if (data.isPresent()) {
                return data;
            }
        }
        return Optional.empty();
    }

    private static List<String> xmlPathsSorted(List<String> relativeNames) {
        List<String> xmls = new ArrayList<>();
        for (String n : relativeNames) {
            String lower = n.toLowerCase();
            if (lower.endsWith(".xml") || lower.endsWith(".opml")) {
                xmls.add(n);
            }
        }
        return xmls;
    }

    public sealed interface OpenManuscriptProjectResult permits OpenManuscriptProjectResult.Success, OpenManuscriptProjectResult.Failure {

        record Success(ManuscriptProject project) implements OpenManuscriptProjectResult {}

        record Failure(List<String> errors) implements OpenManuscriptProjectResult {}
    }
}
