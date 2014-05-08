package com.energyict.mdc.engine.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.cache.DeviceCacheImpl;
import org.osgi.service.component.annotations.Reference;

import java.io.Serializable;

/**
 * Copyrights EnergyICT
 * Date: 08/05/14
 * Time: 13:17
 */
public class EngineServiceImpl implements EngineService {

    private volatile DataModel dataModel;

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "Device data");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public DeviceCache newDeviceCache(Device device, Serializable simpleCacheObject) {
        return dataModel.getInstance(DeviceCacheImpl.class).initialize(device, simpleCacheObject);
    }

    @Override
    public DeviceCache findDeviceCacheByDeviceId(Device device) {
        return dataModel.mapper(DeviceCache.class).getUnique("RTUID", device).orNull();
    }

}
