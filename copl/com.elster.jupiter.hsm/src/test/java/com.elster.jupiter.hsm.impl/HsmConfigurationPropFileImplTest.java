/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.configuration.HsmLabelConfiguration;
import com.elster.jupiter.hsm.model.keys.KeyType;

import org.hamcrest.core.IsInstanceOf;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HsmConfigurationPropFileImplTest {

    private final static String CONFIG_FILE = "hsm-bundle-configuration.properties";
    private String testFilePath;

    @Mock
    private Properties properties;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private HsmConfigurationPropFileImpl hsmConfigurationPropFile;

    @Before
    public void setUp() {
        URL resource = this.getClass().getClassLoader().getResource(CONFIG_FILE);
        this.testFilePath = resource.getFile();
    }

    @Test
    public void testEmtpyFilePath() throws HsmBaseException {
        expectedException.expect(HsmBaseException.class);
        expectedException.expectCause(IsInstanceOf.instanceOf(FileNotFoundException.class));
        hsmConfigurationPropFile = new HsmConfigurationPropFileImpl("");
    }

    @Test
    public void testJssInitFileIsReturned() throws HsmBaseException {
        hsmConfigurationPropFile = new HsmConfigurationPropFileImpl(testFilePath);

        assertEquals(CONFIG_FILE, hsmConfigurationPropFile.getJssInitFile());
    }

    @Test
    public void testNullJssInitFileIsReturned() throws HsmBaseException {
        when(properties.get(HsmConfigurationPropFileImpl.HSM_CONFIG_JSS_INIT_FILE)).thenReturn(null);
        expectedException.expect(HsmBaseException.class);
        expectedException.expectMessage("Wrong HSM configuration, cause: JSS init file not set");
        hsmConfigurationPropFile = new HsmConfigurationPropFileImpl(properties);
        hsmConfigurationPropFile.getJssInitFile();
    }

    @Test
    public void testEmptyStringJssInitFileIsReturned() throws HsmBaseException {
        when(properties.get(HsmConfigurationPropFileImpl.HSM_CONFIG_JSS_INIT_FILE)).thenReturn("");
        expectedException.expect(HsmBaseException.class);
        expectedException.expectMessage("Wrong HSM configuration, cause: JSS init file not set");
        hsmConfigurationPropFile = new HsmConfigurationPropFileImpl(properties);
        hsmConfigurationPropFile.getJssInitFile();
    }

    @Test
    public void testMapReturnsSameLabeLIfNoMappingExists() throws HsmBaseException {
        hsmConfigurationPropFile = new HsmConfigurationPropFileImpl(testFilePath);
        String label = "Label";
        assertEquals(label, hsmConfigurationPropFile.map(label));
    }

    @Test
    public void testNotMappedLabel() throws HsmBaseException {
        hsmConfigurationPropFile = new HsmConfigurationPropFileImpl(testFilePath);
        expectedException.expect(HsmBaseException.class);
        String label = "NOT_CONFIGURED_LABEL";
        expectedException.expectMessage("Asking configuration for a label that is missing, label:" + label);
        hsmConfigurationPropFile.get(label);

    }

    @Test
    public void testMapReturnConfiguredLabel() throws HsmBaseException {
        hsmConfigurationPropFile = new HsmConfigurationPropFileImpl(testFilePath);
        String label = "S_DB";
        HsmLabelConfiguration hsmLabelConfiguration = hsmConfigurationPropFile.get(label);
        assertEquals("PUB_KEK", hsmLabelConfiguration.getFileImportLabel());
        assertEquals(KeyType.SM_KEK_AUTHENTIC, hsmLabelConfiguration.getImportKeyType());
        assertEquals(KeyType.SM_KEK_RENEWAL, hsmLabelConfiguration.getRenewKeyType());
        assertEquals(label, hsmLabelConfiguration.getImportReEncryptHsmLabel());
    }
}
