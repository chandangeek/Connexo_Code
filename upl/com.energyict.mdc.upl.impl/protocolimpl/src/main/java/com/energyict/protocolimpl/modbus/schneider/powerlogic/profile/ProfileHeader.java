package com.energyict.protocolimpl.modbus.schneider.powerlogic.profile;

import java.util.List;

public class ProfileHeader {

    private int recordCount;
    private int integrationPeriod;
    private Object numeratorRate;
    private int denominatorRate;

    public ProfileHeader() {
    }

    public static ProfileHeader parse(List values) {
        ProfileHeader profileHeader = new ProfileHeader();
        profileHeader.setRecordCount(values.size());

        return profileHeader;
    }

    public int getWordLength() {
        return 6;
    }

   public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public int getIntegrationPeriod() {
        return integrationPeriod;
    }

    public void setIntegrationPeriod(int integrationPeriod) {
        this.integrationPeriod = integrationPeriod;
    }

    public Object getNumeratorRate() {
        return numeratorRate;
    }

    public int getDenominatorRate() {
        return denominatorRate;
    }
}
