package com.energyict.mdc.device.data.rest;

/**
 * Created by bvn on 8/12/14.
 */
public class SuccessIndicatorInfo {
    public String id;
    public String displayValue;
    public Integer retries;

    public SuccessIndicatorInfo() {
    }

    public SuccessIndicatorInfo(String id, String displayValue) {
        this();
        this.id = id;
        this.displayValue = displayValue;
    }

}