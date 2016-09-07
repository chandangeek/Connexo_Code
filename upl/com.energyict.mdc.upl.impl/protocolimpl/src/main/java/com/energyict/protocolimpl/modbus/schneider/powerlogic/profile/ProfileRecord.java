package com.energyict.protocolimpl.modbus.schneider.powerlogic.profile;

import java.util.Date;
import java.util.List;


public class ProfileRecord {

    private Date date;
    private List<Number> values;

    public ProfileRecord() {
    }

    public static ProfileRecord parse(Date intervalDate, List<Number> values) {
        ProfileRecord profileRecord = new ProfileRecord();
        profileRecord.setDate(intervalDate);
        profileRecord.setValues(values);
        return profileRecord;
    }

    public Date getDate() {
        return date;
    }

    protected void setDate(Date date) {
        this.date = date;
    }

    protected void setValues(List<Number> values) {
        this.values = values;
    }

    public List getValues() {
        return values;
    }
}