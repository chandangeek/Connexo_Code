package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cisac on 8/6/2015.
 */
public class XemexWatchTalkMessageConverter extends Dsmr23MessageConverter{

    public XemexWatchTalkMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        super(messagingProtocol, propertySpecService, nlsService, converter);
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>(super.getRegistry());
        registry.remove(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN));
        registry.remove(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE));
        registry.remove(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE));
        registry.remove(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE));
        registry.remove(messageSpec(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE));
        registry.remove(messageSpec(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL));
        registry.remove(messageSpec(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD));
        registry.remove(messageSpec(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM));
        registry.remove(messageSpec(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP));
        registry.remove(messageSpec(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS));
        registry.remove(messageSpec(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS));
        registry.remove(messageSpec(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST));
        registry.remove(messageSpec(DeviceActionMessage.GLOBAL_METER_RESET));
        registry.remove(messageSpec(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS));
        registry.remove(messageSpec(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS));
        registry.remove(messageSpec(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION));
        registry.remove(messageSpec(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST));
        registry.remove(messageSpec(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST));
        registry.remove(messageSpec(MBusSetupDeviceMessage.Commission_With_Channel));
        registry.remove(messageSpec(ConfigurationChangeDeviceMessage.ChangeDefaultResetWindow));
        return registry;
    }
}
