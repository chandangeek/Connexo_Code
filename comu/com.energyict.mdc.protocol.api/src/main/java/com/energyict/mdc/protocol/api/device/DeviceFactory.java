package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.ApplicationComponent;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpec;

import java.util.List;

/**
 * Defines the behavior of an {@link ApplicationComponent}
 * that is capable of finding {@link Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-08 (16:34)
 */
public interface DeviceFactory {

    public <C extends Channel, LP extends LoadProfile<C>, R extends Register> List<Device<C, LP, R>> findDevicesBySerialNumber(String serialNumber);

    public <C extends Channel, LP extends LoadProfile<C>, R extends Register> List<Device<C, LP, R>> findDevicesByNotInheritedProtocolProperty(PropertySpec propertySpec, Object propertyValue);

}