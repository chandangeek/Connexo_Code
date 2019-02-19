/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeDomainExtension;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionStatusChangeDomainExtensionTest {

    private String id = "1";
    private String categoryCode = "2";
    private String reasonCode = "01";
    private String url = "http://localhost:8080/test";
    private Instant nowDate = Instant.now();

    private CustomPropertySetValues cpsValues;
    private ConnectionStatusChangeDomainExtension domainExtension;

    @Mock
    private ServiceCall serviceCall;

    @Before
    public void setup() {
        cpsValues = CustomPropertySetValues.empty();
        domainExtension = new ConnectionStatusChangeDomainExtension();
    }

    @Test
    public void testCopyFrom() {
        cpsValues.setProperty(ConnectionStatusChangeDomainExtension.FieldNames.ID.javaName(), id);
        cpsValues.setProperty(ConnectionStatusChangeDomainExtension.FieldNames.CATEGORY_CODE.javaName(), categoryCode);
        cpsValues.setProperty(ConnectionStatusChangeDomainExtension.FieldNames.REASON_CODE.javaName(), reasonCode);
        cpsValues.setProperty(ConnectionStatusChangeDomainExtension.FieldNames.CONFIRMATION_URL.javaName(), url);
        cpsValues.setProperty(ConnectionStatusChangeDomainExtension.FieldNames.PROCESS_DATE.javaName(), nowDate);

        domainExtension.copyFrom(serviceCall, cpsValues);

        assertThat(domainExtension.getId()).isSameAs(id);
        assertThat(domainExtension.getCategoryCode()).isSameAs(categoryCode);
        assertThat(domainExtension.getReasonCode()).isSameAs(reasonCode);
        assertThat(domainExtension.getConfirmationURL()).isSameAs(url);
        assertThat(domainExtension.getProcessDate()).isSameAs(nowDate);
    }

    @Test
    public void testCopyTo() {
        domainExtension.setId(id);
        domainExtension.setCategoryCode(categoryCode);
        domainExtension.setReasonCode(reasonCode);
        domainExtension.setConfirmationURL(url);
        domainExtension.setProcessDate(nowDate);

        domainExtension.copyTo(cpsValues);

        assertThat(cpsValues.getProperty(ConnectionStatusChangeDomainExtension.FieldNames.ID.javaName()))
                .isSameAs(id);
        assertThat(cpsValues.getProperty(ConnectionStatusChangeDomainExtension.FieldNames.CATEGORY_CODE.javaName()))
                .isSameAs(categoryCode);
        assertThat(cpsValues.getProperty(ConnectionStatusChangeDomainExtension.FieldNames.REASON_CODE.javaName()))
                .isSameAs(reasonCode);
        assertThat(cpsValues.getProperty(ConnectionStatusChangeDomainExtension.FieldNames.CONFIRMATION_URL.javaName()))
                .isSameAs(url);
        assertThat(cpsValues.getProperty(ConnectionStatusChangeDomainExtension.FieldNames.PROCESS_DATE.javaName()))
                .isSameAs(nowDate);
    }

    @Test
    public void testValidateDelete() {
        domainExtension.validateDelete();
    }
}