package com.energyict.mdc.upl;

/**
 * Placeholder implementation
 */
public enum EictModel implements Model {
    AM100,
    ABBA1500;

    @Override
    public String getName() {
        return this.name();
    }
}