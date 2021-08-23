package com.energyict.protocolimplv2.umi.ei4.structures;

import com.energyict.protocolimplv2.umi.types.UmiCode;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.util.Date;

public class UmiwanMetrologyClock extends LittleEndianData {
    public static final int SIZE = 12;
    public static final UmiCode METROLOGY_CLOCK_UMI_CODE = new UmiCode("umi.1.1.194.31");

    private Date currentDateTime;  // 4
    private Date lastSync;         // 4
    private int localTimeAdjust;   // 2
    private int statusFlags;       // 2


    public UmiwanMetrologyClock(byte[] raw) {
        super(raw, SIZE, false);
        currentDateTime = UmiHelper.convertToDateFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));
        lastSync = UmiHelper.convertToDateFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));
        localTimeAdjust = Short.toUnsignedInt(getRawBuffer().getShort());
        statusFlags = Short.toUnsignedInt(getRawBuffer().getShort());
    }

    /**
     * Constructor for testing purposes
     */
    public UmiwanMetrologyClock(Date currentDateTime, Date lastSync, int localTimeAdjust, int statusFlags) {
        super(SIZE);
        this.currentDateTime = currentDateTime;
        this.lastSync = lastSync;
        this.localTimeAdjust = localTimeAdjust;
        this.statusFlags = statusFlags;


        getRawBuffer().putInt((int)UmiHelper.convertToUmiFormatFromDate(this.currentDateTime))
                .putInt((int)UmiHelper.convertToUmiFormatFromDate(this.lastSync))
                .putShort((short) this.localTimeAdjust)
                .putShort((short) this.statusFlags);

    }

    public Date getCurrentDateTime() {
        return this.currentDateTime;
    }

    public Date getLastSync() {
        return this.lastSync;
    }

    public int getStatusFlags() {
        return this.statusFlags;
    }

    public int getLocalTimeAdjust() {
        return this.localTimeAdjust;
    }
}
