package com.energyict.mdc.device.data.validation;


import java.time.Instant;

public class DeviceValidationKpiResults {

    public boolean allDataValidated;
    public long amountOfSuspects;
    public long channelSuspects;
    public long registerSuspects;
    public Instant lastSuspect;


    public DeviceValidationKpiResults(long amountOfSuspects, long channelSuspects, long registerSuspects, long allDataValidated, Instant lastSuspect) {
        this.amountOfSuspects = amountOfSuspects;
        this.channelSuspects = channelSuspects;
        this.registerSuspects = registerSuspects;
        this.allDataValidated = allDataValidated == 1;
        this.lastSuspect = lastSuspect;
    }

    public boolean isAllDataValidated() {
        return allDataValidated;
    }

    public void setAllDataValidated(boolean allDataValidated) {
        this.allDataValidated = allDataValidated;
    }

    public long getAmountOfSuspects() {
        return amountOfSuspects;
    }

    public void setAmountOfSuspects(long amountOfSuspects) {
        this.amountOfSuspects = amountOfSuspects;
    }

    public long getChannelSuspects() {
        return channelSuspects;
    }

    public void setChannelSuspects(long channelSuspects) {
        this.channelSuspects = channelSuspects;
    }

    public long getRegisterSuspects() {
        return registerSuspects;
    }

    public void setRegisterSuspects(long registerSuspects) {
        this.registerSuspects = registerSuspects;
    }

    public Instant getLastSuspect() {
        return lastSuspect;
    }

    public void setLastSuspect(Instant lastSuspect) {
        this.lastSuspect = lastSuspect;
    }
}
