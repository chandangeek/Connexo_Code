package com.energyict.protocolimpl.base;

import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 15:59:31
 */
public interface ProtocolProperties extends HasDynamicProperties {

    void setProperties(Properties properties) throws PropertyValidationException;

}