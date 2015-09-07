package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cbo.TimeOfDay;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.UserFile;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * @author sva
 * @since 13/08/2015 - 16:04
 */
public class A1MessageConverter extends AbstractMessageConverter {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        // Network and connectivity
        registry.put(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS, new ApnCredentialsMessageEntry(apnAttributeName, usernameAttributeName, passwordAttributeName));
        registry.put(NetworkConnectivityMessage.ChangeSessionTimeout, new MultipleAttributeMessageEntry("SessionTimeout", "SessionTimeout[ms]"));
        registry.put(NetworkConnectivityMessage.SetCyclicMode, new DateConfigurationMessage("CyclicMode", "CallDistance"));
        registry.put(NetworkConnectivityMessage.SetPreferredDateMode, new DateConfigurationMessage("PreferredDateMode", "PreferredDate"));
        registry.put(NetworkConnectivityMessage.SetWANConfiguration, new MultipleAttributeMessageEntry("WanConfiguration", "Destination1", "Destination2"));

        // Configuration change
        registry.put(ConfigurationChangeDeviceMessage.WriteNewPDRNumber, new MultipleAttributeMessageEntry("WritePDR", "PdrToWrite"));
        registry.put(ConfigurationChangeDeviceMessage.ConfigureBillingPeriodStartDate, new ConfigureBillingPeriodStartDate("BillingPeriodStart", "BILLING_PERIOD_START_DATE"));
        registry.put(ConfigurationChangeDeviceMessage.ConfigureBillingPeriodLength, new MultipleAttributeMessageEntry("BillingPeriod", "BILLING_PERIOD_LENGTH"));
        registry.put(ConfigurationChangeDeviceMessage.WriteNewOnDemandBillingDate, new MultipleAttributeMessageEntry("OnDemandSnapshotTime", "DATE", "REASON"));
        registry.put(ConfigurationChangeDeviceMessage.ChangeUnitStatus, new MultipleAttributeMessageEntry("UnitsStatus", "UNITS_Status"));
        registry.put(ConfigurationChangeDeviceMessage.ConfigureStartOfGasDaySettings, new MultipleAttributeMessageEntry("GasDayConfiguration", "GDC_FLAG"));
        registry.put(ConfigurationChangeDeviceMessage.ConfigureStartOfGasDay, new MultipleAttributeMessageEntry("StartOfGasDay", "SGD_TIME"));
        registry.put(ConfigurationChangeDeviceMessage.ConfigureRSSIMultipleSampling, new MultipleAttributeMessageEntry("RSSIMultipleSampling", "RSSIMS_ACTION"));

        // Activity calendar
        registry.put(ActivityCalendarDeviceMessage.CLEAR_AND_DISABLE_PASSIVE_TARIFF, new OneTagMessageEntry("ClearPassiveTariff"));
        registry.put(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND_FROM_XML_USER_FILE, new A1SpecialDaysMessageEntry());
        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDAR_SEND_WITH_DATETIME_FROM_XML_USER_FILE, new A1ActivityCalendarMessageEntry());

        // Security messages
        registry.put(SecurityMessage.CHANGE_SECURITY_KEYS, new MultipleAttributeMessageEntry("ChangeKeys", "ClientId", "WrapperKey", "NewAuthenticationKey", "NewEncryptionKey"));

        // Device actions
        registry.put(DeviceActionMessage.ALARM_REGISTER_RESET, new OneTagMessageEntry("ResetAlarms"));
        registry.put(DeviceActionMessage.EVENT_LOG_RESET, new OneTagMessageEntry("ResetUNITSLog"));

        // Clock
        registry.put(ClockDeviceMessage.ConfigureClock, new MultipleAttributeMessageEntry("SetClockConfiguration", "TIMEZONE_OFFSET", "DST_ENABLED", "DST_DEVIATION"));

        // Firmware upgrade
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE, new FirmwareUdateWithUserFileMessageEntry(firmwareUpdateUserFileAttributeName));
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public A1MessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
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
            TimeOfDay timeOfDay = (TimeOfDay) messageAttribute;
            return String.format("%02d", timeOfDay.getHoursPart()) + ":" + String.format("%02d", timeOfDay.getMinutesPart()) + ":" + String.format("%02d", timeOfDay.getSecondsPart());
        } else if (propertySpec.getName().equals(XmlUserFileAttributeName) || propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            UserFile userFile = (UserFile) messageAttribute;
            return new String(userFile.loadFileInByteArray());  //Bytes of the userFile, as a string
        } else {
            return messageAttribute.toString();
        }
    }
}