package com.energyict.protocolimpl.elster.ctr;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.base.AbstractProtocol;

import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 10-aug-2010
 * Time: 9:11:32
 */
public interface ProtocolProperties extends ConfigurationSupport {

    String PASSWORD = MeterProtocol.PASSWORD;
    String ENCRYPTIONKEY = "EncryptionKey";
    String PROFILEINTERVAL = MeterProtocol.PROFILEINTERVAL;
    String SERIALNUMBER = MeterProtocol.SERIALNUMBER;
    String RETRIES = AbstractProtocol.PROP_RETRIES;
    String TIMEOUT = AbstractProtocol.PROP_TIMEOUT;
    String FORCEDDELAY = AbstractProtocol.PROP_FORCED_DELAY;
    String NODEADDRESS = AbstractProtocol.NODEID;

    /**
     * 
     * @param properties
     */
    void initProperties(Properties properties);

    /**
     * Get the device password
     * @return
     */
    String getPassword();

    /**
     * Get the encryption key used to communicate to the device
     * @return
     */
    String getEncryptionKey();

    /**
     * Get the device serial number
     * @return
     */
    String getSerialNumber();

    /**
     * Get the profile interval in seconds
     * @return
     */
    int getrofileInterval();

    /**
     * The number of reties to use while communicating to the device
     * @return
     */
    int getRetries();

    /**
     * The timeout before throwing an exception while waiting for a response from the device
     * @return
     */
    int getTimeout();

    /**
     * The delay in millis before sending each request
     * @return
     */
    int getForcedDelay();

    /**
     * Getter for the address of the device
     * @return
     */
    int getNodeAddress();

}
