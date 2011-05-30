package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

public class BatteryLifeDurationCounter extends AbstractParameter {

    final int INITIAL_BATTERY_LIFE_COUNT_SRTM_AND_EVOHOP = 0xF7F490;
    final int INITIAL_BATTERY_LIFE_COUNT_RTM = 0x895440;

    private int batteryLifeCounter;
    private byte[] radioAddress = new byte[6];          //The radio address contains the module type.
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
        radioAddress = rtm.getWaveFlowConnect().readRadioAddress();
    }

    public BatteryLifeDurationCounter(RTM rtm, int batteryLifeCounter) throws IOException {
        super(rtm);
        this.batteryLifeCounter = batteryLifeCounter;
        radioAddress = rtm.getWaveFlowConnect().readRadioAddress();
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.BatteryLifeDurationCounter;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        batteryLifeCounter = ProtocolUtils.getInt(data, 0, 3);
    }

    protected byte[] prepare() throws IOException {
        throw new UnsupportedException();
    }
}