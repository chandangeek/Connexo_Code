package com.energyict.mdc.firmware.rest.impl;

public class IdWithLocalizedValue<T> {
    public T id;
    public String localizedValue;

    public IdWithLocalizedValue(){}
    
    public IdWithLocalizedValue(T id, String localizedValue) {
        this.id = id;
        this.localizedValue = localizedValue;
    }
}
