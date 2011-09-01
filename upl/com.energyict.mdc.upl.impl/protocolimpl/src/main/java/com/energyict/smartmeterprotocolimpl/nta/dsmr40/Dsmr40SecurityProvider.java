package com.energyict.smartmeterprotocolimpl.nta.dsmr40;

import com.energyict.genericprotocolimpl.nta.abstractnta.NTASecurityProvider;
import com.energyict.protocol.MeterProtocol;

import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 1-sep-2011
 * Time: 11:22:36
 */
public class Dsmr40SecurityProvider extends NTASecurityProvider{

    /**
     * Create a new instance of LocalSecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     */
    public Dsmr40SecurityProvider(Properties properties) {
        super(properties);
    }

    /**
     * The HLSSecret is the password of the RTU
     *
     * @return the password of the RTU
     */
    @Override
    public byte[] getHLSSecret() {
        String hexPassword = this.properties.getProperty(Dsmr40Properties.Dsmr40HexPassword);
        if(hexPassword != null){
            return com.energyict.dlms.DLMSUtils.hexStringToByteArray(hexPassword);
        } else {
            return super.getHLSSecret();
        }
    }
}
