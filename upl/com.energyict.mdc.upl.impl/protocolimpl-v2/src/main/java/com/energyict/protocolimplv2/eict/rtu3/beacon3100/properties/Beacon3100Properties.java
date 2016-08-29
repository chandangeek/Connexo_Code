package com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties;

import com.energyict.cbo.TimeDuration;
import com.energyict.dlms.CipheringType;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540ConfigurationSupport;
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
        //TODO add general signing, deduce it from the security set access level: should somehow indicate that requests and/or responses use signing
        return getCipheringType().equals(CipheringType.GENERAL_DEDICATED) || getCipheringType().equals(CipheringType.GENERAL_GLOBAL) || getCipheringType().equals(CipheringType.GENERAL_CIPHERING);
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
     * PollingDelay is 0 ms by default, to disable polling. This will optimize the reading of responses from the Beacon device
     */
    @Override
    public TimeDuration getPollingDelay() {
        return getProperties().getTypedProperty(Beacon3100ConfigurationSupport.POLLING_DELAY, new TimeDuration(0));
    }

    public boolean getRequestAuthenticatedFrameCounter() {
        return getProperties().getTypedProperty(Beacon3100ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER, false);
    }
}