package com.energyict.protocolimplv2.abnt.common;

import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeFactory;
import com.energyict.mdc.protocol.api.cim.EndDeviceEventTypeMapping;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLogBookSupport;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.frame.field.Function;
import com.energyict.protocolimplv2.abnt.common.structure.HistoryLogResponse;
import com.energyict.protocolimplv2.abnt.common.structure.PowerFailLogResponse;
import com.energyict.protocolimplv2.abnt.common.structure.field.HistoryLogRecord;
import com.energyict.protocolimplv2.abnt.common.structure.field.PowerFailRecord;
import com.energyict.protocols.mdc.services.impl.Bus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 2/09/2014 - 11:53
 */
public class LogBookFactory implements DeviceLogBookSupport {

    public final static ObisCode HISTORY_LOG_OBIS = ObisCode.fromString("0.0.99.98.0.255");
    public final static ObisCode POWER_FAIL_LOG_OBIS = ObisCode.fromString("1.0.99.97.0.255");

    private final AbstractAbntProtocol meterProtocol;

    public LogBookFactory(AbstractAbntProtocol meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> collectedLogBooks = new ArrayList<>(logBooks.size());
        for (LogBookReader logBook : logBooks) {
            CollectedLogBook deviceLogBook = com.energyict.mdc.protocol.api.CollectedDataFactoryProvider.instance.get().getCollectedDataFactory().createCollectedLogBook(logBook.getLogBookIdentifier());
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
        for (HistoryLogRecord historyLogRecord : logRecords) {
            MeterProtocolEvent protocolEvent = new MeterProtocolEvent(
                    historyLogRecord.getEventDate().getDate(getMeterProtocol().getTimeZone()),
                    MeterEvent.CONFIGURATIONCHANGE,
                    historyLogRecord.getEvent().getEventCode(),
                    EndDeviceEventTypeMapping.CONFIGURATIONCHANGE.getEventType(),
                    "Reader ".concat(historyLogRecord.getReaderSerialNumber().getSerialNumber().getText()).concat(" - ").concat(historyLogRecord.getEvent().getEventMessage()),
                    Function.FunctionCode.HISTORY_LOG.getFunctionCode(),
                    0
            );
            if (protocolEvent.getTime().after(logBook.getLastLogBook())) {
                meterEvents.add(protocolEvent);
            }
        }
        deviceLogBook.setMeterEvents(meterEvents);
    }

    private void readPowerFailLog(LogBookReader logBook, CollectedLogBook deviceLogBook) {
        try {
            PowerFailLogResponse powerFailLog = getRequestFactory().readPowerFailLog();

            List<MeterProtocolEvent> meterEvents = new ArrayList<>();
            for (PowerFailRecord powerFailRecord : powerFailLog.getPowerFailRecords()) {
                MeterProtocolEvent protocolEvent = new MeterProtocolEvent(
                        powerFailRecord.getEndOfPowerFail().getDate(getMeterProtocol().getTimeZone()),
                        MeterEvent.POWERDOWN,
                        0,
                        EndDeviceEventTypeMapping.POWERDOWN.getEventType(),
                        constructEventMessage(powerFailRecord),
                        Function.FunctionCode.POWER_FAIL_LOG.getFunctionCode(),
                        0
                );
                if (protocolEvent.getTime().after(logBook.getLastLogBook())) {
                    meterEvents.add(protocolEvent);
                }
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
        deviceLogBook.setFailureInformation(ResultType.NotSupported, Bus.getIssueService().newWarning(deviceLogBook, "logBookXnotsupported", logBookObisCode));
    }

    private void logBookParsingException(CollectedLogBook deviceLogBook) {
        deviceLogBook.setFailureInformation(ResultType.InCompatible, com.energyict.protocols.mdc.services.impl.Bus.getIssueService().newProblem(deviceLogBook, "CouldNotParseLogBookData"));
    }

    public AbstractAbntProtocol getMeterProtocol() {
        return meterProtocol;
    }

    public RequestFactory getRequestFactory() {
        return getMeterProtocol().getRequestFactory();
    }
}