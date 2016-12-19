package com.energyict.mdc.device.alarms.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.issue.share.service.spi.IssueGroupTranslationProvider;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.alarms.DeviceAlarmFilter;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.entity.HistoricalDeviceAlarm;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.device.alarms.impl.database.TableSpecs;
import com.energyict.mdc.device.alarms.impl.i18n.MessageSeeds;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;
import com.energyict.mdc.device.alarms.impl.install.Installer;
import com.energyict.mdc.device.alarms.impl.install.UpgraderV10_3;
import com.energyict.mdc.device.alarms.impl.records.OpenDeviceAlarmImpl;
import com.energyict.mdc.device.data.DeviceService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;
import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.energyict.mdc.device.alarms.DeviceAlarmServiceImpl",
        service = {TranslationKeyProvider.class, MessageSeedProvider.class, DeviceAlarmService.class, IssueProvider.class, IssueGroupTranslationProvider.class},
        property = "name=" + DeviceAlarmService.COMPONENT_NAME,
        immediate = true)
public class DeviceAlarmServiceImpl implements TranslationKeyProvider, MessageSeedProvider, DeviceAlarmService, IssueProvider, IssueGroupTranslationProvider {
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
                                  MeteringService meteringService) {
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

        activate();
    }

    @Activate
    public final void activate() {
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
            }
        });
        upgradeService.register(identifier("MultiSense", DeviceAlarmService.COMPONENT_NAME), dataModel, Installer.class, ImmutableMap
                .of(
                        version(10, 3), UpgraderV10_3.class
                ));
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
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceAlarmService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public final void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public final void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(DeviceAlarmService.COMPONENT_NAME, "Device Alarms");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
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
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
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
            return findOpenAlarm(id);
        }
        return findHistoricalAlarm(id);
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
            eagerClasses.addAll(Arrays.asList(IssueStatus.class, EndDevice.class, User.class, IssueReason.class, IssueType.class));
        } else {
            eagerClasses.addAll(Arrays.asList(eagers));
        }
        return DefaultFinder.of((Class<DeviceAlarm>) eagerClasses.remove(0), condition, dataModel, eagerClasses.toArray(new Class<?>[eagerClasses
                .size()]));
    }

    private List<Class<?>> determineMainApiClass(DeviceAlarmFilter filter) {
        List<Class<?>> eagerClasses = new ArrayList<>();
        List<IssueStatus> statuses = filter.getStatuses();
        if (!statuses.isEmpty() && statuses.stream().allMatch(status -> !status.isHistorical())) {
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

    private Condition buildConditionFromFilter(DeviceAlarmFilter filter) {
        Condition condition = Condition.TRUE;
        //filter by user assignee
        Condition assigneeCondition = Condition.TRUE;
        if (filter.getUserAssignee().isPresent()) {
            assigneeCondition = where("baseIssue.user").isEqualTo(filter.getUserAssignee().get());
        }
        if (filter.isUnassignedOnly()) {
            assigneeCondition = where("baseIssue.user").isNull();
        }
        condition = condition.and(assigneeCondition);
        //filter by reason
        if (!filter.getAlarmReasons().isEmpty()) {
            condition = condition.and(where("baseIssue.reason").in(filter.getAlarmReasons()));
        }
        //filter by device
        if (!filter.getDevices().isEmpty()) {
            condition = condition.and(where("baseIssue.device").in(filter.getDevices()));
        }
        //filter by statuses
        if (!filter.getStatuses().isEmpty()) {
            condition = condition.and(where("baseIssue.status").in(filter.getStatuses()));
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
        return Arrays.asList(TranslationKeys.values());
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


}