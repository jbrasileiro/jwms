package com.github.jbrasileiro.jwms.infrastructure.persistence;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FolderEntryLister {

    public List<String> listRelativePaths(Path projectContentRoot) throws IOException {
        if (!Files.isDirectory(projectContentRoot)) {
            return List.of();
        }
        Path root = projectContentRoot.toAbsolutePath().normalize();
        List<String> names = new ArrayList<>();
        Files.walkFileTree(
                root,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        Path rel = root.relativize(dir);
                        String s = rel.toString();
                        if (s.isEmpty()) {
                            return FileVisitResult.CONTINUE;
                        }
                        if (startsWithHiddenSegment(rel)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        Path rel = root.relativize(file);
                        if (rel.getFileName() != null && rel.getFileName().toString().charAt(0) == '.') {
                            return FileVisitResult.CONTINUE;
                        }
                        if (startsWithHiddenSegment(rel)) {
                            return FileVisitResult.CONTINUE;
                        }
                        names.add(rel.toString().replace('\\', '/'));
                        return FileVisitResult.CONTINUE;
                    }
                });
        Collections.sort(names);
        return names;
    }

    private static boolean startsWithHiddenSegment(Path relative) {
        for (Path part : relative) {
            String name = part.toString();
            if (!name.isEmpty() && name.charAt(0) == '.') {
                return true;
            }
        }
        return false;
    }
}
