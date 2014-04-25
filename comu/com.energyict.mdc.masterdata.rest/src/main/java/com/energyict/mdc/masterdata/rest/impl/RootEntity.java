package com.energyict.mdc.masterdata.rest.impl;

public class RootEntity <E>{
    private E data;

    public E getData() {
        return data;
    }

    public RootEntity(E data) {
        this.data = data;
    }
}
