/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.device.messages.DlmsAuthenticationLevelMessageValues;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;

import javax.inject.Inject;
import java.util.Map;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.authenticationLevelAttributeName;

/**
 * Represents a MessageConverter for the legacy DSMR4.0 L&G DE350 protocol.
 *
 * @author sva
 * @since 30/10/13 - 14:00
 */
public class Dsmr40MessageConverter extends Dsmr23MessageConverter {

    @Inject
    public Dsmr40MessageConverter(TopologyService topologyService) {
        super(topologyService);
    }

    @Override
    protected void initializeRegistry(Map<DeviceMessageId, MessageEntryCreator> registry) {
        super.initializeRegistry(registry);
        // Restore factory settings
        registry.put(DeviceMessageId.DEVICE_ACTIONS_RESTORE_FACTORY_SETTINGS, new OneTagMessageEntry("Restore_Factory_Settings"));

        // Change administrative status
        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_CHANGE_ADMINISTRATIVE_STATUS, new MultipleAttributeMessageEntry("Change_Administrative_Status", "Status"));

        // Authentication and encryption - remove the DSMR2.3 message & replace by 4 new DSMR4.0 messages
        registry.remove(DeviceMessageId.SECURITY_CHANGE_DLMS_AUTHENTICATION_LEVEL);
        registry.put(DeviceMessageId.SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P0, new MultipleAttributeMessageEntry("Disable_authentication_level_P0", "AuthenticationLevel"));
        registry.put(DeviceMessageId.SECURITY_DISABLE_DLMS_AUTHENTICATION_LEVEL_P1, new MultipleAttributeMessageEntry("Disable_authentication_level_P1", "AuthenticationLevel"));
        registry.put(DeviceMessageId.SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P0, new MultipleAttributeMessageEntry("Enable_authentication_level_P0", "AuthenticationLevel"));
        registry.put(DeviceMessageId.SECURITY_ENABLE_DLMS_AUTHENTICATION_LEVEL_P1, new MultipleAttributeMessageEntry("Enable_authentication_level_P1", "AuthenticationLevel"));
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case authenticationLevelAttributeName:
                return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
            default:
                return super.format(propertySpec, messageAttribute);
        }
    }

}