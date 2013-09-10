package com.elster.jupiter.fileimport.impl;

import java.io.File;
import java.util.Iterator;

/**
 * Scans a folder for files to be imported.
 */
interface FolderScanner {

    /**
     * @return the next new file in the scanned folder.
     */
    Iterator<File> getFiles();

}
