package com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import static com.energyict.dlms.common.DlmsProtocolProperties.CIPHERING_TYPE;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 9/06/2015 - 17:27
 */
public class Beacon3100Properties extends DlmsProperties {

    /**
     * Property indicating to read the cache out (useful because there's no config change state)
     */
    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(Beacon3100ConfigurationSupport.READCACHE_PROPERTY, false);
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

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new Beacon3100SecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel());
        }
        return securityProvider;
    }

    @Override
    public CipheringType getCipheringType() {
        String cipheringDescription = getProperties().getTypedProperty(CIPHERING_TYPE, CipheringType.GLOBAL.getDescription());
        for (CipheringType cipheringType : CipheringType.values()) {
            if (cipheringType.getDescription().equals(cipheringDescription)) {
                return cipheringType;
            }
        }
        return CipheringType.GLOBAL;
    }

    /**
     * Optimize the reading of responses from the Beacon device
     */
    @Override
    public boolean isUsePolling() {
        return false;
    }

    @Override
    public boolean isGeneralSigning() {
        //TODO add general signing, deduce it from the security set access level: should somehow indicate that requests and/or responses use signing
        return false;   //TODO find out somehow, it will probably be in the new security sets levels
    }

    @Override
    public int getSecuritySuite() {
        return 1;   //TODO get this from security property
    }
}