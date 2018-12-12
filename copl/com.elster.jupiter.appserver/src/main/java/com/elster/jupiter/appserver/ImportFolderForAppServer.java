/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver;

import java.nio.file.Path;
import java.util.Optional;

public interface ImportFolderForAppServer {
    Optional<Path> getImportFolder();
    AppServer getAppServer();
    void setImportFolder(Path path);
    void save();
    void delete();
}
