package com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.idis.IDIS;
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

    public boolean getRequestAuthenticatedFrameCounter() {
        return getProperties().getTypedProperty(Beacon3100ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER, false);
    }

    @Override
    public CipheringType getCipheringType() {
        String cipheringDescription = getProperties().getTypedProperty(DlmsProtocolProperties.CIPHERING_TYPE, CipheringType.GLOBAL.getDescription());
        for (CipheringType cipheringType : CipheringType.values()) {
            if (cipheringType.getDescription().equals(cipheringDescription)) {
                return cipheringType;
            }
        }
        return CipheringType.GLOBAL;
    }


    @Override
    public byte[] getSystemIdentifier() {
        //Property CallingAPTitle is used as system title in the AARQ
        final HexString callingAPTitle = getProperties().getTypedProperty(IDIS.CALLING_AP_TITLE);
        if (callingAPTitle == null || callingAPTitle.getContent() == null || callingAPTitle.getContent().isEmpty()) {
            return super.getSystemIdentifier();
        } else {
            return ProtocolTools.getBytesFromHexString(callingAPTitle.getContent(), "");
        }
    }

    /**
     * PollingDelay is 0 ms by default, to disable polling. This will optimize the reading of responses from the Beacon device
     */
    @Override
    public TimeDuration getPollingDelay() {
        return getProperties().getTypedProperty(Beacon3100ConfigurationSupport.POLLING_DELAY, new TimeDuration(0));
    }
}
