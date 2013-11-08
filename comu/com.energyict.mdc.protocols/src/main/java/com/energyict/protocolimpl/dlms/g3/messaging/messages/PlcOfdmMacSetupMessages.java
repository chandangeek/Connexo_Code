package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.*;

/**
 * Copyrights EnergyICT
 * Date: 7/13/12
 * Time: 11:02 AM
 */
public interface PlcOfdmMacSetupMessages {

    String G3_PLC_OFDM_MAC_SETUP_CATEGORY = "G3 PLC OFDM MAC setup";

    @RtuMessageDescription(category = G3_PLC_OFDM_MAC_SETUP_CATEGORY, description = "Reset PLC G3 MAC counters", tag = "ResetPlcOfdmMacCounters", advanced = true)
    interface ResetPlcOfdmMacCountersMessage extends AnnotatedMessage {
        // No attributes, just the reset command
    }


    @RtuMessageDescription(category = G3_PLC_OFDM_MAC_SETUP_CATEGORY, description = "Set PAN id", tag = "SetPanId", advanced = true)
    interface SetPanIdMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "panId", required = true)
        int getPanId();

    }

    @RtuMessageDescription(category = G3_PLC_OFDM_MAC_SETUP_CATEGORY, description = "Set maximum orphan timer", tag = "SetMaxOrphanTimer", advanced = true)
    interface SetMaxOrphanTimerMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "maxOrphanTimer", required = true)
        int getMaxOrphanTimer();

    }

}
