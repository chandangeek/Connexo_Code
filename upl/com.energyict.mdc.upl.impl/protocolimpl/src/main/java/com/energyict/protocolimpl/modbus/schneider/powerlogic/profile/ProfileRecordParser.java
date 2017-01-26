package com.energyict.protocolimpl.modbus.schneider.powerlogic.profile;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.modbus.schneider.powerlogic.DateTime;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ProfileRecordParser {

    private static final int HEADER_LENGTH = 4;
    private static final int DATE_TIME_LENGTH = 8;

    public ProfileRecordParser() {
    }

    public static ProfileRecord parse(byte[] values, List<ChannelConfigMapping.DataType> loadProfileRecordItemsDataTypes) throws ProtocolException {
        int offset = HEADER_LENGTH; // First 4 bytes are header (which we don't use/need)
        List<Number> lstValues = new ArrayList<>();
        Date intervalTime = new DateTime().parseProfileEntryDateTime(values, offset).getMeterCalender().getTime();
        offset += DATE_TIME_LENGTH;

        for (ChannelConfigMapping.DataType dataType : loadProfileRecordItemsDataTypes) {
            if (dataType.equals(ChannelConfigMapping.DataType.INT64)) {
                lstValues.add(ProtocolUtils.getLong(values, offset, 8));
                offset += 8;
            } else if (dataType.equals(ChannelConfigMapping.DataType.FLOAT32)) {
                Float amount = getFloat32(values, offset);
                lstValues.add(amount.isInfinite() ? 0 : amount);    // If the amount is 'infinite', then add 0
                offset += 4;
            }
        }
        return ProfileRecord.parse(intervalTime, lstValues);
    }

    public static float getFloat32(byte[] byteBuffer, int iOffset) {
        int signBit = (((int) byteBuffer[iOffset] >> 7) & 0xFF);
        int exponent = (((((int) byteBuffer[iOffset]) << 1) & 0xFF) |
                ((((int) byteBuffer[iOffset + 1]) >> 7) & 0x01));

        int fractionDigits = ((((int) byteBuffer[iOffset + 1] << 16) & 0x7F0000) |
                (((int) byteBuffer[iOffset + 2] << 8) & 0x00FF00) |
                (((int) byteBuffer[iOffset + 3]) & 0x0000FF));
        String fractionPart = Integer.toBinaryString(fractionDigits);

        // fractionPart should be length 23 -- the leading 0's should be present in the string!
        while (fractionPart.length() < 23) {
            fractionPart = "0" + fractionPart;
        }

        float fraction = 1;
        for (int i = 0; i < 23; i++) {
            fraction += Integer.parseInt(fractionPart.substring(i, i + 1)) * Math.pow(2, -1 - i);
        }
        return (exponent > 0) ? ((float) ((Math.pow(-1, signBit)) * Math.pow(2, exponent - 127)) * fraction) : 0;
    }
}