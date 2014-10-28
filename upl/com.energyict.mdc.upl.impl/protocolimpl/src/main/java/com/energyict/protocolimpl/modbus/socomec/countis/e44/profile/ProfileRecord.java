package com.energyict.protocolimpl.modbus.socomec.countis.e44.profile;

import java.util.Date;

/**
 * @author sva
 * @since 9/10/2014 - 11:01
 */
public class ProfileRecord {

    private static long EPOCH_FIRST_JAN_2000 = 946684800;

    private Date date;
    private boolean incompleteIntegrationPeriod;
    private long value;

    public ProfileRecord() {
    }

    public static ProfileRecord parse(int[] values, int offset) {
        int ptr = offset;
        ProfileRecord profileRecord = new ProfileRecord();
        long secondsSinceFistJan2000 = values[ptr++] <<16 | values[ptr++];
        profileRecord.setDate(new Date((EPOCH_FIRST_JAN_2000 + secondsSinceFistJan2000) * 1000));
        profileRecord.setIncompleteIntegrationPeriod(values[ptr++] != 0);
        profileRecord.setValue(values[ptr++]);
        return profileRecord;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isIncompleteIntegrationPeriod() {
        return incompleteIntegrationPeriod;
    }

    public void setIncompleteIntegrationPeriod(boolean incompleteIntegrationPeriod) {
        this.incompleteIntegrationPeriod = incompleteIntegrationPeriod;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public int getWordLength() {
        return 4;
    }
}
