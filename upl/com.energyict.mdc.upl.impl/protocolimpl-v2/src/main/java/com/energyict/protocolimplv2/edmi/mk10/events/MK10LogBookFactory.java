package com.energyict.protocolimplv2.edmi.mk10.events;

import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.tasks.support.DeviceLogBookSupport;

import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocolimpl.edmi.common.CommandLineProtocol;
import com.energyict.protocolimpl.edmi.common.command.CommandFactory;
import com.energyict.protocolimpl.edmi.common.command.CommandResponseException;
import com.energyict.protocolimplv2.MdcManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 24/02/2017 - 12:33
 */
public class MK10LogBookFactory implements DeviceLogBookSupport {

    private final CommandLineProtocol protocol;

    public MK10LogBookFactory(CommandLineProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBooks) {
            CollectedLogBook collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(logBookReader.getLogBookIdentifier());
            LogBookDescription logBookDescription = LogBookDescription.fromObisCode(logBookReader.getLogBookObisCode());
            if (!logBookDescription.equals(LogBookDescription.UNKNOWN)) {
                try {
                    EventSurvey eventSurvey = new EventSurvey(getCommandFactory(), logBookDescription);
                    List<MeterProtocolEvent> meterProtocolEvents = eventSurvey.readFile(logBookReader.getLastLogBook());
                    collectedLogBook.setCollectedMeterEvents(meterProtocolEvents);
                } catch (ProtocolException e) {
                    collectedLogBook.setFailureInformation(ResultType.DataIncomplete,
                             MdcManager.getIssueFactory().createProblem(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode(), e.getMessage()));
                } catch (CommunicationException e) {
                    if (e.getCause() instanceof CommandResponseException && ((CommandResponseException)e.getCause()).getResponseCANCode() == 3) {
                        collectedLogBook.setFailureInformation(ResultType.NotSupported,
                                MdcManager.getIssueFactory().createProblem(logBookReader.getLogBookObisCode(), "logBookXnotsupported", logBookReader.getLogBookObisCode()));
                    } else {
                        throw e; // Rethrow the original communication exception
                    }
                }
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported,
                        MdcManager.getIssueFactory().createWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode()));
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