/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.IssueGroupFilter;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.IssueWebServiceClient;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueDateRange;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.issue.share.service.spi.IssueGroupTranslationProvider;
import com.elster.jupiter.license.License;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.spi.RelativePeriodCategoryTranslationProvider;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.alarms.DeviceAlarmFilter;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.device.alarms.event.OpenDeviceAlarmRelatedEvent;
import com.energyict.mdc.device.alarms.impl.database.TableSpecs;
import com.energyict.mdc.device.alarms.impl.database.UpgraderV10_4;
import com.energyict.mdc.device.alarms.impl.database.UpgraderV10_4_12;
import com.energyict.mdc.device.alarms.impl.database.UpgraderV10_7;
import com.energyict.mdc.device.alarms.impl.database.UpgraderV10_8_7;
import com.energyict.mdc.device.alarms.impl.database.groups.DeviceAlarmGroupOperation;
import com.energyict.mdc.device.alarms.impl.i18n.MessageSeeds;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;
import com.energyict.mdc.device.alarms.impl.install.Installer;
import com.energyict.mdc.device.alarms.impl.records.OpenDeviceAlarmImpl;
import com.energyict.mdc.device.alarms.impl.templates.BasicDeviceAlarmRuleTemplate;
import com.energyict.mdc.device.alarms.security.Privileges;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;
import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.energyict.mdc.device.alarms.DeviceAlarmServiceImpl",
        service = {TranslationKeyProvider.class, MessageSeedProvider.class, DeviceAlarmService.class, IssueProvider.class, IssueGroupTranslationProvider.class, RelativePeriodCategoryTranslationProvider.class},
        property = "name=" + DeviceAlarmService.COMPONENT_NAME,
        immediate = true)
public class DeviceAlarmServiceImpl implements TranslationKeyProvider, MessageSeedProvider, DeviceAlarmService, IssueProvider, IssueGroupTranslationProvider, RelativePeriodCategoryTranslationProvider {
    private volatile IssueService issueService;
    private volatile IssueActionService issueActionService;
    private volatile MessageService messageService;
    private volatile QueryService queryService;
    private volatile Thesaurus thesaurus;
    private volatile EventService eventService;
    private volatile DeviceService deviceService;
    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile UserService userService;
    private volatile MeteringService meteringService;
    private volatile TimeService timeService;
    private volatile BpmService bpmService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private final List<IssueWebServiceClient> alarmWebServiceClients = new ArrayList<>();
    private static BundleContext bundleContext;
    private volatile PropertySpecService propertySpecService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile MeteringTranslationService meteringTranslationService;
    private volatile NlsService nlsService;
    private volatile List<ServiceRegistration> registrations = new ArrayList<>();

    // For OSGi framework
    public DeviceAlarmServiceImpl() {
    }

    // For unit testing
    @Inject
    public DeviceAlarmServiceImpl(IssueService issueService,
                                  MessageService messageService,
                                  NlsService nlsService,
                                  OrmService ormService,
                                  QueryService queryService,
                                  DeviceService deviceService,
                                  EventService eventService,
                                  UpgradeService upgradeService,
                                  UserService userService,
                                  MeteringService meteringService,
                                  BpmService bpmService,
                                  EndPointConfigurationService endPointConfigurationService,
                                  TimeService timeService,
                                  BundleContext bundleContext,
                                  PropertySpecService propertySpecService,
                                  DeviceConfigurationService deviceConfigurationService,
                                  DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
                                  MeteringGroupsService meteringGroupsService,
                                  MeteringTranslationService meteringTranslationService) {
        this();
        setMessageService(messageService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setOrmService(ormService);
        setQueryService(queryService);
        setDeviceService(deviceService);
        setEventService(eventService);
        setUpgradeService(upgradeService);
        setUserService(userService);
        setMeteringService(meteringService);
        setTimeService(timeService);
        setBpmService(bpmService);
        setEndPointConfigurationService(endPointConfigurationService);
        setPropertySpecService(propertySpecService);
        setDeviceConfigurationService(deviceConfigurationService);
        setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        setMeteringGroupsService(meteringGroupsService);
        setMeteringTranslationService(meteringTranslationService);
        activate(bundleContext);
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MeteringService.class).toInstance(meteringService);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(MessageService.class).toInstance(messageService);
                bind(IssueService.class).toInstance(issueService);
                bind(IssueActionService.class).toInstance(issueActionService);
                bind(DeviceAlarmService.class).toInstance(DeviceAlarmServiceImpl.this);
                bind(QueryService.class).toInstance(queryService);
                bind(DeviceService.class).toInstance(deviceService);
                bind(EventService.class).toInstance(eventService);
                bind(UserService.class).toInstance(userService);
                bind(TimeService.class).toInstance(timeService);
                bind(BpmService.class).toInstance(bpmService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(DeviceLifeCycleConfigurationService.class).toInstance(deviceLifeCycleConfigurationService);
                bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                bind(EndPointConfigurationService.class).toInstance(endPointConfigurationService);
                bind(MeteringTranslationService.class).toInstance(meteringTranslationService);
            }
        });

        setBundleContext(bundleContext);
        BasicDeviceAlarmRuleTemplate basicDeviceAlarmRuleTemplate = dataModel.getInstance(BasicDeviceAlarmRuleTemplate.class);
        DeviceAlarmProcessAssociationProvider deviceAlarmProcessAssociationProvider = new DeviceAlarmProcessAssociationProvider(thesaurus, issueService, propertySpecService);

        registrations.add(bundleContext.registerService(CreationRuleTemplate.class, basicDeviceAlarmRuleTemplate, new Hashtable<>(ImmutableMap.of("name", BasicDeviceAlarmRuleTemplate.NAME))));
        registrations.add(bundleContext.registerService(BasicDeviceAlarmRuleTemplate.class, basicDeviceAlarmRuleTemplate, new Hashtable<>(ImmutableMap.of("name", BasicDeviceAlarmRuleTemplate.NAME))));
        registrations.add(bundleContext.registerService(ProcessAssociationProvider.class, deviceAlarmProcessAssociationProvider, new Hashtable<>(ImmutableMap.of("name", DeviceAlarmProcessAssociationProvider.NAME))));

        upgradeService.register(identifier("MultiSense", DeviceAlarmService.COMPONENT_NAME), dataModel, Installer.class, ImmutableMap.of(
                version(10, 4), UpgraderV10_4.class,
                version(10, 4, 12), UpgraderV10_4_12.class,
                version(10, 7), UpgraderV10_7.class,
                version(10, 8, 7), UpgraderV10_8_7.class
        ));
    }

    @Deactivate
    public void deactivate() {
        registrations.forEach(ServiceRegistration::unregister);
        registrations.clear();
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        this.issueActionService = issueService.getIssueActionService();
    }

    @Reference
    public final void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public final void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(DeviceAlarmService.COMPONENT_NAME, Layer.DOMAIN)
                .join(nlsService.getThesaurus(TimeService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference(target = "(com.elster.jupiter.license.rest.key=" + DeviceAlarmProcessAssociationProvider.APP_KEY + ")")
    public void setLicense(License license) {
        // explicit dependency on license
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public Optional<BundleContext> getBundleContext() {
        return Optional.of(bundleContext);
    }

    @Reference
    public final void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(DeviceAlarmService.COMPONENT_NAME, "Device Alarms");
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setMeteringTranslationService(MeteringTranslationService meteringTranslationService) {
        this.meteringTranslationService = meteringTranslationService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public final void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public final void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Override
    public Optional<? extends DeviceAlarm> findAlarm(long id) {
        Optional<OpenDeviceAlarm> issue = findOpenAlarm(id);
        if (issue.isPresent()) {
            return issue;
        }
        return findHistoricalAlarm(id);
    }

    @Override
    public Optional<? extends DeviceAlarm> findAndLockDeviceAlarmByIdAndVersion(long id, long version) {
        Optional<? extends Issue> issue = issueService.findAndLockIssueByIdAndVersion(id, version);
        if (issue.isPresent()) {
            Optional<OpenDeviceAlarm> openDeviceAlarm = findOpenAlarm(id);
            return openDeviceAlarm.isPresent() ? openDeviceAlarm : findHistoricalAlarm(id);
        }
        return Optional.empty();
    }

    @Override
    public Optional<OpenDeviceAlarm> findOpenAlarm(long id) {
        return find(OpenDeviceAlarm.class, id, OpenIssue.class);
    }

    @Override
    public Optional<HistoricalDeviceAlarm> findHistoricalAlarm(long id) {
        return find(HistoricalDeviceAlarm.class, id, HistoricalIssue.class);
    }

    @Override
    public OpenDeviceAlarm createAlarm(OpenIssue baseIssue, IssueEvent issueEvent) {
        OpenDeviceAlarmImpl issue = dataModel.getInstance(OpenDeviceAlarmImpl.class);
        issue.setIssue(baseIssue);
        issueEvent.apply(issue);
        issue.save();
        return issue;
    }

    private <T extends Entity> Optional<T> find(Class<T> clazz, Object key, Class<?>... eagers) {
        return queryService.wrap(dataModel.query(clazz, eagers)).get(key);
    }

    @Override
    public <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers) {
        QueryExecutor<T> queryExecutor = dataModel.query(clazz, eagers);
        Query<T> query = queryService.wrap(queryExecutor);
        query.setEager();
        return query;
    }

    @Override
    public Finder<? extends DeviceAlarm> findAlarms(DeviceAlarmFilter filter, Class<?>... eagers) {
        Condition condition = buildConditionFromFilter(filter);
        List<Class<?>> eagerClasses = determineMainApiClass(filter);
        if (eagers == null) {
            eagerClasses.addAll(Arrays.asList(IssueStatus.class, EndDevice.class, User.class, IssueReason.class, IssueType.class, CreationRule.class));
        } else {
            eagerClasses.addAll(Arrays.asList(eagers));
        }
        return DefaultFinder.of((Class<DeviceAlarm>) eagerClasses.remove(0), condition, dataModel, eagerClasses.toArray(new Class<?>[eagerClasses
                .size()]));
    }

    @Override
    public Finder<OpenDeviceAlarm> findOpenDeviceAlarms(Condition condition) {
        return DefaultFinder.of(OpenDeviceAlarm.class, condition, dataModel, OpenDeviceAlarmRelatedEvent.class);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addIssueWebServiceClient(IssueWebServiceClient issueWebServiceClient) {
        alarmWebServiceClients.add(issueWebServiceClient);
    }

    public void removeIssueWebServiceClient(IssueWebServiceClient issueWebServiceClient) {
        alarmWebServiceClients.remove(issueWebServiceClient);
    }

    public List<IssueWebServiceClient> getIssueWebServiceClients() {
        return Collections.unmodifiableList(this.alarmWebServiceClients);
    }

    @Override
    public List<IssueGroup> getDeviceAlarmGroupList(IssueGroupFilter filter) {
        return DeviceAlarmGroupOperation.from(filter, this.dataModel, thesaurus).execute();
    }

    @Override
    public Thesaurus thesaurus() {
        return thesaurus;
    }

    public DataModel getDataModel() {
        return this.dataModel;
    }

    private List<Class<?>> determineMainApiClass(DeviceAlarmFilter filter) {
        List<Class<?>> eagerClasses = new ArrayList<>();
        List<IssueStatus> statuses = filter.getStatuses();
        if (!statuses.isEmpty() && statuses.stream().noneMatch(IssueStatus::isHistorical)) {
            eagerClasses.add(OpenDeviceAlarm.class);
            eagerClasses.add(OpenIssue.class);
        } else if (!statuses.isEmpty() && statuses.stream().allMatch(IssueStatus::isHistorical)) {
            eagerClasses.add(HistoricalDeviceAlarm.class);
            eagerClasses.add(HistoricalIssue.class);
        } else {
            eagerClasses.add(DeviceAlarm.class);
            eagerClasses.add(Issue.class);
        }
        return eagerClasses;
    }

    private Condition getDeviceGroupSearchCondition(Collection<EndDeviceGroup> endDeviceGroups) {
        return endDeviceGroups.stream()
                .map(endDeviceGroup -> ListOperator.IN.contains(endDeviceGroup.toSubQuery("id"), "baseIssue.device"))
                .map(Condition.class::cast)
                .reduce(Condition.FALSE, Condition::or);
    }

    private Condition buildConditionFromFilter(DeviceAlarmFilter filter) {
        Condition condition = Condition.TRUE;
        //filter by issue id
        if (filter.getAlarmId() != null) {
            String[] alarmIdPart = filter.getAlarmId().split("-");
            if (alarmIdPart.length == 2) {
                if (alarmIdPart[0].toLowerCase().equals("alm")) {
                    condition = condition.and(where("id").isEqualTo(getNumericValueOrZero(alarmIdPart[1])));
                } else {
                    condition = condition.and(where("id").isEqualTo(0));
                }
            } else {
                condition = condition.and(where("id").isEqualTo(0));
            }
        }
        //filter by user assignee
        if (!filter.getUserAssignee().isEmpty()) {
            Condition userCondition = Condition.TRUE;
            userCondition = userCondition.and(where("baseIssue.user").in(filter.getUserAssignee()));
            if (filter.isUnassignedOnly()) {
                userCondition = userCondition.or(where("baseIssue.user").isNull());
            }
            condition = condition.and(userCondition);
        }
        if (filter.getUserAssignee().isEmpty() && filter.isUnassignedOnly()) {
            condition = condition.and(where("baseIssue.user").isNull());
        }
        //filter by reason
        if (!filter.getAlarmReasons().isEmpty()) {
            condition = condition.and(where("baseIssue.reason").in(filter.getAlarmReasons()));
        }
        //cleared alarm cleared status
        if (!filter.getCleared().isEmpty()) {
            condition = condition.and(where("clearStatus.statusValue").in(filter.getCleared()));
        }
        //filter by workGroup
        if (!filter.getWorkGroupAssignees().isEmpty()) {
            Condition wgCondition = Condition.TRUE;
            wgCondition = wgCondition.and(where("baseIssue.workGroup").in(filter.getWorkGroupAssignees()));
            if (filter.isUnassignedWorkGroupSelected()) {
                wgCondition = wgCondition.or(where("baseIssue.workGroup").isNull());
            }
            condition = condition.and(wgCondition);
        }
        if (filter.getWorkGroupAssignees().isEmpty() && filter.isUnassignedWorkGroupSelected()) {
            condition = condition.and(where("baseIssue.workGroup").isNull());
        }
        //filter by device
        if (!filter.getDevices().isEmpty()) {
            condition = condition.and(where("baseIssue.device").in(filter.getDevices()));
        }
        //filter by device group
        if (!filter.getDeviceGroups().isEmpty()) {
            condition = condition.and(getDeviceGroupSearchCondition(filter.getDeviceGroups()));
        }
        //filter by statuses
        if (!filter.getStatuses().isEmpty()) {
            condition = condition.and(where("baseIssue.status").in(filter.getStatuses()));
        }
        //filter by due date
        if (!filter.getDueDates().isEmpty()) {
            Condition dueDateCondition = Condition.FALSE;
            for (DueDateRange dueDateRange : filter.getDueDates()) {
                dueDateCondition = dueDateCondition.or(where("baseIssue.dueDate").isGreaterThanOrEqual(dueDateRange.getStartTimeAsInstant())
                        .and(where("baseIssue.dueDate").isLessThan(dueDateRange.getEndTimeAsInstant())));
            }
            condition = condition.and(dueDateCondition);
        }
        //filter by create time
        if (filter.getStartCreateTime() != null) {
            condition = condition.and(where("baseIssue.createDateTime").isGreaterThanOrEqual(filter.getStartCreateTime()));
        }
        if (filter.getEndCreateTime() != null) {
            condition = condition.and(where("baseIssue.createDateTime").isLessThanOrEqual(filter.getEndCreateTime()));
        }
        //filter by alarm rule
        if (filter.getRule() != null) {
            condition = condition.and(where("baseIssue.rule").isEqualTo(filter.getRule()));
        }
        return condition;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(TranslationKeys.values()),
                Arrays.stream(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public Optional<? extends OpenIssue> getOpenIssue(OpenIssue issue) {
        return issue instanceof OpenDeviceAlarm ? Optional.of(issue) : findOpenAlarm(issue.getId());
    }

    @Override
    public Optional<? extends HistoricalIssue> getHistoricalIssue(HistoricalIssue issue) {
        return issue instanceof HistoricalDeviceAlarm ? Optional.of(issue) : findHistoricalAlarm(issue.getId());
    }

    @Override
    public Optional<? extends Issue> findIssue(long l) {
        return findAlarm(l);
    }

    private long getNumericValueOrZero(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
