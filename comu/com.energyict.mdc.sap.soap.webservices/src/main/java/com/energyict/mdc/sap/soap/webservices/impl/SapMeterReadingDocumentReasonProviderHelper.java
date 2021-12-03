/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentReason;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
@Component(name = "com.energyict.mdc.sap.meterreadingdocument.provider.helper",
        service = SapMeterReadingDocumentReasonProviderHelper.class, immediate = true)
public class SapMeterReadingDocumentReasonProviderHelper {

    //Check external system
    public static final String EXTERNAL_SYSTEM = "com.elster.jupiter.sap.externalsystem";
    public static final String EXTERNAL_SYSTEM_FEWA = "FEWA";
    public static final String EXTERNAL_SYSTEM_EDA = "EDA";

    private static String externalSystemName;
    private static MessageSeeds errorMessage;

    @Activate
    public void activate(BundleContext bundleContext) {
        externalSystemName = Optional.ofNullable(bundleContext.getProperty(EXTERNAL_SYSTEM)).orElse(EXTERNAL_SYSTEM_FEWA);
    }

    public static Optional<SAPMeterReadingDocumentReason> findReadingReasonProvider(String readingReasonCode, String dataSourceTypeCode) {
        switch (externalSystemName) {
            case EXTERNAL_SYSTEM_EDA:
                return (dataSourceTypeCode != null) ? findReadingReasonProviderByDataSourceCode(readingReasonCode, dataSourceTypeCode) : dataSourceTypeCodeIsRequired();
            case EXTERNAL_SYSTEM_FEWA:
                return findReadingReasonProviderByReasonCode(readingReasonCode);
            default:
                return Optional.ofNullable(null);
        }
    }

    private static Optional<SAPMeterReadingDocumentReason> findReadingReasonProviderByDataSourceCode(String readingReasonCode, String dataSourceTypeCode) {
        Optional<SAPMeterReadingDocumentReason> readingDocumentReason = WebServiceActivator.METER_READING_REASONS
                .stream()
                .filter(readingReason -> readingReason.getDataSourceTypeCodeCodes().contains(dataSourceTypeCode))
                .findFirst();

        if (!readingDocumentReason.isPresent()) {
            errorMessage = MessageSeeds.UNSUPPORTED_DATA_SOURCE_TYPE_CODE;
        } else {
            if (!readingDocumentReason.get().getReasonCodeCodes().contains(readingReasonCode)) {
                errorMessage = MessageSeeds.REASON_CODE_DATA_CONTRADICTS_SOURCE_TYPE_CODE;
                readingDocumentReason = Optional.ofNullable(null);
            }
        }
        return readingDocumentReason;
    }


    private static Optional<SAPMeterReadingDocumentReason> findReadingReasonProviderByReasonCode(String readingReasonCode) {
        Optional<SAPMeterReadingDocumentReason> readingDocumentReason = WebServiceActivator.METER_READING_REASONS
                .stream()
                .filter(readingReason -> readingReason.getReasonCodeCodes().contains(readingReasonCode))
                .findFirst();

        if (!readingDocumentReason.isPresent()) {
            errorMessage = MessageSeeds.UNSUPPORTED_REASON_CODE;
        }
        return readingDocumentReason;
    }

    private static Optional<SAPMeterReadingDocumentReason> dataSourceTypeCodeIsRequired() {
        errorMessage = MessageSeeds.NO_REQUIRED_DATA_SOURCE_TYPE_CODE;
        return Optional.ofNullable(null);
    }


    public static MessageSeeds getErrorMessage() {
        return errorMessage;
    }
}
