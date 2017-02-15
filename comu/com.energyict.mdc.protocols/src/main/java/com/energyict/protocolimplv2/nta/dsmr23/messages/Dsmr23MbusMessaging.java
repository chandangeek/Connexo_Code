/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaMbusDevice;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link DeviceMessageSupport} for the DSMR 2.3 Mbus slave device, that:
 * - Formats the device message attributes from objects to proper string values
 * - Executes a given message
 * - Has a list of all supported device message specs
 * <p/>
 *
 * @author sva
 * @since 29/11/13 - 14:17
 */
public class Dsmr23MbusMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    private final Set<DeviceMessageId> supportedMessages = EnumSet.of(
            DeviceMessageId.CONTACTOR_OPEN,
            DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE,
            DeviceMessageId.CONTACTOR_CLOSE,
            DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE,
            DeviceMessageId.CONTACTOR_CHANGE_CONNECT_CONTROL_MODE,
            DeviceMessageId.MBUS_SETUP_DECOMMISSION,
            DeviceMessageId.MBUS_SETUP_SET_ENCRYPTION_KEYS,
            DeviceMessageId.MBUS_SETUP_USE_CORRECTED_VALUES,
            DeviceMessageId.MBUS_SETUP_USE_UNCORRECTED_VALUES,
            DeviceMessageId.LOAD_PROFILE_PARTIAL_REQUEST,
            DeviceMessageId.LOAD_PROFILE_REGISTER_REQUEST
    );
    private final TopologyService topologyService;

    public Dsmr23MbusMessaging(AbstractNtaMbusDevice mbusProtocol, TopologyService topologyService) {
        super(mbusProtocol.getMeterProtocol());
        this.topologyService = topologyService;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return supportedMessages;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.loadProfileAttributeName:
                return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute, this.topologyService);
            case DeviceMessageConstants.fromDateAttributeName:
            case DeviceMessageConstants.toDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());
            case DeviceMessageConstants.contactorActivationDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());  //Epoch (millis)
            default:
                return messageAttribute.toString();  //Used for String and BigDecimal attributes
        }
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "updateSentMessages", this.getClass());
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        throw new CommunicationException(MessageSeeds.UNSUPPORTED_METHOD, "executePendingMessages", this.getClass());
    }
}