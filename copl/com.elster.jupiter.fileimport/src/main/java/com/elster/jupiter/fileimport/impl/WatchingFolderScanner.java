package com.elster.jupiter.fileimport.impl;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

public class WatchingFolderScanner implements FolderScanner {

    private final WatchService watcher;
    private final WatchKey key;
    private final Path directory;

    public WatchingFolderScanner(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException();
        }
        this.watcher = FileSystems.getDefault().newWatchService();
        key = directory.register(watcher);
        this.directory = directory;
    }

    @Override
    public Path next() throws InterruptedException {
        while (true) {
            WatchKey key = watcher.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                if (kind == ENTRY_CREATE) {
                    Path name = (Path) event.context();
                    Path child = directory.resolve(name);
                    if (!Files.isDirectory(child)) {
                        return child;
                    }
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        key.cancel();
        watcher.close();
    }

}
