package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.config.HsmConfiguration;

import java.io.File;
import java.util.Objects;

public class HsmConfigRefreshableResourceLoader implements HsmRefreshableResourceLoader<HsmConfiguration> {

    private final File file;

    public HsmConfigRefreshableResourceLoader(File file) throws HsmBaseException {
        if (Objects.isNull(file) || !file.exists()) {
            throw new HsmBaseException("Cowardly refusing to create config resource loader based on null or non-existing file (" + file + ")");
        }
        this.file = file;
    }

    @Override
    public HsmConfiguration load() throws HsmBaseException {
        return new HsmConfigurationPropFileImpl(file);
    }

    @Override
    public Long timeStamp() {
        return file.lastModified();
    }
}
