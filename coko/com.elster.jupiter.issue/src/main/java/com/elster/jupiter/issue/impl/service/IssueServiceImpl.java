package com.elster.jupiter.issue.impl.service;

import static com.elster.jupiter.util.conditions.Where.where;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;

import org.kie.api.io.KieResources;
import org.kie.internal.KnowledgeBaseFactoryService;
import org.kie.internal.builder.KnowledgeBuilderFactoryService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.database.TableSpecs;
import com.elster.jupiter.issue.impl.database.groups.IssuesGroupOperation;
import com.elster.jupiter.issue.impl.module.Installer;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.records.IssueReasonImpl;
import com.elster.jupiter.issue.impl.records.IssueStatusImpl;
import com.elster.jupiter.issue.impl.records.IssueTypeImpl;
import com.elster.jupiter.issue.impl.records.assignee.AssigneeRoleImpl;
import com.elster.jupiter.issue.impl.records.assignee.AssigneeTeamImpl;
import com.elster.jupiter.issue.impl.records.assignee.types.AssigneeType;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.IssueActionFactory;
import com.elster.jupiter.issue.share.entity.AssigneeRole;
import com.elster.jupiter.issue.share.entity.AssigneeTeam;
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
import com.elster.jupiter.issue.share.service.IssueGroupFilter;
import com.elster.jupiter.issue.share.service.IssueProvider;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

@Component(name = "com.elster.jupiter.issue",
    service = {IssueService.class, InstallService.class, TranslationKeyProvider.class},
    property = {"name=" + IssueService.COMPONENT_NAME,
                "osgi.command.scope=issue",
                "osgi.command.function=rebuildAssignmentRules",
                "osgi.command.function=loadAssignmentRuleFromFile"},
    immediate = true)
public class IssueServiceImpl implements IssueService, InstallService, TranslationKeyProvider {
    private static final Logger LOG = Logger.getLogger(IssueServiceImpl.class.getName());

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

    private volatile Map<String, IssueActionFactory> issueActionFactories = new ConcurrentHashMap<>();
    private volatile Map<String, CreationRuleTemplate> creationRuleTemplates = new ConcurrentHashMap<>();
    private volatile List<IssueProvider> issueProviders = new ArrayList<>();

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
                            ThreadPrincipalService threadPrincipalService) {
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
        
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
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
    
    @Override
    public void install() {
        new Installer(dataModel, this, userService, messageService, taskService).install(true);
    }
    
    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("USR", "TSK", "MSG", "ORM", "NLS", "MTR");
    }
    
    @Override
    public String getComponentName() {
        return IssueService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCreationRuleTemplate(CreationRuleTemplate ruleTemplate) {
        creationRuleTemplates.put(ruleTemplate.getUUID(), ruleTemplate);
        if (issueCreationService != null) {
            issueCreationService.reReadRules();
        }
    }

    public void removeCreationRuleTemplate(CreationRuleTemplate template) {
        creationRuleTemplates.values().remove(template);
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
    public IssueAssignee findIssueAssignee(String type, long id) {
        AssigneeType assigneeType = AssigneeType.fromString(type);
        if (assigneeType != null) {
            return assigneeType.getAssignee(this, userService, id);
        }
        return null;
    }

    @Override
    public boolean checkIssueAssigneeType(String type) {
        return AssigneeType.fromString(type) != null;
    }

    @Override
    public Optional<AssigneeRole> findAssigneeRole(long id) {
        return find(AssigneeRole.class, id);
    }

    @Override
    public Optional<AssigneeTeam> findAssigneeTeam(long id) {
        return find(AssigneeTeam.class, id);
    }

    @Override
    public IssueStatus createStatus(String key, boolean isHistorical, MessageSeed seed) {
        if(findStatus(key).isPresent()){
            throw new NotUniqueKeyException(thesaurus, key);
        }
        installEntityTranslation(seed);
        IssueStatusImpl status = dataModel.getInstance(IssueStatusImpl.class);
        status.init(key, isHistorical, seed).save();
        return status;
    }

    @Override
    public IssueReason createReason(String key, IssueType type, MessageSeed seed) {
        if(findReason(key).isPresent()){
            throw new NotUniqueKeyException(thesaurus, key);
        }
        installEntityTranslation(seed);
        IssueReasonImpl reason = dataModel.getInstance(IssueReasonImpl.class);
        reason.init(key, type, seed).save();
        return reason;
    }

    @Override
    public IssueType createIssueType(String key, MessageSeed seed) {
        if(findIssueType(key).isPresent()){
            throw new NotUniqueKeyException(thesaurus, key);
        }
        installEntityTranslation(seed);
        IssueTypeImpl issueType = dataModel.getInstance(IssueTypeImpl.class);
        issueType.init(key, seed).save();
        return issueType;
    }

    @Override
    public AssigneeRole createAssigneeRole() {
        return dataModel.getInstance(AssigneeRoleImpl.class);
    }

    @Override
    public AssigneeTeam createAssigneeTeam() {
        return dataModel.getInstance(AssigneeTeamImpl.class);
    }

    private void installEntityTranslation(MessageSeed seed) {
        if (seed == null){
            throw new IllegalArgumentException("Translation for the new entity can't be null");
        }
        if (thesaurus.getTranslations().get(seed.getKey()) == null) {
            try {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(IssueService.COMPONENT_NAME, Layer.DOMAIN, seed.getKey()).defaultMessage(seed.getDefaultFormat());
                thesaurus.addTranslations(Collections.singletonList(SimpleTranslation.translation(nlsKey, Locale.ENGLISH, seed.getDefaultFormat())));
            } catch (Exception ex) {
                LOG.warning("Unable to setup translation for: key = " + seed.getKey() + ", value = " + seed.getDefaultFormat());
            }
        }
    }

    private <T extends Entity> Optional<T> find(Class<T> clazz, Object... key) {
        return queryService.wrap(dataModel.query(clazz)).get(key);
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
    public int countOpenDataCollectionIssues(String mRID) {
        Optional<IssueType> issueType = findIssueType("datacollection");
        if (issueType.isPresent()) {
            Condition condition = Where.where("reason.issueType").isEqualTo(issueType.get())
                    .and(where("device.mRID").isEqualTo(mRID));
            List<OpenIssue> issues = dataModel.query(OpenIssue.class, IssueReason.class, EndDevice.class)
                    .select(condition);
            return issues.size();
        } else {
            return 0;
        }
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
}