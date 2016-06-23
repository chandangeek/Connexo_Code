package com.energyict.protocolimpl.modbus.schneider.powerlogic.profile;

import java.util.Date;
import java.util.List;


public class ProfileRecord {

    private static long EPOCH_FIRST_JAN_2000 = 946684800;

    private Date date;
    private List values;
    private boolean incompleteIntegrationPeriod;

    public ProfileRecord() {
    }

    public static ProfileRecord parse(List values) {
        ProfileRecord profileRecord = new ProfileRecord();
        long secondsSinceFistJan2000 = ((long)values.get(0)) << 16 | ((long)values.get(0));
        profileRecord.setDate(new Date((EPOCH_FIRST_JAN_2000 + secondsSinceFistJan2000) * 1000));
        profileRecord.setIncompleteIntegrationPeriod(values.get(0) != 0);
        profileRecord.setValues(values.subList(1, values.size()));
        return profileRecord;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setValues(List values) {
        this.values = values;
    }

    public int getWordLength() {
        return 4;
    }

    public List getValues() {
        return values;
    }

    public void setIncompleteIntegrationPeriod(boolean incompleteIntegrationPeriod) {
        this.incompleteIntegrationPeriod = incompleteIntegrationPeriod;
    }

    public boolean isIncompleteIntegrationPeriod() {
        return incompleteIntegrationPeriod;
    }

}
