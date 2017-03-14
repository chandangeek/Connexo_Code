/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarFilter;
import com.elster.jupiter.calendar.CalendarResolver;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.calendar.security.Privileges;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.FieldType;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.calendar.impl.CalendarTimeSeriesImpl.FIELD_SPEC_CODE_NAME;
import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.calendar",
        service = {CalendarService.class, ServerCalendarService.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        property = "name=" + CalendarService.COMPONENTNAME,
        immediate = true)
public class CalendarServiceImpl implements ServerCalendarService, MessageSeedProvider, TranslationKeyProvider {

    private static final long VAULT_ID = 1L;
    private static final long RECORD_SPEC_ID = 1L;

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile IdsService idsService;
    private volatile UserService userService;
    private volatile EventService eventService;
    private volatile MessageService messageService;
    private volatile TaskService taskService;
    private volatile UpgradeService upgradeService;
    private volatile Clock clock;

    private Vault vault;
    private RecordSpec recordSpec;

    private final List<CalendarResolver> calendarResolvers = new CopyOnWriteArrayList<>();

    public CalendarServiceImpl() {
    }

    @Inject
    public CalendarServiceImpl(OrmService ormService, NlsService nlsService, IdsService idsService, UserService userService, EventService eventService, UpgradeService upgradeService, MessageService messageService, TaskService taskService, Clock clock) {
        this();
        setOrmService(ormService);
        setNlsService(nlsService);
        setIdsService(idsService);
        setUserService(userService);
        setEventService(eventService);
        setUpgradeService(upgradeService);
        setMessageService(messageService);
        setTaskService(taskService);
        setClock(clock);
        activate();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(CalendarService.COMPONENTNAME, "Calendars");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(this.dataModel);
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(CalendarService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public IdsService getIdsService() {
        return idsService;
    }

    @Reference
    public void setIdsService(IdsService idsService) {
        this.idsService = idsService;
    }

    @Override
    public UserService getUserService() {
        return userService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public EventService getEventService() {
        return eventService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Override
    public MessageService getMessageService() {
        return messageService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public TaskService getTaskService() {
        return taskService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public Clock getClock() {
        return clock;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
        upgradeService.register(
                InstallIdentifier.identifier("Pulse", CalendarService.COMPONENTNAME),
                dataModel,
                InstallerImpl.class,
                ImmutableMap.of(
                        version(10, 2), UpgraderV10_2.class,
                        version(10, 3), UpgraderV10_3.class
                ));
        // Installer or upgrader will have created the vault and the record spec
        this.initVaultAndRecordSpec();
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(CalendarService.class).to(ServerCalendarService.class);
                bind(ServerCalendarService.class).toInstance(CalendarServiceImpl.this);
                bind(EventService.class).toInstance(eventService);
                bind(IdsService.class).toInstance(idsService);
                bind(UserService.class).toInstance(userService);
                bind(MessageService.class).toInstance(messageService);
                bind(TaskService.class).toInstance(taskService);
                bind(Clock.class).toInstance(clock);
            }
        };
    }

    @Override
    public String getComponentName() {
        return CalendarService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Stream.of(TranslationKeys.values()),
                Stream.of(Privileges.values()),
                Stream.of(CalendarStatusTranslationKeys.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public CalendarBuilder newCalendar(String name, Year start, EventSet eventSet) {
        CalendarBuilderImpl builder = new CalendarBuilderImpl(getDataModel(), eventSet);
        builder.init(name, start);
        return builder;
    }

    @Override
    public EventSetBuilder newEventSet(String name) {
        return new EventSetBuilderImpl(dataModel, name);
    }

    @Override
    public Finder<Calendar> getCalendarFinder(Condition condition) {
        return DefaultFinder.of(Calendar.class, getNonObsoleteCalendarsCondition().and(condition), dataModel);
    }

    private Condition getNonObsoleteCalendarsCondition() {
        return where(CalendarImpl.Fields.OBSOLETETIME.fieldName()).isNull();
    }

    @Override
    public CalendarFilter newCalendarFilter() {
        return new CalendarFilterImpl();
    }

    public List<Calendar> findAllCalendars() {
        return DefaultFinder.of(Calendar.class, getNonObsoleteCalendarsCondition(), this.getDataModel()).defaultSortColumn("lower(name)").find();
    }

    @Override
    public List<ServerCalendar> findAllCalendarsForExtension() {
        Year thisYear = Year.now(this.clock);
        Condition condition =
                this.getNonObsoleteCalendarsCondition()
                .and(   where(CalendarImpl.Fields.ENDYEAR.fieldName()).isEqualTo(thisYear.getValue())
                    .or(where(CalendarImpl.Fields.ENDYEAR.fieldName()).isNull()));
        return this.getDataModel()
                    .query(Calendar.class, CalendarTimeSeriesEntity.class, TimeSeries.class)
                    .select(condition)
                    .stream()
                    .map(ServerCalendar.class::cast)
                    .collect(Collectors.toList());
    }

    @Override
    public Optional<Calendar> findCalendar(long id) {
        return calendarMapper().getUnique(CalendarImpl.Fields.ID.fieldName(), id);
    }

    private DataMapper<Calendar> calendarMapper() {
        return this.getDataModel().mapper(Calendar.class);
    }

    @Override
    public Optional<Category> findCategoryByName(String name) {
        return categoryMapper().getUnique(CategoryImpl.Fields.NAME.fieldName(), name);
    }

    @Override
    public Optional<Category> findCategoryByDisplayName(String displayName) {
        return categoryMapper()
                .find()
                .stream()
                .filter(each -> each.getDisplayName().equals(displayName))
                .findAny();
    }

    private DataMapper<Category> categoryMapper() {
        return this.getDataModel().mapper(Category.class);
    }

    @Override
    public Optional<Category> findCategory(long id) {
        return categoryMapper().getOptional(id);
    }

    @Override
    public List<Category> findAllCategories() {
        return categoryMapper().find();
    }

    @Override
    public List<Category> findUsedCategories() {
        return categoryMapper().find()
                .stream()
                .filter(category -> {
                    CalendarFilter calendarFilter = newCalendarFilter().setCategory(category);
                    List<Calendar> calendars = this.getCalendarFinder(calendarFilter.toCondition()).find();
                    Optional<Calendar> any = calendars.stream().filter(Calendar::isActive).findAny();
                    return any.isPresent();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Calendar> findCalendarByName(String name) {
        return calendarMapper().getUnique(CalendarImpl.Fields.NAME.fieldName(), name, CalendarImpl.Fields.OBSOLETETIME.fieldName(), null);
    }

    @Override
    public Optional<Calendar> findCalendarByMRID(String mRID) {
        return calendarMapper().getUnique(CalendarImpl.Fields.MRID.fieldName(), mRID, CalendarImpl.Fields.OBSOLETETIME.fieldName(), null);
    }

    @Override
    public Optional<EventSet> findEventSetByName(String name) {
        return eventSetMapper().getUnique(EventSetImpl.Fields.NAME.fieldName(), name);
    }

    private DataMapper<EventSet> eventSetMapper() {
        return getDataModel().mapper(EventSet.class);
    }

    @Override
    public Optional<EventSet> findEventSet(long id) {
        return eventSetMapper().getOptional(id);
    }

    @Override
    public List<EventSet> findEventSets() {
        return eventSetMapper().find();
    }

    @Override
    public boolean isCalendarInUse(Calendar calendar) {
        for (CalendarResolver resolver : calendarResolvers) {
            if (resolver.isCalendarInUse(calendar)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Calendar> lockCalendar(long id, long version) {
        return calendarMapper().lockObjectIfVersion(version, id);
    }

    @Override
    public DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCalendarResolver(CalendarResolver resolver) {
        calendarResolvers.add(resolver);
    }

    public void removeCalendarResolver(CalendarResolver resolver) {
        calendarResolvers.remove(resolver);
    }

    @Override
    public void createVault() {
        this.vault = this.idsService.createVault(COMPONENTNAME, VAULT_ID, "Calendar timeseries", 1, 0, true);
        this.createPartitions();
    }

    @Override
    public Vault getVault() {
        return vault;
    }

    private void createPartitions() {
        LocalDate now = LocalDate.now(this.clock);
        LocalDate startOfThisYear = now.withDayOfYear(1);
        LocalDate end;

        LocalDate dec1stThisYear = Year.now(this.clock).atMonthDay(MonthDay.of(Month.DECEMBER, 1));
        if (now.isAfter(dec1stThisYear)) {
            /* Recurrent task that extends partitions runs on last sunday of the month.
             * Also, the task that extends cached timeseries of Calendar (CalendarTimeSeriesEntity)
             * runs on first of December so for safety, if we are already in the last month of the year
             * we will create the initial partition until the end of the next year.*/
            end = startOfThisYear.plusYears(2);
        } else {
            end = startOfThisYear.plusYears(1);
        }
        Instant start = startOfThisYear.minusYears(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        this.vault.activate(start);
        this.vault.extendTo(end.atStartOfDay(ZoneOffset.UTC).toInstant(), Logger.getLogger(getClass().getPackage().getName()));
    }

    @Override
    public void createRecordSpec() {
        this.recordSpec =
                idsService
                        .createRecordSpec(COMPONENTNAME, RECORD_SPEC_ID, "calendar")
                        .addFieldSpec(FIELD_SPEC_CODE_NAME, FieldType.LONGINTEGER)
                        .create();
    }

    @Override
    public RecordSpec getRecordSpec() {
        return recordSpec;
    }

    private void initVaultAndRecordSpec() {
        if (this.vault == null) {
            this.vault = this.idsService.getVault(COMPONENTNAME, VAULT_ID).orElseThrow(() -> new IllegalStateException("Installer or upgrader failed to create the calendar vault"));
        }
        if (this.recordSpec == null) {
            this.recordSpec = this.idsService.getRecordSpec(COMPONENTNAME, RECORD_SPEC_ID).orElseThrow(() -> new IllegalStateException("Installer or upgrader failed to create the calendar record spec"));
        }
    }

}