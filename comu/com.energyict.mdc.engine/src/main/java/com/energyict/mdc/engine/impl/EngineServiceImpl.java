package com.energyict.mdc.engine.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.cache.DeviceCacheImpl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Copyrights EnergyICT
 * Date: 08/05/14
 * Time: 13:17
 */
@Component(name="com.energyict.mdc.engine", service = {EngineService.class, InstallService.class}, property = "name=" + EngineService.COMPONENTNAME, immediate = true)
public class EngineServiceImpl implements EngineService, InstallService {

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;

    @Inject
    public EngineServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService) {
        super();
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        if (!this.dataModel.isInstalled()) {
            this.install(true);
        }
    }

    @Override
    public DeviceCache newDeviceCache(Device device, Serializable simpleCacheObject) {
        return dataModel.getInstance(DeviceCacheImpl.class).initialize(device, simpleCacheObject);
    }

    @Override
    public DeviceCache findDeviceCacheByDeviceId(Device device) {
        return dataModel.mapper(DeviceCache.class).getUnique("RTUID", device).orNull();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "Meter Data Collection Engine");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    DataModel getDataModel() {
        return dataModel;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    @Override
    public void install() {
        this.install(false);
    }

    private void install(boolean exeuteDdl) {
        new Installer(this.dataModel, this.thesaurus, this.eventService).install(exeuteDdl);
    }

}
