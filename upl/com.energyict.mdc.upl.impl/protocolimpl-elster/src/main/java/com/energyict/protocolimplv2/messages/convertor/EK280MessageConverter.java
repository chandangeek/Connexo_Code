package com.energyict.protocolimplv2.messages.convertor;

import com.elster.protocolimpl.dlms.tariff.CodeTableBase64Builder;
import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.UserFile;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * @author sva
 * @since 13/08/2015 - 16:04
 */
public class EK280MessageConverter extends AbstractMessageConverter {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {
        // Network and connectivity
        registry.put(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS, new ApnCredentialsMessageEntry(apnAttributeName, usernameAttributeName, passwordAttributeName));
        registry.put(NetworkConnectivityMessage.ConfigureAutoAnswer, new MultipleAttributeMessageEntry("SetAutoAnswer", "AutoAnswerId", "AutoAnswerStart", "AutoAnswerEnd"));
        registry.put(NetworkConnectivityMessage.DisableAutoAnswer, new MultipleAttributeMessageEntry("DisableAutoAnswer", "AutoAnswerId"));
        registry.put(NetworkConnectivityMessage.ConfigureAutoConnect, new ConfigureAutoConnectModeMessageEntry());
        registry.put(NetworkConnectivityMessage.DisableAutoConnect, new MultipleAttributeMessageEntry("DisableAutoConnect", "AutoConnectId"));

        // Configuration change
        registry.put(ConfigurationChangeDeviceMessage.WriteNewPDRNumber, new MultipleAttributeMessageEntry("WritePDR", "PdrToWrite"));
        registry.put(ConfigurationChangeDeviceMessage.ConfigureAllGasParameters, new MultipleAttributeMessageEntry(
                "WriteGasParameters",
                "GasDensity",
                "RelativeDensity",
                "N2_Percentage",
                "CO2_Percentage",
                "CO_Percentage",
                "H2_Percentage",
                "Methane_Percentage",
                "CalorificValue"
        ));
        registry.put(ConfigurationChangeDeviceMessage.ChangeMeterLocation, new MultipleAttributeMessageEntry("MeterLocation", "Location"));
        registry.put(ConfigurationChangeDeviceMessage.ConfigureGasMeterMasterData, new MultipleAttributeMessageEntry("WriteMeterMasterData", "MeterType", "MeterCaliber", "MeterSerial"));

        // Activity calendar
        registry.put(ActivityCalendarDeviceMessage.CLEAR_AND_DISABLE_PASSIVE_TARIFF, new OneTagMessageEntry("ClearPassiveTariff"));
        registry.put(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME_AND_DEFAULT_TARIFF_CODE, new EK280ActivityCalendarMessageEntry());

        // Security messages
        registry.put(SecurityMessage.CHANGE_SECURITY_KEYS, new MultipleAttributeMessageEntry("ChangeKeys", "ClientId", "WrapperKey", "NewAuthenticationKey", "NewEncryptionKey"));

        // Firmware upgrade
        registry.put(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE, new FirmwareUdateWithUserFileMessageEntry(firmwareUpdateUserFileAttributeName));
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public EK280MessageConverter() {
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
        } else if (propertySpec.getName().equals(activityCalendarCodeTableAttributeName)) {
            return messageAttribute instanceof Code ? CodeTableBase64Builder.getXmlStringFromCodeTable((Code) messageAttribute) : messageAttribute.toString();
        } else if (propertySpec.getName().equals(activityCalendarActivationDateAttributeName)) {
            return dateFormat.format((Date) messageAttribute);
        } else if (propertySpec.getName().equals(firmwareUpdateUserFileAttributeName)) {
            UserFile userFile = (UserFile) messageAttribute;
            return new String(userFile.loadFileInByteArray());  //Bytes of the userFile, as a string
        } else {
            return messageAttribute.toString();
        }
    }
}
