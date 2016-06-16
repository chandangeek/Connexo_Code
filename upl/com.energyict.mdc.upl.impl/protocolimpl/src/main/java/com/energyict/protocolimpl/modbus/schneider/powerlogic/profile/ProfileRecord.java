package com.energyict.protocolimpl.modbus.schneider.powerlogic.profile;

import java.util.Date;
import java.util.List;


public class ProfileRecord {

    private static long EPOCH_FIRST_JAN_2000 = 946684800;

    private Date date;
    private boolean incompleteIntegrationPeriod;
    private List values;
    private Object value;

    public ProfileRecord() {
    }

    public static ProfileRecord parse(List values) {
        ProfileRecord profileRecord = new ProfileRecord();
        Long secondsSinceFistJan2000 = (Long) values.get(0);
        profileRecord.setDate(new Date((EPOCH_FIRST_JAN_2000 + secondsSinceFistJan2000.longValue()) * 1000));
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
}
