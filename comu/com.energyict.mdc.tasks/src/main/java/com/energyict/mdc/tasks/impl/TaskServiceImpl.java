package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ClockTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.FirmwareManagementTask;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.tasks", service = {TaskService.class, ServerTaskService.class, InstallService.class}, property = "name=" + TaskService.COMPONENT_NAME, immediate = true)
public class TaskServiceImpl implements ServerTaskService, InstallService {

    private final Logger logger = Logger.getLogger(TaskServiceImpl.class.getName());

    private volatile EventService eventService;
    private volatile NlsService nlsService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile MasterDataService masterDataService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;

    public TaskServiceImpl() {
        super();
    }

    @Inject
    public TaskServiceImpl(OrmService ormService, NlsService nlsService, EventService eventService, DeviceMessageSpecificationService deviceMessageSpecificationService, MasterDataService masterDataService) {
        this();
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
        this.setEventService(eventService);
        this.setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        this.setMasterDataService(masterDataService);
        this.activate();
        this.install();
    }

    @Override
    public void install() {
        if (!dataModel.isInstalled()) {
            dataModel.install(true, true);
        }
        this.createEventTypes();
        this.createFirmwareComTaskIfNotPresentYet();
        this.createStatusInformationComTaskIfNotPresentYet();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "EVT", "MDS");
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
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

    @Override
    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Reference
    public void setMasterDataService(MasterDataService masterDataService) {
        // Not actively used but required for foreign keys in TableSpecs
        this.masterDataService = masterDataService;
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.createIfNotExists(this.eventService);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private void createFirmwareComTaskIfNotPresentYet() {
        if (!this.findFirmwareComTask().isPresent()) {
            createFirmwareComTask();
        }
    }

    private void createStatusInformationComTaskIfNotPresentYet() {
        if (!this.findStatusInformationComTask().isPresent()) {
            createStatusInformationComTask();
        }
    }

    private void createFirmwareComTask() {
        SystemComTask systemComTask = dataModel.getInstance(SystemComTask.class);
        systemComTask.setName(FIRMWARE_COMTASK_NAME);
        systemComTask.createFirmwareUpgradeTask();
        systemComTask.save();
    }

    private void createStatusInformationComTask() {
        ComTask comTask = dataModel.getInstance(ComTask.class);
        comTask.setName(STATUS_INFORMATION_COMTASK_NAME);
        comTask.createStatusInformationTask();
        comTask.save();
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
                bind(DeviceMessageSpecificationService.class).toInstance(deviceMessageSpecificationService);
                bind(TaskService.class).toInstance(TaskServiceImpl.this);
                bind(ComTask.class).to(ComTaskDefinedByUserImpl.class);
                bind(SystemComTask.class).to(ComTaskDefinedBySystemImpl.class);
                bind(BasicCheckTask.class).to(BasicCheckTaskImpl.class);
                bind(ClockTask.class).to(ClockTaskImpl.class);
                bind(StatusInformationTask.class).to(StatusInformationTaskImpl.class);
                bind(RegistersTask.class).to(RegistersTaskImpl.class);
                bind(LoadProfilesTask.class).to(LoadProfilesTaskImpl.class);
                bind(LogBooksTask.class).to(LogBooksTaskImpl.class);
                bind(TopologyTask.class).to(TopologyTaskImpl.class);
                bind(MessagesTask.class).to(MessagesTaskImpl.class);
                bind(FirmwareManagementTask.class).to(FirmwareManagementTaskImpl.class);
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
    public Optional<ComTask> findComTask(long id) {
        return dataModel.mapper(ComTask.class).getUnique("id", id);
    }

    @Override
    public Optional<ProtocolTask> findProtocolTask(long id) {
        return dataModel.mapper(ProtocolTask.class).getUnique("id", id);
    }

    @Override
    public Finder<ProtocolTask> findAllProtocolTasks() {
        return DefaultFinder.of(ProtocolTask.class, dataModel).defaultSortColumn("id").maxPageSize(thesaurus, 100);
    }

    @Override
    public List<ComTask> findAllUserComTasks() {
        return dataModel.mapper(ComTaskDefinedByUserImpl.class).find().stream().map(userComTask -> (ComTask) userComTask).collect(Collectors.toList());
    }

    @Override
    public List<ComTask> findAllSystemComTasks() {
        return dataModel.mapper(ComTaskDefinedBySystemImpl.class).find().stream().map(comTaskDefinedBySystem -> (ComTask) comTaskDefinedBySystem).collect(Collectors.toList());
    }

    @Override
    public Finder<ComTask> findAllComTasks() {
        return DefaultFinder.of(ComTask.class, dataModel).defaultSortColumn(ComTaskImpl.Fields.NAME.fieldName());
    }

    @Override
    public Optional<ComTask> findFirmwareComTask() {
        List<ComTask> comTasks = dataModel.mapper(ComTask.class).find("name", FIRMWARE_COMTASK_NAME);
        return comTasks.size() == 1 ? Optional.of(comTasks.get(0)) : Optional.empty();
    }

    private Optional<ComTask> findStatusInformationComTask() {
        List<ComTask> comTasks = dataModel.mapper(ComTask.class).find("name", STATUS_INFORMATION_COMTASK_NAME);
        return comTasks.size() == 1 ? Optional.of(comTasks.get(0)) : Optional.empty();
    }

    @Override
    public List<LogBooksTask> findTasksUsing(LogBookType logBookType) {
        List<LogBookTypeUsageInProtocolTask> usages =
                this.dataModel
                        .mapper(LogBookTypeUsageInProtocolTask.class)
                        .find(LogBookTypeUsageInProtocolTaskImpl.Fields.LOGBOOK_TYPE_REFERENCE.fieldName(), logBookType);
        return usages.stream().map(LogBookTypeUsageInProtocolTask::getLogBooksTask).collect(Collectors.toList());
    }

    @Override
    public List<LoadProfilesTask> findTasksUsing(LoadProfileType loadProfileType) {
        List<LoadProfileTypeUsageInProtocolTask> usages =
                this.dataModel
                        .mapper(LoadProfileTypeUsageInProtocolTask.class)
                        .find(LoadProfileTypeUsageInProtocolTaskImpl.Fields.LOADPROFILE_TYPE_REFERENCE.fieldName(), loadProfileType);
        return usages.stream().map(LoadProfileTypeUsageInProtocolTask::getLoadProfilesTask).collect(Collectors.toList());
    }

}