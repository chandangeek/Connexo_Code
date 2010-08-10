package com.energyict.protocolimpl.elster.ctr;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.protocol.MeterProtocol;

import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 10-aug-2010
 * Time: 9:11:32
 */
public interface ProtocolProperties extends ConfigurationSupport {

    String PASSWORD = MeterProtocol.PASSWORD;
    String ENCRYPTION_KEY = "EncryptionKey";

    void initProperties(Properties properties);

    String getPassword();

    String getEncryptionKey();

}
