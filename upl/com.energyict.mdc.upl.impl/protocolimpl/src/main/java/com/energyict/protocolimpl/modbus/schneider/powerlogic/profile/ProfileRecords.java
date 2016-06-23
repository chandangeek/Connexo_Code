package com.energyict.protocolimpl.modbus.schneider.powerlogic.profile;

import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;

import java.util.ArrayList;
import java.util.List;


public class ProfileRecords {
    private static final int NO_OBJECTS = 11;
    private static final int objectSize[] = {8, 8, 8, 4, 4, 4, 4, 8, 8, 8, 8};
    List<ProfileRecord> profileRecords;

    public ProfileRecords() {
    }

    public static ProfileRecords parse(byte[] values, ProfileHeader profileHeader, int offset) {
        ProfileRecords profileRecords = new ProfileRecords();
        int ptr = offset;
        List lstValues = new ArrayList();
        for (int i = 0; i < NO_OBJECTS - 1; i++){
            try {
                lstValues.add(ProtocolUtils.getLong(ProtocolUtils.getSubArray2(values,offset,objectSize[i]), 0, objectSize[i]));
                offset+= objectSize[i];
            } catch (ProtocolException e) {
                e.printStackTrace();
            }

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
