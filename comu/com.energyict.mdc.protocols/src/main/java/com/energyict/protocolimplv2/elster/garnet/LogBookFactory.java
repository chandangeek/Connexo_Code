package com.energyict.protocolimplv2.elster.garnet;


import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLogBookSupport;

import com.energyict.protocolimplv2.elster.garnet.exception.GarnetException;
import com.energyict.protocolimplv2.elster.garnet.exception.NotExecutedException;
import com.energyict.protocolimplv2.elster.garnet.structure.LogBookEventResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.field.NotExecutedError;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author sva
 * @since 24/06/2014 - 14:48
 */
public class LogBookFactory implements DeviceLogBookSupport {

    public static final ObisCode LOGBOOK_TYPE_OBISCODE = ObisCode.fromString("0.0.99.98.0.255");

    private final GarnetConcentrator deviceProtocol;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;

    public LogBookFactory(GarnetConcentrator deviceProtocol, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        this.deviceProtocol = deviceProtocol;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> collectedLogBooks = new ArrayList<>(logBooks.size());
        for (LogBookReader logBookReader : logBooks) {
            if (!logBookReader.getLogBookObisCode().equals(LOGBOOK_TYPE_OBISCODE)) {
                collectedLogBooks.add(createNotSupportedCollectedLogBook(logBookReader));
            } else {
                collectedLogBooks.add(readLogBook(logBookReader));
            }
        }

        return collectedLogBooks;
    }

    private CollectedLogBook readLogBook(LogBookReader logBookReader) {
        CollectedLogBook collectedLogBook = createDeviceLogBook(logBookReader);
        List<MeterProtocolEvent> meterEvents = new ArrayList<>();

        try {
            int concentratorId = getDeviceProtocol().getTopologyMaintainer().getDeviceIdOfMaster();
            int totalNrOfEvents = readTotalNrOfEvents();

            Date eventDate = null;
            int currentEventNr = totalNrOfEvents;
            while ((currentEventNr > 0) && (eventDate == null || eventDate.after(logBookReader.getLastLogBook()))) {
                LogBookEventResponseStructure logBookEvent = getDeviceProtocol().getRequestFactory().readLogBookEvent(currentEventNr);
                eventDate = logBookEvent.getDateTimeOfEvent().getDate();
                if (logBookEvent.getDateTimeOfEvent().getDate().after(logBookReader.getLastLogBook())) {
                    if (logBookEvent.getSourceOfEvent().getAddress() == concentratorId) {   // The event is not inherited from a downstream concentrator
                        meterEvents.add(createMeterProtocolEventFor(logBookEvent));
                    }
                }
                currentEventNr--;
            }
        } catch (NotExecutedException e) {
            if (e.getErrorStructure().getNotExecutedError().getErrorCode().equals(NotExecutedError.ErrorCode.COMMAND_NOT_IMPLEMENTED)) {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, this.issueService.newWarning(logBookReader.getLogBookObisCode(), "logBookXnotsupported", logBookReader.getLogBookObisCode()));
            } else if (e.getErrorStructure().getNotExecutedError().getErrorCode().equals(NotExecutedError.ErrorCode.SLAVE_DOES_NOT_EXIST)) {
                collectedLogBook.setFailureInformation(ResultType.ConfigurationMisMatch, this.issueService.newWarning(logBookReader.getDeviceIdentifier(), "topologyMismatch", logBookReader.getDeviceIdentifier()));
            } else {
                collectedLogBook.setFailureInformation(ResultType.InCompatible, this.issueService.newProblem(logBookReader.getLogBookObisCode(), "CouldNotParseLogBookData"));
            }
        } catch (GarnetException e) {
            collectedLogBook.setFailureInformation(ResultType.InCompatible, this.issueService.newProblem(logBookReader.getLogBookObisCode(), "CouldNotParseLogBookData"));
        }

        collectedLogBook.setMeterEvents(meterEvents);
        return collectedLogBook;
    }

    private MeterProtocolEvent createMeterProtocolEventFor(LogBookEventResponseStructure logBookEvent) {
        return new MeterProtocolEvent(
                logBookEvent.getDateTimeOfEvent().getDate(),
                mapLogBookEventCodeToMeterEventCode(logBookEvent),
                logBookEvent.getEventCode().getEventCode().getCode(),
                EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(mapLogBookEventCodeToMeterEventCode(logBookEvent)),
                logBookEvent.getEventDescription(),
                logBookEvent.getSourceOfEvent().getAddress(),
                logBookEvent.getLogNr().getNr()
        );
    }

    private int mapLogBookEventCodeToMeterEventCode(LogBookEventResponseStructure logBookEvent) {
        switch (logBookEvent.getEventCode().getEventCode()) {
            case SLAVE_REGISTRATION:
                return MeterEvent.CONFIGURATIONCHANGE;
            case SCHEDULING_CONFIGURATION:
                return MeterEvent.CONFIGURATIONCHANGE;
            case CUSTOMER_CONFIGURATION:
                return MeterEvent.CONFIGURATIONCHANGE;
            case DISPLAY_CONFIGURATION:
                return MeterEvent.CONFIGURATIONCHANGE;
            case DISCONNECT_EVENT:
                return MeterEvent.REMOTE_DISCONNECTION;
            case RECONNECT_EVENT:
                return MeterEvent.REMOTE_CONNECTION;
            case CONCENTRATOR_CONFIGURATION:
                return MeterEvent.CONFIGURATIONCHANGE;
            case CONTACTOR_OPERATION_VIA_SERIAL:
                return MeterEvent.REMOTE_CONNECTION;
            case SENSOR_IN_ALARM:
                return MeterEvent.METER_ALARM;
            case METER_SETUP_INCORRECT:
                return MeterEvent.PROGRAM_FLOW_ERROR;
            case AUTOMATIC_CONTACOTR_OPERATION:
                return MeterEvent.REMOTE_CONNECTION;
            default:
                return MeterEvent.OTHER;
        }
    }

    private int readTotalNrOfEvents() throws GarnetException {
        LogBookEventResponseStructure logBookEvent = getDeviceProtocol().getRequestFactory().readLogBookEvent(1); // Read out the first event
        return  logBookEvent.getTotalNrOfLogs().getNr();
    }

    private CollectedLogBook createDeviceLogBook(LogBookReader logBookReader) {
        return this.collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
    }

    private CollectedLogBook createNotSupportedCollectedLogBook(LogBookReader logBookReader) {
        CollectedLogBook failedLogBook = createDeviceLogBook(logBookReader);
        failedLogBook.setFailureInformation(ResultType.NotSupported, this.issueService.newWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode()));
        return failedLogBook;
    }

    public GarnetConcentrator getDeviceProtocol() {
        return deviceProtocol;
    }

}