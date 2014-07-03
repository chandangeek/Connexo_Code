package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ComTask;
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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.energyict.mdc.tasks", service = {TaskService.class, InstallService.class}, property = "name=" + TaskService.COMPONENT_NAME, immediate = true)
public class TaskServiceImpl implements TaskService, InstallService {

    private final Logger logger = Logger.getLogger(TaskServiceImpl.class.getName());

    private volatile EventService eventService;
    private volatile NlsService nlsService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile MasterDataService masterDataService;

    public TaskServiceImpl() {
        super();
    }

    @Inject
    public TaskServiceImpl(OrmService ormService, NlsService nlsService, EventService eventService) {
        this();
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
        this.setEventService(eventService);
        this.activate();
        this.install();
    }

    @Override
    public void install() {
        if (!dataModel.isInstalled()) {
            dataModel.install(true, true);
        }
        this.createEventTypes();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(TaskService.COMPONENT_NAME, "Communication Task Service");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
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

    @Reference
    public void setMasterDataService(MasterDataService masterDataService) {
        // Not actively used but required for foreign keys in TableSpecs
        this.masterDataService = masterDataService;
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.install(this.eventService);
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(NlsService.class).toInstance(nlsService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(EventService.class).toInstance(eventService);
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
            }
        };
    }

    @Activate
    public void activate() {
        dataModel.register(getModule());
    }

    @Override
    public ComTask newComTask(String name) {
        ComTask comTask = dataModel.getInstance(ComTask.class);
        comTask.setName(name);
        return comTask;
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