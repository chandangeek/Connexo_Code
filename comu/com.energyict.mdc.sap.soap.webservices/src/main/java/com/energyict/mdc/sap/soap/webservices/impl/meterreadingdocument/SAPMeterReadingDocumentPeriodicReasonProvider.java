/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentCollectionData;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentReason;
import com.energyict.mdc.sap.soap.webservices.impl.AdditionalProperties;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Singleton
@Component(name = "com.energyict.mdc.sap.meterreadingdocument.periodicreason.provider",
        service = SAPMeterReadingDocumentReason.class, immediate = true, property = "name=ReadingReasonCode1")
public class SAPMeterReadingDocumentPeriodicReasonProvider implements SAPMeterReadingDocumentReason {
    private static final String REASON_CODES_PERIODIC = "com.elster.jupiter.sap.reasoncodes.periodic";
    private static final String REASON_CODES_PERIODIC_DEFAULT_VALUE = "1";

    private static List<String> codes;

    @Activate
    public void activate(BundleContext bundleContext) {
        String valueCodes = bundleContext.getProperty(REASON_CODES_PERIODIC);
        if (Checks.is(valueCodes).emptyOrOnlyWhiteSpace()) {
            codes = Collections.singletonList(REASON_CODES_PERIODIC_DEFAULT_VALUE);
        }else{
            codes = Arrays.asList((valueCodes.split(",")));
        }
    }

    @Override
    public List<String> getCodes() {
        return codes;
    }

    @Override
    public boolean hasCollectionInterval() {
        return true;
    }

    @Override
    public long getShiftDate() {
        return WebServiceActivator.SAP_PROPERTIES.get(AdditionalProperties.SCHEDULED_METER_READING_DATE_SHIFT_PERIODIC)*INTERVAL_ONE_DAY;
    }

    @Override
    public boolean isUseCurrentDateTime() {
        return false;
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