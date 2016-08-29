package com.energyict.protocolimpl.modbus.schneider.powerlogic.profile;

import com.energyict.dlms.DataContainerException;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.modbus.schneider.powerlogic.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class ProfileRecords {
    private static final int NO_OBJECTS = 11;
    private static final int objectSize[] = {8, 8, 8, 4, 4, 4, 4, 8, 8, 8, 8};
    public static final String LONG = "Long";
    public static final String FLOAT32 = "Float32";
    public static final String NOT_AVAILABLE = "Not Available";
    private static final String objectType[] = {LONG, LONG, LONG, FLOAT32, NOT_AVAILABLE, NOT_AVAILABLE, FLOAT32, LONG, LONG, LONG, LONG};
    List<ProfileRecord> profileRecords;

    public ProfileRecords() {
    }

    public static ProfileRecords parse(byte[] values, TimeZone timezone) throws ProtocolException {
        ProfileRecords profileRecords = new ProfileRecords();
        int offset = 0;
        List lstValues = new ArrayList();
        byte dateArray[] = ProtocolUtils.getSubArray2(values,0,objectSize[0]);
        int year = 2000 + dateArray[1];
        int month = dateArray[2];
        int day = dateArray[3];
        int hours = dateArray[4];
        int minutes = dateArray[5];
        int milliseconds = Integer.parseInt(new String(dateArray[6] + dateArray[7] + ""));
        lstValues.add(DateTime.parseDateTime(year, month, day, hours, minutes, milliseconds, timezone).getMeterCalender().getTime());
        offset += objectSize[0];
        for (int i = 1; i < NO_OBJECTS; i++){
            if(objectType[i].equals(LONG)) {
                lstValues.add(ProtocolUtils.getLong(ProtocolUtils.getSubArray2(values, offset, objectSize[i]), 0, objectSize[i]));
            }else if(objectType[i].equals(FLOAT32)) {
                try {
                    lstValues.add(getFloat32(ProtocolUtils.getSubArray2(values, offset, objectSize[i]),0));
                } catch (DataContainerException e) {
                    throw new ProtocolException(e);
                }
            }
            offset += objectSize[i];
        }
        ProfileRecord profileRecord = ProfileRecord.parse(lstValues);
        profileRecords.getProfileRecords().add(profileRecord);
        return profileRecords;
    }

    public static float getFloat32(byte[] byteBuffer, int iOffset) throws DataContainerException {
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
    public List<ProfileRecord> getProfileRecords() {
        if (this.profileRecords == null) {
            this.profileRecords = new ArrayList<>();
        }
        return profileRecords;
    }
}
