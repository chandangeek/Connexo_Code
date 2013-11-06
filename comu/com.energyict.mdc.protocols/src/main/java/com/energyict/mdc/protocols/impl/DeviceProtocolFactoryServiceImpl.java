package com.energyict.mdc.protocols.impl;

import com.energyict.cpo.Environment;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.rest.impl.DeviceProtocolFactoryService;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.PluggableClass;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * Copyrights EnergyICT
 * Date: 06/11/13
 * Time: 11:03
 */
@Component(name="com.energyict.mdc.protocols", service=DeviceProtocolFactoryService.class)
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
    public DeviceProtocolPluggableClass createDeviceProtocolPluggableClassFor(PluggableClass pluggableClass) {
        return ManagerFactory.getCurrent().getDeviceProtocolPluggableClassFactory().newForPluggableClass(pluggableClass);
    }
}
