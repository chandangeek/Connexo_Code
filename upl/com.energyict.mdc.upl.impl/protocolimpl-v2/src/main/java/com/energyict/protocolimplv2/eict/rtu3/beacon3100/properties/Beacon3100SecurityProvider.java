package com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties;

import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.protocolimplv2.GeneralCipheringSecurityProvider;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;

import java.util.Random;

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
    public byte[] getSessionKey(SecurityContext securityContext) {
        if (sessionKey == null) {
            setInitialFrameCounter(1);  //New key in use, so start using a new frame counter
            securityContext.setFrameCounter(1);     //TODO response FC also?
            sessionKey = new byte[16];
            Random rnd = new Random();
            rnd.nextBytes(sessionKey);
        }
        return sessionKey;
    }

    @Override
    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }
}