/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ConnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.DisconnectLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.IskraMx372ActivityCalendarConfigMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.IskraMx372AddManagedPhoneNumbersToWhiteListMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.IskraMx372AddPhoneNumbersToWhiteListMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleInnerTagsMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleValueMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.LoadProfileRegisterRequestMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.PartialLoadProfileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.managedWhiteListPhoneNumbersAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.toDateAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.whiteListPhoneNumbersAttributeName;

/**
 * Represents a MessageConverter for the legacy IskraMx372  PreNTA protocol.
 *
 *  @author sva
  * @since 25/10/13 - 10:10
 */
public class IskraMx372MessageConverter extends AbstractMessageConverter {

    private final TopologyService topologyService;

    @Inject
    public IskraMx372MessageConverter(TopologyService topologyService) {
        super();
        this.topologyService = topologyService;
    }

    @Override
    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();
        // Authentication and encryption
        registry.put(DeviceMessageId.SECURITY_CHANGE_LLS_SECRET, new OneTagMessageEntry("Change_LLS_Secret"));

        // Basic messages
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_USER_CREDENTIALS, new MultipleAttributeMessageEntry("GPRS_modem_credentials", "Username", "Password"));
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_GPRS_APN_CREDENTIALS, new MultipleInnerTagsMessageEntry("GPRS_modem_setup", "APN", "Username", "Password"));
        //
        registry.put(DeviceMessageId.CONTACTOR_CHANGE_CONNECT_CONTROL_MODE, new SimpleValueMessageEntry("Mode"));
        registry.put(DeviceMessageId.CONTACTOR_CLOSE, new ConnectLoadMessageEntry());
        registry.put(DeviceMessageId.CONTACTOR_OPEN, new DisconnectLoadMessageEntry());
        registry.put(DeviceMessageId.ACTIVITY_CALENDER_SEND, new IskraMx372ActivityCalendarConfigMessageEntry(activityCalendarNameAttributeName, activityCalendarAttributeName));

        // Load limit
        registry.put(DeviceMessageId.LOAD_BALANCING_ENABLE_LOAD_LIMITING_FOR_GROUP, new MultipleInnerTagsMessageEntry("ApplyLoadLimiting", "Threshold GroupId *", "StartDate (dd/mm/yyyy HH:MM:SS)", "EndDate (dd/mm/yyyy HH:MM:SS)"));
        registry.put(DeviceMessageId.LOAD_BALANCING_CLEAR_LOAD_LIMIT_CONFIGURATION_FOR_GROUP, new SimpleValueMessageEntry("Clear threshold - groupID"));
        registry.put(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_PARAMETERS_FOR_GROUP, new MultipleInnerTagsMessageEntry("ConfigureLoadLimitingParameters", "Parameter GroupId *", "Threshold PowerLimit (W)", "Contractual PowerLimit (W)"));

        // MBusMessages
        registry.put(DeviceMessageId.MBUS_SETUP_COMMISSION, new OneTagMessageEntry("Mbus_Install"));
        registry.put(DeviceMessageId.MBUS_SETUP_DATA_READOUT, new OneTagMessageEntry("Mbus_DataReadout"));
        registry.put(DeviceMessageId.MBUS_SETUP_DECOMMISSION, new OneTagMessageEntry("Mbus_Remove"));

        // Wake up functionality
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_ACTIVATE_WAKEUP_MECHANISM, new OneTagMessageEntry("Activate_the_wakeup_mechanism"));
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_ADD_MANAGED_PHONENUMBERS_TO_WHITE_LIST, new IskraMx372AddManagedPhoneNumbersToWhiteListMessageEntry(managedWhiteListPhoneNumbersAttributeName));
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_ADD_PHONENUMBERS_TO_WHITE_LIST, new IskraMx372AddPhoneNumbersToWhiteListMessageEntry(whiteListPhoneNumbersAttributeName));
        registry.put(DeviceMessageId.NETWORK_CONNECTIVITY_CHANGE_INACTIVITY_TIMEOUT, new SimpleValueMessageEntry("Inactivity_timeout"));

        // LoadProfiles
        registry.put(DeviceMessageId.LOAD_PROFILE_PARTIAL_REQUEST, new PartialLoadProfileMessageEntry(loadProfileAttributeName, fromDateAttributeName, toDateAttributeName));
        registry.put(DeviceMessageId.LOAD_PROFILE_REGISTER_REQUEST, new LoadProfileRegisterRequestMessageEntry(loadProfileAttributeName, fromDateAttributeName));
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.loadLimitEndDateAttributeName:
            case DeviceMessageConstants.loadLimitStartDateAttributeName:
                return europeanDateTimeFormat.format((Date) messageAttribute);
            case DeviceMessageConstants.loadProfileAttributeName:
            	return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute, this.topologyService);
            case DeviceMessageConstants.fromDateAttributeName:
            case DeviceMessageConstants.toDateAttributeName:
            	return dateTimeFormatWithTimeZone.format((Date) messageAttribute);
            default:
                return messageAttribute.toString();
        }
    }

}