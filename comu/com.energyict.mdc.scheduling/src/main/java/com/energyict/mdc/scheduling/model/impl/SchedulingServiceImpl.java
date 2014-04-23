package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.energyict.mdc.common.services.DefaultFinder;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.TaskService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import javax.inject.Inject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.scheduling", service = { SchedulingService.class, InstallService.class }, immediate = true, property = "name=" + SchedulingService.COMPONENT_NAME)
public class SchedulingServiceImpl implements SchedulingService, InstallService {

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;
    private volatile TaskService tasksService;

    public SchedulingServiceImpl() {
    }

    @Inject
    public SchedulingServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, TaskService tasksService) {
        setOrmService(ormService);
        setEventService(eventService);
        setNlsService(nlsService);
        setTasksService(tasksService);
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

    @Reference
    public void setTasksService(TaskService tasksService) { // Added this to solve issues with FK to MDCTASKS table
        this.tasksService = tasksService;
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    @Override
    public void install() {
        new Installer(dataModel, eventService, thesaurus).install(true, true);
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
            bind(TaskService.class).toInstance(tasksService);
            bind(SchedulingService.class).toInstance(SchedulingServiceImpl.this);
            }
        };
    }


    @Override
    public NextExecutionSpecs findNextExecutionSpecs(long id) {
        return null;
    }

    @Override
    public NextExecutionSpecs newNextExecutionSpecs(TemporalExpression temporalExpression) {
        NextExecutionSpecsImpl instance = dataModel.getInstance(NextExecutionSpecsImpl.class);
        instance.setTemporalExpression(temporalExpression);
        return instance;
    }

    @Override
    public Finder<ComSchedule> findAllSchedules() {
        return DefaultFinder.of(ComSchedule.class, this.dataModel).defaultSortColumn("name");
    }

    @Override
    public ComSchedule newComSchedule(String name, TemporalExpression temporalExpression) {
        ComScheduleImpl instance = dataModel.getInstance(ComScheduleImpl.class);
        instance.setName(name);
        instance.setTemporalExpression(temporalExpression);
        return null;
    }
}
