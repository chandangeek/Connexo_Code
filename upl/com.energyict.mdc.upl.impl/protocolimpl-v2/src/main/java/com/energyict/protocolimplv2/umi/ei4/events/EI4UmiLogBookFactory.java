package com.energyict.protocolimplv2.umi.ei4.events;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolcommon.Pair;
import com.energyict.protocolimplv2.umi.ei4.EI4Umi;
import com.energyict.protocolimplv2.umi.ei4.messages.EI4UmiMessaging;
import com.energyict.protocolimplv2.umi.ei4.structures.UmiHelper;
import com.energyict.protocolimplv2.umi.packet.payload.ReadObjPartRspPayload;
import com.energyict.protocolimplv2.umi.packet.payload.ReadObjRspPayload;
import com.energyict.protocolimplv2.umi.session.IUmiSession;
import com.energyict.protocolimplv2.umi.types.ResultCode;
import com.energyict.protocolimplv2.umi.types.UmiCode;
import com.energyict.protocolimplv2.umi.types.UmiObjectPart;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class EI4UmiLogBookFactory implements DeviceLogBookSupport {
    protected static final ObisCode STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");
    public static final UmiCode UMI_CODE_EVENT_CONTROL = new UmiCode("umi.1.1.194.61");
    public static final UmiCode UMI_CODE_EVENT_STATUS = new UmiCode("umi.1.1.194.62");
    public static final UmiCode UMI_CODE_EVENT_TABLE = new UmiCode("umi.1.1.194.63");

    public static final int QUANTITY_OF_EVENTS_PER_READ = 10;

    private final IssueFactory issueFactory;
    private EI4Umi ei4Umi;
    private final CollectedDataFactory collectedDataFactory;

    protected final List<ObisCode> supportedLogBooks;

    public EI4UmiLogBookFactory(EI4Umi ei4Umi, IssueFactory issueFactory, CollectedDataFactory collectedDataFactory) {
        this.ei4Umi = ei4Umi;
        this.issueFactory = issueFactory;
        this.collectedDataFactory = collectedDataFactory;

        supportedLogBooks = new ArrayList<>();
        supportedLogBooks.add(STANDARD_EVENT_LOG);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        List<CollectedLogBook> result = new ArrayList<>();
        for (LogBookReader logBookReader : logBooks) {
            CollectedLogBook collectedLogBook = collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
            if (!isSupported(logBookReader)) {
                collectedLogBook.setFailureInformation(ResultType.NotSupported, issueFactory.createWarning(logBookReader, "logBookXnotsupported", logBookReader.getLogBookObisCode().toString()));
            }

            IUmiSession session = ei4Umi.getUmiSession();
            try {
                EI4UmiwanEventStatus umiwanEventStatus = getEventStatus();
                int numberOfEntries = umiwanEventStatus.getNumberOfEntries();
                long startTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(umiwanEventStatus.getStartTime().getTime());
                long nextReadingBlockStartSeconds = TimeUnit.MILLISECONDS.toSeconds(logBookReader.getLastLogBook().getTime());
                long dataUntilSeconds = TimeUnit.MILLISECONDS.toSeconds(umiwanEventStatus.getMostRecentTimeStamp().getTime());

                if (nextReadingBlockStartSeconds == dataUntilSeconds) {
                    getProtocol().journal(Level.INFO, "No new events present");
                    result.add(collectedLogBook);
                    return result;
                }
                boolean rightStartTime = (startTimeSeconds <= nextReadingBlockStartSeconds)
                        && (startTimeSeconds != TimeUnit.MILLISECONDS.toSeconds(UmiHelper.UMI_ZERO_DATE.getTime()));

                EI4UmiEventTable eventTable = new EI4UmiEventTable(numberOfEntries);
                if (rightStartTime) {
                    int loops = numberOfEntries / QUANTITY_OF_EVENTS_PER_READ;
                    int remainder = numberOfEntries % QUANTITY_OF_EVENTS_PER_READ;
                    for (int i = 0; i <= loops; i++) {
                        boolean lastCycle = loops == i;
                        if (lastCycle) {
                            if (remainder == 0) {
                                continue;
                            }
                        }
                        String lastPartOfCode = lastCycle ?
                                ("[" + (i * QUANTITY_OF_EVENTS_PER_READ) + ":" + (i * QUANTITY_OF_EVENTS_PER_READ + remainder - 1) + "]")
                                : ("[" + (i * QUANTITY_OF_EVENTS_PER_READ) + ":" + (i * QUANTITY_OF_EVENTS_PER_READ + QUANTITY_OF_EVENTS_PER_READ - 1) + "]");
                        Pair<ResultCode, ReadObjPartRspPayload> eventTableResult = ei4Umi.getUmiSession().readObjectPart(new UmiObjectPart(UMI_CODE_EVENT_TABLE.toString() + lastPartOfCode));
                        if (!eventTableResult.getFirst().equals(ResultCode.OK)) {
                            getProtocol().journal(Level.WARNING, "Read logbook event operation failed." + eventTableResult.getFirst().getDescription());
                            throw new ProtocolException("Error while reading Umiwan event table (" + UMI_CODE_EVENT_TABLE.getCode() + lastPartOfCode + "): " + eventTableResult.getFirst()
                                    .getDescription());

                        }
                        getEvents(eventTableResult.getLast().getValue(), lastCycle ? remainder : QUANTITY_OF_EVENTS_PER_READ).forEach(eventTable::addEvent);
                    }
                    collectedLogBook.setCollectedMeterEvents(parseEvents(umiwanEventStatus, eventTable, logBookReader.getLogBookObisCode())
                            .stream()
                            .map(item -> {
                                item.getEventType().setType(ei4Umi.getTypeMeter());
                                return item;
                            }).collect(Collectors.toList()));
                } else {
                    String warning = "Start time mismatch. No events can be collected. Event control correction is required.";
                    getProtocol().journal(Level.WARNING, warning);
                    collectedLogBook.setFailureInformation(ResultType.DataIncomplete, issueFactory.createWarning(warning));
                }

                // Move startTime to the last log book read
                Date dateToSet = (rightStartTime && numberOfEntries > 0) ? eventTable.getEvents().get(eventTable.getSize()-1).getTimestamp() : logBookReader.getLastLogBook();
                sendEventControl(dateToSet, 0, 0);

            } catch (Exception ex) {
                collectedLogBook.setFailureInformation(ResultType.Other, issueFactory.createWarning(logBookReader, "unexpectedError", logBookReader.getLogBookObisCode().toString(), ex.getMessage()));
                throw ConnectionCommunicationException.unExpectedProtocolError(new IOException(ex.getMessage()));
            }
            result.add(collectedLogBook);
        }
        return result;
    }

    private List<EI4UmiwanEvent> getEvents(byte[] value, int q) {
        int l = value.length / q;
        List<EI4UmiwanEvent> list = new ArrayList<>();
        for (int i = 0; i < q; i++) {
            byte[] el = new byte[l];
            for (int j = 0; j < l; j++) {
                el[j] = value[i * l + j];
            }
            list.add(new EI4UmiwanEvent(el));
        }
        return list;
    }

    protected List<MeterProtocolEvent> parseEvents(EI4UmiwanEventStatus umiwanEventStatus, EI4UmiEventTable eventTable, ObisCode logBookObisCode) {
        List<MeterEvent> meterEvents;
        if (logBookObisCode.equals(STANDARD_EVENT_LOG)) {
            meterEvents = new EI4UmiStandardEventLog(ei4Umi.getTimeZone(), umiwanEventStatus, eventTable.getEvents()).getMeterEvents();
        } else {
            return new ArrayList<>();
        }
        return MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents);
    }


    public EI4Umi getProtocol() {
        return ei4Umi;
    }

    private boolean isSupported(LogBookReader logBookReader) {
        for (ObisCode supportedLogBook : supportedLogBooks) {
            if (supportedLogBook.equalsIgnoreBChannel(logBookReader.getLogBookObisCode())) {
                return true;
            }
        }
        return false;
    }

    public EI4UmiwanEventControl setUmiwanEventControl(OfflineDeviceMessage pendingMessage) {
        try {
            Map<String, String> map = new HashMap<>();
            pendingMessage.getDeviceMessageAttributes().forEach(offlineDeviceMessageAttribute -> map.put(offlineDeviceMessageAttribute.getName(), offlineDeviceMessageAttribute.getValue()));
            return sendEventControl(EI4UmiMessaging.europeanDateTimeFormat.parse(map.get("startTime")), 0, 0);
        } catch (Exception e) {
            getProtocol().journal(Level.WARNING, "Set event control operation failed.", e);
            throw ConnectionCommunicationException.unExpectedProtocolError(new IOException(e.getMessage()));
        }
    }


    private EI4UmiwanEventControl sendEventControl(Date dateToSet, long controlFlags, long acknowledgeFlags) throws IOException, GeneralSecurityException {
        EI4UmiwanEventControl control = new EI4UmiwanEventControl(dateToSet, controlFlags, acknowledgeFlags);

        ResultCode resultCode = ei4Umi.getUmiSession().writeObject(UMI_CODE_EVENT_CONTROL, control.getRaw());
        if (resultCode != ResultCode.OK) {
            getProtocol().journal(Level.WARNING, "Set event control operation failed." + resultCode.getDescription());
            throw new ProtocolException("Writing of Umiwan event control " + UMI_CODE_EVENT_CONTROL.getCode() + " failed." + resultCode.getDescription());
        }
        getProtocol().journal(Level.INFO, "Event control has been set to " + dateToSet);
        return control;
    }

    private EI4UmiwanEventStatus getEventStatus() throws IOException, GeneralSecurityException {
        Pair<ResultCode, ReadObjRspPayload> eventStatusResult = ei4Umi.getUmiSession().readObject(UMI_CODE_EVENT_STATUS);
        if (!eventStatusResult.getFirst().equals(ResultCode.OK)) {
            getProtocol().journal(Level.WARNING, "Read logbook status operation failed." + eventStatusResult.getFirst().getDescription());
            throw new ProtocolException("Error while reading Umiwan event status (" + UMI_CODE_EVENT_STATUS.getCode() + "): " + eventStatusResult.getFirst().getDescription());
        }
        EI4UmiwanEventStatus status = new EI4UmiwanEventStatus(eventStatusResult.getLast().getValue());
        getProtocol().journal(Level.INFO, "Event status start time: " + status.getStartTime());
        getProtocol().journal(Level.INFO, "Event status number of events: " + status.getNumberOfEntries());
        return status;
    }
}
