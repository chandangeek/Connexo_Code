package com.elster.jupiter.hsm.impl.resources;

import com.elster.jupiter.hsm.model.HsmBaseException;

import java.io.File;
import java.util.Objects;

public abstract class AbstractFileResource<T> implements HsmReloadableResource<T> {

    private File file;
    private boolean fileChanged;

    protected AbstractFileResource(File file) throws HsmBaseException {
        if (Objects.isNull(file) || !file.exists()) {
            throw new HsmBaseException("Cowardly refusing to create config resource loader based on null or non-existing file (" + file + ")");
        }
        this.file = file;
    }


    protected void setFile(File file) throws HsmBaseException {
        if (Objects.isNull(file) || !file.exists()) {
            throw new HsmBaseException("Cowardly refusing to create config resource loader based on null or non-existing file (" + file + ")");
        }
        if (!this.file.equals(file)) {
            fileChanged = true;
        }

        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean changed() {
        if (fileChanged) {
            // we need to reset file changed while it seems it was acknowledged
            fileChanged = false;
            return true;
        }
        return false;
    }

    @Override
    public Long timeStamp() {
        return file.lastModified();
    }

    @Override
    public void close(){

    }
}
