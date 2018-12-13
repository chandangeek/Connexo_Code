package com.energyict.protocolimpl.modbus.socomec.countis.e44.profile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 9/10/2014 - 11:00
 */
public class ProfileRecords {

    List<ProfileRecord> profileRecords;

    public ProfileRecords() {
    }

    public static ProfileRecords parse(int[] values, ProfileHeader profileHeader, int offset) {
        ProfileRecords profileRecords = new ProfileRecords();
        int ptr = offset;
        for (int i = 0; i < profileHeader.getRecordCount(); i++) {
            ProfileRecord profileRecord = ProfileRecord.parse(values, ptr);
            profileRecords.getProfileRecords().add(profileRecord);
            ptr += profileRecord.getWordLength();

        }
        return profileRecords;
    }

    public List<ProfileRecord> getProfileRecords() {
        if (this.profileRecords == null) {
            this.profileRecords = new ArrayList<ProfileRecord>();
        }
        return profileRecords;
    }
}
