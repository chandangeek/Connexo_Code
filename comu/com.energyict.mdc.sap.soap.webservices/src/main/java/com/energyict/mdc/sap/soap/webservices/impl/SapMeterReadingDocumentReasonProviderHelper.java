/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentReason;

import java.util.Optional;

public class SapMeterReadingDocumentReasonProviderHelper {

    private Thesaurus thesaurus;

    public SapMeterReadingDocumentReasonProviderHelper(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }


    public Optional<SAPMeterReadingDocumentReason> findReadingReasonProvider(String readingReasonCode, String dataSourceTypeCode) {
        switch (WebServiceActivator.getReadingsStrategySelector()) {
            case WebServiceActivator.DATA_SOURCE_TYPE_CODE_STRATEGY:
                return (dataSourceTypeCode != null) ? findReadingReasonProviderByDataSourceCode(readingReasonCode, dataSourceTypeCode) : dataSourceTypeCodeIsRequired();
            case WebServiceActivator.REASON_CODE_STRATEGY:
                return findReadingReasonProviderByReasonCode(readingReasonCode);
            default:
                return Optional.ofNullable(null);
        }
    }

    private Optional<SAPMeterReadingDocumentReason> findReadingReasonProviderByDataSourceCode(String readingReasonCode, String dataSourceTypeCode) {
        Optional<SAPMeterReadingDocumentReason> readingDocumentReason = WebServiceActivator.METER_READING_REASONS
                .stream()
                .filter(readingReason -> readingReason.getDataSourceTypeCodeCodes().contains(dataSourceTypeCode))
                .findFirst();

        if (!readingDocumentReason.isPresent()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.UNSUPPORTED_DATA_SOURCE_TYPE_CODE);
        } else {
            if (!readingDocumentReason.get().getReasonCodeCodes().contains(readingReasonCode)) {
                throw new SAPWebServiceException(thesaurus, MessageSeeds.REASON_CODE_DATA_CONTRADICTS_SOURCE_TYPE_CODE);
            }
        }
        return readingDocumentReason;
    }


    private Optional<SAPMeterReadingDocumentReason> findReadingReasonProviderByReasonCode(String readingReasonCode) {
        Optional<SAPMeterReadingDocumentReason> readingDocumentReason = WebServiceActivator.METER_READING_REASONS
                .stream()
                .filter(readingReason -> readingReason.getReasonCodeCodes().contains(readingReasonCode))
                .findFirst();

        if (!readingDocumentReason.isPresent()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.UNSUPPORTED_REASON_CODE);
        }
        return readingDocumentReason;
    }

    private Optional<SAPMeterReadingDocumentReason> dataSourceTypeCodeIsRequired() {
        throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_REQUIRED_DATA_SOURCE_TYPE_CODE);
    }
}
