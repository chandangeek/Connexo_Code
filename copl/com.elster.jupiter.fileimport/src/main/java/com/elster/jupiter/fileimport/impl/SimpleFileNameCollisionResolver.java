package com.elster.jupiter.fileimport.impl;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * FileNameCollisionResolver that resolves conflicts by appending a number to the original file name in case of a conflict.
 */
class SimpleFileNameCollisionResolver implements FileNameCollisionResolver {

    private final FileSystem fileSystem;

    SimpleFileNameCollisionResolver(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    public Path resolve(Path path) {
        if (fileSystem.exists(path)) {
            String fileName = path.toString();
            String extension = getExtension(fileName);
            String preExtension = getBaseName(fileName);
            for (int i = 1; i < Integer.MAX_VALUE; i++) {
                fileName = preExtension + i + '.' + extension;
                Path candidate = Paths.get(fileName);
                if (!fileSystem.exists(candidate)) {
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
