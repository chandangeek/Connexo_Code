/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageAttribute;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

public interface PlcOfdmMacSetupMessages {

    String G3_PLC_OFDM_MAC_SETUP_CATEGORY = "G3 PLC OFDM MAC setup";

    @RtuMessageDescription(category = G3_PLC_OFDM_MAC_SETUP_CATEGORY, description = "Reset PLC G3 MAC counters", tag = "ResetPlcOfdmMacCounters", advanced = true)
    interface ResetPlcOfdmMacCountersMessage extends AnnotatedMessage {
        // No attributes, just the reset command
    }

    @RtuMessageDescription(category = G3_PLC_OFDM_MAC_SETUP_CATEGORY, description = "Set tone mask", tag = "SetToneMask", advanced = true)
    interface SetToneMaskMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "ToneMask", required = true)
        boolean[] getToneMask();

    }

    @RtuMessageDescription(category = G3_PLC_OFDM_MAC_SETUP_CATEGORY, description = "Set TMR TTL", tag = "SetTMRTTL", advanced = true)
    interface SetTMRTTL extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "tmrTTL", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_PLC_OFDM_MAC_SETUP_CATEGORY, description = "Set max frame retries", tag = "SetMaxFrameRetries", advanced = true)
    interface SetMaxFrameRetries extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "maxFrameRetries", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_PLC_OFDM_MAC_SETUP_CATEGORY, description = "Set neighbour table entry TTL", tag = "SetNeighbourTableEntryTTL", advanced = true)
    interface SetNeighbourTableEntryTTL extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "NeighbourTableEntryTTL", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_PLC_OFDM_MAC_SETUP_CATEGORY, description = "Set high priority windows size", tag = "SetHighPriorityWindowSize", advanced = true)
    interface SetHighPriorityWindowSize extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "windowSize", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_PLC_OFDM_MAC_SETUP_CATEGORY, description = "Set CSMA fairness limit", tag = "SetCSMAFairnessLimit", advanced = true)
    interface SetCSMAFairnessLimit extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "CSMAFairnessLimit", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_PLC_OFDM_MAC_SETUP_CATEGORY, description = "Set beacon randomization window length", tag = "SetBeaconRandomizationWindowLength", advanced = true)
    interface SetBeaconRandomizationWindowLength extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "WindowLength", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_PLC_OFDM_MAC_SETUP_CATEGORY, description = "Set MAC A", tag = "SetMacA", advanced = true)
    interface SetMacA extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "MAC_A", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_PLC_OFDM_MAC_SETUP_CATEGORY, description = "Set MAC K", tag = "SetMacK", advanced = true)
    interface SetMacK extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "MAC_K", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_PLC_OFDM_MAC_SETUP_CATEGORY, description = "Set minimum CW attempts", tag = "SetMinimumCWAttempts", advanced = true)
    interface SetMinimumCWAttempts extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "minimumCWAttempts", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_PLC_OFDM_MAC_SETUP_CATEGORY, description = "Set max BE", tag = "SetMaxBe", advanced = true)
    interface SetMaxBe extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "maxBE", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_PLC_OFDM_MAC_SETUP_CATEGORY, description = "Set max CSMA back off", tag = "SetMaxCSMABackOff", advanced = true)
    interface SetMaxCSMABackOff extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "maxCSMABackOff", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_PLC_OFDM_MAC_SETUP_CATEGORY, description = "Set min BE", tag = "SetMinBe", advanced = true)
    interface SetMinBe extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "minBE", required = true)
        int getValue();

    }

}
