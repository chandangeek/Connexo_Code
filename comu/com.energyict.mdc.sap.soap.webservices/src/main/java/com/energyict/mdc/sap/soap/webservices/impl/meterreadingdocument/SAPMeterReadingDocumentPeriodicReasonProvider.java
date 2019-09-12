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
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.LIST_OF_ROLE_CODES;

@Singleton
@Component(name = "com.energyict.mdc.sap.meterreadingdocument.periodicreason.provider",
        service = SAPMeterReadingDocumentReason.class, immediate = true, property = "name=ReadingReasonCode1")
public class SAPMeterReadingDocumentPeriodicReasonProvider implements SAPMeterReadingDocumentReason {
    private static final String REASON_CODES_PERIODIC = "com.elster.jupiter.sap.reasoncodes.periodic";
    private static final String REASON_CODES_PERIODIC_DEFAULT_VALUE = "1";
    private static final long INTERVAL_ONE_DAY = 86400; //24 hours

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
    public long gedAdditionalTime() {
        return INTERVAL_ONE_DAY;
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