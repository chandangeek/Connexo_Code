package com.elster.jupiter.issue.impl.drools;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.Rule;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueMainService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

//@Component(name = "com.elster.jupiter.issue.drools", service = IssueAssignmentService.class)
@Component(name = "com.elster.jupiter.issue.drools", service = {IssueAssignmentService.class, IssueAssignmentServiceImpl.class}, property = {"osgi.command.scope=issue", "osgi.command.function=rebuild", "osgi.command.function=fromFile"}, immediate = true)
public class IssueAssignmentServiceImpl implements IssueAssignmentService {
    public static final Logger LOG = Logger.getLogger(IssueAssignmentServiceImpl.class.getName());

    private volatile KnowledgeBase knowledgeBase;

    private volatile KnowledgeBuilderFactoryService knowledgeBuilderFactoryService;
    private volatile KnowledgeBaseFactoryService knowledgeBaseFactoryService;
    private volatile KieResources resourceFactoryService;
    private volatile IssueMainService issueMainService;

    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;

    public IssueAssignmentServiceImpl() {
    }

    @Inject
    public IssueAssignmentServiceImpl(KnowledgeBuilderFactoryService knowledgeBuilderFactoryService, KnowledgeBaseFactoryService knowledgeBaseFactoryService, KieResources resourceFactoryService) {
        setKnowledgeBaseFactoryService(knowledgeBaseFactoryService);
        setKnowledgeBuilderFactoryService(knowledgeBuilderFactoryService);
        setResourceFactoryService(resourceFactoryService);
    }

    @Activate
    public void activate() {
        createKnowledgeBase();
    }

    @Reference
    public void setKnowledgeBuilderFactoryService(KnowledgeBuilderFactoryService knowledgeBuilderFactoryService) {
        this.knowledgeBuilderFactoryService = knowledgeBuilderFactoryService;
    }

    @Reference
    public void setKnowledgeBaseFactoryService(KnowledgeBaseFactoryService knowledgeBaseFactoryService) {
        this.knowledgeBaseFactoryService = knowledgeBaseFactoryService;
    }

    @Reference
    public void setResourceFactoryService(KieResources resourceFactoryService) {
        this.resourceFactoryService = resourceFactoryService;
    }

    @Reference
    public void setIssueMainService(IssueMainService issueMainService) {
        this.issueMainService = issueMainService;
    }
    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    private boolean createKnowledgeBase() {
        try {
            KnowledgeBuilderConfiguration kbConf = knowledgeBuilderFactoryService.newKnowledgeBuilderConfiguration(null, getClass().getClassLoader());

            KnowledgeBuilder kbuilder = knowledgeBuilderFactoryService.newKnowledgeBuilder(kbConf);
            List<Rule> allRules = getAssignRules();
            for (Rule rule : allRules) {
                kbuilder.add(resourceFactoryService.newByteArrayResource(rule.getRuleData()), ResourceType.DRL);
            }

            if (kbuilder.hasErrors()) {
                throw new DroolsValidationException(MessageSeeds.ISSUE_DROOLS_VALIDATION, kbuilder.getErrors().toString());
            }

            KieBaseConfiguration kbaseConf = knowledgeBaseFactoryService.newKnowledgeBaseConfiguration(null, getClass().getClassLoader(), ProjectClassLoader.class.getClassLoader());

            knowledgeBase = knowledgeBaseFactoryService.newKnowledgeBase(kbaseConf);
            knowledgeBase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        } catch (IllegalStateException | DroolsValidationException ex) {
            LOG.warning(ex.getMessage());
            return false;
        }
        return true;
    }

    private List<Rule> getAssignRules() {
        List<Rule> ruleList = null;
        try {
            Query<Rule> query = issueMainService.query(Rule.class);
            Condition condition = where("enabled").isEqualTo(Boolean.TRUE);
            // Setting the '0' value for 'from' argument causes fetching all rules from database
            ruleList = query.select(condition, 0, 100, Order.ascending("priority"));
        } catch (UnderlyingSQLFailedException sqlEx) {
            throw new IllegalStateException("Rule store is not available yet");
        }
        return ruleList;
    }

    @Override
    public void assignIssue(List<IssueForAssign> issueList) {
        if (issueList != null) {
            processRules(issueList);
        } else {
            throw new IllegalArgumentException("List of issue can't be null");
        }
    }

    private void processRules(List<IssueForAssign> issueList) {
        if (canEvaluateRules()) {
            StatefulKnowledgeSession ksession = knowledgeBase.newStatefulKnowledgeSession();
            ksession.addEventListener(new DebugAgendaEventListener());
            ksession.addEventListener(new DebugRuleRuntimeEventListener());

            for (IssueForAssign issue : issueList) {
                ksession.insert(issue);
            }

            ksession.fireAllRules();
            ksession.dispose();
        }
    }

    private boolean canEvaluateRules() {
        boolean canEvaluateRules = true;
        if (knowledgeBase == null) {
            createKnowledgeBase();
            canEvaluateRules = knowledgeBase != null;
        }
        return canEvaluateRules;
    }

    public void rebuild() {
        createKnowledgeBase();
    }

    public void fromFile(String absolutePath) {
        if (absolutePath != null) {
            try {
                byte[] source = Files.readAllBytes(Paths.get(absolutePath));
                Rule rule = new Rule();
                rule.setTitle(absolutePath);
                rule.setDescription("Some description");
                rule.setRuleData(Charset.defaultCharset().decode(ByteBuffer.wrap(source)).toString());
                threadPrincipalService.set(new Principal() {
                    @Override
                    public String getName() {
                        return "console";
                    }
                });
                try (TransactionContext context = transactionService.getContext()){
                    issueMainService.save(rule);
                    if (!createKnowledgeBase()) {
                        issueMainService.delete(rule);
                    }
                    context.commit();
                }
            } catch (IOException e) {
                LOG.warning("Unable to read the file! Path = " + absolutePath);
            }
        } else {
            LOG.warning("Please specify the not null absolute path");
        }
    }

}
