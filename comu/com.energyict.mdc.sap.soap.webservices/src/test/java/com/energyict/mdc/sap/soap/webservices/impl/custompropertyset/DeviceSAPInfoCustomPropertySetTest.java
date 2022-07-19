/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class DeviceSAPInfoCustomPropertySetTest extends AbstractCustomPropertySetTest {

    private DeviceSAPInfoCustomPropertySet cps;
    private PersistenceSupport<Device, DeviceSAPInfoDomainExtension> persistenceSupport;

    @Mock
    protected SAPCustomPropertySets sapCustomPropertySets;

    @Override
    @Before
    public void setup() {
        super.setup();
        cps = new DeviceSAPInfoCustomPropertySet(propertySpecService, thesaurus, sapCustomPropertySets, null);
        persistenceSupport = cps.getPersistenceSupport();
    }

    @Test
    public void testGetId() {
        assertThat(cps.getId()).isEqualTo(DeviceSAPInfoCustomPropertySet.CPS_ID);
    }

    @Test
    public void testGetName() {
        when(thesaurus.getFormat(TranslationKeys.CPS_DEVICE_SAP_INFO)).thenReturn(nlsMessageFormat);

        assertThat(cps.getName()).isEqualTo(EXPECTED_TRANSLATED_VALUE);
    }

    @Test
    public void testGetDomainClassDisplayName() {
        when(thesaurus.getFormat(TranslationKeys.DOMAIN_NAME_DEVICE)).thenReturn(nlsMessageFormat);

        assertThat(cps.getDomainClassDisplayName()).isEqualTo(EXPECTED_TRANSLATED_VALUE);
    }

    @Test
    public void testGetDomainClass() {
        assertThat(cps.getDomainClass()).isEqualTo(Device.class);
    }

    @Test
    public void testGetPersistenceSupport() {
        assertThat(cps.getPersistenceSupport())
                .isInstanceOf(DeviceSAPInfoCustomPropertySet.CustomPropertyPersistenceSupport.class);
    }

    @Test
    public void testIsRequired() {
        assertThat(cps.isRequired()).isTrue();
    }

    @Test
    public void testIsVersioned() {
        assertThat(cps.isVersioned()).isFalse();
    }

    @Test
    public void testDefaultViewPrivileges() {
        assertThat(cps.defaultViewPrivileges()).hasSize(4);
    }

    @Test
    public void testDefaultEditPrivileges() {
        assertThat(cps.defaultEditPrivileges()).hasSize(4);
    }

    @Test
    public void testComponentName() {
        assertThat(persistenceSupport.componentName()).isEqualTo(DeviceSAPInfoCustomPropertySet.MODEL_NAME);
    }

    @Test
    public void testDomainFieldName() {
        assertThat(persistenceSupport.domainFieldName())
                .isEqualTo(DeviceSAPInfoDomainExtension.FieldNames.DOMAIN.javaName());
    }

    @Test
    public void testPersistenceClass() {
        assertThat(persistenceSupport.persistenceClass()).isSameAs(DeviceSAPInfoDomainExtension.class);
    }

    @Test
    public void testApplication() {
        assertThat(persistenceSupport.application()).isEqualTo(APPLICATION_NAME);
    }

    @Test
    public void testAddCustomPropertyColumnsTo() {
        @SuppressWarnings("rawtypes")
        Table table = mock(Table.class, withSettings().defaultAnswer(Mockito.RETURNS_DEEP_STUBS));
        persistenceSupport.addCustomPropertyColumnsTo(table, null);

        verify(table, times(14)).column(anyString());
    }

    @Test
    public void testColumnNameFor() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_IDENTIFIER.javaName());

        assertThat(persistenceSupport.columnNameFor(propertySpec))
                .isEqualTo(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_IDENTIFIER.javaName());
    }
}
