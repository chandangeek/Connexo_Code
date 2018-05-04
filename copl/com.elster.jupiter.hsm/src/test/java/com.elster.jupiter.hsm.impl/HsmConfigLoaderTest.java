package com.elster.jupiter.hsm.impl;

import com.atos.worldline.jss.configuration.RawConfiguration;

import java.io.InputStream;

import static org.junit.Assert.*;

public class HsmConfigLoaderTest {

    HsmConfigLoader hl;

    @org.junit.Before
    public void setUp() throws Exception {
        hl = new HsmConfigLoader();
    }

    @org.junit.Test
    public void load() {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("hsm-runtime-configuration.json");
        RawConfiguration cfg = hl.load(is);
        assertNotNull(cfg);
    }
}