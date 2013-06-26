package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileIOException;
import com.elster.jupiter.fileimport.FileImport;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.State;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileImportImpl implements FileImport {
    private long id;
    private ImportSchedule importSchedule;
    private long importScheduleId;
    private File file;
    private State state;
    private transient InputStream inputStream;

    public static FileImport create(ImportSchedule importSchedule, File file) {
        FileImportImpl fileImport = new FileImportImpl(importSchedule, file);
        fileImport.moveFile();
        return fileImport;
    }

    private FileImportImpl(ImportSchedule importSchedule, File file) {
        this.file = file;
        this.importSchedule = importSchedule;
        this.importScheduleId = importSchedule.getId();
        this.state = State.PROCESSING;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public InputStream getFile() {
        try {
            if (inputStream == null) {
                inputStream = new FileInputStream(file);
            }
            return inputStream;
        } catch (FileNotFoundException e) {
            throw new FileIOException(e);
        }
    }

    @Override
    public ImportSchedule getImportSchedule() {
        if (importSchedule == null) {
            importSchedule = Bus.getOrmClient().getImportScheduleFactory().getExisting(importScheduleId);
        }
        return importSchedule;
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
    }

    private void moveFile() {
        try {
            Path path = file.toPath();
            Path target = targetPath(path);
            Files.move(path, target);
            file = target.toFile();
        } catch (IOException e) {
            throw new FileIOException(e);
        }
    }

    private Path targetPath(Path path) {
        return Bus.getFileNameCollisionResolver().resolve(getTargetDirectory().resolve(path.getFileName()));
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
            throw new FileIOException(e);
        }
    }

    @Override
    public void markSuccess() {
        validateState();
        state = State.SUCCESS;
        ensureStreamClosed();
        moveFile();
    }

    private void validateState() {
        if (!State.PROCESSING.equals(state)) {
            throw new IllegalStateException();
        }
    }
}
