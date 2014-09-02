package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComScheduleBuilder;
import com.energyict.mdc.scheduling.model.SchedulingStatus;
import com.energyict.mdc.tasks.TaskService;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.energyict.mdc.scheduling", service = { SchedulingService.class, InstallService.class }, immediate = true, property = "name=" + SchedulingService.COMPONENT_NAME)
public class SchedulingServiceImpl implements SchedulingService, InstallService {

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
        new Installer(this.dataModel, this.eventService, this.thesaurus, this.userService).install(true);
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
            }
        };
    }


    @Override
    public NextExecutionSpecs findNextExecutionSpecs(long id) {
        return dataModel.mapper(NextExecutionSpecs.class).getUnique("id", id).orNull();
    }

    @Override
    public NextExecutionSpecs newNextExecutionSpecs(TemporalExpression temporalExpression) {
        NextExecutionSpecsImpl instance = dataModel.getInstance(NextExecutionSpecsImpl.class);
        instance.setTemporalExpression(temporalExpression);
        return instance;
    }

    @Override
    public NextExecutionSpecs previewNextExecutions(TemporalExpression temporalExpression, Date startDate) {
        return null;
    }

    @Override
    public List<ComSchedule> findAllSchedules() {
        return this.dataModel.query(ComSchedule.class, NextExecutionSpecs.class).select(where(ComScheduleImpl.Fields.OBSOLETE_DATE.fieldName()).isNull());
    }

    @Override
    public ListPager<ComSchedule> findAllSchedules(final Calendar calendar) {
        List<ComSchedule> comSchedules = this.dataModel.query(ComSchedule.class, NextExecutionSpecs.class).select(where(ComScheduleImpl.Fields.OBSOLETE_DATE.fieldName()).isNull());
        return ListPager.of(comSchedules, new CompareBySchedulingStatus());
    }

    @Override
    public Optional<ComSchedule> findSchedule(long id) {
        return this.findUniqueSchedule("id", id);
    }

    private Optional<ComSchedule> findUniqueSchedule(String fieldName, Object value) {
        Condition condition = where(fieldName).isEqualTo(value).and(where(ComScheduleImpl.Fields.OBSOLETE_DATE.fieldName()).isNull());
        List<ComSchedule> comSchedules = this.dataModel.query(ComSchedule.class).select(condition);
        if (comSchedules.isEmpty()) {
            return Optional.absent();
        }
        else {
            return Optional.of(comSchedules.get(0));
        }
    }

    @Override
    public Optional<ComSchedule> findScheduleBymRID(String mRID) {
        return this.findUniqueSchedule(ComScheduleImpl.Fields.MRID.fieldName(), mRID);
    }

    @Override
    public ComScheduleBuilder newComSchedule(String name, TemporalExpression temporalExpression, UtcInstant startDate) {
        return new ComScheduleBuilderImpl(name, temporalExpression, startDate);
    }

    private static class CompareBySchedulingStatus implements Comparator<ComSchedule> {
        @Override
        public int compare(ComSchedule o1, ComSchedule o2) {
            if (SchedulingStatus.PAUSED.equals(o1.getSchedulingStatus()) && SchedulingStatus.PAUSED.equals(o2.getSchedulingStatus())) {
                return 0;
            }
            if (SchedulingStatus.PAUSED.equals(o1.getSchedulingStatus()) && !SchedulingStatus.PAUSED.equals(o2.getSchedulingStatus())) {
                return 1;
            }
            if (!SchedulingStatus.PAUSED.equals(o1.getSchedulingStatus()) && SchedulingStatus.PAUSED.equals(o2.getSchedulingStatus())) {
                return -1;
            }
            return o1.getPlannedDate().compareTo(o2.getPlannedDate());
        }
    }

    class ComScheduleBuilderImpl implements ComScheduleBuilder {
        private ComSchedule instance;

        ComScheduleBuilderImpl(String name, TemporalExpression temporalExpression, UtcInstant startDate) {
            instance = dataModel.getInstance(ComScheduleImpl.class);
            instance.setName(name);
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
            Save.CREATE.save(dataModel, instance);
            return instance;
        }
    }

}