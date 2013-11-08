package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.*;

/**
 * Copyrights EnergyICT
 * Date: 7/13/12
 * Time: 10:55 AM
 */
public interface SixLoWPanMessages {

    String SIX_LOW_PAN_SETUP_CATEGORY = "G3 6LoWPAN layer setup";

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set active scan duration", tag = "SetActiveScanDuration", advanced = true)
    interface SetActiveScanDurationMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "ActiveScanDuration", required = true)
        int getActiveScanDuration();

    }

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set broadcast log table entry TTL", tag = "SetBroadcastLogTableEntryTTL", advanced = true)
    interface SetBroadcastLogTableEntryTTLMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "BroadcastLogTableEntryTTL", required = true)
        int getBroadcastLogTableEntryTTL();

    }

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set discovery attempts speed", tag = "SetDiscoveryAttemptsSpeed", advanced = true)
    interface SetDiscoveryAttemptsSpeedMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "DiscoveryAttemptsSpeed", required = true)
        int getDiscoveryAttemptsSpeed();

    }

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set maximum age time", tag = "SetMaxAgeTime", advanced = true)
    interface SetMaxAgeTimeMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "MaxAgeTime", required = true)
        int getMaxAgeTime();

    }

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set maximum number of hops", tag = "SetMaxHops", advanced = true)
    interface SetMaxHopsMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "MaxHops", required = true)
        int getMaxHops();

    }

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set maximum PAN conflict count", tag = "SetMaxPanConflictCount", advanced = true)
    interface SetMaxPanConflictCountMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "MaxPanConflictCount", required = true)
        int getMaxPanConflictCount();

    }

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set PAN conflict wait time", tag = "SetPanConflictWaitTime", advanced = true)
    interface SetPanConflictWaitTimeMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "PanConflictWaitTime", required = true)
        int getPanConflictWaitTime();

    }

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set tone mask", tag = "SetToneMask", advanced = true)
    interface SetToneMaskMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "ToneMask", required = true)
        boolean[] getToneMask();

    }

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set weak LQI value", tag = "SetWeakLQIValue", advanced = true)
    interface SetWeakLQIValueMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "WeakLQIValue", required = true)
        int getWeakLQIValue();

    }
}
