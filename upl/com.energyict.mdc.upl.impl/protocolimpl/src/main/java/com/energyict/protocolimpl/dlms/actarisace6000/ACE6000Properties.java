package com.energyict.protocolimpl.dlms.actarisace6000;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.DLMSReference;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Created by avrancea on 3/15/2017.
 */
public class ACE6000Properties extends DlmsProtocolProperties {


    private final PropertySpecService propertySpecService;

    public ACE6000Properties(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public DLMSReference getReference() {
        return DLMSReference.SN;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.specBuilder("Timeout", false, PropertyTranslationKeys.DLMS_TIMEOUT, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder("Retries", false, PropertyTranslationKeys.DLMS_RETRIES, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder("DelayAfterFail", false, PropertyTranslationKeys.DLMS_DELAY_AFTERFAIL, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder("RequestTimeZone", false, PropertyTranslationKeys.DLMS_REQUEST_TIME_ZONE, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder("FirmwareVersion", false, PropertyTranslationKeys.DLMS_FIRMWARE_VERSION, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder("ServerUpperMacAddress", false, PropertyTranslationKeys.DLMS_SERVER_UPPER_MAC_ADDRESS, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder("ServerLowerMacAddress", false, PropertyTranslationKeys.DLMS_SERVER_LOWER_MAC_ADDRESS, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder("ExtendedLogging", false, PropertyTranslationKeys.DLMS_EXTENDED_LOGGING, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder("AddressingMode", false, PropertyTranslationKeys.DLMS_ADDRESSING_MODE, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder("AlarmStatusFlagChannel", false, PropertyTranslationKeys.DLMS_ALARM_STATUS_FLAG_CHANNEL, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder("Password", false, PropertyTranslationKeys.DLMS_PASSWORD, this.propertySpecService::stringSpec).finish());
    }
}
