/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.impl.config;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;

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
        expectedException.expectMessage("Wrong label configuration format, label configuration value:" + "");
        new HsmLabelConfiguration("");
    }



    @Test
    public void testAllOkConfiguration() throws HsmBaseException {
        String fileLabel = "PUB_KEK";
        String importSessionCapability = "SM_KEK_AUTHENTIC";
        String deviceKeyLegnth = "32";
        String renewSessionCapability = "SM_KEK_AUTHENTIC";
        String importLabel = "S-DB";
        String value = fileLabel + ", " + importLabel + ", " + importSessionCapability + ", " + deviceKeyLegnth + ", " + renewSessionCapability;
        HsmLabelConfiguration hsmLabelConfiguration = new HsmLabelConfiguration(value);
        Assert.assertEquals(fileLabel, hsmLabelConfiguration.getImportFileLabel());
    }

}
