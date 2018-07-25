/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.model.configuration;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;

import org.hamcrest.core.IsInstanceOf;

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
    public void testWrongFormatStringConfiguration() throws HsmBaseException {
        String value = "PUB_KEK;SM_AUTHENTIC";
        expectedException.expect(HsmBaseException.class);
        expectedException.expectMessage("Wrong label configuration format, label configuration value:" + value);
        new HsmLabelConfiguration(value);
    }

    @Test
    public void testWrongImportKeyTypeConfiguration() throws HsmBaseException {
        String value = "PUB_KEK, WRONG_KEY_TYPE,32, SM_KEK_AUTHENTIC, S_DB";
        expectedException.expect(HsmBaseException.class);
        expectedException.expectCause(IsInstanceOf.instanceOf(IllegalArgumentException.class));
        new HsmLabelConfiguration(value);
    }

    @Test
    public void testNoImportCapabilityConfiguration() throws HsmBaseException {
        String value = "PUB_KEK, ,, SM_KEK_AUTHENTIC, S_DB";
        expectedException.expect(HsmBaseException.class);
        expectedException.expectMessage("Asking for missing import capability");
        HsmLabelConfiguration hsmLabelConfiguration = new HsmLabelConfiguration(value);
        hsmLabelConfiguration.getImportSessionKeyCapability();
    }

    @Test
    public void testReEncryptLabelConfiguration() throws HsmBaseException {
        String value = "PUB_KEK, SM_KEK_AUTHENTIC, 32, SM_KEK_AUTHENTIC,";
        expectedException.expect(HsmBaseException.class);
        expectedException.expectMessage("Asking for re-encrypt label but not configured");
        HsmLabelConfiguration hsmLabelConfiguration = new HsmLabelConfiguration(value);
        hsmLabelConfiguration.getImportReEncryptHsmLabel();
    }

    @Test
    public void testAllOkConfiguration() throws HsmBaseException {
        String importLabel = "PUB_KEK";
        String importKeyType = "SM_KEK_AUTHENTIC";
        String deviceKeyLegnth = "32";
        String renewKeyType = "SM_KEK_AUTHENTIC";
        String reEncryptLabel = "S-DB";
        String value = importLabel + ", " + importKeyType + ", " + deviceKeyLegnth + ", " + renewKeyType + ", " + reEncryptLabel;
        HsmLabelConfiguration hsmLabelConfiguration = new HsmLabelConfiguration(value);
        Assert.assertEquals(importLabel, hsmLabelConfiguration.getFileImportLabel());
        Assert.assertEquals(SessionKeyCapability.valueOf(importKeyType), hsmLabelConfiguration.getImportSessionKeyCapability());
        Assert.assertTrue(Integer.parseInt(deviceKeyLegnth) == hsmLabelConfiguration.getKeyLength());
        Assert.assertEquals(SessionKeyCapability.valueOf(renewKeyType), hsmLabelConfiguration.getRenewSessionKeyCapability());
        Assert.assertEquals(reEncryptLabel, hsmLabelConfiguration.getImportReEncryptHsmLabel());


    }

}
