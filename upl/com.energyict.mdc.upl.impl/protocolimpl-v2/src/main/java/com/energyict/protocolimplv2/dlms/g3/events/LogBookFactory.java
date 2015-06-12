package com.energyict.protocolimplv2.dlms.g3.events;

import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.dlms.g3.events.*;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/06/2015 - 16:59
 */
public class LogBookFactory {

    public static final ObisCode MAIN_LOG = ObisCode.fromString("0.0.99.98.1.255");
    public static final ObisCode COVER_LOG = ObisCode.fromString("0.0.99.98.2.255");
    public static final ObisCode BREAKER_LOG = ObisCode.fromString("0.0.99.98.3.255");
    public static final ObisCode COMMUNICATION_LOG = ObisCode.fromString("0.0.99.98.4.255");
    public static final ObisCode VOLTAGE_CUT_LOG = ObisCode.fromString("0.0.99.98.5.255");
    public static final ObisCode LQI_EVENT_LOG = ObisCode.fromString("0.0.99.98.10.255");

    private final DlmsSession dlmsSession;
    private final List<EventLog> supportedEventLogs;

    public LogBookFactory(DlmsSession dlmsSession) {
        this.dlmsSession = dlmsSession;

        supportedEventLogs = Arrays.asList(
                new G3BasicEventLog(dlmsSession.getCosemObjectFactory(), MAIN_LOG, new MainEventMapper(), dlmsSession.getLogger(), dlmsSession.getTimeZone()),
                new G3BasicEventLog(dlmsSession.getCosemObjectFactory(), COVER_LOG, new CoverEventMapper(), dlmsSession.getLogger(), dlmsSession.getTimeZone()),
                new G3BasicEventLog(dlmsSession.getCosemObjectFactory(), BREAKER_LOG, new BreakerEventMapper(), dlmsSession.getLogger(), dlmsSession.getTimeZone()),
                new G3BasicEventLog(dlmsSession.getCosemObjectFactory(), COMMUNICATION_LOG, new CommunicationEventMapper(), dlmsSession.getLogger(), dlmsSession.getTimeZone()),
                new G3BasicEventLog(dlmsSession.getCosemObjectFactory(), VOLTAGE_CUT_LOG, new VoltageCutEventMapper(), dlmsSession.getLogger(), dlmsSession.getTimeZone()),
                new G3LqiEventLog(dlmsSession.getCosemObjectFactory(), LQI_EVENT_LOG, dlmsSession.getLogger(), dlmsSession.getTimeZone())
        );
    }

    /**
     * Read all the events from the meter between the two given dates, and return them as a list of {@link com.energyict.protocol.MeterEvent}
     */
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBookReaders) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBookReaders) {
            CollectedLogBook collectedLogBook = MdcManager.getCollectedDataFactory().createCollectedLogBook(logBookReader.getLogBookIdentifier());
            EventLog eventLog = findEventLog(logBookReader);
            if (eventLog != null) {
                try {
                    List<MeterEvent> events = eventLog.getEvents(getCalendar(logBookReader.getLastLogBook()), getCalendar(new Date()));
                    List<MeterProtocolEvent> meterProtocolEvents = MeterEvent.mapMeterEventsToMeterProtocolEvents(events);
                    collectedLogBook.setCollectedMeterEvents(meterProtocolEvents);
                } catch (NotInObjectListException e) {
                    collectedLogBook.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                } catch (IOException e) {
                    if (IOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                        collectedLogBook.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
                    } else if (IOExceptionHandler.isUnexpectedResponse(e, dlmsSession)) {
                        collectedLogBook.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addWarning(logBookReader, "logBookXissue", logBookReader.getLogBookObisCode().toString(), e.getMessage()));
                    }
                }
            } else {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
            }
            result.add(collectedLogBook);
        }
        return result;

    }

    private EventLog findEventLog(LogBookReader logBookReader) {
        for (EventLog eventLog : supportedEventLogs) {
            if (eventLog.getObisCode().equals(logBookReader.getLogBookObisCode())) {
                return eventLog;
            }
        }
        return null;
    }

    private Calendar getCalendar(Date date) {
        Calendar calendar = ProtocolUtils.getCalendar(dlmsSession.getTimeZone());
        calendar.setTime(date);
        return calendar;
    }
}