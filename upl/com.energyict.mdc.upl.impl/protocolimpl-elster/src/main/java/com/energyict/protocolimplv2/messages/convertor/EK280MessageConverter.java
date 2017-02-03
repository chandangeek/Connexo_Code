package com.energyict.protocolimplv2.messages.convertor;

import com.elster.protocolimpl.dlms.tariff.CodeTableBase64Builder;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TariffCalendar;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ApnCredentialsMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ek280.ConfigureAutoConnectModeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ek280.EK280ActivityCalendarMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.FirmwareUdateWithUserFileMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.apnAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.masterKey;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.passwordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.usernameAttributeName;

/**
 * @author sva
 * @since 13/08/2015 - 16:04
 */
public class EK280MessageConverter extends AbstractMessageConverter {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final TariffCalendarExtractor calendarExtractor;
    private final TariffCalendarFinder calendarFinder;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final DeviceMessageFileFinder deviceMessageFileFinder;

    public EK280MessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, DeviceMessageFileFinder deviceMessageFileFinder) {
        super(messagingProtocol, propertySpecService, nlsService, converter);
        this.calendarFinder = calendarFinder;
        this.calendarExtractor = calendarExtractor;
        this.messageFileExtractor = messageFileExtractor;
        this.deviceMessageFileFinder = deviceMessageFileFinder;
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                // Network and connectivity
                .put(messageSpec(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS), new ApnCredentialsMessageEntry(apnAttributeName, usernameAttributeName, passwordAttributeName))
                .put(messageSpec(NetworkConnectivityMessage.ConfigureAutoAnswer), new MultipleAttributeMessageEntry("SetAutoAnswer", "AutoAnswerId", "AutoAnswerStart", "AutoAnswerEnd"))
                .put(messageSpec(NetworkConnectivityMessage.DisableAutoAnswer), new MultipleAttributeMessageEntry("DisableAutoAnswer", "AutoAnswerId"))
                .put(messageSpec(NetworkConnectivityMessage.ConfigureAutoConnect), new ConfigureAutoConnectModeMessageEntry())
                .put(messageSpec(NetworkConnectivityMessage.DisableAutoConnect), new MultipleAttributeMessageEntry("DisableAutoConnect", "AutoConnectId"))

                // Configuration change
                .put(messageSpec(ConfigurationChangeDeviceMessage.WriteNewPDRNumber), new MultipleAttributeMessageEntry("WritePDR", "PdrToWrite"))
                .put(messageSpec(ConfigurationChangeDeviceMessage.ConfigureAllGasParameters),
                        new MultipleAttributeMessageEntry(
                                "WriteGasParameters",
                                "GasDensity",
                                "RelativeDensity",
                                "N2_Percentage",
                                "CO2_Percentage",
                                "CO_Percentage",
                                "H2_Percentage",
                                "Methane_Percentage",
                                "CalorificValue"))
                .put(messageSpec(ConfigurationChangeDeviceMessage.ChangeMeterLocation), new MultipleAttributeMessageEntry("MeterLocation", "Location"))
                .put(messageSpec(ConfigurationChangeDeviceMessage.ConfigureGasMeterMasterData), new MultipleAttributeMessageEntry("WriteMeterMasterData", "MeterType", "MeterCaliber", "MeterSerial"))

                // Activity calendar
                .put(messageSpec(ActivityCalendarDeviceMessage.CLEAR_AND_DISABLE_PASSIVE_TARIFF), new OneTagMessageEntry("ClearPassiveTariff"))
                .put(messageSpec(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_DEFAULT_TARIFF_CODE), new EK280ActivityCalendarMessageEntry(calendarFinder, calendarExtractor, messageFileExtractor, deviceMessageFileFinder))

                // Security messages
                .put(messageSpec(SecurityMessage.CHANGE_SECURITY_KEYS), new MultipleAttributeMessageEntry("ChangeKeys", "ClientId", "WrapperKey", "NewAuthenticationKey", "NewEncryptionKey"))

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
        } else if (propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
            return messageAttribute instanceof TariffCalendar ? CodeTableBase64Builder.getXmlStringFromCodeTable((TariffCalendar) messageAttribute, this.calendarExtractor) : messageAttribute.toString();
        } else if (propertySpec.getName().equals(activityCalendarActivationDateAttributeName)) {
            return dateFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            DeviceMessageFile userFile = (DeviceMessageFile) messageAttribute;
            return this.messageFileExtractor.contents(userFile);  //Bytes of the userFile, as a string
        } else {
            return messageAttribute.toString();
        }
    }

}