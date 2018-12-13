/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import java.nio.file.Path;

/**
 * Abstraction for algorithms to generate a Path name that does not exist yet in a directory, so a new File can be created with the generated Path
 */
interface FileNameCollisionResolver {

    /**
     * @param path path pointing to a file.
     * @return the given path, if it doesn't exist, or a Path in the same folder, with a file name that does not exist in that folder.
     */
    Path resolve(Path path);
}
