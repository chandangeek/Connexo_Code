package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import javax.inject.Inject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.scheduling", service = SchedulingService.class, immediate = true)
public class SchedulingServiceImpl implements SchedulingService {

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;

    public SchedulingServiceImpl() {
    }

    @Inject
    public SchedulingServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService) {
        setOrmService(ormService);
        setEventService(eventService);
        setNlsService(nlsService);
        if (!this.dataModel.isInstalled()) {
            this.install(true);
        }
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(SchedulingService.COMPONENT_NAME, "Scheduling service");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(SchedulingService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    private void install(boolean exeuteDdl) {
        new Installer(this.dataModel, this.eventService, this.thesaurus).install(exeuteDdl, true);
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


    @Override
    public NextExecutionSpecs findNextExecutionSpecs(long id) {
        return null;
    }

    @Override
    public NextExecutionSpecs newNextExecutionSpecs(TemporalExpression temporalExpression) {
        return null;
    }
}
