package com.energyict.protocols.mdc.services;

import com.energyict.comserver.adapters.meterprotocol.MeterProtocolAdapter;
import com.energyict.comserver.adapters.smartmeterprotocol.SmartMeterProtocolAdapter;
import com.energyict.comserver.exceptions.CodingException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.Pluggable;
import com.energyict.mdc.services.DeviceProtocolService;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.SmartMeterProtocol;
import org.osgi.service.component.annotations.Component;

/**
 * Copyrights EnergyICT
 * Date: 06/11/13
 * Time: 11:03
 */
@Component(name = "com.energyict.mdc.service.deviceprotocols", service = DeviceProtocolService.class, immediate = true)
public class DeviceProtocolServiceImpl extends AbstractPluggableClassServiceImpl implements DeviceProtocolService {

    @Override
    public DeviceProtocol createDeviceProtocolFor(DeviceProtocolPluggableClass pluggableClass) {
        try {
            Pluggable pluggable = (Pluggable) (Class.forName(pluggableClass.getJavaClassName())).newInstance();
            return checkForProtocolWrappers(pluggable);
        } catch (BusinessException e) {
            throw CodingException.reflectionError(e, pluggableClass);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw CodingException.genericReflectionError(e, pluggableClass.getJavaClassName());
        }
    }

    @Override
    public DeviceProtocol createDeviceProtocolFor(String javaClassName) {
        try {
            Pluggable pluggable = (Pluggable) (Class.forName(javaClassName)).newInstance();
            return checkForProtocolWrappers(pluggable);
        } catch (BusinessException e) {
            throw CodingException.genericReflectionError(e, javaClassName);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw CodingException.genericReflectionError(e, javaClassName);
        }
    }

    /**
     * Check if the given {@link Pluggable} needs a Protocol adapter to create a {@link DeviceProtocol}
     *
     * @param protocolInstance the instantiated protocol
     * @throws BusinessException if and only if the given Pluggable does not implement: <ul>
     *                           <li>{@link com.energyict.protocol.SmartMeterProtocol}</li>
     *                           <li>{@link com.energyict.protocol.MeterProtocol}</li>
     *                           <li>{@link DeviceProtocol}</li>
     *                           </ul>
     */
    protected DeviceProtocol checkForProtocolWrappers(Pluggable protocolInstance) throws BusinessException {
        if (protocolInstance instanceof SmartMeterProtocol) {
            return new SmartMeterProtocolAdapter((SmartMeterProtocol) protocolInstance);
        } else if (protocolInstance instanceof MeterProtocol) {
            return new MeterProtocolAdapter((MeterProtocol) protocolInstance);
        } else if (protocolInstance instanceof DeviceProtocol) {
            return (DeviceProtocol) protocolInstance;
        } else {
            throw new BusinessException("protocolInterfaceNotSupported", "A DeviceProtocol must implement one of the following interfaces : MeterProtocol, SmartMeterProtocol or DeviceProtocol");
        }
    }

}
