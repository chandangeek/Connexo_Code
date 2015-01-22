package com.elster.jupiter.issue.impl.service;

import static com.elster.jupiter.util.conditions.Where.where;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.drools.compiler.compiler.RuleBaseLoader;
import org.drools.core.common.ProjectClassLoader;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.io.KieResources;
import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactoryService;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderConfiguration;
import org.kie.internal.builder.KnowledgeBuilderFactoryService;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.module.DroolsValidationException;
import com.elster.jupiter.issue.impl.records.CreationRuleImpl;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.impl.tasks.IssueActionExecutor;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.conditions.Condition;

@SuppressWarnings("deprecation")
public class IssueCreationServiceImpl implements IssueCreationService {
    public static final Logger LOG = Logger.getLogger(IssueCreationServiceImpl.class.getName());
    public static final String ISSUE_CREATION_SERVICE = "issueCreationService";

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile QueryService queryService;
    private volatile IssueService issueService;

    private volatile KnowledgeBase knowledgeBase;
    private volatile KnowledgeBuilderFactoryService knowledgeBuilderFactoryService;
    private volatile KnowledgeBaseFactoryService knowledgeBaseFactoryService;
    private volatile KieResources resourceFactoryService;

    //for test purpose only
    public IssueCreationServiceImpl() {
    }
    
    @Inject
    public IssueCreationServiceImpl(
            DataModel dataModel, 
            IssueService issueService,
            QueryService queryService,
            KnowledgeBuilderFactoryService knowledgeBuilderFactoryService,
            KnowledgeBaseFactoryService knowledgeBaseFactoryService,
            KieResources resourceFactoryService,
            Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.queryService = queryService;
        this.knowledgeBaseFactoryService = knowledgeBaseFactoryService;
        this.knowledgeBuilderFactoryService = knowledgeBuilderFactoryService;
        this.resourceFactoryService = resourceFactoryService;
        this.thesaurus = thesaurus;
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
        CreationRuleTemplate template = issueService.getCreationRuleTemplates().get(uuid);
        if (template == null) {
            return Optional.empty();
        }
        return Optional.of(template);
    }

    @Override
    public List<CreationRuleTemplate> getCreationRuleTemplates() {
        List<CreationRuleTemplate> templates = new ArrayList<>();
        templates.addAll(issueService.getCreationRuleTemplates().values());
        return templates;
    }

    @Override
    public void dispatchCreationEvent(List<IssueEvent> events) {
        if (canEvaluateRules()) {
            StatefulKnowledgeSession ksession = knowledgeBase.newStatefulKnowledgeSession();
            /*
             * Uncomment lines below to have a lot of drools logs
             */
            //ksession.addEventListener(new DebugAgendaEventListener());
            //ksession.addEventListener(new DebugRuleRuntimeEventListener());
            
            try {
                ksession.setGlobal(ISSUE_CREATION_SERVICE, this);
            } catch (RuntimeException ex){
                LOG.warning("Unable to set the issue creation service as a global for all rules. This means that no " +
                        "issues will be created! Check that at least one rule contais string 'global com.elster.jupiter." +
                        "issue.share.service.IssueCreationService issueCreationService;' and this rule calls " +
                        "'issueCreationService.createIssue(@{ruleId}, event);' or something like that.");
            }
            events.stream().forEach(event -> ksession.insert(event));
            ksession.fireAllRules();
            ksession.dispose();
        }
    }

    @Override
    public void processIssueEvent(long ruleId, IssueEvent event) {
        CreationRule firedRule = findCreationRule(ruleId).orElse(null);
        CreationRuleTemplate template = firedRule.getTemplate();
        Issue baseIssue = dataModel.getInstance(OpenIssueImpl.class);
        baseIssue.setReason(firedRule.getReason());
        baseIssue.setStatus(event.getStatus());
        baseIssue.setDueDate(Instant.ofEpochMilli(firedRule.getDueInType().dueValueFor(firedRule.getDueInValue())));
        baseIssue.setOverdue(false);
        baseIssue.setRule(firedRule);
        baseIssue.setDevice(event.getEndDevice());
        Optional<? extends Issue> newIssue = template.createIssue(baseIssue, event);
        if (newIssue.isPresent()){
            newIssue.get().autoAssign();
            executeCreationActions(newIssue.get());
        }
    }

    @Override
    public void processIssueResolveEvent(long ruleId, IssueEvent event) {
        CreationRule firedRule = findCreationRule(ruleId).orElse(null);
        CreationRuleTemplate template = firedRule.getTemplate();
        template.resolveIssue(firedRule, event);
    }

    @Override
    public boolean reReadRules() {
        return createKnowledgeBase();
    }

    private void executeCreationActions(Issue issue) {
        new IssueActionExecutor(issue, CreationRuleActionPhase.CREATE, thesaurus, issueService.getIssueActionService()).run();
    }

    private  <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers) {
        QueryExecutor<T> queryExecutor = dataModel.query(clazz, eagers);
        Query<T> query = queryService.wrap(queryExecutor);
        query.setEager();
        return query;
    }

    boolean createKnowledgeBase() {
        try {
            ClassLoader[] rulesClassloader = getRulesClassloader();
            KnowledgeBuilderConfiguration kbConf = knowledgeBuilderFactoryService.newKnowledgeBuilderConfiguration(null, rulesClassloader);
            KnowledgeBuilder kbuilder = knowledgeBuilderFactoryService.newKnowledgeBuilder(kbConf);

            List<CreationRule> allRules = getCreationRules();
            for (CreationRule rule : allRules) {
                if (issueService.getCreationRuleTemplates().get(rule.getTemplateUuid()) != null) {
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
        for (CreationRuleTemplate template : issueService.getCreationRuleTemplates().values()) {
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
