package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.properties.PropertySpec;
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
public class SDKTopologyTaskProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    public static final String slaveOneSerialNumberPropertyName = "SlaveOneSerialNumber";
    public static final String slaveTwoSerialNumberPropertyName = "SlaveTwoSerialNumber";

    public SDKTopologyTaskProtocolDialectProperties(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    public SDKTopologyTaskProtocolDialectProperties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectTranslationKeys.SDK_SAMPLE_TOPOLOGY_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.SDK_SAMPLE_TOPOLOGY_DIALECT_NAME).format();
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                getSlaveOneSerialNumber(),
                getSlaveTwoSerialNumber());
    }

    private PropertySpec getSlaveOneSerialNumber() {
        return UPLPropertySpecFactory.specBuilder(slaveOneSerialNumberPropertyName, false, PropertyTranslationKeys.SDKSAMPLE_SLAVE_ONE_SERIAL_NUMBER, getPropertySpecService()::stringSpec).finish();
    }

    public PropertySpec getSlaveTwoSerialNumber() {
        return UPLPropertySpecFactory.specBuilder(slaveTwoSerialNumberPropertyName, false, PropertyTranslationKeys.SDKSAMPLE_SLAVE_TWO_SERIAL_NUMBER, getPropertySpecService()::stringSpec).finish();
    }
}