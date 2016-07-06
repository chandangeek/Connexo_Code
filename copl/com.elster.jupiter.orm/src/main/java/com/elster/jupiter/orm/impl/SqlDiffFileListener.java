package com.elster.jupiter.orm.impl;

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
    private DifferencesListener state = difference -> {
        writer = createFile();
        SubsequentState subsequentState = new SubsequentState();
        state = subsequentState;
        subsequentState.onDifference(difference);
    };

    private class SubsequentState implements DifferencesListener {
        @Override
        public void onDifference(Difference difference) {
            writeEntry(buildEntryString(difference));
        }

        private void writeEntry(String entry) {
            try {
                writer.write(entry);
            } catch (IOException e) {
                throw new UnderlyingIOException(e);
            }
        }

        private String buildEntryString(Difference difference) {
            String entryPrefix = new StringJoiner("", "---- ", " start ----\n").add(difference.description()).toString();
            String entrySuffix = new StringJoiner("", "---- ", "  end  ----\n").add(difference.description()).toString();
            return difference.ddl()
                    .stream()
                    .collect(Collectors.joining("\n", entryPrefix, entrySuffix));
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

    private Writer createFile() {
        try {
            return tryCreateFile();
        } catch (IOException e) {
            throw new UnderlyingIOException(e);
        }
    }

    private Writer tryCreateFile() throws IOException {
        Path targetFile = fileSystem.getPath("./logs/connexo_difference.sql");
        Files.createDirectories(targetFile.getParent());
        return new OutputStreamWriter(Files.newOutputStream(targetFile, StandardOpenOption.CREATE));
    }
}
