/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import java.io.File;
import java.nio.file.Path;

/**
 * Interface for classes that can handle Files to import.
 */
interface FileHandler {

    void handle(Path path);
}
