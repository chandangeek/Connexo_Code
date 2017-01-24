package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;

import java.io.IOException;

public class BatteryLifeDurationCounter extends AbstractParameter {

    final int INITIAL_BATTERY_LIFE_COUNT_SRTM_AND_EVOHOP = 0xF7F490;
    final int INITIAL_BATTERY_LIFE_COUNT_RTM = 0x895440;

    private int batteryLifeCounter;
    private byte[] radioAddress = null;                 //The radio address contains the module type.
                                                        //The module type indicates the initial battery level.
    final int getBatteryLifeCounter() {
        return batteryLifeCounter;
    }

    public int getInitialCount() throws IOException {
        if (isRtm()) {
            return INITIAL_BATTERY_LIFE_COUNT_RTM;
        }
        if (isSRTM() || isEvoHop()) {
            return INITIAL_BATTERY_LIFE_COUNT_SRTM_AND_EVOHOP;
        }
        return INITIAL_BATTERY_LIFE_COUNT_SRTM_AND_EVOHOP;
    }

    private boolean isRtm() {
        return (radioAddress[1] & 0xFF) == 0x50;
    }

    private boolean isSRTM() {
        return (radioAddress[1] & 0xFF) == 0x51;
    }

    private boolean isEvoHop() {
        return (radioAddress[1] & 0xFF) == 0x56;
    }

    /**
     * The remaining battery life in 0..100 % knowing that the initial battery life count is 100 % and the getBatteryLifeCounter() is the remaining
     */
    final public int remainingBatteryLife() throws IOException {
        return 100 - (((getInitialCount() * 100) - (getBatteryLifeCounter() * 100)) / getInitialCount());
    }

    BatteryLifeDurationCounter(RTM rtm) throws IOException {
        super(rtm);
        if (radioAddress == null) {
            radioAddress = rtm.getWaveFlowConnect().readRadioAddress();
        }
    }

    public BatteryLifeDurationCounter(RTM rtm, int batteryLifeCounter, byte[] radioAddress) throws IOException {
        super(rtm);
        this.batteryLifeCounter = batteryLifeCounter;
        if (radioAddress == null) {
            this.radioAddress = rtm.getWaveFlowConnect().readRadioAddress();
        } else {
            this.radioAddress = radioAddress;
        }
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.BatteryLifeDurationCounter;
    }

    @Override
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        batteryLifeCounter = ProtocolUtils.getInt(data, 0, 3);
    }

    protected byte[] prepare() throws IOException {
        throw new UnsupportedException();
    }
}