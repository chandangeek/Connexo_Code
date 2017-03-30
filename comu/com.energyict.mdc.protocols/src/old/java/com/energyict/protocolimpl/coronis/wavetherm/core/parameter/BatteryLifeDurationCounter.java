/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavetherm.core.parameter;

import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;

import java.io.IOException;

public class BatteryLifeDurationCounter extends AbstractParameter {

    final int INITIAL_BATTERY_LIFE_COUNT = 0xC15C;


    /**
     * This is the remaining battery life. Use the default value to calculate the remaining life time of the battery
     */
    private int batteryLifeCounter;

    /**
     * The remaining battery life count
     *
     * @return
     */
    final int getBatteryLifeCounter() {
        return batteryLifeCounter;
    }

    /**
     * The factory initial battery life count value
     *
     * @return the factory initial battery life count
     */
    final int initialBatteryLifeCount() {
        return INITIAL_BATTERY_LIFE_COUNT;
    }

    /**
     * The remaining battery life in 0..100 % knowing that the initial battery life count is 100 % and the getBatteryLifeCounter() is the remaining
     *
     * @return the remaining battery life in percentage
     */
    final public int remainingBatteryLife() {
        return 100 - (((INITIAL_BATTERY_LIFE_COUNT * 100) - (getBatteryLifeCounter() * 100)) / INITIAL_BATTERY_LIFE_COUNT);
    }

    BatteryLifeDurationCounter(WaveTherm waveFlow) {
        super(waveFlow);
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.BatteryLifeDurationCounter;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        batteryLifeCounter = ProtocolUtils.getInt(data, 0, 2);
    }


    protected byte[] prepare() throws IOException {
        throw new UnsupportedException();
    }


}
