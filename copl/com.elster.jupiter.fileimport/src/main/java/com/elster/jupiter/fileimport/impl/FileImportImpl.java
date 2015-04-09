package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileIOException;
import com.elster.jupiter.fileimport.FileImport;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

final class FileImportImpl implements FileImport {

    private long id;
    private ImportSchedule importSchedule;
    private long importScheduleId;
    private File file;
    private State state;
    private transient InputStream inputStream;
    private final FileSystem fileSystem;
    private final DataModel dataModel;
    private final FileNameCollisionResolver fileNameCollisionResolver;
    private final Thesaurus thesaurus;

    private FileImportImpl(FileSystem fileSystem, DataModel dataModel, FileNameCollisionResolver fileNameCollisionResolver, Thesaurus thesaurus) {
        this.fileSystem = fileSystem;
        this.dataModel = dataModel;
        this.fileNameCollisionResolver = fileNameCollisionResolver;
        this.thesaurus = thesaurus;
    }

    public static FileImportImpl create(FileSystem fileSystem, DataModel dataModel, FileNameCollisionResolver fileNameCollisionResolver, Thesaurus thesaurus, ImportSchedule importSchedule, File file) {
        return new FileImportImpl(fileSystem, dataModel, fileNameCollisionResolver, thesaurus).init(importSchedule, file);
    }

    @Override
    public void prepareProcessing() {
        if (!State.NEW.equals(getState())) {
            throw new IllegalStateException();
        }
        this.state = State.PROCESSING;
        moveFile();
        save();
    }

    private FileImportImpl init(ImportSchedule importSchedule, File file) {
        this.file = file;
        this.importSchedule = importSchedule;
        this.importScheduleId = importSchedule.getId();
        this.state = State.NEW;
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

    ImportSchedule getImportSchedule() {
        if (importSchedule == null) {
            importSchedule = importScheduleFactory().getExisting(importScheduleId);
        }
        return importSchedule;
    }

    private DataMapper<ImportSchedule> importScheduleFactory() {
        return dataModel.mapper(ImportSchedule.class);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void markFailure() {
        validateState();
        state = State.FAILURE;
        ensureStreamClosed();
        moveFile();
        save();
    }

    @Override
    public void markSuccess() {
        validateState();
        state = State.SUCCESS;
        ensureStreamClosed();
        moveFile();
        save();
    }

    @Override
    public String getFileName() {
        return file.toPath().getFileName().toString();
    }

    void save() {
        if (id == 0) {
            fileImportFactory().persist(this);
        } else {
            fileImportFactory().update(this);
        }
    }

    private DataMapper<FileImport> fileImportFactory() {
        return dataModel.mapper(FileImport.class);
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
        switch (state) {
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

    private void validateState() {
        if (!State.PROCESSING.equals(state)) {
            throw new IllegalStateException();
        }
    }
}
