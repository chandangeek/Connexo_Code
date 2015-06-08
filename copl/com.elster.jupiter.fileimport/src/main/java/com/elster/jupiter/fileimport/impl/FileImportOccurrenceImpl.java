package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fileimport.*;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

final class FileImportOccurrenceImpl implements FileImportOccurrence {

    private long id;
    private ImportSchedule importSchedule;
    private long importScheduleId;
    private Path path;
    private Status status;
    private String message;
    private Instant startDate;
    private Instant endDate;
    private transient InputStream inputStream;
    private final FileSystem fileSystem;
    private final DataModel dataModel;
    private final FileNameCollisionResolver fileNameCollisionResolver;
    private final Thesaurus thesaurus;
    private FileImportService fileImportService;
    private List<ImportLogEntry> logEntries = new ArrayList<>();

    Logger logger;
    private Clock clock;

    @Inject
    private FileImportOccurrenceImpl(FileImportService fileImportService, FileSystem fileSystem, DataModel dataModel, FileNameCollisionResolver fileNameCollisionResolver, Thesaurus thesaurus, Clock clock) {
        this.fileSystem = fileSystem;
        this.dataModel = dataModel;
        this.fileNameCollisionResolver = fileNameCollisionResolver;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.fileImportService = fileImportService;
    }

    public static FileImportOccurrenceImpl create(FileImportService fileImportService, FileSystem fileSystem, DataModel dataModel, FileNameCollisionResolver fileNameCollisionResolver, Thesaurus thesaurus, Clock clock, ImportSchedule importSchedule, Path path) {
        return new FileImportOccurrenceImpl(fileImportService, fileSystem, dataModel, fileNameCollisionResolver, thesaurus, clock).init(importSchedule, path);
    }

    @Override
    public void prepareProcessing() {
        if (!Status.NEW.equals(getStatus())) {
            throw new IllegalStateException();
        }
        this.status = Status.PROCESSING;
        this.startDate = clock.instant();

        MessageSeeds.FILE_IMPORT_STARTED.log(getLogger(),thesaurus);

        moveFile();
        save();
    }

    private FileImportOccurrenceImpl init(ImportSchedule importSchedule, Path path) {
        this.path = path;
        this.importSchedule = importSchedule;
        this.importScheduleId = importSchedule.getId();
        this.status = Status.NEW;
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public InputStream getContents() {
        if (inputStream == null) {
            inputStream = fileSystem.getInputStream(fileImportService.getBasePath().resolve(path));
        }
        return inputStream;
    }

    @Override
    public ImportSchedule getImportSchedule() {
        if (importSchedule == null) {
            importSchedule = importScheduleFactory().getExisting(importScheduleId);
        }
        return importSchedule;
    }

    private DataMapper<ImportSchedule> importScheduleFactory() {
        return dataModel.mapper(ImportSchedule.class);
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void markFailure(String message) {
        validateStatus();
        this.message = message;
        this.endDate = clock.instant();
        status = Status.FAILURE;
        ensureStreamClosed();
        moveFile();
        save();
    }

    @Override
    public void markSuccess(String message) {
        validateStatus();
        status = Status.SUCCESS;
        this.message = message;
        this.endDate = clock.instant();
        ensureStreamClosed();
        moveFile();
        save();
        MessageSeeds.FILE_IMPORT_FINISHED.log(getLogger(),thesaurus);
    }

    @Override
    public void markSuccessWithFailures(String message) {
        validateStatus();
        status = Status.SUCCESS_WITH_FAILURES;
        this.message = message;
        this.endDate = clock.instant();
        ensureStreamClosed();
        moveFile();
        save();
        MessageSeeds.FILE_IMPORT_FINISHED.log(getLogger(),thesaurus);
    }

    @Override
    public String getFileName() {
        return fileImportService.getBasePath().resolve(path).getFileName().toString();
    }

    @Override
    public Optional<Instant> getStartDate() {
        return Optional.ofNullable(startDate);
    }

    @Override
    public Optional<Instant> getEndDate() {
        return Optional.ofNullable(endDate);
    }

    @Override
    public List<ImportLogEntry> getLogs() {
        return Collections.unmodifiableList(logEntries);
    }

    @Override
    public FileImportLogHandler createFileImportLogHandler() {
        return new FileImportLogHandlerImpl(this);
    }

    public Logger getLogger() {
        if(logger == null) {
            logger = Logger.getAnonymousLogger();
            logger.addHandler(createFileImportLogHandler().asHandler());
        }
        return logger;
    }

    @Override
    public Finder<ImportLogEntry> getLogsFinder() {
        Condition condition = where("fileImportOccurrenceReference").isEqualTo(this);
        //Order[] orders = new Order[]{Order.descending("timeStamp"), Order.ascending("position")};
        return DefaultFinder.of(ImportLogEntry.class, condition, dataModel);
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    //used just for tests
    void setLogger(Logger logger) {
        this.logger = logger;
    }

    void log(Level level, Instant timestamp, String message) {
        logEntries.add(dataModel.getInstance(ImportLogEntryImpl.class).init(this, timestamp, level, message));
    }

    void save() {
        if (id == 0) {
            fileImportFactory().persist(this);
        } else {
            fileImportFactory().update(this);
        }
    }

    private DataMapper<FileImportOccurrence> fileImportFactory() {
        return dataModel.mapper(FileImportOccurrence.class);
    }

    private void moveFile() {
        Path tmpPath = fileImportService.getBasePath().getFileSystem().getPath(path.toString());
        Path filePath = fileImportService.getBasePath().resolve(tmpPath);
        if (Files.exists(filePath)) {
            Path target = targetPath(filePath);
            fileSystem.move(filePath, target);
            path = fileImportService.getBasePath().relativize(target);
        }
    }

    private Path targetPath(Path path) {
        return fileNameCollisionResolver.resolve(fileImportService.getBasePath().resolve(getTargetDirectory()).resolve(path.getFileName()));
    }

    private Path getTargetDirectory() {
        switch (status) {
            case SUCCESS:
            case SUCCESS_WITH_FAILURES:
                return fileImportService.getBasePath().getFileSystem().getPath(getImportSchedule().getSuccessDirectory().toString());
            case FAILURE:
                return fileImportService.getBasePath().getFileSystem().getPath(getImportSchedule().getFailureDirectory().toString());
            case PROCESSING:
                return fileImportService.getBasePath().getFileSystem().getPath(getImportSchedule().getInProcessDirectory().toString());
            default:
                throw new IllegalStateException();
        }
    }

    private void ensureStreamClosed() {
        if (inputStream == null) {
            return;
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new FileIOException(e, thesaurus);
        }
    }

    private void validateStatus() {
        if (!Status.PROCESSING.equals(status)) {
            throw new IllegalStateException();
        }
    }

    protected void setClock(Clock clock) {
        this.clock = clock;
    }

    protected Clock getClock() {
        return clock;
    }
}
