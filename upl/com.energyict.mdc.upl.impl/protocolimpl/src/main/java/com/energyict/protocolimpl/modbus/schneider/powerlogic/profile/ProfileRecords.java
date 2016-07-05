package com.energyict.protocolimpl.modbus.schneider.powerlogic.profile;

import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ProfileRecords {
    private static final int NO_OBJECTS = 11;
    private static final int objectSize[] = {8, 8, 8, 4, 4, 4, 4, 8, 8, 8, 8};
    List<ProfileRecord> profileRecords;

    public ProfileRecords() {
    }

    public static ProfileRecords parse(byte[] values) throws ProtocolException {
        ProfileRecords profileRecords = new ProfileRecords();
        int offset = 0;
        List lstValues = new ArrayList();
        byte dateArray[] = ProtocolUtils.getSubArray2(values,0,objectSize[0]);
        int year = 2000 + dateArray[1] - 1900;
        int month = dateArray[2] - 1;
        int day = dateArray[3];
        int hours = dateArray[4];
        int minutes = dateArray[5];
        lstValues.add(new Date(year, month,day, hours, minutes));
        for (int i = 1; i < NO_OBJECTS; i++){
            lstValues.add(ProtocolUtils.getLong(ProtocolUtils.getSubArray2(values,offset,objectSize[i]), 0, objectSize[i]));
            offset += objectSize[i];
        }
        ProfileRecord profileRecord = ProfileRecord.parse(lstValues);
        profileRecords.getProfileRecords().add(profileRecord);
        return profileRecords;
    }

    public List<ProfileRecord> getProfileRecords() {
        if (this.profileRecords == null) {
            this.profileRecords = new ArrayList<>();
        }
        return profileRecords;
    }
}
