package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageAttribute;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

/**
 * Copyrights EnergyICT
 * Date: 7/13/12
 * Time: 10:55 AM
 */
public interface SixLoWPanMessages {

    String SIX_LOW_PAN_SETUP_CATEGORY = "G3 6LoWPAN layer setup";

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set maximum number of hops", tag = "SetMaxHops", advanced = true)
    interface SetMaxHopsMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "MaxHops", required = true)
        int getMaxHops();

    }

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set weak LQI value", tag = "SetWeakLQIValue", advanced = true)
    interface SetWeakLQIValueMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "WeakLQIValue", required = true)
        int getWeakLQIValue();

    }

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set security level", tag = "SetSecurityLevel", advanced = true)
    interface SetSecurityLevel extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "SecurityLevel", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set routing configuration", tag = "SetRoutingConfiguration", advanced = true)
    interface SetRoutingConfiguration extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "adp_net_traversal_time", required = true)
        int adp_net_traversal_time();

        @RtuMessageAttribute(tag = "adp_routing_table_entry_TTL", required = true)
        int adp_routing_table_entry_TTL();

        @RtuMessageAttribute(tag = "adp_Kr", required = true)
        int adp_Kr();

        @RtuMessageAttribute(tag = "adp_Km", required = true)
        int adp_Km();

        @RtuMessageAttribute(tag = "adp_Kc", required = true)
        int adp_Kc();

        @RtuMessageAttribute(tag = "adp_Kq", required = true)
        int adp_Kq();

        @RtuMessageAttribute(tag = "adp_Kh", required = true)
        int adp_Kh();

        @RtuMessageAttribute(tag = "adp_Krt", required = true)
        int adp_Krt();

        @RtuMessageAttribute(tag = "adp_RREQ_retries", required = true)
        int adp_RREQ_retries();

        @RtuMessageAttribute(tag = "adp_RREQ_RERR_wait", required = true)
        int adp_RREQ_RERR_wait();

        @RtuMessageAttribute(tag = "adp_Blacklist_table_entry_TTL", required = true)
        int adp_Blacklist_table_entry_TTL();

        @RtuMessageAttribute(tag = "adp_unicast_RREQ_gen_enable", required = true)
        boolean adp_unicast_RREQ_gen_enable();

        @RtuMessageAttribute(tag = "adp_RLC_enabled", required = true)
        boolean adp_RLC_enabled();

        @RtuMessageAttribute(tag = "adp_add_rev_link_cost", required = true)
        int adp_add_rev_link_cost();

    }

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set broadcast log table entry TTL", tag = "SetBroadcastLogTableEntryTTL", advanced = true)
    interface SetBroadcastLogTableEntryTTLMessage extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "BroadcastLogTableEntryTTL", required = true)
        int getBroadcastLogTableEntryTTL();

    }

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set max join wait time", tag = "SetMaxJoinWaitTime", advanced = true)
    interface SetMaxJoinWaitTime extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "MaxJoinWaitTime", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set path discovery time", tag = "SetPathDiscoveryTime", advanced = true)
    interface SetPathDiscoveryTime extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "PathDiscoveryTime", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set metric type", tag = "SetMetricType", advanced = true)
    interface SetMetricType extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "MetricType", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set coord short address", tag = "SetCoordShortAddress", advanced = true)
    interface SetCoordShortAddress extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "CoordShortAddress", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set disabled default routing", tag = "SetDisableDefaultRouting", advanced = true)
    interface SetDisableDefaultRouting extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "DisableDefaultRouting", required = true)
        boolean getValue();

}

    @RtuMessageDescription(category = SIX_LOW_PAN_SETUP_CATEGORY, description = "Set device type", tag = "SetDeviceType", advanced = true)
    interface SetDeviceType extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "DeviceType", required = true)
        int getValue();

    }
}