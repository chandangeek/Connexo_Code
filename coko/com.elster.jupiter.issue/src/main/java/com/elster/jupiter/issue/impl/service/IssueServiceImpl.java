/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.impl.IssueFilterImpl;
import com.elster.jupiter.issue.impl.IssueGroupFilterImpl;
import com.elster.jupiter.issue.impl.ManualIssueBuilderImpl;
import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.impl.database.TableSpecs;
import com.elster.jupiter.issue.impl.database.UpgraderV10_2;
import com.elster.jupiter.issue.impl.database.UpgraderV10_3;
import com.elster.jupiter.issue.impl.database.UpgraderV10_4;
import com.elster.jupiter.issue.impl.database.UpgraderV10_4_39;
import com.elster.jupiter.issue.impl.database.UpgraderV10_4_9;
import com.elster.jupiter.issue.impl.database.UpgraderV10_5;
import com.elster.jupiter.issue.impl.database.UpgraderV10_6;
import com.elster.jupiter.issue.impl.database.UpgraderV10_7;
import com.elster.jupiter.issue.impl.database.UpgraderV10_7_2;
import com.elster.jupiter.issue.impl.database.UpgraderV10_8;
import com.elster.jupiter.issue.impl.database.UpgraderV10_9_21;
import com.elster.jupiter.issue.impl.database.groups.IssuesGroupOperation;
import com.elster.jupiter.issue.impl.module.Installer;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.module.TranslationKeys;
import com.elster.jupiter.issue.impl.records.IssueAssigneeImpl;
import com.elster.jupiter.issue.impl.records.IssueImpl;
import com.elster.jupiter.issue.impl.records.IssueReasonImpl;
import com.elster.jupiter.issue.impl.records.IssueStatusImpl;
import com.elster.jupiter.issue.impl.records.IssueTypeImpl;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueActionFactory;
import com.elster.jupiter.issue.share.IssueCreationValidator;
import com.elster.jupiter.issue.share.IssueDeviceFilter;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.IssueGroupFilter;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.IssueResourceUtility;
import com.elster.jupiter.issue.share.IssueWebServiceClient;
import com.elster.jupiter.issue.share.entity.AssigneeType;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.IssueTypes;
import com.elster.jupiter.issue.share.entity.NotUniqueKeyException;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.issue.share.service.ManualIssueBuilder;
import com.elster.jupiter.issue.share.service.spi.IssueGroupTranslationProvider;
import com.elster.jupiter.issue.share.service.spi.IssueReasonTranslationProvider;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.Upgrader;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.kie.api.io.KieResources;
import org.kie.internal.KnowledgeBaseFactoryService;
import org.kie.internal.builder.KnowledgeBuilderFactoryService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

@Component(name = "com.elster.jupiter.issue",
        service = {IssueService.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        property = {"name=" + IssueService.COMPONENT_NAME,
                "osgi.command.scope=issue",
                "osgi.command.function=rebuildAssignmentRules",
                "osgi.command.function=loadAssignmentRuleFromFile"},
        immediate = true)
public class IssueServiceImpl implements IssueService, TranslationKeyProvider, MessageSeedProvider {

    private volatile DataModel dataModel;
    private volatile QueryService queryService;
    private volatile UserService userService;
    private volatile MeteringService meteringService;
    private volatile MessageService messageService;
    private volatile TaskService taskService;
    private volatile EventService eventService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private final Set<ComponentAndLayer> alreadyJoined = ConcurrentHashMap.newKeySet();
    private final Object thesaurusLock = new Object();
    private BundleContext bundleContext;


    private volatile KnowledgeBuilderFactoryService knowledgeBuilderFactoryService;
    private volatile KnowledgeBaseFactoryService knowledgeBaseFactoryService;
    private volatile KieResources resourceFactoryService;

    private volatile IssueActionService issueActionService;
    private volatile IssueAssignmentService issueAssignmentService;
    private volatile IssueCreationService issueCreationService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile UpgradeService upgradeService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile TimeService timeService;
    private volatile Clock clock;
    private volatile IssueResourceUtility issueResourceUtility;

    private final Map<String, IssueActionFactory> issueActionFactories = new ConcurrentHashMap<>();
    private final Map<String, CreationRuleTemplate> creationRuleTemplates = new ConcurrentHashMap<>();
    private final List<IssueProvider> issueProviders = new ArrayList<>();
    private final List<IssueWebServiceClient> issueWebServiceClients = new ArrayList<>();
    private final List<IssueCreationValidator> issueCreationValidators = new CopyOnWriteArrayList<>();
    private volatile Optional<IssueDeviceFilter> issueDeviceFilterProvider = Optional.empty();


    public IssueServiceImpl() {
    }

    @Inject
    public IssueServiceImpl(OrmService ormService,
                            QueryService queryService,
                            UserService userService,
                            NlsService nlsService,
                            MessageService messageService,
                            MeteringService meteringService,
                            TaskService taskService,
                            KnowledgeBuilderFactoryService knowledgeBuilderFactoryService,
                            KnowledgeBaseFactoryService knowledgeBaseFactoryService,
                            KieResources resourceFactoryService,
                            TransactionService transactionService,
                            ThreadPrincipalService threadPrincipalService,
                            EndPointConfigurationService endPointConfigurationService,
                            IssueResourceUtility issueResourceUtility,
                            UpgradeService upgradeService, MeteringGroupsService meteringGroupsService, Clock clock, TimeService timeService, EventService eventService, BundleContext bundleContext) {
        setOrmService(ormService);
        setQueryService(queryService);
        setUserService(userService);
        setNlsService(nlsService);
        setMessageService(messageService);
        setMeteringService(meteringService);
        setTaskService(taskService);
        setKnowledgeBuilderFactoryService(knowledgeBuilderFactoryService);
        setKnowledgeBaseFactoryService(knowledgeBaseFactoryService);
        setResourceFactoryService(resourceFactoryService);
        setTransactionService(transactionService);
        setThreadPrincipalService(threadPrincipalService);
        setUpgradeService(upgradeService);
        setMeteringGroupsService(meteringGroupsService);
        setClock(clock);
        setEndPointConfigurationService(endPointConfigurationService);
        setEventService(eventService);
        setTimeService(timeService);
        setIssueResourceUtility(issueResourceUtility);
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
                bind(Thesaurus.class).toProvider(() -> getThesaurus());
                bind(MessageInterpolator.class).toProvider(() -> getThesaurus());
                bind(MessageService.class).toInstance(messageService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(UserService.class).toInstance(userService);
                bind(TaskService.class).toInstance(taskService);
                bind(KieResources.class).toInstance(resourceFactoryService);
                bind(KnowledgeBaseFactoryService.class).toInstance(knowledgeBaseFactoryService);
                bind(KnowledgeBuilderFactoryService.class).toInstance(knowledgeBuilderFactoryService);
                bind(QueryService.class).toInstance(queryService);
                bind(TransactionService.class).toInstance(transactionService);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
                bind(IssueService.class).toInstance(IssueServiceImpl.this);
                bind(IssueActionService.class).to(IssueActionServiceImpl.class).in(Scopes.SINGLETON);
                bind(IssueAssignmentService.class).to(IssueAssignmentServiceImpl.class).in(Scopes.SINGLETON);
                bind(IssueCreationService.class).to(IssueCreationServiceImpl.class).in(Scopes.SINGLETON);
                bind(Clock.class).toInstance(clock);
                bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                bind(EventService.class).toInstance(eventService);
                bind(EndPointConfigurationService.class).toInstance(endPointConfigurationService);
                bind(TimeService.class).toInstance(timeService);
                bind(IssueResourceUtility.class).toInstance(issueResourceUtility);
            }
        });
        setBundleContext(bundleContext);
        issueCreationService = dataModel.getInstance(IssueCreationService.class);
        issueActionService = dataModel.getInstance(IssueActionService.class);
        issueAssignmentService = dataModel.getInstance(IssueAssignmentService.class);
        upgradeService.register(
                InstallIdentifier.identifier("Pulse", COMPONENT_NAME),
                dataModel,
                Installer.class,
                ImmutableMap.<Version, Class<? extends Upgrader>>builder()
                        .put(version(10, 2), UpgraderV10_2.class)
                        .put(version(10, 3), UpgraderV10_3.class)
                        .put(version(10, 4), UpgraderV10_4.class)
                        .put(version(10, 4, 9), UpgraderV10_4_9.class)
                        .put(version(10, 4, 39), UpgraderV10_4_39.class)
                        .put(version(10, 5), UpgraderV10_5.class)
                        .put(version(10, 6), UpgraderV10_6.class)
                        .put(version(10, 7), UpgraderV10_7.class)
                        .put(version(10, 7, 2), UpgraderV10_7_2.class)
                        .put(version(10, 8), UpgraderV10_8.class)
                        .put(version(10, 9, 21), UpgraderV10_9_21.class)
                        .build()
        );
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setIssueResourceUtility(IssueResourceUtility issueResourceUtility) {
        this.issueResourceUtility = issueResourceUtility;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    private void setTimeService(final TimeService timeService) {
        this.timeService = timeService;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }


    public Optional<BundleContext> getBundleContext() {
        return Optional.of(bundleContext);
    }

    private Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(IssueService.COMPONENT_NAME, "Issue Management");
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public final void setKnowledgeBuilderFactoryService(KnowledgeBuilderFactoryService knowledgeBuilderFactoryService) {
        this.knowledgeBuilderFactoryService = knowledgeBuilderFactoryService;
    }

    @Reference
    public final void setKnowledgeBaseFactoryService(KnowledgeBaseFactoryService knowledgeBaseFactoryService) {
        this.knowledgeBaseFactoryService = knowledgeBaseFactoryService;
    }

    @Reference
    public final void setResourceFactoryService(KieResources resourceFactoryService) {
        this.resourceFactoryService = resourceFactoryService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    @SuppressWarnings("unused") // Called by OSGi framework when IssueGroupTranslationProvider component activates
    public void addIssueGroupTranslationProvider(IssueGroupTranslationProvider provider) {
        this.addTranslationProvider(provider.getComponentName(), provider.getLayer());
    }

    @SuppressWarnings("unused") // Called by OSGi framework when IssueGroupTranslationProvider component deactivates
    public void removeIssueGroupTranslationProvider(IssueGroupTranslationProvider obsolete) {
        // Don't bother unjoining the provider's thesaurus
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    @SuppressWarnings("unused") // Called by OSGi framework when IssueReasonTranslationProvider component activates
    public void addIssueReasonTranslationProvider(IssueReasonTranslationProvider provider) {
        this.addTranslationProvider(provider.getComponentName(), provider.getLayer());
    }

    @SuppressWarnings("unused") // Called by OSGi framework when IssueReasonTranslationProvider component deactivates
    public void removeIssueReasonTranslationProvider(IssueReasonTranslationProvider obsolete) {
        // Don't bother unjoining the provider's thesaurus
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void addIssueDeviceFilter(IssueDeviceFilter issueDeviceFilter) {
        this.issueDeviceFilterProvider = Optional.of(issueDeviceFilter);
    }

    public void removeIssueDeviceFilter(IssueDeviceFilter issueDeviceFilter) {
        this.issueDeviceFilterProvider = Optional.empty();
    }

    private void addTranslationProvider(String componentName, Layer layer) {
        synchronized (this.thesaurusLock) {
            ComponentAndLayer componentAndLayer = new ComponentAndLayer(componentName, layer);
            if (!this.alreadyJoined.contains(componentAndLayer)) {
                Thesaurus providerThesaurus = this.nlsService.getThesaurus(componentName, layer);
                this.thesaurus = this.thesaurus.join(providerThesaurus);
                this.alreadyJoined.add(componentAndLayer);
            }
        }
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public String getComponentName() {
        return IssueService.COMPONENT_NAME;
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

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCreationRuleTemplate(CreationRuleTemplate ruleTemplate) {
        creationRuleTemplates.put(ruleTemplate.getName(), ruleTemplate);
        if (issueCreationService != null) {
            issueCreationService.reReadRules();
        }
    }

    void removeCreationRuleTemplate(CreationRuleTemplate template) {
        creationRuleTemplates.remove(template.getName());
        if (issueCreationService != null) {
            issueCreationService.reReadRules();
        }
    }

    public Map<String, CreationRuleTemplate> getCreationRuleTemplates() {
        return creationRuleTemplates;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addIssueActionFactory(IssueActionFactory issueActionFactory) {
        issueActionFactories.put(issueActionFactory.getId(), issueActionFactory);
    }

    public void removeIssueActionFactory(IssueActionFactory issueActionFactory) {
        issueActionFactories.remove(issueActionFactory.getId());
    }

    @Override
    public Map<String, IssueActionFactory> getIssueActionFactories() {
        return issueActionFactories;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addIssueProvider(IssueProvider issueProvider) {
        issueProviders.add(issueProvider);
    }

    public void removeIssueProvider(IssueProvider issueProvider) {
        issueProviders.remove(issueProvider);
    }

    @Override
    public List<IssueProvider> getIssueProviders() {
        return Collections.unmodifiableList(this.issueProviders);
    }

    @Override
    public List<IssueProvider> getIssueProviders(String issueTypeIdentifier) {
        return issueProviders.stream().filter(issueProvider -> issueProvider.getIssueTypeIdentifiers().contains(issueTypeIdentifier)).collect(Collectors.toList());
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addIssueWebServiceClient(IssueWebServiceClient issueWebServiceClient) {
        issueWebServiceClients.add(issueWebServiceClient);
    }

    public void removeIssueWebServiceClient(IssueWebServiceClient issueWebServiceClient) {
        issueWebServiceClients.remove(issueWebServiceClient);
    }

    public List<IssueWebServiceClient> getIssueWebServiceClients() {
        return Collections.unmodifiableList(this.issueWebServiceClients);
    }

    @Override
    public Optional<? extends Issue> findIssue(long id) {
        Optional<? extends Issue> issue = findOpenIssue(id);
        if (!issue.isPresent()) {
            issue = findHistoricalIssue(id);
        }
        return issue.isPresent() && !issue.get().getReason().getIssueType().getPrefix().equals("ALM") ? issue : Optional.empty();
    }

    @Override
    public ManualIssueBuilder newIssueBuilder() {
        return new ManualIssueBuilderImpl((User) threadPrincipalService.getPrincipal(), dataModel);
    }

    @Override
    public Optional<OpenIssue> findOpenIssue(long id) {
        return find(OpenIssue.class, id);
    }

    @Override
    public Optional<HistoricalIssue> findHistoricalIssue(long id) {
        return find(HistoricalIssue.class, id);
    }

    @Override
    public Optional<IssueStatus> findStatus(String key) {
        return find(IssueStatus.class, key);
    }

    @Override
    public Optional<IssueReason> findReason(String key) {
        return find(IssueReason.class, key);
    }

    @Override
    public IssueReason findOrCreateReason(String key, IssueType issueType) {
        Optional<IssueReason> reason = find(IssueReason.class, key);
        if (reason.isPresent()) {
            return reason.get();
        } else {
            if (!key.isEmpty()) {
                return createReason(key, issueType, new SimpleTranslationKey(key, key), null);
            } else {
                return null;
            }
        }
    }

    @Override
    public Optional<IssueComment> findComment(long id) {
        return find(IssueComment.class, id);
    }

    @Override
    public Optional<IssueType> findIssueType(String key) {
        return find(IssueType.class, key);
    }

    @Override
    public IssueAssignee findIssueAssignee(Long userId, Long workGroupId) {
        IssueAssigneeImpl issueAssignee = new IssueAssigneeImpl();
        issueAssignee.setUser(userId != null ? userService.getUser(userId).orElse(null) : null);
        issueAssignee.setWorkGroup(workGroupId != null ? userService.getWorkGroup(workGroupId).orElse(null) : null);
        return issueAssignee;
    }

    @Override
    public Optional<IssueAssignee> findIssueAssignee(AssigneeType assigneeType, long id) {
        return Optional.of(findIssueAssignee(id, null));
    }

    @Override
    public IssueStatus createStatus(String key, boolean isHistorical, TranslationKey translationKey) {
        if (findStatus(key).isPresent()) {
            throw new NotUniqueKeyException(thesaurus, key);
        }
        IssueStatusImpl status = dataModel.getInstance(IssueStatusImpl.class);
        status.init(key, isHistorical, translationKey).save();
        return status;
    }

    @Override
    public IssueReason createReason(String key, IssueType type, TranslationKey name, TranslationKey description) {
        if (findReason(key).isPresent()) {
            throw new NotUniqueKeyException(thesaurus, key);
        }
        IssueReasonImpl reason = dataModel.getInstance(IssueReasonImpl.class);
        reason.init(key, type, name, description).save();
        return reason;
    }

    @Override
    public IssueType createIssueType(String key, TranslationKey translationKey, String prefix) {
        if (findIssueType(key).isPresent()) {
            throw new NotUniqueKeyException(thesaurus, key);
        }
        IssueTypeImpl issueType = dataModel.getInstance(IssueTypeImpl.class);
        issueType.init(key, translationKey, prefix).save();
        return issueType;
    }

    private <T extends Entity> Optional<T> find(Class<T> clazz, Object... key) {
        return dataModel.mapper(clazz).getOptional(key);
    }

    private List<IssueType> getAllIssueTypes() {
        return dataModel.mapper(IssueType.class).find();
    }

    @Override
    public <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers) {
        QueryExecutor<T> queryExecutor = dataModel.query(clazz, eagers);
        Query<T> query = queryService.wrap(queryExecutor);
        query.setEager();
        return query;
    }

    @Override
    public List<IssueGroup> getIssueGroupList(IssueGroupFilter filter) {
        return IssuesGroupOperation.from(filter, this.dataModel, thesaurus).execute();
    }

    @Override
    public Finder<OpenIssue> findOpenIssuesForDevices(List<String> deviceNames) {
        Condition condition = ListOperator.IN.contains("device.name", deviceNames);
        return DefaultFinder.of(OpenIssue.class, condition, dataModel, IssueReason.class, EndDevice.class);
    }

    @Override
    public Map<Long, List<String>> findOpenIssuesPerIssueTypeForDevices(List<Long> deviceIds) {
        Map<Long, List<String>> issuesPerReason = new HashMap<>();

        if (deviceIds.size() == 0) {
            return issuesPerReason;
        }
        SqlBuilder sqlBuilder = new SqlBuilder("SELECT " +
                " ed.id, ri.issue_type " +
                " FROM mtr_enddevice ed " +
                "   INNER JOIN isu_issue_open oi ON ( oi.device_id = ed.id ) " +
                "   LEFT JOIN isu_reason ri on ri.key = oi.reason_id " +
                " WHERE ");
        sqlBuilder.append(decorate(deviceIds.stream())
                .partitionPer(1000)
                .map(list -> "ed.id IN (" + list.stream().map(l -> l.toString()).collect(Collectors.joining(",")) + ")")
                .collect(Collectors.joining(" OR ", "(", ")")));
        sqlBuilder.append(" GROUP BY ed.id, issue_type");
        try (Connection connection = this.dataModel.getConnection(false);
             PreparedStatement statement = sqlBuilder.prepare(connection)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Long endDeviceId = resultSet.getLong(1);
                    String issueType = resultSet.getString(2);

                    List<String> issueTypes = issuesPerReason.get(endDeviceId);
                    if (issueTypes == null) {
                        issueTypes = new ArrayList<>();
                        issuesPerReason.put(endDeviceId, issueTypes);
                    }
                    issueTypes.add(issueType);
                }
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        return issuesPerReason;
    }

    @Override
    public Finder<OpenIssue> findOpenIssuesForDevice(String deviceName) {
        Condition condition = where("device.name").isEqualTo(deviceName);
        return DefaultFinder.of(OpenIssue.class, condition, dataModel, IssueReason.class, EndDevice.class);
    }

    @Override
    public IssueActionService getIssueActionService() {
        return issueActionService;
    }

    @Override
    public IssueAssignmentService getIssueAssignmentService() {
        return issueAssignmentService;
    }

    @Override
    public IssueCreationService getIssueCreationService() {
        return issueCreationService;
    }

    public void rebuildAssignmentRules() {
        issueAssignmentService.rebuildAssignmentRules();
    }

    public void loadAssignmentRuleFromFile(String absolutePath) {
        issueAssignmentService.loadAssignmentRuleFromFile(absolutePath);
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addIssueCreationValidator(IssueCreationValidator issueCreationValidator) {
        issueCreationValidators.add(issueCreationValidator);
    }

    public void removeIssueCreationValidator(IssueCreationValidator issueCreationValidator) {
        issueCreationValidators.remove(issueCreationValidator);
    }

    @Override
    public List<IssueCreationValidator> getIssueCreationValidators() {
        return Collections.unmodifiableList(this.issueCreationValidators);
    }

    @Override
    public Finder<? extends Issue> findIssues(IssueFilter filter, Class<?>... eagers) {
        Condition condition = buildConditionFromFilter(filter);
        List<Class<?>> eagerClasses = determineMainApiClass(filter);
        if (eagers != null && eagers.length > 0) {
            eagerClasses.addAll(Arrays.asList(eagers));
        }
        eagerClasses.addAll(Arrays.asList(IssueReason.class, IssueType.class));
        return DefaultFinder.of((Class<Issue>) eagerClasses.remove(0), condition, dataModel, eagerClasses.toArray(new Class<?>[eagerClasses.size()]));
    }

    @Override
    public Finder<? extends Issue> findAlarms(IssueFilter filter, Class<?>... eagers) {
        Condition condition = buildAlarmConditionFromFilter(filter);
        Optional<IssueType> alarmIssueType = getAllIssueTypes().stream()
                .filter(issueType -> issueType.getPrefix().equals("ALM"))
                .findFirst();
        if (alarmIssueType.isPresent()) {
            condition = condition.and(where("reason.issueType").isEqualTo(alarmIssueType.get()));
        } else {
            condition = Condition.FALSE;
        }
        List<Class<?>> eagerClasses = determineMainApiClass(filter);
        if (eagers != null && eagers.length > 0) {
            eagerClasses.addAll(Arrays.asList(eagers));
        }
        eagerClasses.addAll(Arrays.asList(IssueReason.class, IssueType.class));
        return DefaultFinder.of((Class<Issue>) eagerClasses.remove(0), condition, dataModel, eagerClasses.toArray(new Class<?>[eagerClasses.size()]));
    }

    @Override
    public Optional<? extends Issue> findAndLockIssueByIdAndVersion(long id, long version) {
        Optional<? extends Issue> issue = findOpenIssue(id);
        if (issue.isPresent()) {
            return dataModel.mapper(OpenIssue.class).lockObjectIfVersion(version, id);
        }
        return dataModel.mapper(HistoricalIssue.class).lockObjectIfVersion(version, id);
    }

    @Override
    public IssueFilter newIssueFilter() {
        return new IssueFilterImpl();
    }

    @Override
    public IssueGroupFilter newIssueGroupFilter() {
        return new IssueGroupFilterImpl();
    }

    @Override
    public boolean checkIssueAssigneeType(String type) {
        return true;
    }

    @Override
    public Map<IssueTypes, Long> getUserOpenIssueCount(User user) {
        return this.count(getOpenIssueCountGenericQueryBuilder(getUserWhereClause(user)));
    }

    @Override
    public Map<IssueTypes, Long> getWorkGroupWithoutUserOpenIssueCount(User user) {
        return this.count(getOpenIssueCountGenericQueryBuilder(getWorkGroupWithoutUserWhereClause(user)));
    }

    @Override
    public List<IssueGroup> getIssuesGroupList(IssueGroupFilter filter) {
        return IssuesGroupOperation.from(filter, this.dataModel, thesaurus).execute();
    }

    @Override
    public Issue wrapOpenOrHistorical(Issue issue) {
        return IssueImpl.wrapOpenOrHistorical(issue);
    }

    private SqlBuilder getOpenIssueCountGenericQueryBuilder(SqlBuilder whereClause) {
        SqlBuilder openIssueGenericQueryBuilder = new SqlBuilder("SELECT " + TableSpecs.ISU_REASON.name() + "." + DatabaseConst.ISSUE_REASON_COLUMN_TYPE + " " + DatabaseConst.ISSUE_REASON_COLUMN_TYPE
                + " , COUNT(" + TableSpecs.ISU_ISSUE_OPEN.name() + ".ID) " + IssueService.COMPONENT_NAME + " FROM ");
        openIssueGenericQueryBuilder.append(TableSpecs.ISU_ISSUE_OPEN.name() + " " + TableSpecs.ISU_ISSUE_OPEN.name());
        openIssueGenericQueryBuilder.append(" RIGHT JOIN " + TableSpecs.ISU_REASON.name() + " " + TableSpecs.ISU_REASON.name()
                + " ON " + TableSpecs.ISU_ISSUE_OPEN.name() + ".REASON_ID = " + TableSpecs.ISU_REASON.name() + "." + DatabaseConst.ISSUE_REASON_COLUMN_KEY + " ");
        openIssueGenericQueryBuilder.add(whereClause);
        openIssueGenericQueryBuilder.append(" GROUP BY " + TableSpecs.ISU_REASON.name() + "." + DatabaseConst.ISSUE_REASON_COLUMN_TYPE);
        return openIssueGenericQueryBuilder;
    }

    private SqlBuilder getUserWhereClause(User user) {
        SqlBuilder userWhereClause = new SqlBuilder();
        userWhereClause.append("WHERE " + TableSpecs.ISU_ISSUE_OPEN.name() + "." + DatabaseConst.ISSUE_COLUMN_USER_ID + " = ");
        userWhereClause.addLong(user.getId());
        userWhereClause.append(" AND ");
        userWhereClause.append(getActiveIssueStatusCondition(Stream.of(IssueStatus.OPEN, IssueStatus.IN_PROGRESS).collect(Collectors.toList())));
        return userWhereClause;
    }

    private SqlBuilder getWorkGroupWithoutUserWhereClause(User user) {
        SqlBuilder workGroupWithoutUserWhereClause = new SqlBuilder();
        /*CONM-1426(WorkGroup device information is incorrect)
        workGroupWithoutUserWhereClause.append(" WHERE " + TableSpecs.ISU_ISSUE_OPEN.name() + "." + DatabaseConst.ISSUE_COLUMN_USER_ID + " IS NULL");
        workGroupWithoutUserWhereClause.append(" AND ");
         */
        workGroupWithoutUserWhereClause.append(" WHERE ");
        workGroupWithoutUserWhereClause.append(TableSpecs.ISU_ISSUE_OPEN.name() + "." + DatabaseConst.ISSUE_COLUMN_WORKGROUP_ID + " IN ( ");
        workGroupWithoutUserWhereClause.append(user.getWorkGroups().isEmpty() ? "NULL" : user.getWorkGroups().stream().map(WorkGroup::getId).map(String::valueOf).collect(Collectors.joining(", ")));
        workGroupWithoutUserWhereClause.append(" ) ");
        workGroupWithoutUserWhereClause.append(" AND ");
        workGroupWithoutUserWhereClause.append(getActiveIssueStatusCondition(Stream.of(IssueStatus.OPEN, IssueStatus.IN_PROGRESS).collect(Collectors.toList())));
        return workGroupWithoutUserWhereClause;
    }

    private String getActiveIssueStatusCondition(List<String> statuses) {
        StringBuffer activeIssueStatusCondition = new StringBuffer();
        activeIssueStatusCondition.append(TableSpecs.ISU_ISSUE_OPEN.name() + "." + DatabaseConst.ISSUE_COLUMN_STATUS_ID + " IN (");
        activeIssueStatusCondition.append(statuses.stream()
                .map((status) -> "'" + status + "'")
                .collect(Collectors.joining(", ")));
        activeIssueStatusCondition.append(" ) ");
        return activeIssueStatusCondition.toString();
    }

    private Map<IssueTypes, Long> count(SqlBuilder sqlBuilder) {
        Map<IssueTypes, Long> countMap = new HashMap<>();
        try (Connection connection = this.dataModel.getConnection(false);
             PreparedStatement statement = sqlBuilder.prepare(connection)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                int typeColPos = resultSet.findColumn(DatabaseConst.ISSUE_REASON_COLUMN_TYPE);
                int valueColPos = resultSet.findColumn(IssueService.COMPONENT_NAME);
                while (resultSet.next()) {
                    IssueTypes typeCol = IssueTypes.getByName(resultSet.getString(typeColPos));
                    Long valueCol = resultSet.getLong(valueColPos);
                    countMap.put(typeCol, valueCol);
                }
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        return countMap;
    }


    private List<Class<?>> determineMainApiClass(IssueFilter filter) {
        List<Class<?>> eagerClasses = new ArrayList<>();
        List<IssueStatus> statuses = filter.getStatuses();
        if (!statuses.isEmpty() && statuses.stream().noneMatch(IssueStatus::isHistorical)) {
            eagerClasses.add(OpenIssue.class);
        } else if (!statuses.isEmpty() && statuses.stream().allMatch(IssueStatus::isHistorical)) {
            eagerClasses.add(HistoricalIssue.class);
        } else {
            eagerClasses.add(Issue.class);
        }
        return eagerClasses;
    }

    private Condition getDeviceGroupSearchCondition(Collection<EndDeviceGroup> endDeviceGroups) {
        return endDeviceGroups.stream()
                .map(endDeviceGroup -> ListOperator.IN.contains(endDeviceGroup.toSubQuery("id"), "device"))
                .map(Condition.class::cast)
                .reduce(Condition.FALSE, Condition::or);
    }

    private Condition getUsagePointGroupSearchCondition(Collection<UsagePointGroup> usagePointGroups) {
        return usagePointGroups.stream()
                .map(usagePointGroup -> ListOperator.IN.contains(usagePointGroup.toSubQuery("id"), "usagePoint"))
                .map(Condition.class::cast)
                .reduce(Condition.FALSE, Condition::or);
    }

    private Condition buildConditionFromFilter(IssueFilter filter) {
        Condition condition = Condition.TRUE;
        //filter by issue id
        if (filter.getIssueId().isPresent()) {
            String[] issueIdPart = filter.getIssueId().get().split("-");
            if (issueIdPart.length == 2) {
                condition = condition.and(where("id").isEqualTo(getNumericValueOrZero(issueIdPart[1])))
                        .and(where("reason.issueType.prefix").isEqualTo(issueIdPart[0].toUpperCase()));
            } else {
                condition = condition.and(where("id").isEqualTo(0));
            }
        }
        //filter by assignee
        if (!filter.getAssignees().isEmpty()) {
            Condition userCondition = Condition.TRUE;
            userCondition = userCondition.and(where("user").in(filter.getAssignees()));
            if (filter.isUnassignedSelected()) {
                userCondition = userCondition.or(where("user").isNull());
            }
            condition = condition.and(userCondition);
        }
        if (filter.getAssignees().isEmpty() && filter.isUnassignedSelected()) {
            condition = condition.and(where("user").isNull());
        }
        //filter by workGroup
        if (!filter.getWorkGroupAssignees().isEmpty()) {
            Condition wgCondition = Condition.TRUE;
            wgCondition = wgCondition.and(where("workGroup").in(filter.getWorkGroupAssignees()));
            if (filter.isUnassignedWorkGroupSelected()) {
                wgCondition = wgCondition.or(where("workGroup").isNull());
            }
            condition = condition.and(wgCondition);
        }
        if (filter.getWorkGroupAssignees().isEmpty() && filter.isUnassignedWorkGroupSelected()) {
            condition = condition.and(where("workGroup").isNull());
        }
        //filter by reason
        if (!filter.getIssueReasons().isEmpty()) {
            condition = condition.and(where("reason").in(filter.getIssueReasons()));
        }
        //filter by device
        if (!filter.getDevices().isEmpty()) {
            List<EndDevice> filterDevices = new ArrayList<>(filter.getDevices());
            //add topology devices
            if (filter.getShowTopology() && issueDeviceFilterProvider.isPresent()) {
                filterDevices.addAll(issueDeviceFilterProvider.get().getShowTopologyCondition(filter.getDevices()));
            }
            condition = condition.and(where("device").in(filterDevices));
        }
        //filter by location
        if (!filter.getLocations().isEmpty()) {
            condition = condition.and(where("device.location").in(filter.getLocations()));
        }

        //filter by device group
        if (!filter.getDeviceGroups().isEmpty()) {
            condition = condition.and(getDeviceGroupSearchCondition(filter.getDeviceGroups()));
        }
        //filter by usagepoint
        if (!filter.getUsagePoints().isEmpty()) {
            condition = condition.and(where("usagePoint").in(filter.getUsagePoints()));
        }

        //filter by usagepoint
        if (!filter.getUsagePointGroups().isEmpty()) {
            condition = condition.and(getUsagePointGroupSearchCondition(filter.getUsagePointGroups()));
        }

        //filter by statuses
        if (!filter.getStatuses().isEmpty()) {
            condition = condition.and(where("status").in(filter.getStatuses()));
        }
        //filter by issue types
        if (!filter.getIssueTypes().isEmpty()) {
            condition = condition.and(where("reason.issueType").in(filter.getIssueTypes()));
        } else {
            List<IssueType> issueTypes = getAllIssueTypes().stream()
                    .filter(issueType -> !issueType.getPrefix().equals("ALM")).collect(Collectors.toList());
            condition = condition.and(where("reason.issueType").in(Collections.unmodifiableList(issueTypes)));
        }
        //filter by due dates
        if (!filter.getDueDates().isEmpty()) {
            Condition dueDateCondition = Condition.FALSE;
            for (int i = 0; i < filter.getDueDates().size(); i++) {
                dueDateCondition = dueDateCondition.or(where("dueDate").isGreaterThanOrEqual(filter.getDueDates().get(i).getStartTimeAsInstant())
                        .and(where("dueDate").isLessThan(filter.getDueDates().get(i).getEndTimeAsInstant())));
            }
            condition = condition.and(dueDateCondition);
        }

        //filter by createDate
        if (filter.getStartCreateTime() != null && filter.getEndCreateTime() != null) {
            Condition creationDate = Condition.FALSE;
            creationDate = creationDate.or(where("createTime").isGreaterThan(filter.getStartCreateTime())
                    .and(where("createTime").isLessThan(filter.getEndCreateTime())));
            condition = condition.and(creationDate);
        }

        //filter by priority
        if (!filter.getPriorities().isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = null;
            try {
                node = mapper.readTree(filter.getPriorities());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (node.get("operator").asText().equals("<")) {
                condition = condition.and(where("priorityTotal").isLessThan(node.get("criteria").asInt()));
            } else if (node.get("operator").asText().equals(">")) {
                condition = condition.and(where("priorityTotal").isGreaterThan(node.get("criteria").asInt()));
            } else if (node.get("operator").asText().equals("==")) {
                condition = condition.and(where("priorityTotal").isEqualTo(node.get("criteria").asInt()));
            } else if (node.get("operator").asText().equals("BETWEEN")) {
                int first = node.get("criteria").get(0).asInt();
                int second = node.get("criteria").get(1).asInt();
                condition = condition.and(where("priorityTotal").isGreaterThan(first))
                        .and(where("priorityTotal").isLessThan(second));
            }

        }

        // filter by SNOOZEDATETIME
        if (filter.getUntilSnoozeDateTime().isPresent()) {
            condition = condition.and(where("snoozeDateTime").isLessThan(filter.getUntilSnoozeDateTime().get()));
        }
        return condition;
    }

    private Condition buildAlarmConditionFromFilter(IssueFilter filter) {
        Condition condition = Condition.TRUE;

        //filter by statuses
        if (!filter.getStatuses().isEmpty()) {
            condition = condition.and(where("status").in(filter.getStatuses()));
        }

        // filter by SNOOZEDATETIME
        if (filter.getUntilSnoozeDateTime().isPresent()) {
            condition = condition.and(where("snoozeDateTime").isLessThan(filter.getUntilSnoozeDateTime().get()));
        }
        return condition;
    }

    private long getNumericValueOrZero(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    private static final class ComponentAndLayer {
        private final String componentName;
        private final Layer layer;

        static ComponentAndLayer from(IssueGroupTranslationProvider provider) {
            return new ComponentAndLayer(provider.getComponentName(), provider.getLayer());
        }

        static ComponentAndLayer from(IssueReasonTranslationProvider provider) {
            return new ComponentAndLayer(provider.getComponentName(), provider.getLayer());
        }

        ComponentAndLayer(String componentName, Layer layer) {
            this.componentName = componentName;
            this.layer = layer;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ComponentAndLayer that = (ComponentAndLayer) o;
            return Objects.equals(componentName, that.componentName) &&
                    layer == that.layer;
        }

        @Override
        public int hashCode() {
            return Objects.hash(componentName, layer);
        }

    }
}
