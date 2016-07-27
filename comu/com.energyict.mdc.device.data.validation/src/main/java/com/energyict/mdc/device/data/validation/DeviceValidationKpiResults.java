package com.energyict.mdc.device.data.validation;


import java.time.Instant;

public class DeviceValidationKpiResults {

    public boolean allDataValidated;
    public long amountOfSuspects;
    public long channelSuspects;
    public long registerSuspects;
    public Instant lastSuspect;
    public boolean thresholdValidator;
    public boolean missingValuesValidator;
    public boolean readingQualitiesValidator;
    public boolean registerIncreaseValidator;

    public DeviceValidationKpiResults(long amountOfSuspects, long channelSuspects, long registerSuspects, long allDataValidated, Instant lastSuspect,
           long thresholdValidator, long missingValuesValidator, long readingQualitiesValidator, long registerIncreaseValidator) {
        this.amountOfSuspects = amountOfSuspects;
        this.channelSuspects = channelSuspects;
        this.registerSuspects = registerSuspects;
        this.allDataValidated = allDataValidated == 1;
        this.lastSuspect = lastSuspect;
        this.thresholdValidator = thresholdValidator == 1;
        this.missingValuesValidator = missingValuesValidator == 1;
        this.readingQualitiesValidator = readingQualitiesValidator == 1;
        this.registerIncreaseValidator = registerIncreaseValidator == 1;
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

    public boolean isThresholdValidator() {
        return thresholdValidator;
    }

    public void setThresholdValidator(boolean thresholdValidator) {
        this.thresholdValidator = thresholdValidator;
    }

    public boolean isMissingValuesValidator() {
        return missingValuesValidator;
    }

    public void setMissingValuesValidator(boolean missingValuesValidator) {
        this.missingValuesValidator = missingValuesValidator;
    }

    public boolean isReadingQualitiesValidator() {
        return readingQualitiesValidator;
    }

    public void setReadingQualitiesValidator(boolean readingQualitiesValidator) {
        this.readingQualitiesValidator = readingQualitiesValidator;
    }

    public boolean isRegisterIncreaseValidator() {
        return registerIncreaseValidator;
    }

    public void setRegisterIncreaseValidator(boolean registerIncreaseValidator) {
        this.registerIncreaseValidator = registerIncreaseValidator;
    }
}
