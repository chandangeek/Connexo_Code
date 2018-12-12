package com.energyict.protocolimplv2.edmi.mk10.events;

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
import com.energyict.protocolimpl.edmi.common.command.CommandFactory;
import com.energyict.protocolimpl.edmi.common.command.CommandResponseException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 24/02/2017 - 12:33
 */
public class MK10LogBookFactory implements DeviceLogBookSupport {

    private final CommandLineProtocol protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public MK10LogBookFactory(CommandLineProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
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
                    EventSurvey eventSurvey = new EventSurvey(getCommandFactory(), logBookDescription);
                    List<MeterProtocolEvent> meterProtocolEvents = eventSurvey.readFile(logBookReader.getLastLogBook());
                    collectedLogBook.setCollectedMeterEvents(meterProtocolEvents);
                } catch (ProtocolException e) {
                    collectedLogBook.setFailureInformation(ResultType.DataIncomplete,
                            issueFactory.createProblem(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode(), e.getMessage()));
                } catch (CommunicationException e) {
                    if (e.getCause() instanceof CommandResponseException && ((CommandResponseException) e.getCause()).getResponseCANCode() == 3) {
                        collectedLogBook.setFailureInformation(ResultType.NotSupported,
                                issueFactory.createProblem(logBookReader.getLogBookObisCode(), "logBookXnotsupported", logBookReader.getLogBookObisCode()));
                    } else {
                        throw e; // Rethrow the original communication exception
                    }
                }
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported,
                        issueFactory.createWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode()));
            }
            result.add(collectedLogBook);
        }
        return result;
    }

    private CommandFactory getCommandFactory() {
        return getProtocol().getCommandFactory();
    }

    public CommandLineProtocol getProtocol() {
        return protocol;
    }
}