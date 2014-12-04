package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;
import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class DstConfigurationRecord extends AbstractField<DstConfigurationRecord> {

    public static final int LENGTH = 5;

    private DstEnablementStatus dstEnablementStatus;
    private BcdEncodedField dayOfStartOfDst;
    private BcdEncodedField monthOfStartOfDst;
    private BcdEncodedField dayOfEndOfDst;
    private BcdEncodedField monthOfEndOfDst;

    public DstConfigurationRecord() {
        this.dstEnablementStatus = new DstEnablementStatus();
        this.dayOfStartOfDst = new BcdEncodedField();
        this.monthOfStartOfDst = new BcdEncodedField();
        this.dayOfEndOfDst = new BcdEncodedField();
        this.monthOfEndOfDst = new BcdEncodedField();
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                dstEnablementStatus.getBytes(),
                dayOfStartOfDst.getBytes(),
                monthOfStartOfDst.getBytes(),
                dayOfEndOfDst.getBytes(),
                monthOfEndOfDst.getBytes()
        );
    }

    @Override
    public DstConfigurationRecord parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        dstEnablementStatus.parse(rawData, ptr);
        ptr += dstEnablementStatus.getLength();

        dayOfStartOfDst.parse(rawData, ptr);
        ptr += dayOfStartOfDst.getLength();

        monthOfStartOfDst.parse(rawData, ptr);
        ptr += monthOfStartOfDst.getLength();

        dayOfEndOfDst.parse(rawData, ptr);
        ptr += dayOfEndOfDst.getLength();

        monthOfEndOfDst.parse(rawData, ptr);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public DstEnablementStatus getDstEnablementStatus() {
        return dstEnablementStatus;
    }

    public void setDstEnablementStatus(DstEnablementStatus dstEnablementStatus) {
        this.dstEnablementStatus = dstEnablementStatus;
    }

    public BcdEncodedField getDayOfStartOfDst() {
        return dayOfStartOfDst;
    }

    public void setDayOfStartOfDst(BcdEncodedField dayOfStartOfDst) {
        this.dayOfStartOfDst = dayOfStartOfDst;
    }

    public BcdEncodedField getMonthOfStartOfDst() {
        return monthOfStartOfDst;
    }

    public void setMonthOfStartOfDst(BcdEncodedField monthOfStartOfDst) {
        this.monthOfStartOfDst = monthOfStartOfDst;
    }

    public BcdEncodedField getDayOfEndOfDst() {
        return dayOfEndOfDst;
    }

    public void setDayOfEndOfDst(BcdEncodedField dayOfEndOfDst) {
        this.dayOfEndOfDst = dayOfEndOfDst;
    }

    public BcdEncodedField getMonthOfEndOfDst() {
        return monthOfEndOfDst;
    }

    public void setMonthOfEndOfDst(BcdEncodedField monthOfEndOfDst) {
        this.monthOfEndOfDst = monthOfEndOfDst;
    }

    public String getDstConfigurationInfo() throws ParsingException {
        StringBuilder builder = new StringBuilder();
        builder.append(getDstEnablementStatus().getStatusMessage());
        if (getDstEnablementStatus().getStatus().equals(DstEnablementStatus.EnablementStatus.ENABLED)) {
            builder.append(" - Start of DST: ");
            builder.append(getDayOfStartOfDst().getValue());
            builder.append("/");
            builder.append(getMonthOfStartOfDst().getValue());
            builder.append(" - End of DST: ");
            builder.append(getDayOfEndOfDst().getValue());
            builder.append("/");
            builder.append(getMonthOfEndOfDst().getValue());

        }
        return builder.toString();
    }
}