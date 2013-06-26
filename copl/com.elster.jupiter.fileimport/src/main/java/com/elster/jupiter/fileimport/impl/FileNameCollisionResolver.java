package com.elster.jupiter.fileimport.impl;

import java.nio.file.Path;

public interface FileNameCollisionResolver {

    /**
     * @param path
     * @return the given path, if it doesn't exist, or a Path in the same folder, with a file name that does ot exist in that folder.
     */
    Path resolve(Path path);
}
