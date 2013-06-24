package com.elster.jupiter.fileimport.impl;

import java.nio.file.Path;

public interface FolderScanner extends AutoCloseable {

    /**
     * @return the next new file in the scanned folder.
     */
    Path next() throws InterruptedException;

}
