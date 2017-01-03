package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ApnCredentialsMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.a1.A1ActivityCalendarMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.a1.A1SpecialDaysMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.a1.ConfigureBillingPeriodStartDate;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.a1.DateConfigurationMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.FirmwareUdateWithUserFileMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.IgnoreDSTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.StartOfGasDayAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.TimeZoneOffsetInHoursAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.XmlUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.apnAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.enableDSTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.enableRSSIMultipleSampling;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.masterKey;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.passwordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.setOnDemandBillingDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.usernameAttributeName;

/**
 * @author sva
 * @since 13/08/2015 - 16:04
 */
public class A1MessageConverter extends AbstractMessageConverter {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final DeviceMessageFileExtractor messageFileExtractor;

    protected A1MessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter);
        this.messageFileExtractor = messageFileExtractor;
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                // Network and connectivity
                .put(messageSpec(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS), new ApnCredentialsMessageEntry(apnAttributeName, usernameAttributeName, passwordAttributeName))
                .put(messageSpec(NetworkConnectivityMessage.ChangeSessionTimeout), new MultipleAttributeMessageEntry("SessionTimeout", "SessionTimeout[ms]"))
                .put(messageSpec(NetworkConnectivityMessage.SetCyclicMode), new DateConfigurationMessage("CyclicMode", "CallDistance"))
                .put(messageSpec(NetworkConnectivityMessage.SetPreferredDateMode), new DateConfigurationMessage("PreferredDateMode", "PreferredDate"))
                .put(messageSpec(NetworkConnectivityMessage.SetWANConfiguration), new MultipleAttributeMessageEntry("WanConfiguration", "Destination1", "Destination2"))

                // Configuration change
                .put(messageSpec(ConfigurationChangeDeviceMessage.WriteNewPDRNumber), new MultipleAttributeMessageEntry("WritePDR", "PdrToWrite"))
                .put(messageSpec(ConfigurationChangeDeviceMessage.ConfigureBillingPeriodStartDate), new ConfigureBillingPeriodStartDate("BillingPeriodStart", "BILLING_PERIOD_START_DATE"))
                .put(messageSpec(ConfigurationChangeDeviceMessage.ConfigureBillingPeriodLength), new MultipleAttributeMessageEntry("BillingPeriod", "BILLING_PERIOD_LENGTH"))
                .put(messageSpec(ConfigurationChangeDeviceMessage.WriteNewOnDemandBillingDate), new MultipleAttributeMessageEntry("OnDemandSnapshotTime", "DATE", "REASON"))
                .put(messageSpec(ConfigurationChangeDeviceMessage.ChangeUnitStatus), new MultipleAttributeMessageEntry("UnitsStatus", "UNITS_Status"))
                .put(messageSpec(ConfigurationChangeDeviceMessage.ConfigureStartOfGasDaySettings), new MultipleAttributeMessageEntry("GasDayConfiguration", "GDC_FLAG"))
                .put(messageSpec(ConfigurationChangeDeviceMessage.ConfigureStartOfGasDay), new MultipleAttributeMessageEntry("StartOfGasDay", "SGD_TIME"))
                .put(messageSpec(ConfigurationChangeDeviceMessage.ConfigureRSSIMultipleSampling), new MultipleAttributeMessageEntry("RSSIMultipleSampling", "RSSIMS_ACTION"))

                // Activity calendar
                .put(messageSpec(ActivityCalendarDeviceMessage.CLEAR_AND_DISABLE_PASSIVE_TARIFF), new OneTagMessageEntry("ClearPassiveTariff"))
                .put(messageSpec(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND_FROM_XML_USER_FILE), new A1SpecialDaysMessageEntry())
                .put(messageSpec(ActivityCalendarDeviceMessage.ACTIVITY_CALENDAR_SEND_WITH_DATETIME_FROM_XML_USER_FILE), new A1ActivityCalendarMessageEntry())

                // Security messages
                .put(messageSpec(SecurityMessage.CHANGE_SECURITY_KEYS), new MultipleAttributeMessageEntry("ChangeKeys", "ClientId", "WrapperKey", "NewAuthenticationKey", "NewEncryptionKey"))

                // Device actions
                .put(messageSpec(DeviceActionMessage.ALARM_REGISTER_RESET), new OneTagMessageEntry("ResetAlarms"))
                .put(messageSpec(DeviceActionMessage.EVENT_LOG_RESET), new OneTagMessageEntry("ResetUNITSLog"))

                // Clock
                .put(messageSpec(ClockDeviceMessage.ConfigureClock), new MultipleAttributeMessageEntry("SetClockConfiguration", "TIMEZONE_OFFSET", "DST_ENABLED", "DST_DEVIATION"))

                // Firmware upgrade
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE), new FirmwareUdateWithUserFileMessageEntry(firmwareUpdateUserFileAttributeName))
                .build();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(passwordAttributeName) ||
                propertySpec.getName().equals(masterKey) ||
                propertySpec.getName().equals(newAuthenticationKeyAttributeName) ||
                propertySpec.getName().equals(newEncryptionKeyAttributeName)) {
            return ((Password) messageAttribute).getValue();
        } else if (propertySpec.getName().equals(setOnDemandBillingDateAttributeName) ||
                propertySpec.getName().equals(activityCalendarActivationDateAttributeName)) {
            return dateFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(TimeZoneOffsetInHoursAttributeName)) {
            return Integer.toString(Integer.parseInt(messageAttribute.toString()) * 60);    // Offset should be in minutes
        } else if (propertySpec.getName().equals(enableDSTAttributeName) ||
                (propertySpec.getName().equals(enableRSSIMultipleSampling)) ||
                (propertySpec.getName().equals(IgnoreDSTAttributeName))) {
            return (boolean) messageAttribute ? "1" : "0";
        } else if (propertySpec.getName().equals(StartOfGasDayAttributeName)) {
            LocalTime timeOfDay = (LocalTime) messageAttribute;
            return String.format("%02d", timeOfDay.getHour()) + ":" + String.format("%02d", timeOfDay.getMinute()) + ":" + String.format("%02d", timeOfDay.getSecond());
        } else if (propertySpec.getName().equals(XmlUserFileAttributeName) || propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            DeviceMessageFile deviceMessageFile = (DeviceMessageFile) messageAttribute;
            return this.messageFileExtractor.contents(deviceMessageFile);  //Bytes of the deviceMessageFile, as a string
        } else {
            return messageAttribute.toString();
        }
    }

}