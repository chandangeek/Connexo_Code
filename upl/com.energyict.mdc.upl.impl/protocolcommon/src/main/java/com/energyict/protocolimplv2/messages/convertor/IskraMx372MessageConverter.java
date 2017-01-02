package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
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
import com.google.common.collect.ImmutableMap;

import java.util.Date;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.managedWhiteListPhoneNumbersAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.toDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.whiteListPhoneNumbersAttributeName;

/**
 * Represents a MessageConverter for the legacy IskraMx372  PreNTA protocol.
 *
 * @author sva
 * @since 25/10/13 - 10:10
 */
public class IskraMx372MessageConverter extends AbstractMessageConverter {

    private final LoadProfileExtractor loadProfileExtractor;

    protected IskraMx372MessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, LoadProfileExtractor loadProfileExtractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter);
        this.loadProfileExtractor = loadProfileExtractor;
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                // Authentication and encryption
                .put(messageSpec(SecurityMessage.CHANGE_LLS_SECRET_HEX), new MultipleAttributeMessageEntry("Change_LLS_Secret", "LLSSecret'"))

                // Basic messages
                .put(messageSpec(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS), new MultipleAttributeMessageEntry("GPRS_modem_credentials", "Username", "Password"))
                .put(messageSpec(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS), new MultipleInnerTagsMessageEntry("GPRS_modem_setup", "APN", "Username", "Password"))

                .put(messageSpec(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE), new SimpleValueMessageEntry("Mode"))
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_CLOSE), new ConnectLoadMessageEntry())
                .put(messageSpec(ContactorDeviceMessage.CONTACTOR_OPEN), new DisconnectLoadMessageEntry())
                .put(messageSpec(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND), new IskraMx372ActivityCalendarConfigMessageEntry(activityCalendarNameAttributeName, activityCalendarCodeTableAttributeName))

                // Load limit
                .put(messageSpec(LoadBalanceDeviceMessage.ENABLE_LOAD_LIMITING_FOR_GROUP), new MultipleInnerTagsMessageEntry("ApplyLoadLimiting", "Threshold GroupId *", "StartDate (dd/mm/yyyy HH:MM:SS)", "EndDate (dd/mm/yyyy HH:MM:SS)"))
                .put(messageSpec(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION_FOR_GROUP), new SimpleValueMessageEntry("Clear threshold - groupID"))
                .put(messageSpec(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS_FOR_GROUP), new MultipleInnerTagsMessageEntry("ConfigureLoadLimitingParameters", "Parameter GroupId *", "Threshold PowerLimit (W)", "Contractual PowerLimit (W)"))

                // MBusMessages
                .put(messageSpec(MBusSetupDeviceMessage.Commission), new OneTagMessageEntry("Mbus_Install"))
                .put(messageSpec(MBusSetupDeviceMessage.DataReadout), new OneTagMessageEntry("Mbus_DataReadout"))
                .put(messageSpec(MBusSetupDeviceMessage.Decommission), new OneTagMessageEntry("Mbus_Remove"))

                // Wake up functionality
                .put(messageSpec(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM), new OneTagMessageEntry("Activate_the_wakeup_mechanism"))
                .put(messageSpec(NetworkConnectivityMessage.ADD_MANAGED_PHONENUMBERS_TO_WHITE_LIST), new IskraMx372AddManagedPhoneNumbersToWhiteListMessageEntry(managedWhiteListPhoneNumbersAttributeName))
                .put(messageSpec(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST), new IskraMx372AddPhoneNumbersToWhiteListMessageEntry(whiteListPhoneNumbersAttributeName))
                .put(messageSpec(NetworkConnectivityMessage.CHANGE_INACTIVITY_TIMEOUT), new SimpleValueMessageEntry("Inactivity_timeout"))

                // LoadProfiles
                .put(messageSpec(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST), new PartialLoadProfileMessageEntry(loadProfileAttributeName, fromDateAttributeName, toDateAttributeName))
                .put(messageSpec(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST), new LoadProfileRegisterRequestMessageEntry(loadProfileAttributeName, fromDateAttributeName))
                .build();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.loadLimitEndDateAttributeName:
            case DeviceMessageConstants.loadLimitStartDateAttributeName:
                return europeanDateTimeFormat.format((Date) messageAttribute);
            case DeviceMessageConstants.loadProfileAttributeName:
                return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute, this.loadProfileExtractor);
            case DeviceMessageConstants.fromDateAttributeName:
            case DeviceMessageConstants.toDateAttributeName:
                return dateTimeFormatWithTimeZone.format((Date) messageAttribute);
            case DeviceMessageConstants.newHexPasswordAttributeName:
                return ((Password) messageAttribute).getValue();
            default:
                return messageAttribute.toString();
        }
    }
}
