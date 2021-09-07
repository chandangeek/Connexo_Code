/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.impl.config;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.config.HsmLabelConfiguration;

import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import org.hamcrest.core.IsInstanceOf;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
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

    private final static String CONFIG_FILE = "hsm-test-bundle-configuration.properties";
    private String testFilePath;

    private final static String CONFIG_FILE_DEFAULT = "hsm-test-bundle-configuration-default.properties";
    private String testFilePathDefault;
    @Mock
    private Properties properties;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private HsmConfigurationPropFileImpl hsmConfigurationPropFile;

    @Before
    public void setUp() throws URISyntaxException  {
        URL resource = this.getClass().getClassLoader().getResource(CONFIG_FILE);
        this.testFilePath = new URI(resource.getFile()).getPath();

        URL resourceDefault = this.getClass().getClassLoader().getResource(CONFIG_FILE_DEFAULT);
        this.testFilePathDefault = resourceDefault.getFile();
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
    public void testMapLabelReturnsSameLabeLIfNoMappingExists() throws HsmBaseException {
        hsmConfigurationPropFile = new HsmConfigurationPropFileImpl(testFilePath);
        String label = "Label";
        assertEquals(label, hsmConfigurationPropFile.map(label));
    }

    @Test
    public void testMapConfiguredLabel() throws HsmBaseException {
        hsmConfigurationPropFile = new HsmConfigurationPropFileImpl(testFilePath);
        String label = "IMP-SM-KEK";
        HsmLabelConfiguration hsmLabelConfiguration = hsmConfigurationPropFile.get(label);
        assertEquals("Pub_KEK_SM", hsmLabelConfiguration.getImportFileLabel());
    }

    @Test
    public void testNotConfiguredLabel() throws HsmBaseException {
        System.out.println("loaded:" + this.testFilePath);
        hsmConfigurationPropFile = new HsmConfigurationPropFileImpl(testFilePath);
        String labelName = "NOT_CONFIGURED_LABEL";
        HsmLabelConfiguration label = hsmConfigurationPropFile.get(labelName);
        assertEquals(labelName, label.getName());
        assertEquals(ChainingMode.CBC, label.getChainingMode());
        assertEquals(PaddingAlgorithm.PKCS, label.getPaddingAlgorithm());
    }

    @Test
    public void testConfiguredLabel() throws HsmBaseException {
        hsmConfigurationPropFile = new HsmConfigurationPropFileImpl(testFilePath);
        String labelName = "IMP-SM-KEK";
        HsmLabelConfiguration label = hsmConfigurationPropFile.get(labelName);
        assertEquals(labelName, label.getName());
        assertEquals(ChainingMode.CBC, label.getChainingMode());
        assertEquals(PaddingAlgorithm.EME_PKCS1_V1_5, label.getPaddingAlgorithm());
    }

    @Test
    public void testGetAll() throws HsmBaseException {
        hsmConfigurationPropFile = new HsmConfigurationPropFileImpl(testFilePath);
        Collection<HsmLabelConfiguration> labels = hsmConfigurationPropFile.getLabels();
        assertEquals(1, labels.size());
        HsmLabelConfiguration label = new HsmLabelConfiguration("IMP-SM-KEK","Pub_KEK_SM, CBC, EME_PKCS1_V1_5");
        assertEquals(label, labels.iterator().next());
        assertEquals(ChainingMode.CBC, label.getChainingMode());
        assertEquals(PaddingAlgorithm.EME_PKCS1_V1_5, label.getPaddingAlgorithm());
    }

    @Test
    public void testTimeoutRetryCountDefault() throws HsmBaseException {
        hsmConfigurationPropFile = new HsmConfigurationPropFileImpl(testFilePathDefault);

        assertEquals(3, hsmConfigurationPropFile.getTimeoutRetryCount());
    }

    @Test
    public void testTimeoutRetryCount() throws HsmBaseException {
        hsmConfigurationPropFile = new HsmConfigurationPropFileImpl(testFilePath);

        assertEquals(2, hsmConfigurationPropFile.getTimeoutRetryCount());
    }

}
