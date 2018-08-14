/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.impl.config;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;

import org.hamcrest.core.IsInstanceOf;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;

import org.assertj.core.api.Assertions;
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

    private final static String CONFIG_FILE = "hsm-test-bundle-configuration.properties";
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

        assertEquals("hsm-runtime-configuration.json", hsmConfigurationPropFile.getJssInitFile());
    }

    @Test
    public void testNullJssInitFileIsReturned() throws HsmBaseException {
        when(properties.get(HsmConfiguration.HSM_CONFIG_JSS_INIT_FILE)).thenReturn(null);
        expectedException.expect(HsmBaseException.class);
        expectedException.expectMessage("Wrong HSM configuration, cause: JSS init file not set");
        hsmConfigurationPropFile = new HsmConfigurationPropFileImpl(properties);
        hsmConfigurationPropFile.getJssInitFile();
    }

    @Test
    public void testEmptyStringJssInitFileIsReturned() throws HsmBaseException {
        when(properties.get(HsmConfiguration.HSM_CONFIG_JSS_INIT_FILE)).thenReturn("");
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
    public void testConfiguredLabel() throws HsmBaseException {
        hsmConfigurationPropFile = new HsmConfigurationPropFileImpl(testFilePath);
        String label = "IMP-SM-KEK";
        HsmLabelConfiguration hsmLabelConfiguration = hsmConfigurationPropFile.get(label);
        assertEquals("Pub_KEK_SM", hsmLabelConfiguration.getImportFileLabel());
        assertEquals(SessionKeyCapability.SM_KEK_NONAUTHENTIC, hsmLabelConfiguration.getImportSessionCapability());
        assertEquals("S-DB", hsmLabelConfiguration.getImportLabel());
        assertEquals(new Integer(16), hsmLabelConfiguration.getDeviceKeyLength());
    }

    @Test
    public void testGetAll() throws HsmBaseException {
        hsmConfigurationPropFile = new HsmConfigurationPropFileImpl(testFilePath);
        Collection<HsmLabelConfiguration> labels = hsmConfigurationPropFile.getLabels();
        assertEquals(2, labels.size());
        HsmLabelConfiguration label1 = new HsmLabelConfiguration("Pub_KEK_SM", SessionKeyCapability.SM_KEK_NONAUTHENTIC,16, null,"S-DB");
        HsmLabelConfiguration label2 = new HsmLabelConfiguration(null, null,16, SessionKeyCapability.SM_KEK_RENEWAL,null);
        Assertions.assertThat(labels).contains(label1);
        Assertions.assertThat(labels).contains(label2);
    }
}
