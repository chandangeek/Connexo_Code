package com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.GeneralCipheringKeyType;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.DlmsProperties;

/**
 * Copyrights EnergyICT
 * Date: 10/10/16
 * Time: 14:30
 */
public class Beacon3100Properties extends DlmsProperties {
    private Integer securitySuite = null;

    public Beacon3100Properties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    /**
     * Property indicating to read the cache out (useful because there's no config change state)
     */
    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(TranslationKeys.READCACHE_PROPERTY.getPropertySpecName(), false);
    }

    @Override
    public ConformanceBlock getConformanceBlock() {
        ConformanceBlock conformanceBlock = super.getConformanceBlock();
        conformanceBlock.setGeneralProtection(isGeneralProtection());
        return conformanceBlock;
    }

    private boolean isGeneralProtection() {
        return isGeneralSigning() ||
                getCipheringType().equals(CipheringType.GENERAL_DEDICATED) ||
                getCipheringType().equals(CipheringType.GENERAL_GLOBAL) ||
                getCipheringType().equals(CipheringType.GENERAL_CIPHERING);
    }

    /**
     * Return true if bit 4 (request signed) or bit 7 (responses signed) is set in the configured security policy
     */
    @Override
    public boolean isGeneralSigning() {
        return ProtocolTools.isBitSet(getDataTransportSecurityLevel(), 4) || ProtocolTools.isBitSet(getDataTransportSecurityLevel(), 7);
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new Beacon3100SecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel());
        }
        return securityProvider;
    }

    public GeneralCipheringKeyType getGeneralCipheringKeyType() {
        String keyTypeDescription = getProperties().getStringProperty(DlmsSessionProperties.GENERAL_CIPHERING_KEY_TYPE);

        if (keyTypeDescription == null && getCipheringType().equals(CipheringType.GENERAL_CIPHERING)) {
            //In the case of general-ciphering, the key type is a required property
            throw DeviceConfigurationException.missingProperty(DlmsSessionProperties.GENERAL_CIPHERING_KEY_TYPE);
        } else {
            return keyTypeDescription == null ? null : GeneralCipheringKeyType.fromDescription(keyTypeDescription);
        }
    }

    public boolean getRequestAuthenticatedFrameCounter() {
        return getProperties().getTypedProperty(Beacon3100ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER, false);
    }
}
