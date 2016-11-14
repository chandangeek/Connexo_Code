package com.energyict.protocolimplv2.eict.webrtuz3.logbooks;

import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;

import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.NotInObjectListException;
import com.energyict.protocol.exceptions.ProtocolRuntimeException;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.events.EventsLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 23/04/2015 - 15:06
 */
public class LogBookParser implements DeviceLogBookSupport {

    private static final ObisCode EVENT_LOG_OBISCODE = ObisCode.fromString("0.x.99.98.0.255");

    private AbstractDlmsProtocol protocol;

    public LogBookParser(AbstractDlmsProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBooks) {

            CollectedLogBook collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(logBookReader.getLogBookIdentifier());
            String meterSerialNumber = logBookReader.getMeterSerialNumber();

            try {
                protocol.getMeterTopology().getPhysicalAddress(meterSerialNumber);
            } catch (ProtocolRuntimeException e) {
                //Serial number is not in meter map
                collectedLogBook.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
                result.add(collectedLogBook);
                continue;
            }

            ObisCode eventLogObisCode = protocol.getPhysicalAddressCorrectedObisCode(EVENT_LOG_OBISCODE, meterSerialNumber);
            ProfileGeneric profileGeneric = null;
            try {
                profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(eventLogObisCode);
            } catch (NotInObjectListException e) {
                collectedLogBook.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueFactory().createWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
            }

            if (profileGeneric != null) {
                Calendar fromDate = Calendar.getInstance(protocol.getTimeZone());
                fromDate.setTime(logBookReader.getLastLogBook());
                try {
                    EventsLog standardEvents = new EventsLog(profileGeneric.getBuffer(fromDate));
                    List<MeterEvent> meterEvents = standardEvents.getMeterEvents();
                    collectedLogBook.setCollectedMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents));
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession().getProperties().getRetries()+1)) {
                        collectedLogBook.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
                    }
                }
            }

            result.add(collectedLogBook);
        }
        return result;
    }
}