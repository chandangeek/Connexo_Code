package com.energyict.protocolimpl.modbus.schneider.powerlogic.profile;

import java.util.Date;
import java.util.List;


public class ProfileRecord {
    private Date date;
    private List values;
    private boolean incompleteIntegrationPeriod;

    public ProfileRecord() {
    }

    public static ProfileRecord parse(List values) {
        ProfileRecord profileRecord = new ProfileRecord();
        profileRecord.setDate((Date) values.get(0));
        profileRecord.setIncompleteIntegrationPeriod(values.get(0).equals(0));
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
