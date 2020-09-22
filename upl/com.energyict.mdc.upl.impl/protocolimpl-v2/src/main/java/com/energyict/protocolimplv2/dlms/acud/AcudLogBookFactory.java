package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public abstract class AcudLogBookFactory implements DeviceLogBookSupport {

    private Acud protocol;
    private final IssueFactory issueFactory;
    private final CollectedDataFactory collectedDataFactory;

    public AcudLogBookFactory(Acud protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.protocol = protocol;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBooks) {
            CollectedLogBook collectedLogBook = collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
            if (isSupportedDLMS(logBookReader)) {
                getDLMSLogBookData(logBookReader, collectedLogBook);
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, issueFactory.createWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
            }
            result.add(collectedLogBook);
        }
        return result;
    }

    private void getDLMSLogBookData(LogBookReader logBookReader, CollectedLogBook collectedLogBook) {
        ProfileGeneric profileGeneric = null;
        try {
            profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(logBookReader.getLogBookObisCode());
        } catch (NotInObjectListException e) {
            collectedLogBook.setFailureInformation(ResultType.InCompatible, issueFactory.createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
        }

        if (profileGeneric != null) {
            try {
                Calendar fromDate = getCalendar();
                Calendar uptoDate = getCalendar();
                fromDate.setTime(logBookReader.getLastLogBook());
                DataContainer dataContainer = profileGeneric.getBuffer(fromDate, uptoDate);
                collectedLogBook.setCollectedMeterEvents(parseEvents(dataContainer, logBookReader.getLogBookObisCode()));
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries())) {
                    collectedLogBook.setFailureInformation(ResultType.InCompatible, issueFactory.createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                }
            }
        }
    }

    protected List<MeterProtocolEvent> parseEvents(DataContainer dataContainer, ObisCode logBookObisCode) {
        List<MeterEvent> meterEvents = new ArrayList<>();
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }

    protected boolean isSupportedDLMS(LogBookReader logBookReader) {
        for (ObisCode supportedLogBook : getSupportedLogBooks()) {
            if (supportedLogBook.equalsIgnoreBChannel(logBookReader.getLogBookObisCode())) {
                return true;
            }
        }
        return false;
    }

    private Calendar getCalendar() {
        return ProtocolUtils.getCalendar(protocol.getTimeZone());
    }

    protected abstract List<ObisCode> getSupportedLogBooks();

    protected Acud getProtocol() {
        return protocol;
    }
}