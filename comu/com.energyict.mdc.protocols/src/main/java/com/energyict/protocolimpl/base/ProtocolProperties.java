package com.energyict.protocolimpl.base;

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;

import java.util.List;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 15:59:31
 */
public interface ProtocolProperties {

    List<String> getOptionalKeys();

    List<String> getRequiredKeys();

    void addProperties(Properties properties);

    void validateProperties() throws MissingPropertyException, InvalidPropertyException;

}
