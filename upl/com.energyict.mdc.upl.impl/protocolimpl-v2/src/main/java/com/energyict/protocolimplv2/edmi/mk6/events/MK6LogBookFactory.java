/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.mk6.events;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocolimpl.edmi.common.CommandLineProtocol;
import com.energyict.protocolimpl.edmi.common.command.CommandResponseException;
import com.energyict.protocolimplv2.edmi.mk6.MK6;
import com.energyict.protocolimplv2.edmi.mk6.profiles.ExtensionFactory;
import com.energyict.protocolimplv2.edmi.mk6.profiles.LoadSurvey;
import com.energyict.protocolimplv2.edmi.mk6.profiles.LoadSurveyData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 3/03/2017 - 16:55
 */
public class MK6LogBookFactory implements DeviceLogBookSupport {

    private final CommandLineProtocol protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public MK6LogBookFactory(CommandLineProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBooks) {
            CollectedLogBook collectedLogBook = collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
            LogBookDescription logBookDescription = LogBookDescription.fromObisCode(logBookReader.getLogBookObisCode());
            if (!logBookDescription.equals(LogBookDescription.UNKNOWN)) {
                try {
                    LoadSurvey eventSurvey = getCorrespondingLoadSurvey(logBookDescription);
                    if (eventSurvey != null) {
                        LoadSurveyData eventLogData = eventSurvey.readFile(logBookReader.getLastLogBook());
                        List<MeterProtocolEvent> meterProtocolEvents = logBookDescription.getCorrespondingEventLogParser().parseMeterProtocolEvents(eventLogData);
                        collectedLogBook.setCollectedMeterEvents(meterProtocolEvents);
                    } else {
                        markLogBookNotSpported(logBookReader, collectedLogBook);
                    }
                } catch (ProtocolException e) {
                    addLogBookIssue(logBookReader, collectedLogBook, e);
                } catch (CommunicationException e) {
                    if (e.getCause() instanceof CommandResponseException && ((CommandResponseException) e.getCause()).getResponseCANCode() == 3) {
                        markLogBookNotSpported(logBookReader, collectedLogBook);
                    } else {
                        throw e; // Rethrow the original communication exception
                    }
                }
            } else {
                markLogBookNotSpported(logBookReader, collectedLogBook);
            }
            result.add(collectedLogBook);
        }
        return result;
    }

    private void markLogBookNotSpported(LogBookReader logBookReader, CollectedLogBook collectedLogBook) {
        collectedLogBook.setFailureInformation(ResultType.NotSupported,
                issueFactory.createProblem(logBookReader.getLogBookObisCode(), "logBookXnotsupported", logBookReader.getLogBookObisCode()));
    }

    private void addLogBookIssue(LogBookReader logBookReader, CollectedLogBook collectedLogBook, ProtocolException e) {
        collectedLogBook.setFailureInformation(ResultType.DataIncomplete,
                issueFactory.createProblem(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode(), e.getMessage()));
    }

    public CommandLineProtocol getProtocol() {
        return protocol;
    }

    public ExtensionFactory getExtensionFactory() {
        return ((MK6) getProtocol()).getExtensionFactory();
    }

    private LoadSurvey getCorrespondingLoadSurvey(LogBookDescription logBookDescription) {
        try {
            return getExtensionFactory().findLoadSurvey(logBookDescription.getExtensionName());
        } catch (IOException e) {
            return null; // Load survey for the given extension name was not found
        }
    }
}