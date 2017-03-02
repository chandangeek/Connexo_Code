package com.elster.jupiter.metering.imports.impl.usagepoint;

public class MeterRoleWithMeterAndActivationDate {

    public String meterRole;
    public String meter;
    public String activationDate;

    public MeterRoleWithMeterAndActivationDate() {
    }

    public String getMeterRole() {
        return meterRole;
    }

    public void setMeterRole(String meterRole) {
        this.meterRole = meterRole;
    }

    public String getMeter() {
        return meter;
    }

    public void setMeter(String meter) {
        this.meter = meter;
    }

    public String getActivation() {
        return activationDate;
    }

    public void setActivationDate(String activationDate) {
        this.activationDate = activationDate;
    }
}
