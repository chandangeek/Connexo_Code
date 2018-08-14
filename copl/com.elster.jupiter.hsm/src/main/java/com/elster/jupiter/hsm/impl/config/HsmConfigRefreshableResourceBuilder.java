package com.elster.jupiter.hsm.impl.config;

import com.elster.jupiter.hsm.impl.resources.HsmRefreshableResourceBuilder;
import com.elster.jupiter.hsm.model.HsmBaseException;

import java.io.File;
import java.util.Objects;

public class HsmConfigRefreshableResourceBuilder implements HsmRefreshableResourceBuilder<HsmConfiguration> {

    private final File file;

    public HsmConfigRefreshableResourceBuilder(File file) throws HsmBaseException {
        if (Objects.isNull(file) || !file.exists()) {
            throw new HsmBaseException("Cowardly refusing to create config resource loader based on null or non-existing file (" + file + ")");
        }
        this.file = file;
    }

    @Override
    public HsmConfiguration build() throws HsmBaseException {
        return new HsmConfigurationPropFileImpl(file);
    }

    @Override
    public Long timeStamp() {
        return file.lastModified();
    }
}
