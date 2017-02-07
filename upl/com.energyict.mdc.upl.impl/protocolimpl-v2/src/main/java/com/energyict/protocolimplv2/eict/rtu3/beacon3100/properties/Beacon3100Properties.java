package com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties;

import com.energyict.cbo.HexString;
import com.energyict.cbo.TimeDuration;
import com.energyict.dlms.CipheringType;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.protocol.security.AdvancedDeviceProtocolSecurityPropertySet;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.math.BigDecimal;

import static com.energyict.dlms.common.DlmsProtocolProperties.CIPHERING_TYPE;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 9/06/2015 - 17:27
 */
public class Beacon3100Properties extends DlmsProperties {

    private Integer securitySuite = null;

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
            securityProvider = new Beacon3100SecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel(), getSecuritySuite());
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

    public boolean getRequestAuthenticatedFrameCounter() {
        return getProperties().getTypedProperty(Beacon3100ConfigurationSupport.REQUEST_AUTHENTICATED_FRAME_COUNTER, false);
    }

    /**
     * The security policy of suite 1 and 2 are not backwards compatible.
     * It is now a byte where every bit is a flag:
     * 0 unused, shall be set to 0,
     * 1 unused, shall be set to 0,
     * 2 authenticated request,
     * 3 encrypted request,
     * 4 digitally signed request,
     * 5 authenticated response,
     * 6 encrypted response,
     * 7 digitally signed response
     */
    @Override
    protected int doGetDataTransportSecurityLevel() {
        if (getSecurityPropertySet() instanceof AdvancedDeviceProtocolSecurityPropertySet) {
            if (getSecuritySuite() <= 0) {
                //Suite 0 uses the old field, EncryptionDeviceAccessLevel. It is either 0, 1, 2 or 3.
                return super.doGetDataTransportSecurityLevel();
            } else {
                AdvancedDeviceProtocolSecurityPropertySet advancedSecurityPropertySet = (AdvancedDeviceProtocolSecurityPropertySet) getSecurityPropertySet();
                int result = 0;
                result |= advancedSecurityPropertySet.getRequestSecurityLevel() << 2;
                result |= advancedSecurityPropertySet.getResponseSecurityLevel() << 5;
                return result;
            }
        } else {
            return super.doGetDataTransportSecurityLevel();
        }
    }

    /**
     * Return true if bit 4 (request signed) or bit 7 (responses signed) is set in the configured security policy
     */
    @Override
    public boolean isGeneralSigning() {
        return ProtocolTools.isBitSet(getDataTransportSecurityLevel(), 4) || ProtocolTools.isBitSet(getDataTransportSecurityLevel(), 7);
    }

    @Override
    public int getSecuritySuite() {
        if (securitySuite == null) {
            securitySuite = doGetSecuritySuite();
        }
        return securitySuite;
    }

    public void setSecuritySuite(int securitySuite) {
        this.securitySuite = securitySuite;
        ((Beacon3100SecurityProvider) getSecurityProvider()).setSecuritySuite(securitySuite);
    }

    private int doGetSecuritySuite() {
        if (getSecurityPropertySet() instanceof AdvancedDeviceProtocolSecurityPropertySet) {
            AdvancedDeviceProtocolSecurityPropertySet advancedSecurityPropertySet = (AdvancedDeviceProtocolSecurityPropertySet) getSecurityPropertySet();
            return advancedSecurityPropertySet.getSecuritySuite();
        } else {
            return 0;
        }
    }

    public boolean useCachedFrameCounter() {
        return getProperties().getTypedProperty(Beacon3100ConfigurationSupport.USE_CACHED_FRAME_COUNTER, false);
    }

    public boolean validateCachedFrameCounter() {
        return getProperties().getTypedProperty(Beacon3100ConfigurationSupport.VALIDATE_CACHED_FRAMECOUNTER, true);
    }

    public int getFrameCounterRecoveryRetries() {
        return getProperties().getTypedProperty(Beacon3100ConfigurationSupport.FRAME_COUNTER_RECOVERY_RETRIES, BigDecimal.valueOf(100)).intValue();
    }

    public int getFrameCounterRecoveryStep() {
        return getProperties().getTypedProperty(Beacon3100ConfigurationSupport.FRAME_COUNTER_RECOVERY_STEP, BigDecimal.ONE).intValue();
    }

    public long getInitialFrameCounter() {
        return getProperties().getTypedProperty(Beacon3100ConfigurationSupport.INITIAL_FRAME_COUNTER, BigDecimal.valueOf(100)).longValue();
    }

    public boolean getReadOldObisCodes(){
        return getProperties().getTypedProperty(Beacon3100ConfigurationSupport.READ_OLD_OBIS_CODES, false);
    }
}