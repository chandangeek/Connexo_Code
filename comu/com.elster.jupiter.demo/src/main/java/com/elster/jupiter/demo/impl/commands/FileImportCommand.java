package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportLogEntry;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.Status;

import javax.inject.Inject;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class FileImportCommand {

    private static final Logger LOG = Logger.getLogger(FileImportCommand.class.getName());
    private final FileImportService fileImportService;
    private final Clock clock;

    private String importerName;
    private InputStream contentStream;
    private Consumer<String> onSuccess;
    private Consumer<String> onFailure;
    private Instant currentTime;
    private Map<String, Object> importProperties = Collections.emptyMap();

    @Inject
    public FileImportCommand(FileImportService fileImportService, Clock clock) {
        this.fileImportService = fileImportService;
        this.clock = clock;
    }

    public FileImportCommand useImporter(String importerName) {
        this.importerName = importerName;
        return this;
    }

    public FileImportCommand content(InputStream contentStream) {
        this.contentStream = contentStream;
        return this;
    }

    public FileImportCommand onSuccess(Consumer<String> successMessageConsumer) {
        this.onSuccess = successMessageConsumer;
        return this;
    }

    public FileImportCommand onFailure(Consumer<String> failureMessageConsumer) {
        this.onFailure = failureMessageConsumer;
        return this;
    }

    public FileImportCommand withProperties(Map<String, Object> properties) {
        this.importProperties = Collections.unmodifiableMap(properties);
        return this;
    }

    public void run() {
        this.currentTime = this.clock.instant();
        if (this.importerName == null) {
            throw new UnableToCreate("Importer was not specified.");
        }
        if (this.onFailure == null) {
            this.onFailure = msg -> {
                throw new UnableToCreate("Importer '" + this.importerName + "' failed with message: " + msg);
            };
        }
        this.fileImportService.getImportFactory(this.importerName)
                .orElseThrow(() -> new UnableToCreate("Importer '" + this.importerName + "' was not found"))
                .createImporter(this.importProperties)
                .process(new VirtualFileImportOccurrence());
    }

    private class VirtualFileImportOccurrence implements FileImportOccurrence {

        @Override
        public InputStream getContents() {
            return FileImportCommand.this.contentStream;
        }

        @Override
        public String getFileName() {
            throw new UnsupportedOperationException("Virtual file occurrence has no real file occurrence");
        }

        @Override
        public Status getStatus() {
            return Status.PROCESSING;
        }

        @Override
        public String getStatusName() {
            return Status.PROCESSING.name();
        }

        @Override
        public void markSuccess(String message) throws IllegalStateException {
            if (FileImportCommand.this.onSuccess != null) {
                FileImportCommand.this.onSuccess.accept(message);
            }
        }

        @Override
        public void markSuccessWithFailures(String message) throws IllegalStateException {
            if (FileImportCommand.this.onFailure != null) {
                FileImportCommand.this.onFailure.accept(message);
            }
        }

        @Override
        public void markFailure(String message) {
            if (FileImportCommand.this.onFailure != null) {
                FileImportCommand.this.onFailure.accept(message);
            }
        }

        @Override
        public long getId() {
            return 0L;
        }

        @Override
        public ImportSchedule getImportSchedule() {
            throw new UnsupportedOperationException("Virtual file occurrence hasn't import schedule.");
        }

        @Override
        public Optional<Instant> getStartDate() {
            return Optional.of(FileImportCommand.this.currentTime);
        }

        @Override
        public Optional<Instant> getEndDate() {
            return Optional.empty();
        }

        @Override
        public Instant getTriggerDate() {
            return FileImportCommand.this.currentTime;
        }

        @Override
        public List<ImportLogEntry> getLogs() {
            return Collections.emptyList();
        }

        @Override
        public Logger getLogger() {
            return LOG;
        }

        @Override
        public Finder<ImportLogEntry> getLogsFinder() {
            throw new UnsupportedOperationException("Virtual file occurrence doesn't produce log entries.");
        }

        @Override
        public String getMessage() {
            throw new UnsupportedOperationException("Virtual file occurrence doesn't provide summary.");
        }
    }
}
