package com.energyict.protocolimplv2.abnt.common;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLogBookSupport;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.frame.field.Function;
import com.energyict.protocolimplv2.abnt.common.structure.HistoryLogResponse;
import com.energyict.protocolimplv2.abnt.common.structure.PowerFailLogResponse;
import com.energyict.protocolimplv2.abnt.common.structure.field.HistoryLogRecord;
import com.energyict.protocolimplv2.abnt.common.structure.field.PowerFailRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author sva
 * @since 2/09/2014 - 11:53
 */
public class LogBookFactory implements DeviceLogBookSupport {

    public static final ObisCode HISTORY_LOG_OBIS = ObisCode.fromString("0.0.99.98.0.255");
    public static final ObisCode POWER_FAIL_LOG_OBIS = ObisCode.fromString("1.0.99.97.0.255");

    private final AbstractAbntProtocol meterProtocol;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;

    public LogBookFactory(AbstractAbntProtocol meterProtocol, IssueService issueService, CollectedDataFactory collectedDataFactory, MeteringService meteringService) {
        this.meterProtocol = meterProtocol;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> collectedLogBooks = new ArrayList<>(logBooks.size());
        for (LogBookReader logBook : logBooks) {
            CollectedLogBook deviceLogBook = this.collectedDataFactory.createCollectedLogBook(logBook.getLogBookIdentifier());
            if (logBook.getLogBookObisCode().equals(HISTORY_LOG_OBIS)) {
                readHistoryLog(logBook, deviceLogBook);
            } else if (logBook.getLogBookObisCode().equals(POWER_FAIL_LOG_OBIS)) {
                readPowerFailLog(logBook, deviceLogBook);
            } else {
                logBookNotSupported(deviceLogBook, logBook.getLogBookObisCode());
            }
            collectedLogBooks.add(deviceLogBook);
        }

        return collectedLogBooks;
    }

    private void readHistoryLog(LogBookReader logBook, CollectedLogBook deviceLogBook) {
        try {
            HistoryLogResponse historyLogResponse = getRequestFactory().readHistoryLog();
            addAllEventsFromLog(logBook, deviceLogBook, historyLogResponse.getEventLog());
            addAllEventsFromLog(logBook, deviceLogBook, historyLogResponse.getExtendedEventLog());
        } catch (ParsingException e) {
            logBookParsingException(deviceLogBook);
        }
    }

    private void addAllEventsFromLog(LogBookReader logBook, CollectedLogBook deviceLogBook, List<HistoryLogRecord> logRecords) throws ParsingException {
        List<MeterProtocolEvent> meterEvents = new ArrayList<>();
        Optional<EndDeviceEventType> configurationChangeEventType = EndDeviceEventTypeMapping.CONFIGURATIONCHANGE.getEventType(this.meteringService);
        if (configurationChangeEventType.isPresent()) {
            for (HistoryLogRecord historyLogRecord : logRecords) {
                MeterProtocolEvent protocolEvent = new MeterProtocolEvent(
                        historyLogRecord.getEventDate().getDate(getMeterProtocol().getTimeZone()),
                        MeterEvent.CONFIGURATIONCHANGE,
                        historyLogRecord.getEvent().getEventCode(),
                        configurationChangeEventType.get(),
                        (("Reader " + historyLogRecord.getReaderSerialNumber().getSerialNumber().getText()) + " - ") + historyLogRecord.getEvent().getEventMessage(),
                        Function.FunctionCode.HISTORY_LOG.getFunctionCode(),
                        0);
                if (protocolEvent.getTime().after(Date.from(logBook.getLastLogBook()))) {
                    meterEvents.add(protocolEvent);
                }
            }
        }
        else {
            this.endDeviceEventTypeNotSupported(deviceLogBook, EndDeviceEventTypeMapping.CONFIGURATIONCHANGE.getEndDeviceEventTypeMRID());
        }
        deviceLogBook.setMeterEvents(meterEvents);
    }

    private void readPowerFailLog(LogBookReader logBook, CollectedLogBook deviceLogBook) {
        try {
            PowerFailLogResponse powerFailLog = getRequestFactory().readPowerFailLog();

            List<MeterProtocolEvent> meterEvents = new ArrayList<>();
            Optional<EndDeviceEventType> powerDownEventType = EndDeviceEventTypeMapping.POWERDOWN.getEventType(this.meteringService);
            if (powerDownEventType.isPresent()) {
                for (PowerFailRecord powerFailRecord : powerFailLog.getPowerFailRecords()) {
                    MeterProtocolEvent protocolEvent = new MeterProtocolEvent(
                            powerFailRecord.getEndOfPowerFail().getDate(getMeterProtocol().getTimeZone()),
                            MeterEvent.POWERDOWN,
                            0,
                            powerDownEventType.get(),
                            constructEventMessage(powerFailRecord),
                            Function.FunctionCode.POWER_FAIL_LOG.getFunctionCode(),
                            0);
                    if (protocolEvent.getTime().after(Date.from(logBook.getLastLogBook()))) {
                        meterEvents.add(protocolEvent);
                    }
                }
            }
            else {
                this.endDeviceEventTypeNotSupported(deviceLogBook, EndDeviceEventTypeMapping.POWERDOWN.getEndDeviceEventTypeMRID());
            }
            deviceLogBook.setMeterEvents(meterEvents);
        } catch (ParsingException e) {
            logBookParsingException(deviceLogBook);
        }
    }

    private String constructEventMessage(PowerFailRecord powerFailRecord) throws ParsingException {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Power failure for period [");
        messageBuilder.append(powerFailRecord.getStartOfPowerFail().getDate(getMeterProtocol().getTimeZone()));
        messageBuilder.append(" - ");
        messageBuilder.append(powerFailRecord.getEndOfPowerFail().getDate(getMeterProtocol().getTimeZone()));
        messageBuilder.append("]");
        return messageBuilder.toString();
    }

    private void logBookNotSupported(CollectedLogBook deviceLogBook, ObisCode logBookObisCode) {
        deviceLogBook.setFailureInformation(
                ResultType.NotSupported,
                this.issueService.newWarning(deviceLogBook, MessageSeeds.LOGBOOK_NOT_SUPPORTED, logBookObisCode));
    }

    private void endDeviceEventTypeNotSupported(CollectedLogBook deviceLogBook, String endDeviceEventTypeMRID) {
        deviceLogBook.setFailureInformation(
                ResultType.NotSupported,
                this.issueService.newWarning(deviceLogBook, MessageSeeds.END_DEVICE_EVENT_TYPE_NOT_SUPPORTED, endDeviceEventTypeMRID));
    }

    private void logBookParsingException(CollectedLogBook deviceLogBook) {
        deviceLogBook.setFailureInformation(
                ResultType.InCompatible,
                this.issueService.newProblem(deviceLogBook, MessageSeeds.COULD_NOT_PARSE_LOGBOOK_DATA));
    }

    public AbstractAbntProtocol getMeterProtocol() {
        return meterProtocol;
    }

    public RequestFactory getRequestFactory() {
        return getMeterProtocol().getRequestFactory();
    }

}