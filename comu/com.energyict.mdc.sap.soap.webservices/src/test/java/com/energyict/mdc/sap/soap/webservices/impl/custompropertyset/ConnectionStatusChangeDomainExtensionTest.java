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
    private String requestId = "2";
    private String uuid = "7ccbe11f-25e1-4236-b1ea-dc3b923d1799";
    private String categoryCode = "2";
    private String reasonCode = "01";
    private String url = "http://localhost:8080/test";
    private Instant nowDate = Instant.now();
    private boolean bulk = true;
    private boolean cancelledBySap = true;

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
        cpsValues.setProperty(ConnectionStatusChangeDomainExtension.FieldNames.REQUEST_ID.javaName(), requestId);
        cpsValues.setProperty(ConnectionStatusChangeDomainExtension.FieldNames.UUID.javaName(), uuid);
        cpsValues.setProperty(ConnectionStatusChangeDomainExtension.FieldNames.CATEGORY_CODE.javaName(), categoryCode);
        cpsValues.setProperty(ConnectionStatusChangeDomainExtension.FieldNames.REASON_CODE.javaName(), reasonCode);
        cpsValues.setProperty(ConnectionStatusChangeDomainExtension.FieldNames.PROCESS_DATE.javaName(), nowDate);
        cpsValues.setProperty(ConnectionStatusChangeDomainExtension.FieldNames.BULK.javaName(), bulk);
        cpsValues.setProperty(ConnectionStatusChangeDomainExtension.FieldNames.CANCELLED_BY_SAP.javaName(), cancelledBySap);

        domainExtension.copyFrom(serviceCall, cpsValues);

        assertThat(domainExtension.getId()).isSameAs(id);
        assertThat(domainExtension.getRequestId()).isSameAs(requestId);
        assertThat(domainExtension.getUuid()).isSameAs(uuid);
        assertThat(domainExtension.getCategoryCode()).isSameAs(categoryCode);
        assertThat(domainExtension.getReasonCode()).isSameAs(reasonCode);
        assertThat(domainExtension.getProcessDate()).isSameAs(nowDate);
        assertThat(domainExtension.isBulk()).isSameAs(bulk);
        assertThat(domainExtension.isCancelledBySap()).isSameAs(cancelledBySap);
    }

    @Test
    public void testCopyTo() {
        domainExtension.setId(id);
        domainExtension.setRequestId(requestId);
        domainExtension.setUuid(uuid);
        domainExtension.setCategoryCode(categoryCode);
        domainExtension.setReasonCode(reasonCode);
        domainExtension.setProcessDate(nowDate);
        domainExtension.setBulk(bulk);
        domainExtension.setCancelledBySap(cancelledBySap);

        domainExtension.copyTo(cpsValues);

        assertThat(cpsValues.getProperty(ConnectionStatusChangeDomainExtension.FieldNames.ID.javaName()))
                .isSameAs(id);
        assertThat(cpsValues.getProperty(ConnectionStatusChangeDomainExtension.FieldNames.REQUEST_ID.javaName()))
                .isSameAs(requestId);
        assertThat(cpsValues.getProperty(ConnectionStatusChangeDomainExtension.FieldNames.UUID.javaName()))
                .isSameAs(uuid);
        assertThat(cpsValues.getProperty(ConnectionStatusChangeDomainExtension.FieldNames.CATEGORY_CODE.javaName()))
                .isSameAs(categoryCode);
        assertThat(cpsValues.getProperty(ConnectionStatusChangeDomainExtension.FieldNames.REASON_CODE.javaName()))
                .isSameAs(reasonCode);
        assertThat(cpsValues.getProperty(ConnectionStatusChangeDomainExtension.FieldNames.PROCESS_DATE.javaName()))
                .isSameAs(nowDate);
        assertThat(cpsValues.getProperty(ConnectionStatusChangeDomainExtension.FieldNames.BULK.javaName()))
                .isSameAs(bulk);
        assertThat(cpsValues.getProperty(ConnectionStatusChangeDomainExtension.FieldNames.CANCELLED_BY_SAP.javaName()))
                .isSameAs(cancelledBySap);
    }

    @Test
    public void testValidateDelete() {
        domainExtension.validateDelete();
    }
}