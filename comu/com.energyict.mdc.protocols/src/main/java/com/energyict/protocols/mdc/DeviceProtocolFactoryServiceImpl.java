package com.energyict.protocols.mdc;

import com.energyict.cbo.BusinessException;
import com.energyict.comserver.adapters.meterprotocol.MeterProtocolAdapter;
import com.energyict.comserver.adapters.smartmeterprotocol.SmartMeterProtocolAdapter;
import com.energyict.comserver.exceptions.CodingException;
import com.energyict.cpo.Environment;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.services.DeviceProtocolFactoryService;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Pluggable;
import com.energyict.mdw.core.PluggableClass;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.SmartMeterProtocol;
import com.energyict.protocols.common.MdcProtocolReflectionMagic;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * Copyrights EnergyICT
 * Date: 06/11/13
 * Time: 11:03
 */
@Component(name="com.energyict.mdc.protocols", service=DeviceProtocolFactoryService.class, immediate = true)
public class DeviceProtocolFactoryServiceImpl implements DeviceProtocolFactoryService {

    public DeviceProtocolFactoryServiceImpl() {
    }

    @Activate
    public void activate(){
        MeteringWarehouse.createBatchContext(true);
        System.out.println("Activating DeviceProtocolFactoryService");
    }

    @Deactivate
    public void deactivate(){
        Environment.getDefault().closeConnection();
        System.out.println("Deactivating DeviceProtocolFactoryService");
    }

    @Override
    public DeviceProtocol createDeviceProtocolFor(PluggableClass pluggableClass) {
        try {
            Pluggable pluggable = (Pluggable) (Class.forName(pluggableClass.getJavaClassName())).newInstance();
            return checkForProtocolWrappers(pluggable);
        } catch (BusinessException e) {
            throw CodingException.reflectionError(e, pluggableClass);
        } catch (ClassNotFoundException e) {
            throw CodingException.genericReflectionError(e, pluggableClass.getClass());
        } catch (InstantiationException e) {
            throw CodingException.genericReflectionError(e, pluggableClass.getClass());
        } catch (IllegalAccessException e) {
            throw CodingException.genericReflectionError(e, pluggableClass.getClass());
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
        MdcProtocolReflectionMagic protocolReflectionMagic = new MdcProtocolReflectionMagic();
        if (protocolInstance instanceof SmartMeterProtocol) {
            return new SmartMeterProtocolAdapter((SmartMeterProtocol) protocolInstance);
        } else if (protocolInstance instanceof MeterProtocol) {
            return new MeterProtocolAdapter((MeterProtocol) protocolInstance, protocolReflectionMagic);
        } else if (protocolInstance instanceof DeviceProtocol) {
            return (DeviceProtocol) protocolInstance;
        } else {
            throw new BusinessException("protocolInterfaceNotSupported", "A DeviceProtocol must implement one of the following interfaces : MeterProtocol, SmartMeterProtocol or DeviceProtocol");
        }
    }
}
