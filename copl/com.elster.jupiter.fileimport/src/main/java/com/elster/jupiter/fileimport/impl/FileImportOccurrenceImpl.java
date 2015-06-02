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
    private File file;
    private Status status;
    private String message;
    private Instant startDate;
    private Instant endDate;
    private transient InputStream inputStream;
    private final FileSystem fileSystem;
    private final DataModel dataModel;
    private final FileNameCollisionResolver fileNameCollisionResolver;
    private final Thesaurus thesaurus;
    private Clock clock;
    private List<ImportLogEntry> logEntries = new ArrayList<>();

    Logger logger;

    @Inject
    private FileImportOccurrenceImpl(FileSystem fileSystem, DataModel dataModel, FileNameCollisionResolver fileNameCollisionResolver, Thesaurus thesaurus) {
        this.fileSystem = fileSystem;
        this.dataModel = dataModel;
        this.fileNameCollisionResolver = fileNameCollisionResolver;
        this.thesaurus = thesaurus;
    }

    public static FileImportOccurrenceImpl create(FileSystem fileSystem, DataModel dataModel, FileNameCollisionResolver fileNameCollisionResolver, Thesaurus thesaurus, ImportSchedule importSchedule, File file) {
        return new FileImportOccurrenceImpl(fileSystem, dataModel, fileNameCollisionResolver, thesaurus).init(importSchedule, file);
    }

    @Override
    public void prepareProcessing() {
        if (!Status.NEW.equals(getStatus())) {
            throw new IllegalStateException();
        }
        this.status = Status.PROCESSING;

        MessageSeeds.FILE_IMPORT_STARTED.log(getLogger(),thesaurus);

        moveFile();
        save();
    }

    private FileImportOccurrenceImpl init(ImportSchedule importSchedule, File file) {
        this.file = file;
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
            inputStream = fileSystem.getInputStream(file);
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
    public void markFailure() {
        validateStatus();
        status = Status.FAILURE;
        ensureStreamClosed();
        moveFile();
        save();
    }

    @Override
    public void markSuccess() {
        validateStatus();
        status = Status.SUCCESS;
        ensureStreamClosed();
        moveFile();
        save();
        MessageSeeds.FILE_IMPORT_FINISHED.log(getLogger(),thesaurus);
    }

    @Override
    public String getFileName() {
        return file.toPath().getFileName().toString();
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
    public void setClock(Clock clock){
        this.clock = clock;
    }

    @Override
    public Finder<ImportLogEntry> getLogsFinder() {
        Condition condition = where("fileImportOccurrenceReference").isEqualTo(this);
        //Order[] orders = new Order[]{Order.descending("timeStamp"), Order.ascending("position")};
        return DefaultFinder.of(ImportLogEntry.class,condition , dataModel)
                .sorted("timeStamp", false)
                .sorted("position", true);
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
        if (file.exists()) {
            Path path = file.toPath();
            Path target = targetPath(path);
            fileSystem.move(path, target);
            file = target.toFile();
        }
    }

    private Path targetPath(Path path) {
        return fileNameCollisionResolver.resolve(getTargetDirectory().resolve(path.getFileName()));
    }

    private Path getTargetDirectory() {
        switch (status) {
            case SUCCESS:
                return getImportSchedule().getSuccessDirectory().toPath();
            case FAILURE:
                return getImportSchedule().getFailureDirectory().toPath();
            case PROCESSING:
                return getImportSchedule().getInProcessDirectory().toPath();
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


}
