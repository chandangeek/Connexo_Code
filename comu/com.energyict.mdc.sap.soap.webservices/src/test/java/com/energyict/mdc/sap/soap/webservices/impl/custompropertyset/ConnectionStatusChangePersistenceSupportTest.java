/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.app.MdcAppService;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangePersistenceSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionStatusChangePersistenceSupportTest {

    private ConnectionStatusChangePersistenceSupport persistenceSupport;

    @Before
    public void setup() {
        persistenceSupport = new ConnectionStatusChangePersistenceSupport();
    }

    @Test
    public void testComponentName() {
        assertThat(persistenceSupport.componentName()).isEqualTo(ConnectionStatusChangePersistenceSupport.COMPONENT_NAME);
    }

    @Test
    public void testTableName() {
        assertThat(persistenceSupport.tableName()).startsWith(ConnectionStatusChangePersistenceSupport.TABLE_NAME);
    }

    @Test
    public void testDomainFieldName() {
        assertThat(persistenceSupport.domainFieldName())
                .isEqualTo(ConnectionStatusChangeDomainExtension.FieldNames.DOMAIN.javaName());
    }

    @Test
    public void testPersistenceClass() {
        assertThat(persistenceSupport.persistenceClass()).isSameAs(ConnectionStatusChangeDomainExtension.class);
    }

    @Test
    public void testModule() {
        assertThat(persistenceSupport.module()).isEmpty();
    }

    @Test
    public void testAddCustomPropertyColumnsTo() {
        @SuppressWarnings("rawtypes")
        Table table = mock(Table.class, withSettings().defaultAnswer(Mockito.RETURNS_DEEP_STUBS));
        persistenceSupport.addCustomPropertyColumnsTo(table, null);

        verify(table, times(5)).column(anyString());
    }

    @Test
    public void testApplication() {
        assertThat(persistenceSupport.application()).isEqualTo(MdcAppService.APPLICATION_NAME);
    }

    @Test
    public void testColumnNameFor() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(ConnectionStatusChangeDomainExtension.FieldNames.ID.javaName());

        assertThat(persistenceSupport.columnNameFor(propertySpec))
                .isEqualTo(ConnectionStatusChangeDomainExtension.FieldNames.ID.databaseName());
    }
}