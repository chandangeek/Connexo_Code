package com.energyict.protocolimplv2.abnt.common.structure;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.frame.ResponseFrame;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;
import com.energyict.protocolimplv2.abnt.common.structure.field.HistoryLogRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class HistoryLogResponse extends Data<HistoryLogResponse> {

    private static final int EVENT_LOG_SIZE = 16;
    private static final int EXTENDED_EVENT_LOG_SIZE = 9;
    private static final String INVALID_DATE_ENTRY = "000000000000";

    private List<HistoryLogRecord> eventLog;
    private List<HistoryLogRecord> extendedEventLog;

    public HistoryLogResponse(TimeZone timeZone) {
        super(ResponseFrame.RESPONSE_DATA_LENGTH, timeZone);
        this.eventLog = new ArrayList<>(EVENT_LOG_SIZE);
        this.extendedEventLog = new ArrayList<>(EXTENDED_EVENT_LOG_SIZE);
    }

    @Override
    public HistoryLogResponse parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;
        super.parse(rawData, ptr);

        for (int i = 0; i < EVENT_LOG_SIZE; i++) {
            HistoryLogRecord historyLogRecord = new HistoryLogRecord(getTimeZone()).parse(rawData, ptr);
            if (!historyLogRecord.getEventDate().getBcdEncodedDate().getText().equals(INVALID_DATE_ENTRY)) { // Only add valid entries
                eventLog.add(historyLogRecord);
            }
            ptr += historyLogRecord.getLength();
        }

        for (int i = 0; i < EXTENDED_EVENT_LOG_SIZE; i++) {
            HistoryLogRecord historyLogRecord = new HistoryLogRecord(getTimeZone()).parse(rawData, ptr);
            if (!historyLogRecord.getEventDate().getBcdEncodedDate().getText().equals(INVALID_DATE_ENTRY)) { // Only add valid entries
                extendedEventLog.add(historyLogRecord);
            }
            ptr += historyLogRecord.getLength();
        }
        return this;
    }

    public List<HistoryLogRecord> getEventLog() {
        return eventLog;
    }

    public List<HistoryLogRecord> getExtendedEventLog() {
        return extendedEventLog;
    }
}