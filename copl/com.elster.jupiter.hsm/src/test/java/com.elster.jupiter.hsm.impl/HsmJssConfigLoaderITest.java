package com.elster.jupiter.hsm.impl;


import com.atos.worldline.jss.configuration.RawConfiguration;
import com.atos.worldline.jss.configuration.RawFunctionTimeout;
import com.atos.worldline.jss.configuration.RawHsm;
import com.atos.worldline.jss.configuration.RawLabel;
import com.atos.worldline.jss.configuration.RawLabelMapping;
import com.atos.worldline.jss.configuration.RawRoutingEngineRule;
import com.atos.worldline.jss.internal.runtime.HSMState;
import com.google.common.collect.ImmutableList;

import java.io.InputStream;

import static org.junit.Assert.*;

public class HsmJssConfigLoaderITest {

    HsmJssConfigLoader hl;

    @org.junit.Before
    public void setUp() {
        hl = new HsmJssConfigLoader();
    }

    @org.junit.Test
    public void load() {
        // bellow resource should be found in src/test/resources
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("hsm-junit-test.json");
        RawConfiguration cfg = hl.load(is);

        assertNotNull(cfg);

        // check HSM
        assertEquals(2, cfg.getRawHsms().size());
        RawHsm expectedRawHsm1 = RawHsm.builder().id(1).name("HSM1").type("ASM7.6a").description("").host("10.0.0.84").port(9000).moduleId(0).secure(false).state(HSMState.ENABLED).protocol("KS").build();
        assertEquals(expectedRawHsm1, cfg.getRawHsms().get(0));
        RawHsm expectedRawHsm2 = RawHsm.builder().id(2).name("HSM2").type("ASM7.6a").description("").host("10.0.0.85").port(9000).moduleId(0).secure(false).protocol("KS").build();
        assertEquals(expectedRawHsm2, cfg.getRawHsms().get(1));

        // check Labels
        assertEquals(1, cfg.getRawLabels().size());
        RawLabelMapping mapping = RawLabelMapping.builder().hsmId(1).keyAddress("DB KEY AUTH  000").enabled(true).build();
        RawLabel label1 = RawLabel.builder().id(1).name("LABEL1").kek(false).mappings(ImmutableList.of(mapping)).build();
        assertEquals(label1, cfg.getRawLabels().get(0));

        //assert rawFunctionTimeouts
        assertEquals(1, cfg.getRawFunctionTimeouts().size());
        RawFunctionTimeout f1 = RawFunctionTimeout.builder().functionName("default_function_timeout").timeoutMillis(500).build();
        assertEquals(f1, cfg.getRawFunctionTimeouts().get(0));

        // assert routingEngineRules
        assertEquals(1, cfg.getRawRoutingEngineRules().size());
        RawRoutingEngineRule r1 = RawRoutingEngineRule.builder().id(1).hsmId(1).functionName("f1").ruleExpression("rex1").build();
        assertEquals(r1, cfg.getRawRoutingEngineRules().get(0));
    }
}