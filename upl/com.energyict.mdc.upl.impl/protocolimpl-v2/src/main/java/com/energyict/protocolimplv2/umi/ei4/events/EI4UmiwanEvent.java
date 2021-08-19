package com.energyict.protocolimplv2.umi.ei4.events;

import com.energyict.protocolimplv2.umi.ei4.structures.UmiHelper;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EI4UmiwanEvent extends LittleEndianData {
    public static final int SIZE = 16;

    private int eventType;           // 4
    private Date timestamp;          // 4
    private byte[] associatedData;   // 8

    public EI4UmiwanEvent(byte[] raw) {
        super(raw, SIZE, false);
        this.eventType = getRawBuffer().getInt();
        this.timestamp = UmiHelper.convertToDateFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));
        this.associatedData = new byte[8];
        getRawBuffer().get(this.associatedData);
    }

    /**
     * Constructor for testing purposes
     */
    public EI4UmiwanEvent(int eventType, Date timestamp, byte[] associatedData) {
        super(SIZE);
        long timestampSeconds = TimeUnit.MILLISECONDS.toSeconds(timestamp.getTime());
        Date dateTimestamp = new Date(TimeUnit.SECONDS.toMillis(timestampSeconds));

        this.eventType = eventType;
        this.timestamp = dateTimestamp;
        this.associatedData = associatedData;

        getRawBuffer().putInt((int) this.eventType).putInt((int)UmiHelper.convertToUmiFormatFromDate(this.timestamp))
                .put(this.associatedData);
    }

    public Integer getEventType() {
        return eventType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public byte[] getAssociatedData() {
        return associatedData;
    }
}
