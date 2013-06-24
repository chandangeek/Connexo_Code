package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileIOException;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class PollingFolderScanner implements FolderScanner {

    private final Path directory;
    private final long interval;
    private final TimeUnit timeUnit;
    private volatile State state;

    public PollingFolderScanner(Path directory, long interval, TimeUnit timeUnit) {
        this.directory = directory;
        this.interval = interval;
        this.timeUnit = timeUnit;
        this.state = new UsingDirectoryStream();
    }

    @Override
    public Path next() throws InterruptedException {
        Path next = state.next();
        while (next == null) {
            timeUnit.sleep(interval);
            state = new UsingDirectoryStream();
        }
        return next;
    }

    @Override
    public void close() throws Exception {
        state.close();
    }

    private interface State extends AutoCloseable {
        Path next() throws InterruptedException;
    }

    private class UsingDirectoryStream implements State {
        private final DirectoryStream<Path> directoryStream;
        private final Iterator<Path> onlyFiles;

        private UsingDirectoryStream() {
            try {
                directoryStream = Files.newDirectoryStream(directory);
                onlyFiles = FluentIterable.from(directoryStream).filter(Only.FILES).iterator();
            } catch (IOException e) {
                throw new FileIOException(e);
            }
        }

        @Override
        public Path next() throws InterruptedException {
            try {
                if (onlyFiles.hasNext()) {
                    return onlyFiles.next();
                }
                directoryStream.close();
                return null;
            } catch (IOException e) {
                throw new FileIOException(e);
            }
        }

        @Override
        public void close() throws Exception {
            directoryStream.close();
        }
    }

    private class Sleeping implements State {

        @Override
        public void close() throws Exception {

        }

        @Override
        public Path next() throws InterruptedException {
            return null;
        }
    }

    private enum Only implements Predicate<Path> {
        FILES;

        @Override
        public boolean apply(Path input) {
            return !Files.isDirectory(input);
        }
    }
}
