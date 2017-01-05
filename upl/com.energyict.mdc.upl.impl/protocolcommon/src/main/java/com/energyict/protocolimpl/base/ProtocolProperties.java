package com.energyict.protocolimpl.base;

import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 15:59:31
 */
public interface ProtocolProperties extends HasDynamicProperties {

    void setUPLProperties(TypedProperties properties) throws PropertyValidationException;

}