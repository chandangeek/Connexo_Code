package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ApnCredentialsMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.EnableOrDisableDSTMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.GprsUserCredentialsMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleValueMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.LoadProfileRegisterRequestMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.PartialLoadProfileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.google.common.collect.ImmutableMap;

import java.util.Date;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.apnAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.enableDSTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.passwordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.toDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.usernameAttributeName;

/**
 * Represents a MessageConverter for the legacy Xemex ReMI datalogger protocol.
 *
 * @author sva
 * @since 30/10/13 - 12:21
 */
public class REMIDataloggerMessageConverter extends AbstractMessageConverter {

    public REMIDataloggerMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter, extractor);
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                // Connectivity setup
                .put(messageSpec(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS), new GprsUserCredentialsMessageEntry(usernameAttributeName, passwordAttributeName))
                .put(messageSpec(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS), new ApnCredentialsMessageEntry(apnAttributeName, usernameAttributeName, passwordAttributeName))

                // Configuration
                .put(messageSpec(ClockDeviceMessage.EnableOrDisableDST), new EnableOrDisableDSTMessageEntry(enableDSTAttributeName))
                .put(messageSpec(DeviceActionMessage.ALARM_REGISTER_RESET), new OneTagMessageEntry("Reset_Alarm_Register"))
                .put(messageSpec(DeviceActionMessage.ERROR_REGISTER_RESET), new OneTagMessageEntry("Reset_Error_Register"))
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetAlarmFilter), new SimpleValueMessageEntry("Alarm_Filter"))

                // LoadProfiles
                .put(messageSpec(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST), new PartialLoadProfileMessageEntry(loadProfileAttributeName, fromDateAttributeName, toDateAttributeName))
                .put(messageSpec(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST), new LoadProfileRegisterRequestMessageEntry(loadProfileAttributeName, fromDateAttributeName))
                .build();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.enableDSTAttributeName:
                return ((Boolean) messageAttribute) ? "1" : "0";
            case DeviceMessageConstants.loadProfileAttributeName:
                return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute, this.getExtractor());
            case DeviceMessageConstants.fromDateAttributeName:
            case DeviceMessageConstants.toDateAttributeName:
                return dateTimeFormatWithTimeZone.format((Date) messageAttribute);
            case DeviceMessageConstants.passwordAttributeName:
                return ((Password) messageAttribute).getValue();
            default:
                return messageAttribute.toString();
        }
    }
}
