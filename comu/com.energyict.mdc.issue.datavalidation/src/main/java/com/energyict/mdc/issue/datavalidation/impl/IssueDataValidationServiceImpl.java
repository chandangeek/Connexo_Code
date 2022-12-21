/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.issue.share.service.spi.IssueGroupTranslationProvider;
import com.elster.jupiter.issue.share.service.spi.IssueReasonTranslationProvider;
import com.elster.jupiter.license.License;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datavalidation.DataValidationIssueFilter;
import com.energyict.mdc.issue.datavalidation.HistoricalIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.impl.entity.IssueDataValidationImpl;
import com.energyict.mdc.issue.datavalidation.impl.entity.OpenIssueDataValidationImpl;
import com.energyict.mdc.issue.datavalidation.impl.template.DataValidationIssueCreationRuleTemplate;
import com.energyict.mdc.issue.datavalidation.impl.template.SuspectCreatedIssueCreationRuleTemplate;

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

@Component(name = "com.energyict.mdc.issue.datavalidation",
        service = {TranslationKeyProvider.class, MessageSeedProvider.class, IssueDataValidationService.class, IssueProvider.class, IssueGroupTranslationProvider.class, IssueReasonTranslationProvider.class},
        property = "name=" + IssueDataValidationService.COMPONENT_NAME,
        immediate = true)
public class IssueDataValidationServiceImpl implements IssueDataValidationService, TranslationKeyProvider, MessageSeedProvider, IssueProvider, IssueGroupTranslationProvider, IssueReasonTranslationProvider {

    private volatile IssueService issueService;
    private volatile IssueActionService issueActionService;
    private volatile Thesaurus thesaurus;
    private volatile EventService eventService;
    private volatile MessageService messageService;
    private volatile UpgradeService upgradeService;
    private volatile PropertySpecService propertySpecService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private volatile MeteringTranslationService meteringTranslationService;
    private volatile TimeService timeService;
    private volatile ValidationService validationService;
    /* for dependency - startup/installation order */
    private volatile MeteringService meteringService;
    private volatile EstimationService estimationService;

    private volatile DataModel dataModel;

    private final List<ServiceRegistration> serviceRegistrations = new ArrayList<>();

    public IssueDataValidationServiceImpl() {
        // for OSGi
    }

    @Inject
    public IssueDataValidationServiceImpl(OrmService ormService, IssueService issueService, NlsService nlsService, EventService eventService,
                                          MessageService messageService, UpgradeService upgradeService, BundleContext bundleContext,
                                          PropertySpecService propertySpecService, DeviceConfigurationService deviceConfigurationService,
                                          DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, MeteringTranslationService meteringTranslationService,
                                          TimeService timeService, ValidationService validationService) {
        this();
        setOrmService(ormService);
        setIssueService(issueService);
        setNlsService(nlsService);
        setEventService(eventService);
        setMessageService(messageService);
        setUpgradeService(upgradeService);
        setPropertySpecService(propertySpecService);
        setDeviceConfigurationService(deviceConfigurationService);
        setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
        setMeteringTranslationService(meteringTranslationService);
        setTimeService(timeService);
        setValidationService(validationService);
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
                bind(IssueCreationService.class).toInstance(issueService.getIssueCreationService());
                bind(IssueDataValidationService.class).toInstance(IssueDataValidationServiceImpl.this);
                bind(EventService.class).toInstance(eventService);
                bind(MessageService.class).toInstance(messageService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(DeviceLifeCycleConfigurationService.class).toInstance(deviceLifeCycleConfigurationService);
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(MeteringTranslationService.class).toInstance(meteringTranslationService);
                bind(TimeService.class).toInstance(timeService);
                bind(ValidationService.class).toInstance(validationService);
            }
        });

        registerCreationRuleTemplateServices(bundleContext);
        registerProcessAssociationProvider(bundleContext);

        upgradeService.register(
                InstallIdentifier.identifier("MultiSense", IssueDataValidationService.COMPONENT_NAME),
                dataModel,
                Installer.class,
                ImmutableMap.of(
                        Version.version(10, 2), UpgraderV10_2.class,
                        Version.version(10, 7, 2), UpgraderV10_7_2.class,
                        Version.version(10, 8), UpgraderV10_8.class,
                        Version.version(10, 8, 1), UpgraderV10_8_1.class
                ));
    }

    @Deactivate
    public void stop() {
        serviceRegistrations.forEach(ServiceRegistration::unregister);
        serviceRegistrations.clear();
    }

    @Override
    public Optional<? extends IssueDataValidation> findIssue(long id) {
        Optional<OpenIssueDataValidation> issue = findOpenIssue(id);
        if (issue.isPresent()) {
            return issue;
        }
        return findHistoricalIssue(id);
    }

    @Override
    public Optional<? extends IssueDataValidation> findAndLockIssueDataValidationByIdAndVersion(long id, long version) {
        Optional<? extends Issue> issue = issueService.findAndLockIssueByIdAndVersion(id, version);
        if (issue.isPresent()) {
            return findOpenIssue(id);
        }
        return findHistoricalIssue(id);
    }

    @Override
    public Optional<OpenIssueDataValidation> findOpenIssue(long id) {
        return dataModel.query(OpenIssueDataValidation.class, OpenIssue.class)
                .select(Where.where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".id").isEqualTo(id))
                .stream()
                .findFirst();
    }

    @Override
    public Optional<HistoricalIssueDataValidation> findHistoricalIssue(long id) {
        return dataModel.query(HistoricalIssueDataValidation.class, HistoricalIssue.class)
                .select(Where.where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".id").isEqualTo(id))
                .stream()
                .findFirst();
    }

    @Override
    public OpenIssueDataValidation createIssue(OpenIssue baseIssue, IssueEvent issueEvent) {
        OpenIssueDataValidationImpl issue = dataModel.getInstance(OpenIssueDataValidationImpl.class);
        issue.setIssue(baseIssue);
        issueEvent.apply(issue);
        issue.save();
        return issue;
    }

    @Override
    public String getComponentName() {
        return IssueDataValidationService.COMPONENT_NAME;
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
        dataModel = ormService.newDataModel(IssueDataValidationService.COMPONENT_NAME, "Issue Data Validation");
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
        this.thesaurus = nlsService.getThesaurus(IssueDataValidationService.COMPONENT_NAME, Layer.DOMAIN);
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
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setPropertySpecService(final PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setDeviceConfigurationService(final DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    @Reference
    public void setMeteringTranslationService(final MeteringTranslationService meteringTranslationService) {
        this.meteringTranslationService = meteringTranslationService;
    }

    @Reference
    public void setTimeService(final TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setValidationService(final ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference(target = "(com.elster.jupiter.license.rest.key=" + IssueDataValidationAssociationProvider.APP_KEY + ")")
    public void setLicense(License license) {
        // explicit dependency on license
    }

    @Override
    public Optional<? extends OpenIssue> getOpenIssue(OpenIssue issue) {
        return issue instanceof OpenIssueDataValidation ? Optional.of(issue) : findOpenIssue(issue.getId());
    }

    @Override
    public Optional<? extends HistoricalIssue> getHistoricalIssue(HistoricalIssue issue) {
        return issue instanceof HistoricalIssueDataValidation ? Optional.of(issue) : findHistoricalIssue(issue.getId());
    }

    @Override
    public Finder<? extends IssueDataValidation> findAllDataValidationIssues(DataValidationIssueFilter filter) {
        Condition condition = buildConditionFromFilter(filter);

        Class<? extends IssueDataValidation> mainClass;
        Class<? extends Issue> issueEager;
        List<IssueStatus> statuses = filter.getStatuses();
        if (!statuses.isEmpty() && statuses.stream().noneMatch(IssueStatus::isHistorical)) {
            mainClass = OpenIssueDataValidation.class;
            issueEager = OpenIssue.class;
        } else if (!statuses.isEmpty() && statuses.stream().allMatch(IssueStatus::isHistorical)) {
            mainClass = HistoricalIssueDataValidation.class;
            issueEager = HistoricalIssue.class;
        } else {
            mainClass = IssueDataValidation.class;
            issueEager = Issue.class;
        }
        return DefaultFinder.of(mainClass, condition, dataModel, issueEager, IssueStatus.class, EndDevice.class, User.class, IssueReason.class, IssueType.class);
    }

    private Condition buildConditionFromFilter(DataValidationIssueFilter filter) {
        Condition condition = Condition.TRUE;
        //filter by assignee
        Condition assigneeCondition = Condition.TRUE;
        if (filter.getAssignee().isPresent()) {
            assigneeCondition = where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".user").isEqualTo(filter.getAssignee().get());
        }
        if (filter.isUnassignedOnly()) {
            assigneeCondition = where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".user").isNull();
        }
        condition = condition.and(assigneeCondition);
        //filter by reason
        if (filter.getIssueReason().isPresent()) {
            condition = condition.and(where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".reason").isEqualTo(filter.getIssueReason().get()));
        }
        //filter by device
        if (filter.getDevice().isPresent()) {
            condition = condition.and(where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".device").isEqualTo(filter.getDevice().get()));
        }
        //filter by statuses
        if (!filter.getStatuses().isEmpty()) {
            condition = condition.and(where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".status").in(filter.getStatuses()));
        }
        //filter by rules
        if (!filter.getRules().isEmpty()) {
            condition = condition.and(where(IssueDataValidationImpl.Fields.BASEISSUE.fieldName() + ".rule").in(filter.getRules()));
        }
        return condition;
    }

    private void registerCreationRuleTemplateServices(BundleContext bundleContext) {
        CreationRuleTemplate suspectCreatedIssueCreationRuleTemplate = dataModel.getInstance(SuspectCreatedIssueCreationRuleTemplate.class);
        CreationRuleTemplate dataValidationIssueCreationRuleTemplate = dataModel.getInstance(DataValidationIssueCreationRuleTemplate.class);

        serviceRegistrations.add(bundleContext.registerService(CreationRuleTemplate.class, suspectCreatedIssueCreationRuleTemplate,
                new Hashtable<>(ImmutableMap.of("name", SuspectCreatedIssueCreationRuleTemplate.NAME))));
        serviceRegistrations.add(bundleContext.registerService(CreationRuleTemplate.class, dataValidationIssueCreationRuleTemplate,
                new Hashtable<>(ImmutableMap.of("name", DataValidationIssueCreationRuleTemplate.NAME))));
    }

    private void registerProcessAssociationProvider(BundleContext bundleContext) {
        IssueDataValidationAssociationProvider provider = new IssueDataValidationAssociationProvider(thesaurus, issueService, propertySpecService);
        serviceRegistrations.add(bundleContext.registerService(ProcessAssociationProvider.class, provider,
                new Hashtable<>(ImmutableMap.of("name", IssueDataValidationAssociationProvider.NAME))));
    }

    @Override
    public Set<String> getIssueTypeIdentifiers() {
        return Collections.singleton(IssueDataValidationService.ISSUE_TYPE_NAME);
    }

    public Thesaurus thesaurus() {
        return thesaurus;
    }
}
