package com.elster.jupiter.ftpclient;

import java.nio.file.FileSystem;
import java.util.function.Consumer;

public interface FtpSessionFactory {

    void runInSession(Consumer<FileSystem> ftpSessionBehavior);
}
