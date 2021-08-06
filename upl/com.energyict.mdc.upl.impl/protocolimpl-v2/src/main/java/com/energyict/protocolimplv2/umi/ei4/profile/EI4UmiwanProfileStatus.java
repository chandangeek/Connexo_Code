package com.energyict.protocolimplv2.umi.ei4.profile;

import com.energyict.protocolimplv2.umi.ei4.structures.UmiHelper;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.util.Date;

public class EI4UmiwanProfileStatus  extends LittleEndianData {
    public static final int SIZE = 26;

    /**
     * Start time for collecting interval data
     **/
    private Date startTime;              // 4
    /**
     * Timestamp of last interval data collection
     **/
    private Date lastExecTime;           // 4
    /**
     * Number of interval data elements stored in UMIWAN_PROFILE_TABLE prepared for transmission
     **/
    private int numberOfEntries;         // 2
    /**
     * Timestamp of interval data element at index 0 of UMIWAN_PROFILE_TABLE
     **/
    private Date firstTimeStamp;         // 4
    /**
     * Volume information resolution: 1:dm^3, 2:10(dm)^3, 3:100dm^3, 4: m^3
     **/
    private int unitCode;                // 2
    /**
     * 1: no decimal points
     **/
    private int formatCode;              // 2
    /**
     * Elster Unique meter ID (meter serial number)
     * Unique meterID used by the headend.
     * The 'factory-id' and 'serialNumber' of the Host (UMI-object 1.1.0.0 (MANUFACTORING_ID) member 2, 3 (ASCII coded))
     * are combined. For technical reason '1000000000000' (decimal) is added.
     * It's coded here in a byte array with least significant byte first.
     * e.g.: host 'factory-id': '30 30 32 35' (ASCII coded),
     * host 'serialNumber': '33 31 37 33 33 39 37 38' (ASCII coded)
     * meterUID='1002531733978' = 'DA 41 8C 6B E9 00 00 00'
     **/
    private long meterUID;               // 8

    public EI4UmiwanProfileStatus(byte[] payload) {
        super(payload);
        this.startTime = UmiHelper.convertToDateFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));
        this.lastExecTime = UmiHelper.convertToDateFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));
        this.numberOfEntries = Short.toUnsignedInt(getRawBuffer().getShort());
        this.firstTimeStamp = UmiHelper.convertToDateFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));
        this.unitCode = Short.toUnsignedInt(getRawBuffer().getShort());
        this.formatCode = Short.toUnsignedInt(getRawBuffer().getShort());
        this.meterUID = getRawBuffer().getLong();
    }

    /**
     * Constructor for testing purposes
     */
    public EI4UmiwanProfileStatus(Date startTime, Date lastExecTime, int numberOfEntries, Date firstTimeStamp,
                                  int unitCode, int formatCode, long meterUID) {
        super(SIZE);
        this.startTime = startTime;
        this.lastExecTime = lastExecTime;
        this.numberOfEntries = numberOfEntries;

        this.firstTimeStamp = firstTimeStamp;
        this.unitCode = unitCode;
        this.formatCode = formatCode;
        this.meterUID = meterUID;

        getRawBuffer().putInt((int)UmiHelper.convertToUmiFormatFromDate(this.startTime))
                .putInt((int)UmiHelper.convertToUmiFormatFromDate(this.lastExecTime))
                .putShort((short)this.numberOfEntries)

                .putInt((int) UmiHelper.convertToUmiFormatFromDate(this.firstTimeStamp))
                .putShort((short)this.unitCode)
                .putShort((short)this.formatCode)
                .putLong(this.meterUID);
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

    public int getUnitCode() {
        return this.unitCode;
    }

    public Date getFirstTimeStamp() {
        return this.firstTimeStamp;
    }

    public long getMeterUID() {
        return this.meterUID;
    }
}
