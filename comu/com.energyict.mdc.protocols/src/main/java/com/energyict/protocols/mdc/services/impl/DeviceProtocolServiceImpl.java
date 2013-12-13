package com.energyict.protocols.mdc.services.impl;

import com.energyict.comserver.adapters.meterprotocol.MeterProtocolAdapter;
import com.energyict.comserver.adapters.smartmeterprotocol.SmartMeterProtocolAdapter;
import com.energyict.comserver.exceptions.CodingException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.SmartMeterProtocol;
import org.osgi.service.component.annotations.Component;

/**
 * Provides an implementation for the {@link DeviceProtocolService} interface
 * and registers as a OSGi component.
 *
 * Copyrights EnergyICT
 * Date: 06/11/13
 * Time: 11:03
 */
@Component(name = "com.energyict.mdc.service.deviceprotocols", service = DeviceProtocolService.class)
public class DeviceProtocolServiceImpl implements DeviceProtocolService {

    @Override
    public DeviceProtocol createDeviceProtocolFor(String javaClassName) {
        try {
            Class<?> pluggableClass = this.getClass().getClassLoader().loadClass(javaClassName);
            if (DeviceProtocol.class.isAssignableFrom(pluggableClass)) {
                return (DeviceProtocol) pluggableClass.newInstance();
            }
            else {
                // Must be a lecagy pluggable class
                return checkForProtocolWrappers((com.energyict.mdw.core.Pluggable) pluggableClass.newInstance());
            }
        }
        catch (BusinessException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw CodingException.genericReflectionError(e, javaClassName);
        }
    }

    /**
     * Check if the given {@link com.energyict.mdw.core.Pluggable}
     * needs a Protocol adapter to create a {@link DeviceProtocol}.
     *
     * @param protocolInstance the instantiated protocol
     * @throws BusinessException if and only if the given Pluggable does not implement: <ul>
     * <li>{@link com.energyict.protocol.SmartMeterProtocol}</li>
     * <li>{@link com.energyict.protocol.MeterProtocol}</li>
     * </ul>
     */
    protected DeviceProtocol checkForProtocolWrappers(com.energyict.mdw.core.Pluggable protocolInstance) throws BusinessException {
        if (protocolInstance instanceof SmartMeterProtocol) {
            return new SmartMeterProtocolAdapter((SmartMeterProtocol) protocolInstance);
        }
        else if (protocolInstance instanceof MeterProtocol) {
            return new MeterProtocolAdapter((MeterProtocol) protocolInstance);
        }
        else {
            throw new BusinessException("protocolInterfaceNotSupported", "A lecagy DeviceProtocol must implement one of the following interfaces : MeterProtocol or SmartMeterProtocol");
        }
    }

}
