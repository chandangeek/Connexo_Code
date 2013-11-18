package com.energyict.protocols.mdc.services;

import com.energyict.cbo.BusinessException;
import com.energyict.comserver.adapters.meterprotocol.MeterProtocolAdapter;
import com.energyict.comserver.adapters.smartmeterprotocol.SmartMeterProtocolAdapter;
import com.energyict.comserver.exceptions.CodingException;
import com.energyict.cpo.Environment;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.services.DeviceProtocolService;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Pluggable;
import com.energyict.mdw.core.PluggableClass;
import com.energyict.mdw.shadow.PluggableClassShadow;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.SmartMeterProtocol;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.sql.SQLException;

/**
 * Copyrights EnergyICT
 * Date: 06/11/13
 * Time: 11:03
 */
@Component(name = "com.energyict.mdc.service.deviceprotocols", service = DeviceProtocolService.class, immediate = true)
public class DeviceProtocolServiceImpl implements DeviceProtocolService {

    @Activate
    public void activate(BundleContext context) {
        MeteringWarehouse.createBatchContext(true);
    }

    @Deactivate
    public void deactivate(){
        Environment.getDefault().closeConnection();
    }

    public PluggableClass create(PluggableClassShadow shadow) throws BusinessException, SQLException {
        return MeteringWarehouse.getCurrent().getPluggableClassFactory().create(shadow);
    }

    public PluggableClass update(int id, PluggableClassShadow shadow) throws BusinessException, SQLException {
        PluggableClass pluggableClass = MeteringWarehouse.getCurrent().getPluggableClassFactory().find(id);
        if (pluggableClass != null) {
            pluggableClass.update(shadow);
        }
        return pluggableClass;
    }

    public void delete(int id) throws BusinessException, SQLException {
        PluggableClass pluggableClass = MeteringWarehouse.getCurrent().getPluggableClassFactory().find(id);
        pluggableClass.delete();
    }

    @Override
    public DeviceProtocol createDeviceProtocolFor(PluggableClass pluggableClass) {
        try {
            Pluggable pluggable = (Pluggable) (Class.forName(pluggableClass.getJavaClassName())).newInstance();
            return checkForProtocolWrappers(pluggable);
        } catch (BusinessException e) {
            throw CodingException.reflectionError(e, pluggableClass);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw CodingException.genericReflectionError(e, pluggableClass.getJavaClassName());
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
