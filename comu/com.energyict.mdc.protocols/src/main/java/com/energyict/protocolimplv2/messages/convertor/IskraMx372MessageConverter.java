package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.protocol.api.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdw.core.LoadProfile;
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

import java.util.Date;
import java.util.HashMap;
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
 *  @author sva
  * @since 25/10/13 - 10:10
 */
public class IskraMx372MessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link DeviceMessageSpec}s
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        // Authentication and encryption
        registry.put(SecurityMessage.CHANGE_LLS_SECRET, new OneTagMessageEntry("Change_LLS_Secret"));

        // Basic messages
        registry.put(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS, new MultipleAttributeMessageEntry("GPRS_modem_credentials", "Username", "Password"));
        registry.put(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS, new MultipleInnerTagsMessageEntry("GPRS_modem_setup", "APN", "Username", "Password"));
        //
        registry.put(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE, new SimpleValueMessageEntry("Mode"));
        registry.put(ContactorDeviceMessage.CONTACTOR_CLOSE, new ConnectLoadMessageEntry());
        registry.put(ContactorDeviceMessage.CONTACTOR_OPEN, new DisconnectLoadMessageEntry());
        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND, new IskraMx372ActivityCalendarConfigMessageEntry(activityCalendarNameAttributeName, activityCalendarCodeTableAttributeName));

        // Load limit
        registry.put(LoadBalanceDeviceMessage.ENABLE_LOAD_LIMITING_FOR_GROUP, new MultipleInnerTagsMessageEntry("ApplyLoadLimiting", "Threshold GroupId *", "StartDate (dd/mm/yyyy HH:MM:SS)", "EndDate (dd/mm/yyyy HH:MM:SS)"));
        registry.put(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION_FOR_GROUP, new SimpleValueMessageEntry("Clear threshold - groupID"));
        registry.put(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS_FOR_GROUP, new MultipleInnerTagsMessageEntry("ConfigureLoadLimitingParameters", "Parameter GroupId *", "Threshold PowerLimit (W)", "Contractual PowerLimit (W)"));

        // MBusMessages
        registry.put(MBusSetupDeviceMessage.Commission, new OneTagMessageEntry("Mbus_Install"));
        registry.put(MBusSetupDeviceMessage.DataReadout, new OneTagMessageEntry("Mbus_DataReadout"));
        registry.put(MBusSetupDeviceMessage.Decommission, new OneTagMessageEntry("Mbus_Remove"));

        // Wake up functionality
        registry.put(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM, new OneTagMessageEntry("Activate_the_wakeup_mechanism"));
        registry.put(NetworkConnectivityMessage.ADD_MANAGED_PHONENUMBERS_TO_WHITE_LIST, new IskraMx372AddManagedPhoneNumbersToWhiteListMessageEntry(managedWhiteListPhoneNumbersAttributeName));
        registry.put(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST, new IskraMx372AddPhoneNumbersToWhiteListMessageEntry(whiteListPhoneNumbersAttributeName));
        registry.put(NetworkConnectivityMessage.CHANGE_INACTIVITY_TIMEOUT, new SimpleValueMessageEntry("Inactivity_timeout"));

         // LoadProfiles
        registry.put(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST, new PartialLoadProfileMessageEntry(loadProfileAttributeName, fromDateAttributeName, toDateAttributeName));
        registry.put(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST, new LoadProfileRegisterRequestMessageEntry(loadProfileAttributeName, fromDateAttributeName));
    }

    public IskraMx372MessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.loadLimitEndDateAttributeName:
            case DeviceMessageConstants.loadLimitStartDateAttributeName:
                return europeanDateTimeFormat.format((Date) messageAttribute);
            case DeviceMessageConstants.loadProfileAttributeName:
            	return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute);
            case DeviceMessageConstants.fromDateAttributeName:
            case DeviceMessageConstants.toDateAttributeName:
            	return dateTimeFormatWithTimeZone.format((Date) messageAttribute);
            default:
                return messageAttribute.toString();
        }
    }
}
