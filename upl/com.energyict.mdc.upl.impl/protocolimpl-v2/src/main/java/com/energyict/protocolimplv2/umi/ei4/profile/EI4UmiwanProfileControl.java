package com.energyict.protocolimplv2.umi.ei4.profile;

import com.energyict.protocolimplv2.umi.ei4.structures.UmiHelper;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.util.Date;

/**
 * Control Object for headend access
 **/
public class EI4UmiwanProfileControl extends LittleEndianData {
    public static final int SIZE = 8;

    /**
     * Start time of collecting interval data requested by headend
     **/
    private Date startTime;              // 4 bytes

    /**
     * Not in use in this product
     **/
    private long controlFlags;           // 4 bytes

    public EI4UmiwanProfileControl(byte[] rawData) {
        super(rawData, SIZE, false);
        this.startTime = UmiHelper.convertToDateFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));
        this.controlFlags = Integer.toUnsignedLong(getRawBuffer().getInt());
    }

    public EI4UmiwanProfileControl(Date startTime, long controlFlags) {
        super(SIZE);
        this.startTime = startTime;
        this.controlFlags = controlFlags;

        getRawBuffer().putInt((int)UmiHelper.convertToUmiFormatFromDate(this.startTime)).putInt((int)this.controlFlags);
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public long getControlFlags() {
        return this.controlFlags;
    }

}
