/*
 * ProtocolInstantiator.java
 *
 * Created on 2 juni 2005, 10:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocol;

import com.energyict.mdc.upl.MeterProtocol;
import com.energyict.mdc.upl.SmartMeterProtocol;
import com.energyict.mdc.upl.cache.CacheMechanism;
import com.energyict.mdc.upl.properties.PropertySpec;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Koen
 */
public interface ProtocolInstantiator {

    MeterProtocol getMeterProtocol();

    SmartMeterProtocol getSmartMeterProtocol();

    HHUEnabler getHHUEnabler();

    HalfDuplexEnabler getHalfDuplexEnabler();

    SerialNumber getSerialNumber();

    CacheMechanism getCacheMechanism();

    RegisterProtocol getRegisterProtocol();

    BulkRegisterProtocol getBulkRegisterProtocol();

    MultipleLoadProfileSupport getMultipleLoadProfileSupport();

    boolean isRegisterProtocolEnabled();

    boolean isBulkRegisterProtocolEnabled();

    boolean isHHUEnabled();

    boolean isHalfDuplexEnabled();

    boolean isSerialNumber();

    boolean isCacheMechanism();

    boolean isMultipleLoadProfileSupported();

    List<PropertySpec> getPropertySpecs();

    default List<PropertySpec> getOptionalKeys() {
        return this.getPropertySpecs().stream().filter(spec -> !spec.isRequired()).collect(Collectors.toList());
    }

    default List<PropertySpec> getRequiredKeys() {
        return this.getPropertySpecs().stream().filter(PropertySpec::isRequired).collect(Collectors.toList());
    }

    void buildInstance(String className) throws IOException;

    EventMapper getEventMapper();

}