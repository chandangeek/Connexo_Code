package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.RegisterMapping;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

/**
 * Provides an implementation for the {@link DeviceConfigurationService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:38)
 */
@Component(name="com.energyict.mdc.device.config", service = {DeviceConfigurationService.class, InstallService.class})
public class DeviceConfigurationServiceImpl implements DeviceConfigurationService, InstallService {

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;

    public DeviceConfigurationServiceImpl() {
        super();
    }

    @Inject
    public DeviceConfigurationServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService) {
        this();
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
    }

    @Override
    public List<DeviceType> findAllDeviceTypes() {
        return this.getDataModel().mapper(DeviceType.class).find();
    }

    @Override
    public List<RegisterMapping> findAllRegisterMappings() {
        return this.getDataModel().mapper(RegisterMapping.class).find();
    }

    @Override
    public List<LoadProfileType> findAllLoadProfileTypes() {
        return this.getDataModel().mapper(LoadProfileType.class).find();
    }

    @Override
    public List<LogBookType> findAllLogBookTypes() {
        return this.getDataModel().mapper(LogBookType.class).find();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "DeviceType and configurations");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    public EventService getEventService() {
        return eventService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
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
        new Installer(this.dataModel, this.eventService, this.thesaurus).install(true, true, true);
    }

}