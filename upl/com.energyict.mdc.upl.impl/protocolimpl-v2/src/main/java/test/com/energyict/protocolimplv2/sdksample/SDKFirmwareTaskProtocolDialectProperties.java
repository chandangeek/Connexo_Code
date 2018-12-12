package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.properties.nls.PropertyTranslationKeys;
import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 9:59
 */
public class SDKFirmwareTaskProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    public static final String activeMeterFirmwareVersion = "activeMeterFirmwareVersion";
    public static final String passiveMeterFirmwareVersion = "passiveMeterFirmwareVersion";
    public static final String activeCommunicationFirmwareVersion = "activeCommunicationFirmwareVersion";
    public static final String passiveCommunicationFirmwareVersion = "passiveCommunicationFirmwareVersion";

    public SDKFirmwareTaskProtocolDialectProperties(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    public SDKFirmwareTaskProtocolDialectProperties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectTranslationKeys.SDK_SAMPLE_FIRMWARE.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.SDK_SAMPLE_FIRMWARE).format();
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.specBuilder(activeMeterFirmwareVersion, false, PropertyTranslationKeys.SDKSAMPLE_ACTIVE_METER_FIRMWARE_VERSION, getPropertySpecService()::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(passiveMeterFirmwareVersion, false, PropertyTranslationKeys.SDKSAMPLE_PASSIVE_METER_FIRMWARE_VERSION,getPropertySpecService()::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(activeCommunicationFirmwareVersion, false, PropertyTranslationKeys.SDKSAMPLE_ACTIVE_COMMUNICATION_FIRMWARE_VERSION, getPropertySpecService()::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(passiveCommunicationFirmwareVersion, false, PropertyTranslationKeys.SDKSAMPLE_PASSIVE_COMMUNICATION_FIRMWARE_VERSION, getPropertySpecService()::stringSpec).finish()
        );
    }
}