package com.elster.jupiter.validation;

public interface MeterValidation {

    boolean getActivationStatus();

    void setActivationStatus(boolean status);

    void save();
}
