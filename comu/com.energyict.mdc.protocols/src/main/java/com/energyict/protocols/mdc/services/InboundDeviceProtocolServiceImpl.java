package com.energyict.protocols.mdc.services;

import com.energyict.cbo.BusinessException;
import com.energyict.comserver.exceptions.CodingException;
import com.energyict.cpo.Environment;
import com.energyict.mdc.protocol.inbound.InboundDeviceProtocol;
import com.energyict.mdc.services.InboundDeviceProtocolService;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.PluggableClass;
import com.energyict.mdw.shadow.PluggableClassShadow;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.sql.SQLException;

/**
 * Copyrights EnergyICT
 * Date: 15/11/13
 * Time: 12:03
 */
@Component(name = "com.energyict.mdc.service.inbounddeviceprotocols", service = InboundDeviceProtocolService.class, immediate = true)
public class InboundDeviceProtocolServiceImpl implements InboundDeviceProtocolService {

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
    public InboundDeviceProtocol createInboundDeviceProtocolFor(PluggableClass pluggableClass) {
        try {
            return (InboundDeviceProtocol) (Class.forName(pluggableClass.getJavaClassName())).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw CodingException.genericReflectionError(e, pluggableClass.getJavaClassName());
        }
    }
}
