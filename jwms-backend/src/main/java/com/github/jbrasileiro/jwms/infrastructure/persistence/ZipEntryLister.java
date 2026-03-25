package com.github.jbrasileiro.jwms.infrastructure.persistence;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipFile;

public final class ZipEntryLister {

    public List<String> listRelativePaths(Path zipFile) throws IOException {
        try (ZipFile zf = new ZipFile(zipFile.toFile())) {
            List<String> names = new ArrayList<>();
            var entries = zf.entries();
            while (entries.hasMoreElements()) {
                var e = entries.nextElement();
                if (e.isDirectory()) {
                    continue;
                }
                String n = e.getName().replace('\\', '/');
                if (!n.isEmpty() && n.charAt(0) != '.' && !startsWithHiddenPathSegment(n)) {
                    names.add(n);
                }
            }
            Collections.sort(names);
            return names;
        }
    }

    private static boolean startsWithHiddenPathSegment(String n) {
        for (String part : n.split("/")) {
            if (!part.isEmpty() && part.charAt(0) == '.') {
                return true;
            }
        }
        return false;
    }
}
