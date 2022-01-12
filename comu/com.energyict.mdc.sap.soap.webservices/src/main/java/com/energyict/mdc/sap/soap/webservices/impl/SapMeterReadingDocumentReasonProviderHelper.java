/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentReason;

import aQute.bnd.osgi.resource.FilterParser;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
@Component(name = "com.energyict.mdc.sap.meterreadingdocument.provider.helper",
        service = SapMeterReadingDocumentReasonProviderHelper.class, immediate = true)
public class SapMeterReadingDocumentReasonProviderHelper {

    private static MessageSeeds errorMessage;
    private static volatile Thesaurus thesaurus;

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(WebServiceActivator.COMPONENT_NAME, Layer.SERVICE);
    }


    public static Optional<SAPMeterReadingDocumentReason> findReadingReasonProvider(String readingReasonCode, String dataSourceTypeCode) {
        switch (WebServiceActivator.getReadingsStrategy()) {
            case WebServiceActivator.DATA_SOURCE_TYPE_CODE_STRATEGY:
                return (dataSourceTypeCode != null) ? findReadingReasonProviderByDataSourceCode(readingReasonCode, dataSourceTypeCode) : dataSourceTypeCodeIsRequired();
            case WebServiceActivator.REASON_CODE_STRATEGY:
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
            throw new SAPWebServiceException(thesaurus, MessageSeeds.UNSUPPORTED_DATA_SOURCE_TYPE_CODE);
        } else {
            if (!readingDocumentReason.get().getReasonCodeCodes().contains(readingReasonCode)) {
                throw new SAPWebServiceException(thesaurus, MessageSeeds.REASON_CODE_DATA_CONTRADICTS_SOURCE_TYPE_CODE);
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
            throw new SAPWebServiceException(thesaurus, MessageSeeds.UNSUPPORTED_REASON_CODE);
        }
        return readingDocumentReason;
    }

    private static Optional<SAPMeterReadingDocumentReason> dataSourceTypeCodeIsRequired() {
        throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_REQUIRED_DATA_SOURCE_TYPE_CODE);
    }


    public static MessageSeeds getErrorMessage() {
        return errorMessage;
    }
}
