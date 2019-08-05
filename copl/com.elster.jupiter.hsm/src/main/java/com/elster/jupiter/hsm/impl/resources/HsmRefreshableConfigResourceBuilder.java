package com.elster.jupiter.hsm.impl.resources;

import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.impl.config.HsmConfigurationPropFileImpl;
import com.elster.jupiter.hsm.model.HsmBaseException;

import java.io.File;
import java.util.Objects;

public class HsmRefreshableConfigResourceBuilder implements HsmRefreshableResourceBuilder<HsmConfiguration> {

    private final File file;

    public HsmRefreshableConfigResourceBuilder(File file) throws HsmBaseException {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HsmRefreshableConfigResourceBuilder)) {
            return false;
        }

        HsmRefreshableConfigResourceBuilder that = (HsmRefreshableConfigResourceBuilder) o;

        return file.equals(that.file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}
