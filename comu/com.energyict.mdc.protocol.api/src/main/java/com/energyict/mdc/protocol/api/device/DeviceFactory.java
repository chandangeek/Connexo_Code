package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.protocol.api.ConnectionType;

import com.elster.jupiter.properties.PropertySpec;

import java.util.List;

/**
 * Defines the behavior of a component
 * that is capable of finding {@link BaseDevice}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-08 (16:34)
 */
public interface DeviceFactory {

    public <C extends BaseChannel, LP extends BaseLoadProfile<C>, R extends BaseRegister> List<BaseDevice<C, LP, R>> findDevicesBySerialNumber(String serialNumber);

    public <C extends BaseChannel, LP extends BaseLoadProfile<C>, R extends BaseRegister> List<BaseDevice<C, LP, R>> findDevicesByNotInheritedProtocolProperty(PropertySpec propertySpec, Object propertyValue);

    public BaseDevice findById(long id);

    public List<BaseDevice> findByConnectionTypeProperty(Class<? extends ConnectionType> connectionTypeClass, String propertyName, Object propertyValue);
}