package com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.SecurityPolicyMapper;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.protocol.security.AdvancedDeviceProtocolSecurityPropertySet;
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
    public int getDataTransportSecurityLevel() {
        if (getSecurityPropertySet() instanceof AdvancedDeviceProtocolSecurityPropertySet) {
            AdvancedDeviceProtocolSecurityPropertySet advancedSecurityPropertySet = (AdvancedDeviceProtocolSecurityPropertySet) getSecurityPropertySet();
            if (advancedSecurityPropertySet.getSecuritySuite() == 0) {
                //Suite 0 uses the old field, EncryptionDeviceAccessLevel. It is either 0, 1, 2 or 3.
                return super.getDataTransportSecurityLevel();
            } else {
                int result = 0;
                SecurityPolicyMapper mapper = new SecurityPolicyMapper(advancedSecurityPropertySet);
                result |= (mapper.isRequestAuthenticated() ? 1 : 0) << SecurityPolicyMapper.REQUESTS_AUTHENTICATED_FLAG;
                result |= (mapper.isRequestEncrypted() ? 1 : 0) << SecurityPolicyMapper.REQUESTS_ENCRYPTED_FLAG;
                result |= (mapper.isRequestSigned() ? 1 : 0) << SecurityPolicyMapper.REQUESTS_SIGNED_FLAG;
                result |= (mapper.isResponseAuthenticated() ? 1 : 0) << SecurityPolicyMapper.RESPONSES_AUTHENTICATED_FLAG;
                result |= (mapper.isResponseEncrypted() ? 1 : 0) << SecurityPolicyMapper.RESPONSES_ENCRYPTED_FLAG;
                result |= (mapper.isResponseSigned() ? 1 : 0) << SecurityPolicyMapper.RESPONSES_SIGNED_FLAG;
                return result;
            }
        } else {
            return super.getDataTransportSecurityLevel();
        }
    }

    @Override
    public boolean isGeneralSigning() {
        if (getSecurityPropertySet() instanceof AdvancedDeviceProtocolSecurityPropertySet) {
            SecurityPolicyMapper mapper = new SecurityPolicyMapper((AdvancedDeviceProtocolSecurityPropertySet) getSecurityPropertySet());
            return mapper.isRequestSigned() || mapper.isResponseSigned();
        } else {
            return false;
        }
    }

    @Override
    public int getSecuritySuite() {
        if (getSecurityPropertySet() instanceof AdvancedDeviceProtocolSecurityPropertySet) {
            AdvancedDeviceProtocolSecurityPropertySet advancedSecurityPropertySet = (AdvancedDeviceProtocolSecurityPropertySet) getSecurityPropertySet();
            return advancedSecurityPropertySet.getSecuritySuite();
        } else {
            return 0;
        }
    }
}