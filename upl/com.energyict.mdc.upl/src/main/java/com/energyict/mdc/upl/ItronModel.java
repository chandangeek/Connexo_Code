package com.energyict.mdc.upl;

public enum ItronModel implements Model {
    EM620;

    @Override
    public String getName() {
        return this.name();
    }
}
