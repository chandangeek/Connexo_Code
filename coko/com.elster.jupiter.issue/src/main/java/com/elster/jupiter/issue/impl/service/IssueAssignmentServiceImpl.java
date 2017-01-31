/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.module.DroolsValidationException;
import com.elster.jupiter.issue.impl.records.AssignmentRuleImpl;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.IssueForAssign;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
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
import org.kie.internal.utils.CompositeClassLoader;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

@SuppressWarnings("deprecation")
public class IssueAssignmentServiceImpl implements IssueAssignmentService {
    public static final Logger LOG = Logger.getLogger(IssueAssignmentServiceImpl.class.getName());

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile QueryService queryService;

    private volatile KnowledgeBase knowledgeBase;
    private volatile KnowledgeBuilderFactoryService knowledgeBuilderFactoryService;
    private volatile KnowledgeBaseFactoryService knowledgeBaseFactoryService;
    private volatile KieResources resourceFactoryService;

    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;

    @Inject
    public IssueAssignmentServiceImpl(
            DataModel dataModel,
            QueryService queryService,
            KnowledgeBuilderFactoryService knowledgeBuilderFactoryService,
            KnowledgeBaseFactoryService knowledgeBaseFactoryService,
            KieResources resourceFactoryService,
            ThreadPrincipalService threadPrincipalService,
            TransactionService transactionService,
            Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.queryService = queryService;
        this.knowledgeBuilderFactoryService = knowledgeBuilderFactoryService;
        this.knowledgeBaseFactoryService = knowledgeBaseFactoryService;
        this.resourceFactoryService = resourceFactoryService;
        this.threadPrincipalService = threadPrincipalService;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
    }

    private boolean createKnowledgeBase() {
        try {
            ClassLoader classLoader = getRulesClassloader();
            KnowledgeBuilderConfiguration kbConf = knowledgeBuilderFactoryService.newKnowledgeBuilderConfiguration(null, classLoader);

            KnowledgeBuilder kbuilder = knowledgeBuilderFactoryService.newKnowledgeBuilder(kbConf);
            List<AssignmentRule> allRules = getAssignRules();
            for (AssignmentRule rule : allRules) {
                kbuilder.add(resourceFactoryService.newByteArrayResource(rule.getRuleData()), ResourceType.DRL);
            }

            if (kbuilder.hasErrors()) {
                throw new DroolsValidationException(thesaurus, kbuilder.getErrors().toString());
            }

            KieBaseConfiguration kbaseConf = knowledgeBaseFactoryService.newKnowledgeBaseConfiguration(null, classLoader);

            knowledgeBase = knowledgeBaseFactoryService.newKnowledgeBase(kbaseConf);
            knowledgeBase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        } catch (IllegalStateException | DroolsValidationException ex) {
            LOG.warning(ex.getMessage());
            return false;
        }
        return true;
    }
    
    private ClassLoader getRulesClassloader() {
        CompositeClassLoader classLoader = new CompositeClassLoader();
        classLoader.addClassLoader(getClass().getClassLoader());
        classLoader.addClassLoader(ProjectClassLoader.class.getClassLoader());
        return classLoader;
    }

    private List<AssignmentRule> getAssignRules() {
        List<AssignmentRule> ruleList = null;
        try {
            ruleList = dataModel.query(AssignmentRule.class).select(where("enabled").isEqualTo(Boolean.TRUE));
        } catch (UnderlyingSQLFailedException sqlEx) {
            throw new IllegalStateException("Rule store is not available yet");
        }
        return ruleList;
    }

    @Override
    public Optional<AssignmentRule> findAssignmentRule(long id) {
        return queryService.wrap(dataModel.query(AssignmentRule.class)).get(id);
    }

    @Override
    public Query<AssignmentRule> getAssignmentRuleQuery(Class<?>... eagers) {
        return queryService.wrap(dataModel.query(AssignmentRule.class, eagers));
    }

    @Override
    public void assignIssue(List<IssueForAssign> issueList) {
        if (issueList != null) {
            processRules(issueList);
        } else {
            throw new IllegalArgumentException("List of issue can't be null");
        }
    }
    
    @Override
    public AssignmentRule createAssignmentRule(String title, String ruleData) {
        AssignmentRuleImpl assignmentRule = dataModel.getInstance(AssignmentRuleImpl.class);
        assignmentRule.setTitle(title);
        assignmentRule.setRuleData(ruleData);
        assignmentRule.save();
        return assignmentRule;
    }

    private void processRules(List<IssueForAssign> issueList) {
        if (canEvaluateRules()) {
            
            StatefulKnowledgeSession ksession = knowledgeBase.newStatefulKnowledgeSession();
            /*
             * Uncomment lines below to have a lot of drools logs
             */
            //ksession.addEventListener(new DebugAgendaEventListener());
            //ksession.addEventListener(new DebugRuleRuntimeEventListener());

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

    public void rebuildAssignmentRules() {
        createKnowledgeBase();
    }

    public void loadAssignmentRuleFromFile(String absolutePath) {
        if (absolutePath != null) {
            try {
                byte[] source = Files.readAllBytes(Paths.get(absolutePath));
                AssignmentRule rule = dataModel.getInstance(AssignmentRuleImpl.class);
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
                    rule.update();
                    if (!createKnowledgeBase()) {
                        rule.delete();
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
