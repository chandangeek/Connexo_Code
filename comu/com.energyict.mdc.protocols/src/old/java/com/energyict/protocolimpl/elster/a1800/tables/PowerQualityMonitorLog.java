/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.elster.a1800.tables;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableFactory;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class PowerQualityMonitorLog extends AbstractTable {

    /**
     * Creates a new instance of AbstractTable
     */
    public PowerQualityMonitorLog(TableFactory tableFactory, boolean longFormat) {
        super(tableFactory, new TableIdentification(49, true));
        this.longFormat = longFormat;
    }

    private List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
    private boolean longFormat = false;
    private int order;
    private int overflow;
    private int list_type;
    private int sequenceNumber;
    private int validEntries;
    private int unreadEntries;
    private int pqm_status;
    private static final int ORDER_DESCENDING = 1;
    private static final int ORDER_ASCENDING = 0;
    private static final int STATUS_FAILURE = 1;
    private static final int STATUS_NORMAL = 0;

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getUnreadEntries() {
        return unreadEntries;
    }

    public void setUnreadEntries(int unreadEntries) {
        this.unreadEntries = unreadEntries;
    }

    public int getValidEntries() {
        return validEntries;
    }

    public void setValidEntries(int validEntries) {
        this.validEntries = validEntries;
    }

    public boolean isLongFormat() {
        return longFormat;
    }

    public void setLongFormat(boolean longFormat) {
        this.longFormat = longFormat;
    }

    public List<MeterEvent> getMeterEvents() {
        return meterEvents;
    }

    public void setMeterEvents(List<MeterEvent> meterEvents) {
        this.meterEvents = meterEvents;
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

        for (int counter = 0; counter < getSequenceNumber(); counter++) {

            meterEventTypeByte = data[offset++];
            eiServerEventCode = mapMeterStatusToEiServerCode(meterEventTypeByte);

            cal.set(Calendar.YEAR, 2000 + ((data[offset++]) & 0xFF));
            cal.set(Calendar.MONTH, ((data[offset++]) & 0xFF) - 1);
            cal.set(Calendar.DAY_OF_MONTH, (data[offset++]) & 0xFF);
            cal.set(Calendar.HOUR_OF_DAY, (data[offset++]) & 0xFF);
            cal.set(Calendar.MINUTE, (data[offset++]) & 0xFF);
            cal.set(Calendar.SECOND, (data[offset++]) & 0xFF);
            cal.set(Calendar.MILLISECOND, 0);

            value = 0;
            if (isLongFormat()) {
                if (eiServerEventCode != -1) {
                    value = 0xFFFFFFFF;                 //Means this test doesn't support the optional value.
                } else {
                    byte[] valueBytes = ProtocolTools.getSubArray(data, offset, offset + 4);
                    valueBytes = ProtocolTools.reverseByteArray(valueBytes);
                    value = ProtocolTools.getUnsignedIntFromBytes(valueBytes);
                }
                offset += 4;
            }

            if (isInvalidTimeStamp(cal)) {
                continue;                      //don't add an invalid event
            }

            MeterEvent meterEvent = new MeterEvent(cal.getTime(),
                    eiServerEventCode,
                    ((meterEventTypeByte & 0xFF) == 0xFF) ? 255 : (meterEventTypeByte & 0x7F),
                    getDescription(meterEventTypeByte) + ((!isLongFormat()) ? "" : (", optional value" + ((value == 0xFFFFFFFF) ? " not supported" : (": " + value)))));

            meterEvents.add(meterEvent);
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
            return "PQM log cleared";
        }

        int meterEventStatus = ((meterEventTypeByte & 0xFF) & 0x80) >> 7 ;
        int meterEventType = (meterEventTypeByte & 0xFF) & 0x7F;

        switch (meterEventType) {
            case 1:
                return (meterEventStatus == STATUS_FAILURE) ? "Start of low voltage, high voltage or voltage phase angle errors on any phase" : "End of low voltage, high voltage or voltage phase angle errors on any phase";
            case 2:
                return (meterEventStatus == STATUS_FAILURE) ? "Start of exceeding low voltage threshold" : "End of exceeding low voltage threshold";
            case 3:
                return (meterEventStatus == STATUS_FAILURE) ? "Start of exceeding high voltage threshold" : "End of exceeding high voltage threshold";
            case 4:
                return (meterEventStatus == STATUS_FAILURE) ? "Start of reverse power or bad power factor on any phase" : "End of reverse power or bad power factor on any phase";
            case 5:
                return (meterEventStatus == STATUS_FAILURE) ? "Start of low current on any phase" : "End of low current on any phase";
            case 6:
                return (meterEventStatus == STATUS_FAILURE) ? "Start of Power Factor test failure" : "End of Power Factor test failure";
            case 7:
                return (meterEventStatus == STATUS_FAILURE) ? "Start of Second harmonic current test failure" : "End of Second harmonic current test failure";
            case 8:
                return (meterEventStatus == STATUS_FAILURE) ? "Start of total harmonic distortion current test failure" : "End of total harmonic distortion current test failure";
            case 9:
                return (meterEventStatus == STATUS_FAILURE) ? "Start of total harmonic distortion voltage test failure" : "End of total harmonic distortion voltage test failure";
            case 10:
                return (meterEventStatus == STATUS_FAILURE) ? "Start of voltage imbalance" : "End of voltage imbalance";
            case 11:
                return (meterEventStatus == STATUS_FAILURE) ? "Start of current imbalance" : "End of current imbalance";
            case 12:
                return (meterEventStatus == STATUS_FAILURE) ? "Start of total demand distortion test failure" : "End of total demand distortion test failure";
            default:
                return (meterEventStatus == STATUS_FAILURE) ? "Start of unknown test failure" : "End of unknown test failure";
        }


    }

    private int mapMeterStatusToEiServerCode(int meterEventTypeByte) {

        if ((meterEventTypeByte & 0xFF) == 0xFF) {
            return MeterEvent.EVENT_LOG_CLEARED;
        }

        int meterEventStatus = ((meterEventTypeByte & 0xFF)  & 0x80) >> 7;
        int meterEventType = (meterEventTypeByte & 0xFF)  & 0x7F;

        switch (meterEventType) {
            case 1:
                return MeterEvent.OTHER;
            case 2:
                return MeterEvent.VOLTAGE_SAG;
            case 3:
                return MeterEvent.VOLTAGE_SWELL;
            case 4:
                return MeterEvent.REVERSE_RUN;
            case 5:
                return MeterEvent.OTHER;
            case 6:
                return (meterEventStatus == STATUS_FAILURE) ? MeterEvent.LIMITER_THRESHOLD_EXCEEDED : MeterEvent.LIMITER_THRESHOLD_OK;
            case 7:
                return MeterEvent.OTHER;
            case 8:
                return MeterEvent.OTHER;
            case 9:
                return MeterEvent.OTHER;
            case 10:
                return MeterEvent.OTHER;
            case 11:
                return MeterEvent.OTHER;
            case 12:
                return MeterEvent.OTHER;
            default:
                return -1;
        }
    }
}