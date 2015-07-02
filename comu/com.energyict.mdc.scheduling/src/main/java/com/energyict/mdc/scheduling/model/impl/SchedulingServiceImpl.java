package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComScheduleBuilder;
import com.energyict.mdc.scheduling.model.SchedulingStatus;
import com.energyict.mdc.scheduling.security.Privileges;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

@Component(name = "com.energyict.mdc.scheduling", service = {ServerSchedulingService.class,SchedulingService.class, InstallService.class, TranslationKeyProvider.class, PrivilegesProvider.class}, immediate = true, property = "name=" + SchedulingService.COMPONENT_NAME)
public class SchedulingServiceImpl implements ServerSchedulingService, InstallService, TranslationKeyProvider, PrivilegesProvider {

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;
    private volatile TaskService tasksService;
    private volatile UserService userService;

    public SchedulingServiceImpl() {
    }

    @Inject
    public SchedulingServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, TaskService tasksService, UserService userService) {
        this();
        setOrmService(ormService);
        setEventService(eventService);
        setNlsService(nlsService);
        setTasksService(tasksService);
        setUserService(userService);
        activate();
        this.install();
    }

    @Override
    public Thesaurus getThesaurus() {
        return thesaurus;
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

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    @Override
    public void install() {
        new Installer(this.dataModel, this.eventService, this.userService).install(true);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "USR", "EVT", "NLS", "CTS");
    }

    @Override
    public String getComponentName() {
        return SchedulingService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(TaskService.class).toInstance(tasksService);
                bind(UserService.class).toInstance(userService);
                bind(SchedulingService.class).toInstance(SchedulingServiceImpl.this);
                bind(ServerSchedulingService.class).toInstance(SchedulingServiceImpl.this);
            }
        };
    }


    @Override
    public NextExecutionSpecs findNextExecutionSpecs(long id) {
        return dataModel.mapper(NextExecutionSpecs.class).getUnique("id", id).orElse(null);
    }

    @Override
    public NextExecutionSpecs newNextExecutionSpecs(TemporalExpression temporalExpression) {
        NextExecutionSpecsImpl instance = dataModel.getInstance(NextExecutionSpecsImpl.class);
        instance.setTemporalExpression(temporalExpression);
        return instance;
    }

    @Override
    public List<ComSchedule> findAllSchedules() {
        return this.dataModel.query(ComSchedule.class, NextExecutionSpecs.class).select(where(ComScheduleImpl.Fields.OBSOLETE_DATE.fieldName()).isNull());
    }

    @Override
    public Optional<ComSchedule> findSchedule(long id) {
        return this.findUniqueSchedule("id", id);
    }

    private Optional<ComSchedule> findUniqueSchedule(String fieldName, Object value) {
        Condition condition = where(fieldName).isEqualTo(value).and(where(ComScheduleImpl.Fields.OBSOLETE_DATE.fieldName()).isNull());
        return this.dataModel.query(ComSchedule.class).select(condition).stream().findFirst();
    }

    @Override
    public List<ComSchedule> findComSchedulesUsing(ComTask comTask) {
        return decorate(
                this.dataModel
                        .mapper(ComTaskInComSchedule.class)
                        .find(ComTaskInComScheduleImpl.Fields.COM_TASK_REFERENCE.fieldName(), comTask)
                        .stream()
                        .map(ComTaskInComSchedule::getComSchedule))
                        .distinct(HasId::getId)
                        .collect(Collectors.toList());
    }

    @Override
    public Optional<ComSchedule> findScheduleBymRID(String mRID) {
        return this.findUniqueSchedule(ComScheduleImpl.Fields.MRID.fieldName(), mRID);
    }

    @Override
    public ComScheduleBuilder newComSchedule(String name, TemporalExpression temporalExpression, Instant startDate) {
        return new ComScheduleBuilderImpl(name, temporalExpression, startDate);
    }

    @Override
    public String getModuleName() {
        return SchedulingService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(SchedulingService.COMPONENT_NAME, "sharedCommunicationSchedule.sharedCommunicationSchedules", "sharedCommunicationSchedule.sharedCommunicationSchedules.description",
                Arrays.asList(Privileges.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE, Privileges.VIEW_SHARED_COMMUNICATION_SCHEDULE)));
        return resources;
    }

    class ComScheduleBuilderImpl implements ComScheduleBuilder {
        private ComSchedule instance;

        ComScheduleBuilderImpl(String name, TemporalExpression temporalExpression, Instant startDate) {
            instance = dataModel.getInstance(ComScheduleImpl.class);
            instance.setName(Checks.is(name).emptyOrOnlyWhiteSpace() ? null : name);
            instance.setTemporalExpression(temporalExpression);
            instance.setSchedulingStatus(SchedulingStatus.ACTIVE);
            instance.setStartDate(startDate);
        }

        @Override
        public ComScheduleBuilder mrid(String mrid) {
            instance.setmRID(mrid);
            return this;
        }

        @Override
        public ComSchedule build() {
            return instance;
        }
    }

}