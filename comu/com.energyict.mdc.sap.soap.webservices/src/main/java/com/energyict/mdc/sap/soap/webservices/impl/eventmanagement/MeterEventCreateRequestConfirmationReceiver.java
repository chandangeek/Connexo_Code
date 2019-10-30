/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.eventmanagement;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.InboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreateconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreateconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreateconfirmation.UUID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreateconfirmation.UtilitiesSmartMeterEventERPBulkCreateConfirmationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiessmartmetereventerpbulkcreateconfirmation.UtilsSmrtMtrEvtERPBulkCrteConfMsg;

import com.google.common.collect.ImmutableSet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Component(name = MeterEventCreateRequestConfirmationReceiver.NAME,
        service = {InboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterEventCreateRequestConfirmationReceiver.NAME})
public class MeterEventCreateRequestConfirmationReceiver extends AbstractInboundEndPoint
        implements InboundSoapEndPointProvider, UtilitiesSmartMeterEventERPBulkCreateConfirmationCIn, ApplicationSpecific {
    static final String NAME = "SAP SmartMeterEventCreateConfirmation";
    private static final Set<String> FAILURE_CODES = ImmutableSet.of(ProcessingResultCode.FAILED.getCode());

    private volatile Thesaurus thesaurus;

    public MeterEventCreateRequestConfirmationReceiver() {
        // for OSGi purposes
    }

    @Inject
    public MeterEventCreateRequestConfirmationReceiver(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setTranslationsProvider(WebServiceActivator translationsProvider) {
        this.thesaurus = translationsProvider.getThesaurus();
    }

    @Override
    public MeterEventCreateRequestConfirmationReceiver get() {
        return this;
    }

    @Override
    public void utilitiesSmartMeterEventERPBulkCreateConfirmationCIn(UtilsSmrtMtrEvtERPBulkCrteConfMsg confirmation) {
        runWithOccurrence(() -> {
            String uuid = findReferenceUuid(confirmation).orElse("null");
            if (isConfirmed(confirmation)) {
                log(LogLevel.INFO, thesaurus.getSimpleFormat(MessageSeeds.EVENT_CONFIRMED).format(uuid));
            } else {
                String cause = getSeverestError(confirmation).orElseGet(() -> thesaurus.getSimpleFormat(MessageSeeds.EVENT_NO_ERROR_MESSAGE_PROVIDED).format());
                throw new SAPWebServiceException(thesaurus, MessageSeeds.EVENT_FAILED_TO_CONFIRM, uuid, cause);
            }
            return null;
        });
    }

    private static Optional<String> findReferenceUuid(UtilsSmrtMtrEvtERPBulkCrteConfMsg confirmation) {
        return Optional.ofNullable(confirmation)
                .map(UtilsSmrtMtrEvtERPBulkCrteConfMsg::getMessageHeader)
                .map(BusinessDocumentMessageHeader::getReferenceUUID)
                .map(UUID::getValue);
    }

    private static boolean isConfirmed(UtilsSmrtMtrEvtERPBulkCrteConfMsg confirmation) {
        return Optional.ofNullable(confirmation)
                .map(UtilsSmrtMtrEvtERPBulkCrteConfMsg::getLog)
                .map(Log::getBusinessDocumentProcessingResultCode)
                .filter(Predicates.not(FAILURE_CODES::contains))
                .isPresent();
    }

    private static Optional<String> getSeverestError(UtilsSmrtMtrEvtERPBulkCrteConfMsg confirmation) {
        return Optional.ofNullable(confirmation)
                .map(UtilsSmrtMtrEvtERPBulkCrteConfMsg::getLog)
                .map(Log::getItem)
                .map(List::stream)
                .orElseGet(Stream::empty)
                .reduce(MeterEventCreateRequestConfirmationReceiver::findMaximumSeverityOrNotNullOrWhatever)
                .map(LogItem::getNote);
    }

    private static LogItem findMaximumSeverityOrNotNullOrWhatever(LogItem item1, LogItem item2) {
        String s1 = item1.getSeverityCode();
        String s2 = item2.getSeverityCode();
        if (s2 == null) {
            return item1;
        } else if (s1 == null) {
            return item2;
        } else {
            s1 = s1.trim();
            s2 = s2.trim();
            int i1, i2;
            try {
                i2 = Integer.parseInt(s2);
            } catch (NumberFormatException e) {
                return item1;
            }
            try {
                i1 = Integer.parseInt(s1);
            } catch (NumberFormatException e) {
                return item2;
            }
            return i1 < i2 ? item2 : i2 < i1 ? item1 : item1.getNote() == null ? item2 : item1;
        }
    }

    @Override
    public String getApplication(){
        return WebServiceApplicationName.MULTISENSE.getName();
    }
}
