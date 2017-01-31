/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edp.logbooks;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLogBookSupport;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.protocolimplv2.edp.CX20009;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LogbookReader implements DeviceLogBookSupport {

    private final CX20009 protocol;
    private final List<AbstractLogbookParser> logBookParsers;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;

    public LogbookReader(CX20009 protocol, IssueService issueService, CollectedDataFactory collectedDataFactory, MeteringService meteringService) {
        this.protocol = protocol;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
        logBookParsers = new ArrayList<>();
        logBookParsers.add(new StandardLogbookParser(protocol, meteringService));
        logBookParsers.add(new ContractedPowerLogbookParser(protocol, meteringService));
        logBookParsers.add(new FirmwareLogbookParser(protocol, meteringService));
        logBookParsers.add(new ClockSyncLogbookParser(protocol, meteringService));
        logBookParsers.add(new ConfigurationLogbookParser(protocol, meteringService));
        logBookParsers.add(new DisconnectorLogbookParser(protocol, meteringService));
        logBookParsers.add(new PowerFailureLogbookParser(protocol, meteringService));
        logBookParsers.add(new QualityOfServiceLogbookParser(protocol, meteringService));
        logBookParsers.add(new AntiFraudLogbookParser(protocol, meteringService));
        logBookParsers.add(new DemandManagementLogbookParser(protocol, meteringService));
        logBookParsers.add(new CommunicationLogbookParser(protocol, meteringService));
        logBookParsers.add(new PublicLightingLogbookParser(protocol, meteringService));
    }

    private AbstractLogbookParser getLogBookParser(LogBookReader logBookReader) {
        for (AbstractLogbookParser logbookParser : logBookParsers) {
            if (logbookParser.getObisCode().equals(logBookReader.getLogBookObisCode())) {
                return logbookParser;
            }
        }
        return null;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBooks) {
            CollectedLogBook collectedLogBook = this.collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
            AbstractLogbookParser logBookParser = getLogBookParser(logBookReader);
            if (logBookParser != null) {
                ProfileGeneric profileGeneric;
                try {
                    profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(logBookReader.getLogBookObisCode());
                } catch (IOException e) {
                    throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
                }
                Calendar fromDate = getCalendar();
                fromDate.setTimeInMillis(logBookReader.getLastLogBook().toEpochMilli());
                try {
                    byte[] bufferData = profileGeneric.getBufferData(fromDate, getCalendar());
                    collectedLogBook.setMeterEvents(logBookParser.parseEvents(bufferData));
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession())) {
                        collectedLogBook.setFailureInformation(
                                ResultType.NotSupported,
                                this.issueService.newWarning(
                                        logBookReader,
                                        com.energyict.mdc.protocol.api.MessageSeeds.LOGBOOK_NOT_SUPPORTED,
                                        logBookReader.getLogBookObisCode().toString()));
                    }
                }
            } else {
                collectedLogBook.setFailureInformation(
                        ResultType.NotSupported,
                        this.issueService.newWarning(
                                logBookReader,
                                com.energyict.mdc.protocol.api.MessageSeeds.LOGBOOK_NOT_SUPPORTED,
                                logBookReader.getLogBookObisCode().toString()));
            }
            result.add(collectedLogBook);
        }
        return result;
    }

    private Calendar getCalendar() {
        return ProtocolUtils.getCalendar(protocol.getTimeZone());
    }

}