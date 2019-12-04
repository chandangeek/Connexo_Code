/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.device.data.Device;
import org.junit.Before;
import org.junit.Test;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MaxDemandCustomPropertySetTest extends AbstractCustomPropertySetTest {

    private MaxDemandCustomPropertySet cps;
    private PersistenceSupport<Device, MaxDemandDomainExtension> persistenceSupport;

    @Override
    @Before
    public void setup() {
        super.setup();
        cps = new MaxDemandCustomPropertySet(propertySpecService, thesaurus);
        persistenceSupport = cps.getPersistenceSupport();
    }

    @Test
    public void testGetId() {
        assertThat(cps.getId()).isEqualTo(MaxDemandCustomPropertySet.CPS_ID);
    }

    @Test
    public void testGetName() {
        when(thesaurus.getFormat(TranslationKeys.CPS_DEVICE_MAX_DEMAND)).thenReturn(nlsMessageFormat);

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
                .isInstanceOf(MaxDemandCustomPropertySet.CustomPropertyPersistenceSupport.class);
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
        assertThat(persistenceSupport.componentName()).isEqualTo(MaxDemandCustomPropertySet.MODEL_NAME);
    }

    @Test
    public void testDomainFieldName() {
        assertThat(persistenceSupport.domainFieldName())
                .isEqualTo(MaxDemandDomainExtension.FieldNames.DOMAIN.javaName());
    }

    @Test
    public void testPersistenceClass() {
        assertThat(persistenceSupport.persistenceClass()).isSameAs(MaxDemandDomainExtension.class);
    }

    @Test
    public void testModule() {
        assertThat(persistenceSupport.module()).isEmpty();
    }

    @Test
    public void testApplication() {
        assertThat(persistenceSupport.application()).isEqualTo(APPLICATION_NAME);
    }

    @Test
    public void testColumnNameFor() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(MaxDemandDomainExtension.FieldNames.CONNECTED_LOAD.javaName());

        assertThat(persistenceSupport.columnNameFor(propertySpec))
                .isEqualTo(MaxDemandDomainExtension.FieldNames.CONNECTED_LOAD.javaName());
    }
}