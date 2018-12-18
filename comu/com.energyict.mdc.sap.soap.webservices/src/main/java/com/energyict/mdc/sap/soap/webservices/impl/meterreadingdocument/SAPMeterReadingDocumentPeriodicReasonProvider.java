/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentCollectionData;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentReason;

import org.osgi.service.component.annotations.Component;

import javax.inject.Singleton;
import java.time.Instant;

@Singleton
@Component(name = "com.energyict.mdc.sap.meterreadingdocument.periodicreason.provider",
        service = SAPMeterReadingDocumentReason.class, immediate = true, property = "name=ReadingReasonCode1")
public class SAPMeterReadingDocumentPeriodicReasonProvider implements SAPMeterReadingDocumentReason {

    private static final String READING_REASON_CODE = "1";

    @Override
    public String getCode() {
        return READING_REASON_CODE;
    }

    @Override
    public boolean hasCollectionInterval() {
        return true;
    }

    @Override
    public boolean isBulk() {
        return true;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public void process(SAPMeterReadingDocumentCollectionData collectionData) {
        collectionData.calculate();
    }
}