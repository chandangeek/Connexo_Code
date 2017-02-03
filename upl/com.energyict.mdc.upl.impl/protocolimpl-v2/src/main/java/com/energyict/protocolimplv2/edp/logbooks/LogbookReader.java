package com.energyict.protocolimplv2.edp.logbooks;

import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.edp.CX20009;

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
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public LogbookReader(CX20009 protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
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
            CollectedLogBook collectedLogBook = this.collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
            AbstractLogbookParser logBookParser = getLogBookParser(logBookReader);
            if (logBookParser != null) {
                ProfileGeneric profileGeneric = null;
                try {
                    profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(logBookReader.getLogBookObisCode());
                } catch (NotInObjectListException e) {
                    collectedLogBook.setFailureInformation(ResultType.InCompatible, this.issueFactory.createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                }
                if (profileGeneric != null) {
                    Calendar fromDate = getCalendar();
                    fromDate.setTime(logBookReader.getLastLogBook());
                    try {
                        byte[] bufferData = profileGeneric.getBufferData(fromDate, getCalendar());
                        collectedLogBook.setCollectedMeterEvents(logBookParser.parseEvents(bufferData));
                    } catch (IOException e) {
                        if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries()+1)) {
                            collectedLogBook.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
                        }
                    }
                }
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
            }
            result.add(collectedLogBook);
        }
        return result;
    }

    private Calendar getCalendar() {
        return ProtocolUtils.getCalendar(protocol.getTimeZone());
    }
}