package com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties;

import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.protocolimplv2.GeneralCipheringSecurityProvider;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocolimpl.dlms.g3.G3RespondingFrameCounterHandler;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;

import java.security.SecureRandom;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 21/01/2016 - 14:16
 */
public class Beacon3100SecurityProvider extends NTASecurityProvider implements GeneralCipheringSecurityProvider {

    private byte[] sessionKey;

    public Beacon3100SecurityProvider(TypedProperties properties, int authenticationDeviceAccessLevel) {
        super(properties, authenticationDeviceAccessLevel);
        setRespondingFrameCounterHandling(new G3RespondingFrameCounterHandler(DLMSConnectionException.REASON_ABORT_INVALID_FRAMECOUNTER));
    }

    /**
     * Override, the KEK of the Beacon is stored in property DlmsWanKEK
     */
    @Override
    public byte[] getMasterKey() {
        if (this.masterKey == null) {
            String hex = properties.getTypedProperty(Beacon3100ConfigurationSupport.DLMS_WAN_KEK);
            if (hex == null || hex.isEmpty()) {
                throw DeviceConfigurationException.missingProperty(Beacon3100ConfigurationSupport.DLMS_WAN_KEK);
            }
            this.masterKey = DLMSUtils.hexStringToByteArray(hex);
        }
        return this.masterKey;
    }


    @Override
    public byte[] getSessionKey() {
        if (sessionKey == null) {
            sessionKey = new byte[16];
            SecureRandom rnd = new SecureRandom();
            rnd.nextBytes(sessionKey);
        }
        return sessionKey;
    }

    @Override
    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }
}