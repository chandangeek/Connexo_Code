package com.energyict.protocolimplv2.nta.esmr50.common.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.KeyAccessorType;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaMbusDevice;
import com.energyict.sercurity.KeyRenewalInfo;

import java.util.ArrayList;
import java.util.List;

public class ESMR50MbusMessaging extends com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MbusMessaging {

    public ESMR50MbusMessaging(AbstractNtaMbusDevice mbusProtocol, PropertySpecService propertySpecService,
                               NlsService nlsService, Converter converter, LoadProfileExtractor loadProfileExtractor,
                               KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(mbusProtocol, propertySpecService, nlsService, converter, loadProfileExtractor, keyAccessorTypeExtractor);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> supportedMessages = new ArrayList<>();

        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER_AND_RESUME));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER));
        supportedMessages.add(this.get(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER));
        supportedMessages.add(this.get(FirmwareDeviceMessage.MBUS_ESMR5_FIRMWARE_UPGRADE));

        supportedMessages.add(this.get(ContactorDeviceMessage.CONTACTOR_OPEN));
        supportedMessages.add(this.get(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE));
        supportedMessages.add(this.get(ContactorDeviceMessage.CONTACTOR_CLOSE));
        supportedMessages.add(this.get(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE));
        supportedMessages.add(this.get(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE));

        supportedMessages.add(this.get(MBusSetupDeviceMessage.Decommission));

        supportedMessages.add(this.get(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST));
        supportedMessages.add(this.get(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST));

        return supportedMessages;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.P2KeyAttributeName: {
                return getKeyAccessorTypeExtractor().actualValueContent(
                        (KeyAccessorType) messageAttribute, (int) offlineDeviceMessage.getDeviceId()
                );
            }
            case DeviceMessageConstants.FUAKeyAttributeName: {
                KeyRenewalInfo keyRenewalInfo = new KeyRenewalInfo(getKeyAccessorTypeExtractor(), (KeyAccessorType) messageAttribute);
                return keyRenewalInfo.toJson();
            }
            default:
                return super.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
        }
    }

}
