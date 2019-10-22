package com.elster.jupiter.hsm.impl.resources;

import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.impl.config.HsmConfigurationPropFileImpl;
import com.elster.jupiter.hsm.model.HsmBaseException;

import java.io.File;

public class HsmReloadableConfigResource extends AbstractFileResource<HsmConfiguration> {

    private static HsmReloadableConfigResource INSTANCE;

    private HsmReloadableConfigResource(File file) throws HsmBaseException {
        super(file);
    }

    public static HsmReloadableConfigResource getInstance(File file) throws HsmBaseException {
        if (INSTANCE == null) {
            INSTANCE = new HsmReloadableConfigResource(file);
        }
         INSTANCE.setFile(file);
        return INSTANCE;
    }

    @Override
    public HsmConfiguration load() throws HsmBaseException {
        return new HsmConfigurationPropFileImpl(super.getFile());
    }

    @Override
    public HsmConfiguration reload() throws HsmBaseException {
        return load();
    }


}
