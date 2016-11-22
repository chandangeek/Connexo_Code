package com.energyict.mdc.device.data.rest.impl;

public class SummarizedRegisterInfo {
    public Long id;
    public String name;
    public boolean isBilling;

    public SummarizedRegisterInfo(Long id, String name, boolean isBilling) {
        this.id = id;
        this.name = name;
        this.isBilling = isBilling;
    }
}
