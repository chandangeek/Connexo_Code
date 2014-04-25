package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.EventType;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.RegistersTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.TopologyTask;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import java.util.List;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.tasks", service = {TaskService.class, InstallService.class}, property = "name=" + TaskService.COMPONENT_NAME, immediate = true)
public class TaskServiceImpl implements TaskService, InstallService {

    private final Logger logger = Logger.getLogger(TaskServiceImpl.class.getName());

    private volatile EventService eventService;
    private volatile NlsService nlsService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;

    public TaskServiceImpl() {

    }

    @Inject
    public TaskServiceImpl(OrmService ormService, NlsService nlsService, EventService eventService) {
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
        this.setEventService(eventService);
        activate();
        createEventTypes();
        if (!dataModel.isInstalled()) {
            dataModel.install(true, false);
        }
    }

    @Override
    public void install() {
        if(!dataModel.isInstalled()){
            dataModel.install(true, true);
        }
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(TaskService.COMPONENT_NAME, "Connection Task Service");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        // dependency reference so datamodel of DeviceConfigurationService is created FIRST
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(TaskService.COMPONENT_NAME, Layer.DOMAIN);
    }

    private void createEventTypes() {
        try {
            for (EventType eventType : EventType.values()) {
                eventType.install(this.eventService);
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }



    Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(NlsService.class).toInstance(nlsService);
                bind(TaskService.class).toInstance(TaskServiceImpl.this);
                bind(ComTask.class).to(ComTaskImpl.class);
                bind(BasicCheckTask.class).to(BasicCheckTaskImpl.class);
                bind(ClockTask.class).to(ClockTaskImpl.class);
                bind(StatusInformationTask.class).to(StatusInformationTaskImpl.class);
                bind(RegistersTask.class).to(RegistersTaskImpl.class);
                bind(LoadProfilesTask.class).to(LoadProfilesTaskImpl.class);
                bind(LogBooksTask.class).to(LogBooksTaskImpl.class);
                bind(TopologyTask.class).to(TopologyTaskImpl.class);
                bind(MessagesTask.class).to(MessagesTaskImpl.class);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(EventService.class).toInstance(eventService);
            }
        };
    }

    @Activate
    public void activate() {
        dataModel.register(getModule());
    }


    @Override
    public ComTask createComTask() {
        return dataModel.getInstance(ComTask.class);
    }

    @Override
    public ComTask findComTask(long id) {
        return dataModel.mapper(ComTask.class).getUnique("id", id).orNull();
    }

    @Override
    public ProtocolTask findProtocolTask(long id) {
        return dataModel.mapper(ProtocolTask.class).getUnique("id", id).orNull();
    }

    @Override
    public List<ComTask> findAllComTasks() {
        return dataModel.mapper(ComTask.class).find();
    }
}
