package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.KeyAccessorType;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import com.energyict.protocolcommon.exceptions.CodingException;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.DeviceMessageSpecSupplier;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaMbusDevice;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractDlmsMessaging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link DeviceMessageSupport} for the DSMR 2.3 Mbus slave device, that:
 * - Formats the device message attributes from objects to proper string values
 * - Executes a given message
 * - Has a list of all supported device message specs
 * <p/>
 *
 * @author sva
 * @since 29/11/13 - 14:17
 */
public class Dsmr23MbusMessaging extends AbstractDlmsMessaging implements DeviceMessageSupport {

    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;
    private final LoadProfileExtractor loadProfileExtractor;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;

    public Dsmr23MbusMessaging(AbstractNtaMbusDevice mbusProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, LoadProfileExtractor loadProfileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(mbusProtocol.getMeterProtocol());
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
        this.loadProfileExtractor = loadProfileExtractor;
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
    }

    protected DeviceMessageSpec get(DeviceMessageSpecSupplier supplier) {
        return supplier.get(this.propertySpecService, this.nlsService, this.converter);
    }

    public KeyAccessorTypeExtractor getKeyAccessorTypeExtractor() {
        return keyAccessorTypeExtractor;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> supportedMessages = new ArrayList<>();

        // firmware messages
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER_AND_RESUME));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER));

        supportedMessages.add(this.get(ContactorDeviceMessage.CONTACTOR_OPEN));
        supportedMessages.add(this.get(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE));
        supportedMessages.add(this.get(ContactorDeviceMessage.CONTACTOR_CLOSE));
        supportedMessages.add(this.get(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE));
        supportedMessages.add(this.get(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE));

        supportedMessages.add(this.get(MBusSetupDeviceMessage.Decommission));
        supportedMessages.add(this.get(MBusSetupDeviceMessage.SetEncryptionKeys));
        supportedMessages.add(this.get(MBusSetupDeviceMessage.UseCorrectedValues));
        supportedMessages.add(this.get(MBusSetupDeviceMessage.UseUncorrectedValues));

        supportedMessages.add(this.get(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST));
        supportedMessages.add(this.get(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST));
        return supportedMessages;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.loadProfileAttributeName:
                return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute, this.loadProfileExtractor);
            case DeviceMessageConstants.openKeyAttributeName:
            case DeviceMessageConstants.transferKeyAttributeName:
                return this.keyAccessorTypeExtractor.actualValueContent((KeyAccessorType) messageAttribute);
            case DeviceMessageConstants.fromDateAttributeName:
            case DeviceMessageConstants.toDateAttributeName:
            case DeviceMessageConstants.contactorActivationDateAttributeName:
            case DeviceMessageConstants.firmwareUpdateActivationDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());  //Epoch (millis)
            default:
                return messageAttribute.toString();  //Used for String and BigDecimal attributes
        }
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        throw CodingException.unsupportedMethod(this.getClass(), "updateSentMessages");
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        throw CodingException.unsupportedMethod(this.getClass(), "executePendingMessages");
    }
}