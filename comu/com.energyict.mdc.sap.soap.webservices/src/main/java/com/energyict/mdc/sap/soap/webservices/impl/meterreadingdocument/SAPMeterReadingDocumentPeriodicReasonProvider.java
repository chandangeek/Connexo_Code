/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.collections.ImmutableDiffList;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentCollectionData;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentReason;
import com.energyict.mdc.sap.soap.webservices.impl.AdditionalProperties;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Singleton
@Component(name = "com.energyict.mdc.sap.meterreadingdocument.periodicreason.provider",
        service = SAPMeterReadingDocumentReason.class, immediate = true, property = "name=ReadingReasonCode1")
public class SAPMeterReadingDocumentPeriodicReasonProvider implements SAPMeterReadingDocumentReason {
    private static final String REASON_CODES_PERIODIC = "com.elster.jupiter.sap.reasoncodes.periodic";
    private static final String REASON_CODES_PERIODIC_DEFAULT_VALUE = "1";
    private static final String MRO_DATASOURCE_INTERVAL = "com.elster.jupiter.sap.mro.datasource.interval";
    private static final List<String> DATA_SOURCE_TYPE_CODES = ImmutableList.of("0");

    private static List<String> reasonCodeCodes;
    private static Pair<String, String> dataSourceInterval;
    private volatile WebServiceActivator webServiceActivator;

    @Activate
    public void activate(BundleContext bundleContext) {
        String valueCodes = bundleContext.getProperty(REASON_CODES_PERIODIC);
        reasonCodeCodes = Checks.is(valueCodes).emptyOrOnlyWhiteSpace() ?
                Collections.singletonList(REASON_CODES_PERIODIC_DEFAULT_VALUE) :
                Arrays.asList(valueCodes.split(","));

        String valueDataSourceInterval = bundleContext.getProperty(MRO_DATASOURCE_INTERVAL);
        if (Checks.is(valueDataSourceInterval).emptyOrOnlyWhiteSpace()) {
            dataSourceInterval = Pair.of("0", "0");
        } else {
            String[] intervals = valueDataSourceInterval.split(",");
            dataSourceInterval = Pair.of(intervals[0].trim(), intervals[1].trim());
        }
    }

    @Reference
    public final void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }

    @Override
    public List<String> getReasonCodeCodes() {
        return reasonCodeCodes;
    }

    @Override
    public List<String> getDataSourceTypeCodeCodes() {
        return DATA_SOURCE_TYPE_CODES;
    }

    @Override
    public boolean hasCollectionInterval() {
        return true;
    }

    @Override
    public long getShiftDate() {
        return webServiceActivator.getSapProperty(AdditionalProperties.SCHEDULED_METER_READING_DATE_SHIFT_PERIODIC) * SECONDS_IN_DAY;
    }

    @Override
    public Optional<Pair<String, String>> getExtraDataSourceMacroAndMeasuringCodes() {
        return Optional.of(dataSourceInterval);
    }

    @Override
    public boolean shouldUseCurrentDateTime() {
        return false;
    }

    @Override
    public void process(SAPMeterReadingDocumentCollectionData collectionData) {
        collectionData.calculate();
    }

    @Override
    public boolean validateComTaskExecutionIfNeeded(Device device, boolean isRegular, ReadingType readingType) {
        return true;
    }
}