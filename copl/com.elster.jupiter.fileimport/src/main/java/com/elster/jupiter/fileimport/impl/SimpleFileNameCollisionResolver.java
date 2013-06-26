package com.elster.jupiter.fileimport.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SimpleFileNameCollisionResolver implements FileNameCollisionResolver {

    @Override
    public Path resolve(Path path) {
        if (Files.exists(path)) {
            String fileName = path.toString();
            String extension = getExtension(fileName);
            String preExtension = getBaseName(fileName);
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                fileName = preExtension + i + '.' + extension;
                Path candidate = Paths.get(fileName);
                if (!Files.exists(candidate)) {
                    return candidate;
                }
            }
        }
        return path;
    }

    private String getBaseName(String fileName) {
        int i = fileName.lastIndexOf('.');
        return i < 0 ? fileName : fileName.substring(0, i);
    }

    private String getExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        return i < 0 ? null : fileName.substring(i + 1);
    }
}
