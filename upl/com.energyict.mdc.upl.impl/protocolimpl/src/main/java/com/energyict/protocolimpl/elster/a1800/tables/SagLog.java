package com.energyict.protocolimpl.elster.a1800.tables;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableFactory;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class SagLog extends AbstractTable {

    private int order;
    private int overflow;
    private int list_type;
    private int sequenceNumber;
    private int validEntries;
    private int unreadEntries;

    private List<MeterEvent> meterEvents = new ArrayList<>();

    public SagLog(TableFactory tableFactory, TableIdentification tableIdentification) {
        super(tableFactory, tableIdentification);
    }

    public List<MeterEvent> getMeterEvents() {
        return meterEvents;
    }

    @Override
    public void parse(byte[] data) throws IOException {
        int offset = 0;
        Calendar cal = new GregorianCalendar();
        int eiServerEventCode;
        byte meterEventTypeByte;
        int value;

        byte status = data[offset++];
        order = status & 0x01;
        overflow = status & 0x02 >> 1;
        list_type = status & 0x04 >> 2;

        byte[] sequenceNumberBytes = ProtocolTools.getSubArray(data, offset, offset + 4);
        sequenceNumberBytes = ProtocolTools.reverseByteArray(sequenceNumberBytes);
        sequenceNumber = ProtocolTools.getUnsignedIntFromBytes(sequenceNumberBytes);
        offset += 4;

        byte[] validEntriesBytes = ProtocolTools.getSubArray(data, offset, offset + 2);
        validEntriesBytes = ProtocolTools.reverseByteArray(validEntriesBytes);
        validEntries = ProtocolTools.getUnsignedIntFromBytes(validEntriesBytes);
        offset += 2;

        byte[] unreadEntriesBytes = ProtocolTools.getSubArray(data, offset, offset + 2);
        unreadEntriesBytes = ProtocolTools.reverseByteArray(unreadEntriesBytes);
        unreadEntries = ProtocolTools.getUnsignedIntFromBytes(unreadEntriesBytes);
        offset += 2;

        for (int counter = 0; counter < validEntries; counter++) {

            meterEventTypeByte = data[offset++];
            eiServerEventCode = mapMeterStatusToEiServerCode(meterEventTypeByte);

            cal.set(Calendar.YEAR, 2000 + ((data[offset++]) & 0xFF));
            cal.set(Calendar.MONTH, ((data[offset++]) & 0xFF) - 1);
            cal.set(Calendar.DAY_OF_MONTH, (data[offset++]) & 0xFF);
            cal.set(Calendar.HOUR_OF_DAY, (data[offset++]) & 0xFF);
            cal.set(Calendar.MINUTE, (data[offset++]) & 0xFF);
            cal.set(Calendar.SECOND, (data[offset++]) & 0xFF);
            cal.set(Calendar.MILLISECOND, 0);

            if (isInvalidTimeStamp(cal)) {
                continue; // don't add an invalid event
            }

            MeterEvent meterEvent = new MeterEvent(cal.getTime(),
                    eiServerEventCode,
                    ((meterEventTypeByte & 0xFF) == 0xFF) ? 255 : (meterEventTypeByte & 0x7F),
                    getDescription(meterEventTypeByte));

            meterEvents.add(meterEvent);
        }
    }

    private int mapMeterStatusToEiServerCode(int meterEventTypeByte) {

        if ((meterEventTypeByte & 0xFF) == 0xFF) {
            return MeterEvent.SAG_EVENT_CLEARED;
        }

        int meterEventType = (meterEventTypeByte & 0xFF)  & 0x7F;

        switch (meterEventType) {
            case 1:
                return MeterEvent.SAG_PHASE_A;
            case 2:
                return MeterEvent.SAG_PHASE_B;
            case 3:
                return MeterEvent.SAG_PHASE_A_B;
            case 4:
                return MeterEvent.SAG_PHASE_C;
            case 5:
                return MeterEvent.SAG_PHASE_A_C;
            case 6:
                return MeterEvent.SAG_PHASE_B_C;
            case 7:
                return MeterEvent.SAG_PHASE_A_B_C;
            default:
                return -1;
        }
    }

    /**
     * Checks if the timestamp contains a valid date (in the past, future dates are not allowed).
     * Dates in the future can be caused by parsing 0xFF bytes.
     *
     * @param cal = the timestamp
     * @return boolean
     */
    private boolean isInvalidTimeStamp(Calendar cal) {
        return cal.after(new GregorianCalendar());
    }

    private String getDescription(int meterEventTypeByte) {

        if ((meterEventTypeByte & 0xFF) == 0xFF) {
            return "SAG log cleared";
        }

        int meterEventType = (meterEventTypeByte & 0xFF) & 0x7F;

        switch (meterEventType) {
            case 1:
                return "SAG event on Phase A";
            case 2:
                return "SAG event on Phase B";
            case 3:
                return "SAG event on Phase A+B";
            case 4:
                return "SAG event on Phase C";
            case 5:
                return "SAG event on Phase A+C";
            case 6:
                return "SAG event on Phase B+C";
            case 7:
                return "SAG event on Phase A+B+C";
            default:
                return "Unknown sag log";
        }
    }
}
