package com.energyict.mdc.upl;

public enum ActarisModel implements Model{
    Sl7000;

    @Override
    public String getName() {
        return this.name();
    }
}
