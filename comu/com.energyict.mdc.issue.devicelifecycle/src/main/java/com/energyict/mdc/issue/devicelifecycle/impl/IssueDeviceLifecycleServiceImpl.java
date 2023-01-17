/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.impl;

import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.issue.share.service.spi.IssueGroupTranslationProvider;
import com.elster.jupiter.issue.share.service.spi.IssueReasonTranslationProvider;
import com.elster.jupiter.license.License;
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
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.issue.devicelifecycle.DeviceLifecycleIssueFilter;
import com.energyict.mdc.issue.devicelifecycle.HistoricalIssueDeviceLifecycle;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycle;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycleService;
import com.energyict.mdc.issue.devicelifecycle.OpenIssueDeviceLifecycle;
import com.energyict.mdc.issue.devicelifecycle.impl.entity.IssueDeviceLifecycleImpl;
import com.energyict.mdc.issue.devicelifecycle.impl.entity.OpenIssueDeviceLifecycleImpl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.energyict.mdc.issue.devicelifecycle",
           service = { TranslationKeyProvider.class, MessageSeedProvider.class, IssueDeviceLifecycleService.class, IssueProvider.class, IssueGroupTranslationProvider.class, IssueReasonTranslationProvider.class},
           property = "name=" + IssueDeviceLifecycleService.COMPONENT_NAME,
           immediate = true)
public class IssueDeviceLifecycleServiceImpl implements IssueDeviceLifecycleService, TranslationKeyProvider, MessageSeedProvider, IssueProvider, IssueGroupTranslationProvider, IssueReasonTranslationProvider {

    private volatile IssueService issueService;
    private volatile IssueActionService issueActionService;
    private volatile Thesaurus thesaurus;
    private volatile EventService eventService;
    private volatile MessageService messageService;
    private volatile UpgradeService upgradeService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile MeteringService meteringService;
    private volatile PropertySpecService propertySpecService;
    private volatile DataModel dataModel;

    private final List<ServiceRegistration<?>> registrations = new ArrayList<>();

    //for OSGI
    public IssueDeviceLifecycleServiceImpl() {
    }

    @Inject
    public IssueDeviceLifecycleServiceImpl(OrmService ormService,
                                           IssueService issueService,
                                           NlsService nlsService,
                                           EventService eventService,
                                           MessageService messageService,
                                           UpgradeService upgradeService,
                                           DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
                                           PropertySpecService propertySpecService,
                                           BundleContext bundleContext) {
        this();
        setOrmService(ormService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setEventService(eventService);
        setMessageService(messageService);
        setUpgradeService(upgradeService);
        setMeteringService(meteringService);
        setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        setPropertySpecService(propertySpecService);
        activate(bundleContext);
    }

    @Activate
    public final void activate(BundleContext bundleContext) {
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(IssueService.class).toInstance(issueService);
                bind(IssueActionService.class).toInstance(issueActionService);
                bind(IssueDeviceLifecycleService.class).toInstance(IssueDeviceLifecycleServiceImpl.this);
                bind(EventService.class).toInstance(eventService);
                bind(MessageService.class).toInstance(messageService);
            }
        });

        IssueLifecycleProcessAssociationProvider processAssociationProvider = new IssueLifecycleProcessAssociationProvider(thesaurus, issueService, propertySpecService);
        registrations.add(bundleContext.registerService(ProcessAssociationProvider.class, processAssociationProvider,
                new Hashtable<>(ImmutableMap.of("name", IssueLifecycleProcessAssociationProvider.NAME))));

        upgradeService.register(
                InstallIdentifier.identifier("MultiSense", IssueDeviceLifecycleService.COMPONENT_NAME),
                dataModel,
                Installer.class,
                Collections.emptyMap());
    }

    @Deactivate
    public void deactivate() {
        registrations.forEach(ServiceRegistration::unregister);
        registrations.clear();
    }

    @Override
    public Optional<? extends IssueDeviceLifecycle> findIssue(long id) {
        Optional<OpenIssueDeviceLifecycle> issue = findOpenIssue(id);
        if (issue.isPresent()) {
            return issue;
        }
        return findHistoricalIssue(id);
    }

    @Override
    public Optional<? extends IssueDeviceLifecycle> findAndLockIssueDeviceLifecycleByIdAndVersion(long id, long version) {
        Optional<? extends Issue> issue = issueService.findAndLockIssueByIdAndVersion(id, version);
        if (issue.isPresent()) {
            return findOpenIssue(id);
        }
        return findHistoricalIssue(id);
    }

    @Override
    public Optional<OpenIssueDeviceLifecycle> findOpenIssue(long id) {
        return dataModel.query(OpenIssueDeviceLifecycle.class, OpenIssue.class)
                .select(Where.where(IssueDeviceLifecycleImpl.Fields.BASEISSUE.fieldName() + ".id").isEqualTo(id))
                .stream()
                .findFirst();
    }

    @Override
    public Optional<HistoricalIssueDeviceLifecycle> findHistoricalIssue(long id) {
        return dataModel.query(HistoricalIssueDeviceLifecycle.class, HistoricalIssue.class)
                .select(Where.where(IssueDeviceLifecycleImpl.Fields.BASEISSUE.fieldName() + ".id").isEqualTo(id))
                .stream()
                .findFirst();
    }

    @Override
    public OpenIssueDeviceLifecycle createIssue(OpenIssue baseIssue, IssueEvent issueEvent) {
        OpenIssueDeviceLifecycleImpl issue = dataModel.getInstance(OpenIssueDeviceLifecycleImpl.class);
        issue.setIssue(baseIssue);
        issueEvent.apply(issue);
        issue.save();
        return issue;
    }

    @Override
    public String getComponentName() {
        return IssueDeviceLifecycleService.COMPONENT_NAME;
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
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(IssueDeviceLifecycleService.COMPONENT_NAME, "Issue Device Lifecycle");
    }

    public DataModel getDataModel() {
        return this.dataModel;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
        this.issueActionService = issueService.getIssueActionService();
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(IssueDeviceLifecycleService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference(target = "(com.elster.jupiter.license.rest.key=" + IssueLifecycleProcessAssociationProvider.APP_KEY + ")")
    public void setLicense(License license) {
        // explicit dependency on license
    }

    @Override
    public Optional<? extends OpenIssue> getOpenIssue(OpenIssue issue) {
        return issue instanceof OpenIssueDeviceLifecycle ? Optional.of(issue) : findOpenIssue(issue.getId());
    }

    @Override
    public Optional<? extends HistoricalIssue> getHistoricalIssue(HistoricalIssue issue) {
        return issue instanceof HistoricalIssueDeviceLifecycle ? Optional.of(issue) : findHistoricalIssue(issue.getId());
    }

    @Override
    public Finder<? extends IssueDeviceLifecycle> findAllDeviceLifecycleIssues(DeviceLifecycleIssueFilter filter) {
        Condition condition = buildConditionFromFilter(filter);

        Class<? extends IssueDeviceLifecycle> mainClass;
        Class<? extends Issue> issueEager;
        List<IssueStatus> statuses = filter.getStatuses();
        if (!statuses.isEmpty() && statuses.stream().allMatch(status -> !status.isHistorical())) {
            mainClass = OpenIssueDeviceLifecycle.class;
            issueEager = OpenIssue.class;
        } else if (!statuses.isEmpty() && statuses.stream().allMatch(IssueStatus::isHistorical)) {
            mainClass = HistoricalIssueDeviceLifecycle.class;
            issueEager = HistoricalIssue.class;
        } else {
            mainClass = IssueDeviceLifecycle.class;
            issueEager = Issue.class;
        }
        return DefaultFinder.of(mainClass, condition, dataModel, issueEager, IssueStatus.class, EndDevice.class, User.class,  IssueReason.class, IssueType.class);
    }


    @Override
    public Finder<? extends IssueDeviceLifecycle> findIssues(DeviceLifecycleIssueFilter filter, Class<?>... eagers) {
        Condition condition = buildConditionFromFilter(filter);
        List<Class<?>> eagerClasses = determineMainApiClass(filter);
        if (eagers == null) {
            eagerClasses.addAll(Arrays.asList(IssueStatus.class, User.class, IssueReason.class, IssueType.class));
        } else {
            eagerClasses.addAll(Arrays.asList(eagers));
        }
        return DefaultFinder.of((Class<IssueDeviceLifecycle>) eagerClasses.remove(0), condition, dataModel, eagerClasses.toArray(new Class<?>[eagerClasses
                .size()]));
    }

    private List<Class<?>> determineMainApiClass(DeviceLifecycleIssueFilter filter) {
        List<Class<?>> eagerClasses = new ArrayList<>();
        List<IssueStatus> statuses = filter.getStatuses();
        if (!statuses.isEmpty() && statuses.stream().allMatch(status -> !status.isHistorical())) {
            eagerClasses.add(OpenIssueDeviceLifecycle.class);
            eagerClasses.add(OpenIssue.class);
        } else if (!statuses.isEmpty() && statuses.stream().allMatch(IssueStatus::isHistorical)) {
            eagerClasses.add(HistoricalIssueDeviceLifecycle.class);
            eagerClasses.add(HistoricalIssue.class);
        } else {
            eagerClasses.add(IssueDeviceLifecycle.class);
            eagerClasses.add(Issue.class);
        }
        return eagerClasses;
    }

    private Condition buildConditionFromFilter(DeviceLifecycleIssueFilter filter) {
        Condition condition = Condition.TRUE;
        //filter by assignee
        Condition assigneeCondition = Condition.TRUE;
        if (filter.getAssignee().isPresent()) {
            assigneeCondition = where(IssueDeviceLifecycleImpl.Fields.BASEISSUE.fieldName() + ".user").isEqualTo(filter.getAssignee().get());
        }
        if (filter.isUnassignedOnly()) {
            assigneeCondition = where(IssueDeviceLifecycleImpl.Fields.BASEISSUE.fieldName() + ".user").isNull();
        }
        condition = condition.and(assigneeCondition);
        //filter by reason
        if (filter.getIssueReason().isPresent()) {
            condition = condition.and(where(IssueDeviceLifecycleImpl.Fields.BASEISSUE.fieldName() + ".reason").isEqualTo(filter.getIssueReason().get()));
        }
        //filter by device
        if (filter.getDevice().isPresent()) {
            condition = condition.and(where(IssueDeviceLifecycleImpl.Fields.BASEISSUE.fieldName() + ".device").isEqualTo(filter.getDevice().get()));
        }
        //filter by statuses
        if (!filter.getStatuses().isEmpty()) {
            condition = condition.and(where(IssueDeviceLifecycleImpl.Fields.BASEISSUE.fieldName() + ".status").in(filter.getStatuses()));
        }
        return condition;
    }

    @Override
    public Set<String> getIssueTypeIdentifiers() {
        return Collections.singleton(IssueDeviceLifecycleService.ISSUE_TYPE_NAME);
    }

    public Thesaurus thesaurus() {
        return thesaurus;
    }
}
