package com.energyict.protocolimpl.modbus.schneider.powerlogic.profile;


public class ProfileHeader {

    private int recordCount;
    private int recordSize;
    private int integrationPeriod = 5;

    public ProfileHeader() {
    }

    public static ProfileHeader parse(int recordCount) {
        ProfileHeader profileHeader = new ProfileHeader();
        profileHeader.setRecordCount(recordCount);

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

    public int getRecordSize() {
        return recordSize;
    }

    public void setRecordSize(int recordSize) {
        this.recordSize = recordSize;
    }


    public void setIntegrationPeriod(int integrationPeriod) {
        this.integrationPeriod = integrationPeriod;
    }

    public int getIntegrationPeriod() {
        return integrationPeriod;
    }
}

