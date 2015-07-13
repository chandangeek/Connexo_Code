package com.elster.jupiter.ftpclient;

import java.io.IOException;
import java.nio.file.FileSystem;

@FunctionalInterface
public interface IOConsumer {

    void accept(FileSystem fileSystem) throws IOException;
}
