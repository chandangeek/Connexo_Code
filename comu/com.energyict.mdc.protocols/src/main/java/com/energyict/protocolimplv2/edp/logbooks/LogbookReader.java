package com.energyict.protocolimplv2.edp.logbooks;

import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLogBookSupport;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimplv2.edp.CX20009;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import oracle.net.ns.Communication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 16:39
 * Author: khe
 */
public class LogbookReader implements DeviceLogBookSupport {

    private final CX20009 protocol;
    private final List<AbstractLogbookParser> logBookParsers;

    public LogbookReader(CX20009 protocol) {
        this.protocol = protocol;
        logBookParsers = new ArrayList<>();
        logBookParsers.add(new StandardLogbookParser(protocol));
        logBookParsers.add(new ContractedPowerLogbookParser(protocol));
        logBookParsers.add(new FirmwareLogbookParser(protocol));
        logBookParsers.add(new ClockSyncLogbookParser(protocol));
        logBookParsers.add(new ConfigurationLogbookParser(protocol));
        logBookParsers.add(new DisconnectorLogbookParser(protocol));
        logBookParsers.add(new PowerFailureLogbookParser(protocol));
        logBookParsers.add(new QualityOfServiceLogbookParser(protocol));
        logBookParsers.add(new AntiFraudLogbookParser(protocol));
        logBookParsers.add(new DemandManagementLogbookParser(protocol));
        logBookParsers.add(new CommunicationLogbookParser(protocol));
        logBookParsers.add(new PublicLightingLogbookParser(protocol));
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
            CollectedLogBook collectedLogBook = com.energyict.mdc.protocol.api.CollectedDataFactoryProvider.instance.get().getCollectedDataFactory().createCollectedLogBook(logBookReader.getLogBookIdentifier());
            AbstractLogbookParser logBookParser = getLogBookParser(logBookReader);
            if (logBookParser != null) {
                ProfileGeneric profileGeneric;
                try {
                    profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(logBookReader.getLogBookObisCode());
                } catch (IOException e) {
                    throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
                }
                Calendar fromDate = getCalendar();
                fromDate.setTime(logBookReader.getLastLogBook());
                try {
                    byte[] bufferData = profileGeneric.getBufferData(fromDate, getCalendar());
                    collectedLogBook.setMeterEvents(logBookParser.parseEvents(bufferData));
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession())) {
                        collectedLogBook.setFailureInformation(ResultType.NotSupported, com.energyict.protocols.mdc.services.impl.Bus.getIssueService().newWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
                    }
                }
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, com.energyict.protocols.mdc.services.impl.Bus.getIssueService().newWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
            }
            result.add(collectedLogBook);
        }
        return result;
    }

    private Calendar getCalendar() {
        return ProtocolUtils.getCalendar(protocol.getTimeZone());
    }
}