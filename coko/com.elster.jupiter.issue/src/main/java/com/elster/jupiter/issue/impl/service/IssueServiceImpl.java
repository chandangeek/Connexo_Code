package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.IssueFilterImpl;
import com.elster.jupiter.issue.impl.IssueGroupFilterImpl;
import com.elster.jupiter.issue.impl.database.TableSpecs;
import com.elster.jupiter.issue.impl.database.groups.IssuesGroupOperation;
import com.elster.jupiter.issue.impl.module.Installer;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.module.TranslationKeys;
import com.elster.jupiter.issue.impl.records.IssueReasonImpl;
import com.elster.jupiter.issue.impl.records.IssueStatusImpl;
import com.elster.jupiter.issue.impl.records.IssueTypeImpl;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueActionFactory;
import com.elster.jupiter.issue.share.IssueCreationValidator;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.IssueGroupFilter;
import com.elster.jupiter.issue.share.IssueProvider;
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
import com.elster.jupiter.issue.share.entity.NotUniqueKeyException;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
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
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.kie.api.io.KieResources;
import org.kie.internal.KnowledgeBaseFactoryService;
import org.kie.internal.builder.KnowledgeBuilderFactoryService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

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
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile Thesaurus thesaurus;

    private volatile KnowledgeBuilderFactoryService knowledgeBuilderFactoryService;
    private volatile KnowledgeBaseFactoryService knowledgeBaseFactoryService;
    private volatile KieResources resourceFactoryService;

    private volatile IssueActionService issueActionService;
    private volatile IssueAssignmentService issueAssignmentService;
    private volatile IssueCreationService issueCreationService;

    private volatile UpgradeService upgradeService;

    private final Map<String, IssueActionFactory> issueActionFactories = new ConcurrentHashMap<>();
    private final Map<String, CreationRuleTemplate> creationRuleTemplates = new ConcurrentHashMap<>();
    private final List<IssueProvider> issueProviders = new ArrayList<>();
    private final List<IssueCreationValidator> issueCreationValidators = new CopyOnWriteArrayList<>();


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
                            UpgradeService upgradeService) {
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

        activate();
    }

    @Activate
    public void activate() {
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
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
            }
        });
        issueCreationService = dataModel.getInstance(IssueCreationService.class);
        issueActionService = dataModel.getInstance(IssueActionService.class);
        issueAssignmentService = dataModel.getInstance(IssueAssignmentService.class);
        upgradeService.register(InstallIdentifier.identifier("Pulse", COMPONENT_NAME), dataModel, Installer.class, Collections.emptyMap());
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.DOMAIN);
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

    public void removeCreationRuleTemplate(CreationRuleTemplate template) {
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
        return issueProviders;
    }

    @Override
    public Optional<? extends Issue> findIssue(long id) {
        Optional<? extends Issue> issue = findOpenIssue(id);
        if (!issue.isPresent()){
            issue = findHistoricalIssue(id);
        }
        return issue;
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
    public Optional<IssueComment> findComment(long id) {
        return find(IssueComment.class, id);
    }

    @Override
    public Optional<IssueType> findIssueType(String key) {
        return find(IssueType.class, key);
    }

    @Override
    public Optional<IssueAssignee> findIssueAssignee(AssigneeType assigneeType, long id) {
        return assigneeType != null ? assigneeType.getAssignee(this, userService, id) : Optional.empty();
    }

    @Override
    public boolean checkIssueAssigneeType(String type) {
        return AssigneeType.fromString(type) != null;
    }

    @Override
    public IssueStatus createStatus(String key, boolean isHistorical, TranslationKey translationKey) {
        if(findStatus(key).isPresent()){
            throw new NotUniqueKeyException(thesaurus, key);
        }
        IssueStatusImpl status = dataModel.getInstance(IssueStatusImpl.class);
        status.init(key, isHistorical, translationKey).save();
        return status;
    }

    @Override
    public IssueReason createReason(String key, IssueType type, TranslationKey name, TranslationKey description) {
        if(findReason(key).isPresent()){
            throw new NotUniqueKeyException(thesaurus, key);
        }
        IssueReasonImpl reason = dataModel.getInstance(IssueReasonImpl.class);
        reason.init(key, type, name, description).save();
        return reason;
    }

    @Override
    public IssueType createIssueType(String key, TranslationKey translationKey, String prefix) {
        if(findIssueType(key).isPresent()){
            throw new NotUniqueKeyException(thesaurus, key);
        }
        IssueTypeImpl issueType = dataModel.getInstance(IssueTypeImpl.class);
        issueType.init(key, translationKey, prefix).save();
        return issueType;
    }

    private <T extends Entity> Optional<T> find(Class<T> clazz, Object... key) {
        return dataModel.mapper(clazz).getOptional(key);
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
        return IssuesGroupOperation.from(filter, this.dataModel/*, this.meteringGroupsService*/, thesaurus).execute();
    }

    @Override
    public Finder<OpenIssue> findOpenIssuesForDevices(List<String> mRID) {
        Condition condition = ListOperator.IN.contains("device.mRID", mRID);
        return DefaultFinder.of(OpenIssue.class, condition, dataModel, IssueReason.class, EndDevice.class);
    }

    @Override
    public Finder<OpenIssue> findOpenIssuesForDevice(String mRID) {
        Condition condition = where("device.mRID").isEqualTo(mRID);
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
        return issueCreationValidators;
    }

    @Override
    public Finder<? extends Issue> findIssues(IssueFilter filter, Class<?>... eagers) {
        Condition condition = buildConditionFromFilter(filter);
        List<Class<?>> eagerClasses = determineMainApiClass(filter);
        if (eagers == null) {
            eagerClasses.addAll(Arrays.asList(IssueStatus.class, EndDevice.class, User.class, IssueReason.class, IssueType.class));
        } else {
            eagerClasses.addAll(Arrays.asList(eagers));
        }
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

    private List<Class<?>> determineMainApiClass(IssueFilter filter) {
        List<Class<?>> eagerClasses = new ArrayList<>();
        List<IssueStatus> statuses = filter.getStatuses();
        if (!statuses.isEmpty() && statuses.stream().allMatch(status -> !status.isHistorical())) {
            eagerClasses.add(OpenIssue.class);
        } else if (!statuses.isEmpty() && statuses.stream().allMatch(IssueStatus::isHistorical)) {
            eagerClasses.add(HistoricalIssue.class);
        } else {
            eagerClasses.add(Issue.class);
        }
        return eagerClasses;
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
            condition = condition.and(where("user").in(filter.getAssignees()));
            if (filter.isUnassignedSelected()) {
                condition = condition.or(where("user").isNull());
            }
        }
        if (filter.getAssignees().isEmpty() && filter.isUnassignedSelected()) {
            condition = condition.and(where("user").isNull());
        }
        //filter by reason
        if (!filter.getIssueReasons().isEmpty()) {
            condition = condition.and(where("reason").in(filter.getIssueReasons()));
        }
        //filter by device
        if (!filter.getDevices().isEmpty()) {
            condition = condition.and(where("device").in(filter.getDevices()));
        }
        //filter by statuses
        if (!filter.getStatuses().isEmpty()) {
            condition = condition.and(where("status").in(filter.getStatuses()));
        }
        //filter by issue types
        if (!filter.getIssueTypes().isEmpty()) {
            condition = condition.and(where("reason.issueType").in(filter.getIssueTypes()));
        }
        //filter by due dates
        if (!filter.getDueDates().isEmpty()) {
            Condition dueDateCondition = Condition.FALSE;
            for(int i=0; i<filter.getDueDates().size(); i++) {
                dueDateCondition = dueDateCondition.or(where("dueDate").isGreaterThanOrEqual(filter.getDueDates().get(i).getStartTimeAsInstant())
                        .and(where("dueDate").isLessThan(filter.getDueDates().get(i).getEndTimeAsInstant())));
            }
            condition = condition.and(dueDateCondition);
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
}