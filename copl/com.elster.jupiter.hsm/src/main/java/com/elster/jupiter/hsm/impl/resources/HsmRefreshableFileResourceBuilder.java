package com.elster.jupiter.hsm.impl.resources;

import com.elster.jupiter.hsm.model.HsmBaseException;

import java.io.File;
import java.util.Objects;

public class HsmRefreshableFileResourceBuilder implements HsmRefreshableResourceBuilder<File> {

    private final File file;

    public HsmRefreshableFileResourceBuilder(File file) throws HsmBaseException {
        if (Objects.isNull(file) || !file.exists()) {
            throw new HsmBaseException("Cowardly refusing to create config resource loader based on null or non-existing file (" + file + ")");
        }
        this.file = file;
    }

    @Override
    public File build() throws HsmBaseException {
        return file;
    }

    @Override
    public Long timeStamp() {
        return file.lastModified();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HsmRefreshableFileResourceBuilder)) {
            return false;
        }

        HsmRefreshableFileResourceBuilder that = (HsmRefreshableFileResourceBuilder) o;

        return file.equals(that.file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}
