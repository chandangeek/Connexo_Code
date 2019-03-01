/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.model.config;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.config.HsmLabelConfiguration;

import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HsmLabelConfigurationTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testEmptyStringConfiguration() throws HsmBaseException {
        expectedException.expect(HsmBaseException.class);
        expectedException.expectMessage("Wrong label configuration format, label configuration value:");
        new HsmLabelConfiguration("alabel","");
    }

    @Test
    public void testEmptyLabelName() throws HsmBaseException {
        expectedException.expect(HsmBaseException.class);
        expectedException.expectMessage("Wrong label configuration format, label configuration value:");
        new HsmLabelConfiguration("","alabel");
    }



    @Test
    public void testAllOkConfiguration() throws HsmBaseException {
        String label = "IMP-SM-KEK";
        String mapedLabel = "PUB_KEK";
        String chainingMode = "CBC";
        String padding = "EME_PKCS1_V1_5";
        String configuredStringLabel = mapedLabel + ", " + chainingMode + ", " + padding;
        HsmLabelConfiguration hsmLabelConfiguration = new HsmLabelConfiguration(label, configuredStringLabel);
        Assert.assertEquals(mapedLabel, hsmLabelConfiguration.getImportFileLabel());
        Assert.assertEquals(ChainingMode.valueOf(chainingMode), hsmLabelConfiguration.getChainingMode());
        Assert.assertEquals(PaddingAlgorithm.valueOf(padding), hsmLabelConfiguration.getPaddingAlgorithm());
    }

}
