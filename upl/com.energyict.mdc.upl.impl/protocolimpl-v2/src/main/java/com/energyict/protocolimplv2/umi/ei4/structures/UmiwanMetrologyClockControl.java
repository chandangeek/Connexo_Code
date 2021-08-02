package com.energyict.protocolimplv2.umi.ei4.structures;

import com.energyict.protocolimplv2.umi.types.UmiCode;
import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.util.Date;

public class UmiwanMetrologyClockControl extends LittleEndianData {
    public static final int SIZE = 8;
    public static final UmiCode UMIWAN_METROLOGY_CLOCK_CONTROL = new UmiCode("umi.1.1.194.32");

    private Date newDateTime;              // 4 bytes
    private int controlFlags;              // 2 bytes
    private int localTimeAdjustment;       // 2 bytes

    public UmiwanMetrologyClockControl(byte[] rawData) {
        super(rawData, SIZE, false);
        this.newDateTime = UmiHelper.convertToDateFromUmiFormat(Integer.toUnsignedLong(getRawBuffer().getInt()));
        this.controlFlags = Short.toUnsignedInt(getRawBuffer().getShort());
        this.localTimeAdjustment = Short.toUnsignedInt(getRawBuffer().getShort());
    }

    public UmiwanMetrologyClockControl(Date newDateTime, int controlFlags, int localTimeAdjustment) {
        super(SIZE);
        this.newDateTime = newDateTime;
        this.controlFlags = controlFlags;
        this.localTimeAdjustment = localTimeAdjustment;

        getRawBuffer().putInt((int)UmiHelper.convertToUmiFormatFromDate(this.newDateTime))
                .putShort((short) this.controlFlags)
                .putShort((short) this.localTimeAdjustment);
    }

    public Date getNewDateTime() {
        return this.newDateTime;
    }

    public int getControlFlags() {
        return this.controlFlags;
    }

    public int getLocalTimeAdjustment() {
        return this.localTimeAdjustment;
    }
}
