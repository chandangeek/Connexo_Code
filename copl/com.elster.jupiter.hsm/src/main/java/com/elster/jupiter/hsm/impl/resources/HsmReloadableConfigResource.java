package com.elster.jupiter.hsm.impl.resources;

import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.impl.config.HsmConfigurationPropFileImpl;
import com.elster.jupiter.hsm.model.HsmBaseException;

import java.io.File;
import java.util.Objects;

public class HsmReloadableConfigResource implements HsmReloadableResource<HsmConfiguration> {

    private final File file;

    public HsmReloadableConfigResource(File file) throws HsmBaseException {
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
    public HsmConfiguration reload() throws HsmBaseException {
        return load();
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
        if (!(o instanceof HsmReloadableConfigResource)) {
            return false;
        }

        HsmReloadableConfigResource that = (HsmReloadableConfigResource) o;

        return file.equals(that.file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}
