/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Scans a folder for files to be imported.
 */
interface FolderScanner {

    /**
     * @return the next new file in the scanned folder.
     */
    Stream<Path> getFiles();

}
