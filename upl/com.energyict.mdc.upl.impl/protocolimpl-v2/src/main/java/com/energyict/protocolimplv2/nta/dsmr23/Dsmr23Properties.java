package com.energyict.protocolimplv2.nta.dsmr23;

import java.math.BigDecimal;


/**
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 11:26:48
 */
public class Dsmr23Properties extends DlmsProperties {

    public static final String OLD_MBUS_DISCOVERY = "OldMbusDiscovery";
    public static final String FRAME_COUNTER_LIMIT = "FrameCounterLimit";
    public static final String REPLAY_ATTACK_PREVENTION = "ReplayAttackPrevention";

    public boolean getOldMbusDiscovery() {
        return parseBooleanProperty(OLD_MBUS_DISCOVERY, false);
    }

    public long getFrameCounterLimit() {
        return parseBigDecimalProperty(FRAME_COUNTER_LIMIT, BigDecimal.ZERO);
    }

    public boolean replayAttackPreventionEnabled(){
       return parseBooleanProperty(REPLAY_ATTACK_PREVENTION, false);
    }
}
