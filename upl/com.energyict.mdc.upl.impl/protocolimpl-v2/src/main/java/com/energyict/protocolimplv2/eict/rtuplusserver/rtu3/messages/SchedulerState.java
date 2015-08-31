package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages;

import com.energyict.dlms.axrdencoding.TypeEnum;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/08/2015 - 17:25
 */
public enum SchedulerState {

    UNKNOWN(0), NOT_RUNNING(1), RUNNING(2), PAUSED(3);

    private final int state;

    SchedulerState(int state) {
        this.state = state;
    }

    public TypeEnum toDLMSEnum() {
        return new TypeEnum(getState());
    }

    public int getState() {
        return state;
    }
}