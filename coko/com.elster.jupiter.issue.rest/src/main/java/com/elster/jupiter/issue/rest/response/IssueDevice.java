package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.metering.EndDevice;

public class IssueDevice {
    private long id;
    private String sNumber;
    private long version;

    public IssueDevice(EndDevice endDevice){
        if (endDevice != null) {
            this.setId(endDevice.getId());
            this.setsNumber(endDevice.getSerialNumber());
            this.setVersion(endDevice.getVersion());
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getsNumber() {
        return sNumber;
    }

    public void setsNumber(String sNumber) {
        this.sNumber = sNumber;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
