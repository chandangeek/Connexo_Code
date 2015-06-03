package com.elster.jupiter.appserver;

import java.nio.file.Path;
import java.util.Optional;

public interface ImportFolderForAppServer {
    Optional<Path> getImportFolder();
    AppServer getAppServer();
    void save();

}
