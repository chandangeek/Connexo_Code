package com.energyict.protocolimplv2.umi.ei4.events;

import com.energyict.protocolimplv2.umi.ei4.structures.UmiHelper;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.util.Date;

public class EI4UmiwanEventControl extends LittleEndianData {
    public static final int SIZE = 12;

    /**
     * Start time of collecting interval events requested by headend
     **/
    private Date startTime;              // 4 bytes

    /**
     * Not in use in this product, set to 0
     **/
    private long controlFlags;           // 4

    /**
     * Not in use in this product
     **/
    private long acknowledgeFlags;           // 4

    public EI4UmiwanEventControl(byte[] rawData) {
        super(rawData, SIZE, false);
        this.startTime = UmiHelper.convertToDateFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));
        this.controlFlags = Integer.toUnsignedLong(getRawBuffer().getInt());
        this.acknowledgeFlags = Integer.toUnsignedLong(getRawBuffer().getInt());
    }

    public EI4UmiwanEventControl(Date startTime, long controlFlags, long acknowledgeFlags) {
        super(SIZE);
        this.startTime = startTime;
        this.controlFlags = controlFlags;
        this.acknowledgeFlags = acknowledgeFlags;

        getRawBuffer().putInt((int)UmiHelper.convertToUmiFormatFromDate(this.startTime))
                .putInt((int)this.controlFlags)
                .putInt((int)this.acknowledgeFlags);
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public long getControlFlags() {
        return this.controlFlags;
    }

    public long getAcknowledgeFlags() {
        return this.acknowledgeFlags;
    }
}
