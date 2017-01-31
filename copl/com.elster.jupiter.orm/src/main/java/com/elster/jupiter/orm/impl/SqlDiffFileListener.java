/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.DdlDifference;
import com.elster.jupiter.orm.Difference;
import com.elster.jupiter.orm.DifferencesListener;
import com.elster.jupiter.orm.UnderlyingIOException;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.StringJoiner;
import java.util.stream.Collectors;

class SqlDiffFileListener implements DifferencesListener {

    private final FileSystem fileSystem;
    private Writer writer;
    private DifferencesListener state = new InitialDifferencesListener();

    private class SubsequentState implements DifferencesListener {
        @Override
        public void onDifference(Difference difference) {
            if (difference instanceof DdlDifference) {
                writeEntry(buildEntryString((DdlDifference) difference));
            }
        }

        private void writeEntry(String entry) {
            try {
                writer.write(entry);
            } catch (IOException e) {
                throw new UnderlyingIOException(e);
            }
        }

        private String buildEntryString(DdlDifference difference) {
            String entryPrefix = new StringJoiner("", "---- ", " start ----\n").add(difference.description()).toString();
            String entrySuffix = new StringJoiner("", ";\n---- ", "  end  ----\n").add(difference.description()).toString();
            return difference.ddl()
                    .stream()
                    .collect(Collectors.joining(";\n", entryPrefix, entrySuffix));
        }

        @Override
        public void done() {
            try {
                writer.close();
            } catch (IOException e) {
                throw new UnderlyingIOException(e);
            }
        }
    }

    @Inject
    SqlDiffFileListener(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    public void onDifference(Difference difference) {
        state.onDifference(difference);
    }

    @Override
    public void done() {
        state.done();
    }

    private Writer createFile() {
        try {
            return tryCreateFile();
        } catch (IOException e) {
            throw new UnderlyingIOException(e);
        }
    }

    private Writer tryCreateFile() throws IOException {
        Path targetFile = getTargetFile();
        Files.createDirectories(targetFile.getParent());
        return new OutputStreamWriter(Files.newOutputStream(targetFile, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE));
    }

    private Path getTargetFile() {
        return fileSystem.getPath("./connexo_difference.sql");
    }

    private class InitialDifferencesListener implements DifferencesListener {
        @Override
        public void onDifference(Difference difference) {
            writer = SqlDiffFileListener.this.createFile();
            SubsequentState subsequentState = new SubsequentState();
            state = subsequentState;
            subsequentState.onDifference(difference);
        }

        @Override
        public void done() {
            // if this is still the state at done, we should remove the file
            try {
                Files.deleteIfExists(getTargetFile());
            } catch (IOException e) {
                throw new UnderlyingIOException(e);
            }
        }
    }
}
