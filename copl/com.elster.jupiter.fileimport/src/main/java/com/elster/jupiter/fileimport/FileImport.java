package com.elster.jupiter.fileimport;

import java.io.InputStream;

public interface FileImport {

    ImportSchedule getImportSchedule();

    InputStream getFile();

    State getState();

    void markSuccess();

    void markFailure();

    long getId();
}
