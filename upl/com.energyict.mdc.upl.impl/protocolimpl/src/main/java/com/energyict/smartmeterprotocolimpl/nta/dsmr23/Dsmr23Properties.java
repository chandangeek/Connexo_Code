package com.energyict.smartmeterprotocolimpl.nta.dsmr23;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.KeyAccessorType;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.DescriptionTranslationKey;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.MASTERKEY;

/**
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 11:26:48
 */
public class Dsmr23Properties extends DlmsProtocolProperties {

    private static final String OLD_MBUS_DISCOVERY = "OldMbusDiscovery";
    private static final String FIX_MBUS_HEX_SHORT_ID = "FixMbusHexShortId";

    private static final int DEFAULT_CLIENT_MAC_ADDRESS = 1;

    private final PropertySpecService propertySpecService;

    public Dsmr23Properties(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public Dsmr23Properties(TypedProperties properties, PropertySpecService propertySpecService) {
        super(properties);
        this.propertySpecService = propertySpecService;
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return new ArrayList<>(Arrays.asList(
                UPLPropertySpecFactory.specBuilder(ADDRESSING_MODE, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(SERVER_MAC_ADDRESS, false, this.propertySpecService::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(CONNECTION, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_FORCED_DELAY, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_DELAY_AFTER_ERROR, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(INFORMATION_FIELD_SIZE, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(MAX_REC_PDU_SIZE, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_RETRIES, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(PK_TIMEOUT, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(ROUND_TRIP_CORRECTION, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(BULK_REQUEST, false, this.propertySpecService::booleanSpec).finish(),
                UPLPropertySpecFactory.specBuilder(CIPHERING_TYPE, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(NTA_SIMULATION_TOOL, false, this.propertySpecService::booleanSpec).finish(),
                UPLPropertySpecFactory.specBuilder(OLD_MBUS_DISCOVERY, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(FIX_MBUS_HEX_SHORT_ID, false, this.propertySpecService::integerSpec).finish(),
                UPLPropertySpecFactory.specBuilder(WAKE_UP, false, this.propertySpecService::booleanSpec).finish(),
                this.keyAccessorTypeReferencePropertySpec(MASTERKEY, PropertyTranslationKeys.DLMS_MASTERKEY))
        );
    }

    private PropertySpec keyAccessorTypeReferencePropertySpec(String name, TranslationKey translationKey) {
        return this.propertySpecService
                .referenceSpec(KeyAccessorType.class.getName())
                .named(name, translationKey)
                .describedAs(new DescriptionTranslationKey(translationKey))
                .finish();
    }

    protected boolean securityLevelIsRequired() {
        return true;
    }


    @ProtocolProperty
    public boolean getFixMbusHexShortId() {
        return getBooleanProperty(FIX_MBUS_HEX_SHORT_ID, false);
    }

    @ProtocolProperty
    public boolean getOldMbusDiscovery() {
        return getBooleanProperty(OLD_MBUS_DISCOVERY, false);
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        return new NTASecurityProvider(getProtocolProperties());
    }

    @ProtocolProperty
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, true);
    }

    @ProtocolProperty
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, DEFAULT_CLIENT_MAC_ADDRESS);
    }

}