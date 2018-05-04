package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.configuration.RawConfiguration;
import com.atos.worldline.jss.configuration.RawConfigurationConverter;

import java.io.InputStream;

public class HsmConfigLoader {

    public RawConfiguration load(InputStream is){
        return new RawConfigurationConverter().loadFromInputStream(is);
    }

}
