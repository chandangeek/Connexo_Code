package com.energyict.mdc.engine.impl.core.offline;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/07/2014 - 10:28
 */
public enum ComJobResult {

    Failed(0),
    Success(1),
    Pending(2);

    private final int value;

    public int getValue() {
        return value;
    }

    private ComJobResult(int value) {
        this.value = value;
    }
}
