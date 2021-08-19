package com.energyict.protocolimplv2.umi.ei4.events;

import com.energyict.protocolimplv2.umi.ei4.structures.UmiHelper;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.util.Date;

public class EI4UmiwanEventStatus extends LittleEndianData {
    public static final int SIZE = 24;

    private Date startTime;           // 4
    private Date lastExecTime;        // 4
    private int numberOfEntries;    // 2
    private int formatCode;         // 2
    private Date firstTimeStamp;      // 4
    private Date mostRecentTimeStamp; // 4
    private long activeEvents;         // 4

    public EI4UmiwanEventStatus(byte[] payload) {
        super(payload);
        this.startTime = UmiHelper.convertToDateFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));
        this.lastExecTime = UmiHelper.convertToDateFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));
        this.numberOfEntries = Short.toUnsignedInt(getRawBuffer().getShort());
        this.formatCode = Short.toUnsignedInt(getRawBuffer().getShort());
        this.firstTimeStamp = UmiHelper.convertToDateFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));
        this.mostRecentTimeStamp = UmiHelper.convertToDateFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));
        this.activeEvents = Integer.toUnsignedLong(getRawBuffer().getInt());
    }

    /**
     * Constructor for testing purposes
     */
    public EI4UmiwanEventStatus(Date startTime, Date lastExecTime, int numberOfEntries, int formatCode,
                                Date firstTimeStamp, Date mostRecentTimeStamp, long activeEvents) {
        super(SIZE);
        this.startTime = startTime;
        this.lastExecTime = lastExecTime;
        this.numberOfEntries = numberOfEntries;
        this.formatCode = formatCode;
        this.firstTimeStamp = firstTimeStamp;
        this.mostRecentTimeStamp = mostRecentTimeStamp;
        this.activeEvents = activeEvents;

        getRawBuffer().putInt((int)UmiHelper.convertToUmiFormatFromDate(this.startTime))
                .putInt((int)UmiHelper.convertToUmiFormatFromDate(this.lastExecTime))
                .putShort((short)this.numberOfEntries)
                .putShort((short)this.formatCode)
                .putInt((int) UmiHelper.convertToUmiFormatFromDate(this.firstTimeStamp))
                .putInt((int) UmiHelper.convertToUmiFormatFromDate(this.mostRecentTimeStamp))
                .putInt((int)this.activeEvents);
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public Date getLastExecTime() {
        return this.lastExecTime;
    }

    public int getNumberOfEntries() {
        return this.numberOfEntries;
    }

    public int getFormatCode() {
        return this.formatCode;
    }

    public Date getFirstTimeStamp() {
        return this.firstTimeStamp;
    }

    public Date getMostRecentTimeStamp() {
        return this.mostRecentTimeStamp;
    }

    public long getActiveEvents() {
        return this.activeEvents;
    }
}
