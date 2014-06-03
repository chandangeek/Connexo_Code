package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.module.DroolsValidationException;
import com.elster.jupiter.issue.impl.records.CreationRuleImpl;
import com.elster.jupiter.issue.impl.records.IssueImpl;
import com.elster.jupiter.issue.impl.tasks.IssueActionExecutor;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueMappingService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import org.drools.compiler.compiler.RuleBaseLoader;
import org.drools.core.common.ProjectClassLoader;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.event.rule.DebugAgendaEventListener;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.io.KieResources;
import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactoryService;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderConfiguration;
import org.kie.internal.builder.KnowledgeBuilderFactoryService;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.issue.creation", service = {IssueCreationService.class}, immediate = true)
public class IssueCreationServiceImpl implements IssueCreationService{
    public static final Logger LOG = Logger.getLogger(IssueCreationServiceImpl.class.getName());
    public static final String ISSUE_CREATION_SERVICE = "issueCreationService";

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile QueryService queryService;
    private volatile IssueActionService issueActionService;

    private volatile KnowledgeBase knowledgeBase;
    private volatile KnowledgeBuilderFactoryService knowledgeBuilderFactoryService;
    private volatile KnowledgeBaseFactoryService knowledgeBaseFactoryService;
    private volatile KieResources resourceFactoryService;

    private final Map<String, CreationRuleTemplate> registeredTemplates = new ConcurrentHashMap<>();

    public IssueCreationServiceImpl(){}

    @Inject
    public IssueCreationServiceImpl(
            NlsService nlsService,
            QueryService queryService,
            IssueMappingService issueMappingService,
            IssueActionService issueActionService,
            KnowledgeBuilderFactoryService knowledgeBuilderFactoryService,
            KnowledgeBaseFactoryService knowledgeBaseFactoryService,
            KieResources resourceFactoryService) {
        setNlsService(nlsService);
        setQueryService(queryService);
        setIssueMappingService(issueMappingService);
        setKnowledgeBaseFactoryService(knowledgeBaseFactoryService);
        setKnowledgeBuilderFactoryService(knowledgeBuilderFactoryService);
        setResourceFactoryService(resourceFactoryService);
        setIssueActionService(issueActionService);
    }

    @Reference
    public final void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public final void setIssueMappingService(IssueMappingService issueMappingService) {
        dataModel = IssueMappingServiceImpl.class.cast(issueMappingService).getDataModel();
    }

    @Reference
    public final void setIssueActionService(IssueActionService issueActionService) {
        this.issueActionService = issueActionService;
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
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(IssueService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public CreationRule createRule() {
        return dataModel.getInstance(CreationRuleImpl.class);
    }

    @Override
    public Optional<CreationRule> findCreationRule(long id) {
        return find(CreationRule.class, id);
    }

    private <T extends Entity> Optional<T> find(Class<T> clazz, Object... key) {
        return queryService.wrap(dataModel.query(clazz)).get(key);
    }

    @Override
    public Query<CreationRule> getCreationRuleQuery(Class<?>... eagers) {
        Query<CreationRule> query = query(CreationRule.class, eagers);
        query.setRestriction(where("obsoleteTime").isNull());
        return query;
    }

    @Override
    public Optional<CreationRuleAction> findCreationRuleAction(long id) {
        return find(CreationRuleAction.class, id);
    }

    @Override
    public Query<CreationRuleAction> getCreationRuleActionQuery() {
        return query(CreationRuleAction.class);
    }

    @Override
    public Optional<CreationRuleTemplate> findCreationRuleTemplate(String uuid) {
        CreationRuleTemplate template = registeredTemplates.get(uuid);
        if (template == null) {
            return Optional.absent();
        }
        return Optional.of(template);
    }

    @Override
    public List<CreationRuleTemplate> getCreationRuleTemplates() {
        List<CreationRuleTemplate> templates = new ArrayList<>();
        templates.addAll(registeredTemplates.values());
        return templates;
    }

    @Override
    public void dispatchCreationEvent(IssueEvent event) {
        if (canEvaluateRules()) {
            StatefulKnowledgeSession ksession = knowledgeBase.newStatefulKnowledgeSession();
            ksession.addEventListener(new DebugAgendaEventListener());
            ksession.addEventListener(new DebugRuleRuntimeEventListener());
            try {
                ksession.setGlobal(ISSUE_CREATION_SERVICE, this);
            } catch (RuntimeException ex){
                LOG.warning("Unable to set the issue creation service as a global for all rules. This means that no " +
                        "issues will be created! Check that at least one rule contais string 'global com.elster.jupiter." +
                        "issue.share.service.IssueCreationService issueCreationService;' and this rule calls " +
                        "'issueCreationService.processCreationEvent(@{ruleId}, event);' or something like that.");
            }
            ksession.insert(event);
            ksession.fireAllRules();
            ksession.dispose();
        }
    }

    @Override
    public void processCreationEvent(long ruleId, IssueEvent event) {
        CreationRule firedRule = findCreationRule(ruleId).orNull();
        if (firedRule != null && validateEvent(event, firedRule)) {
            createIssue(event, firedRule);
        }
    }

    private boolean validateEvent(IssueEvent event, CreationRule firedRule) {
        if (event == null || firedRule == null) {
            return false;
        }
        Query<Issue> query = queryService.wrap(dataModel.query(Issue.class, CreationRule.class));
        return query.select(where("rule").isEqualTo(firedRule).and(where("device").isEqualTo(event.getDevice()))).isEmpty();
    }

    private void createIssue(IssueEvent event, CreationRule firedRule) {
        Issue issue = dataModel.getInstance(IssueImpl.class);
        issue.setReason(firedRule.getReason());
        issue.setStatus(event.getStatus());
        issue.setDueDate(new UtcInstant(firedRule.getDueInType().dueValueFor(firedRule.getDueInValue())));
        issue.setOverdue(false);
        issue.setRule(firedRule);
        issue.setDevice(event.getDevice());
        issue.save();
        issue.autoAssign();
        executeCreationActions(issue);
    }

    @Override
    public boolean reReadRules() {
        return createKnowledgeBase();
    }

    private void executeCreationActions(Issue issue) {
        new IssueActionExecutor(issue, CreationRuleActionPhase.CREATE, thesaurus, issueActionService).run();
    }

    private  <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers) {
        QueryExecutor<T> queryExecutor = dataModel.query(clazz, eagers);
        Query<T> query = queryService.wrap(queryExecutor);
        query.setEager();
        return query;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRuleTemplate(CreationRuleTemplate ruleTemplate, Map<String, Object> map) {
        registeredTemplates.put(String.valueOf(map.get("uuid")), ruleTemplate);
        createKnowledgeBase(); // TODO at this step datamodel can be not registered

    }

    public void removeRuleTemplate(CreationRuleTemplate template) {
        registeredTemplates.values().remove(template);
        createKnowledgeBase();
    }

    private boolean createKnowledgeBase() {
        try {
            ClassLoader[] rulesClassloader = getRulesClassloader();
            KnowledgeBuilderConfiguration kbConf = knowledgeBuilderFactoryService.newKnowledgeBuilderConfiguration(null, rulesClassloader);
            KnowledgeBuilder kbuilder = knowledgeBuilderFactoryService.newKnowledgeBuilder(kbConf);

            List<CreationRule> allRules = getCreationRules();
            for (CreationRule rule : allRules) {
                if (registeredTemplates.get(rule.getTemplateUuid()) != null) {
                    kbuilder.add(resourceFactoryService.newByteArrayResource(rule.getData()), ResourceType.DRL);
                }
            }
            if (kbuilder.hasErrors()) {
                throw new DroolsValidationException(thesaurus, kbuilder.getErrors().toString());
            }

            KieBaseConfiguration kbaseConf = knowledgeBaseFactoryService.newKnowledgeBaseConfiguration(null, rulesClassloader);

            knowledgeBase = knowledgeBaseFactoryService.newKnowledgeBase(kbaseConf);
            knowledgeBase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        } catch (IllegalStateException | DroolsValidationException ex) {
            LOG.warning(ex.getMessage());
            return false;
        }
        return true;
    }

    private ClassLoader[] getRulesClassloader() {
        Set<ClassLoader> rulesClassloader = new HashSet<>();
        for (CreationRuleTemplate template : registeredTemplates.values()) {
            rulesClassloader.add(template.getClass().getClassLoader());
        }
        rulesClassloader.add(getClass().getClassLoader());
        rulesClassloader.add(ProjectClassLoader.class.getClassLoader());
        rulesClassloader.add(RuleBaseLoader.class.getClassLoader());
        return rulesClassloader.toArray(new ClassLoader[rulesClassloader.size()]);
    }

    private List<CreationRule> getCreationRules() {
        List<CreationRule> ruleList = null;
        try {
            ruleList = getCreationRuleQuery().select(Condition.TRUE);
        } catch (UnderlyingSQLFailedException sqlEx) {
            throw new IllegalStateException("Rule store is not available yet");
        }
        return ruleList;
    }

    private boolean canEvaluateRules() {
        boolean canEvaluateRules = true;
        if (knowledgeBase == null) {
            createKnowledgeBase();
            canEvaluateRules = knowledgeBase != null;
        }
        return canEvaluateRules;
    }
}
