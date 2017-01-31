/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.messaging.messages;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageAttribute;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

public interface G3InterfaceMessages {

    String G3_INTERFACE_CONFIGURATION = "G3 interface configuration";

    @RtuMessageDescription(category = G3_INTERFACE_CONFIGURATION, description = "Set PAN ID", tag = "SetPANID")
    interface SetPANID extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "PANID", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_INTERFACE_CONFIGURATION, description = "Configure automatic route management", tag = "SetAutomaticRouteManagement")
    interface SetAutomaticRouteManagement extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "routeRequestEnabled", required = true)
        boolean routeRequestEnabled();

        @RtuMessageAttribute(tag = "pingEnabled", required = true)
        boolean pingEnabled();

        @RtuMessageAttribute(tag = "pathRequestEnabled", required = true)
        boolean pathRequestEnabled();

    }

    @RtuMessageDescription(category = G3_INTERFACE_CONFIGURATION, description = "Enable SNR", tag = "EnableSNR")
    interface EnableSNR extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "EnableSNR", required = true)
        boolean getValue();

    }

    @RtuMessageDescription(category = G3_INTERFACE_CONFIGURATION, description = "Set SNR packet interval", tag = "SetSNRPacketInterval")
    interface SetSNRPacketInterval extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "SNRPacketInterval", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_INTERFACE_CONFIGURATION, description = "Set SNR quiet time", tag = "SetSNRQuietTime")
    interface SetSNRQuietTime extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "SNRQuietTime", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_INTERFACE_CONFIGURATION, description = "Set SNR payload", tag = "SetSNRPayload")
    interface SetSNRPayload extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "SNRPayload", required = true)
        String getValue();

    }

    @RtuMessageDescription(category = G3_INTERFACE_CONFIGURATION, description = "Enable keep alive", tag = "EnableKeepAlive")
    interface EnableKeepAlive extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "EnableKeepAlive", required = true)
        boolean getValue();

    }

    @RtuMessageDescription(category = G3_INTERFACE_CONFIGURATION, description = "Set keep alive schedule interval", tag = "SetKeepAliveScheduleInterval")
    interface SetKeepAliveScheduleInterval extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "KeepAliveScheduleInterval", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_INTERFACE_CONFIGURATION, description = "Set keep alive bucket size", tag = "SetKeepAliveBucketSize")
    interface SetKeepAliveBucketSize extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "KeepAliveBucketSize", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_INTERFACE_CONFIGURATION, description = "Set minimum inactive meter time", tag = "SetMinInactiveMeterTime")
    interface SetMinInactiveMeterTime extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "MinInactiveMeterTime", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_INTERFACE_CONFIGURATION, description = "Set maximum inactive meter time", tag = "SetMaxInactiveMeterTime")
    interface SetMaxInactiveMeterTime extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "MaxInactiveMeterTime", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_INTERFACE_CONFIGURATION, description = "Set keep alive retries", tag = "SetKeepAliveRetries")
    interface SetKeepAliveRetries extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "KeepAliveRetries", required = true)
        int getValue();

    }

    @RtuMessageDescription(category = G3_INTERFACE_CONFIGURATION, description = "Set keep alive timeout", tag = "SetKeepAliveTimeout")
    interface SetKeepAliveTimeout extends AnnotatedMessage {

        @RtuMessageAttribute(tag = "KeepAliveTimeout", required = true)
        int getValue();

    }
}