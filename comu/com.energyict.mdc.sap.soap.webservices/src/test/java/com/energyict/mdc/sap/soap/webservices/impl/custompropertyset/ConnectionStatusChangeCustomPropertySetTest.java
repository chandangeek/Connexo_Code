/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangePersistenceSupport;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class ConnectionStatusChangeCustomPropertySetTest extends AbstractCustomPropertySetTest {

    private ConnectionStatusChangeCustomPropertySet cps;

    @Override
    @Before
    public void setup() {
        super.setup();
        cps = new ConnectionStatusChangeCustomPropertySet();
        cps.setNlsService(nlsService);
    }

    @Test
    public void testGetName() {
        when(thesaurus.getFormat(TranslationKeys.CONNECTION_STATUS_CHANGE_CPS)).thenReturn(nlsMessageFormat);

        assertThat(cps.getName()).isEqualTo(EXPECTED_TRANSLATED_VALUE);
    }

    @Test
    public void testGetDomainClassDisplayName() {
        when(thesaurus.getFormat(TranslationKeys.DOMAIN_NAME)).thenReturn(nlsMessageFormat);

        assertThat(cps.getDomainClassDisplayName()).isEqualTo(EXPECTED_TRANSLATED_VALUE);
    }

    @Test
    public void testGetDomainClass() {
        assertThat(cps.getDomainClass()).isEqualTo(ServiceCall.class);
    }

    @Test
    public void testGetPersistenceSupport() {
        assertThat(cps.getPersistenceSupport())
                .isInstanceOf(ConnectionStatusChangePersistenceSupport.class);
    }

    @Test
    public void testIsVersioned() {
        assertThat(cps.isVersioned()).isFalse();
    }

    @Test
    public void testDefaultViewPrivileges() {
        assertThat(cps.defaultViewPrivileges()).hasSize(0);
    }

    @Test
    public void testDefaultEditPrivileges() {
        assertThat(cps.defaultEditPrivileges()).hasSize(0);
    }

    @Test
    public void testGetPropertySpecs() {
        PropertySpecService propertySpecService = mock(PropertySpecService.class,
                withSettings().defaultAnswer(Mockito.RETURNS_DEEP_STUBS));
        cps.setPropertySpecService(propertySpecService);

        assertThat(cps.getPropertySpecs()).hasSize(5);
    }
}