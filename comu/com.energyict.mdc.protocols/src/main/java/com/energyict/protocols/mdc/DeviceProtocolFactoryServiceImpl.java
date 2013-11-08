package com.energyict.protocols.mdc;

import com.energyict.cbo.BusinessException;
import com.energyict.comserver.adapters.meterprotocol.MeterProtocolAdapter;
import com.energyict.comserver.adapters.smartmeterprotocol.SmartMeterProtocolAdapter;
import com.energyict.comserver.exceptions.CodingException;
import com.energyict.cpo.Environment;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.rest.DeviceProtocolFactoryService;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Pluggable;
import com.energyict.mdw.core.PluggableClass;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.SmartMeterProtocol;
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
            Pluggable pluggable = pluggableClass.newInstanceWithoutProperties();
            return checkForProtocolWrappers(pluggable);
        } catch (BusinessException e) {
            throw CodingException.reflectionError(e, pluggableClass);
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
