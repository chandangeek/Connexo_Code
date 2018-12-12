/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.impl.module.DroolsValidationException;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.records.CreationRuleBuilderImpl;
import com.elster.jupiter.issue.impl.records.CreationRuleImpl;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.impl.tasks.IssueActionExecutor;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueCreationValidator;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.users.FoundUserIsNotActiveException;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

@SuppressWarnings("deprecation")
public class IssueCreationServiceImpl implements IssueCreationService {
    public static final Logger LOG = Logger.getLogger(IssueCreationServiceImpl.class.getName());

    public static final String ISSUE_CREATION_SERVICE = "issueCreationService";
    public static final String EVENT_SERVICE = "eventService";
    public static final String LOGGER = "LOGGER";
    private static final java.lang.String SEPARATOR = ":";

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile QueryService queryService;
    private volatile IssueService issueService;
    private volatile EventService eventService;
    private volatile Optional<User> batchUser;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile UserService userService;

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
            UserService userService,
            KnowledgeBuilderFactoryService knowledgeBuilderFactoryService,
            KnowledgeBaseFactoryService knowledgeBaseFactoryService,
            KieResources resourceFactoryService,
            EndPointConfigurationService endPointConfigurationService,
            Thesaurus thesaurus,
            EventService eventService) {
        this.dataModel = dataModel;
        this.issueService = issueService;
        this.queryService = queryService;
        this.knowledgeBaseFactoryService = knowledgeBaseFactoryService;
        this.knowledgeBuilderFactoryService = knowledgeBuilderFactoryService;
        this.resourceFactoryService = resourceFactoryService;
        this.thesaurus = thesaurus;
        this.endPointConfigurationService = endPointConfigurationService;
        this.eventService = eventService;
        this.userService = userService;
    }

    @Override
    public CreationRuleBuilder newCreationRule() {
        return new CreationRuleBuilderImpl(dataModel, dataModel.getInstance(CreationRuleImpl.class));
    }

    @Override
    public Optional<CreationRule> findCreationRuleById(long id) {
        return dataModel.mapper(CreationRule.class).getOptional(id);
    }

    @Override
    public Optional<CreationRule> findAndLockCreationRuleByIdAndVersion(long id, long version) {
        return dataModel.mapper(CreationRule.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Query<CreationRule> getCreationRuleQuery(Class<?>... eagers) {
        Query<CreationRule> query = query(CreationRule.class, eagers);
        query.setRestriction(where("obsoleteTime").isNull());
        return query;
    }

    @Override
    public Optional<CreationRuleTemplate> findCreationRuleTemplate(String name) {
        if (name != null) {
            CreationRuleTemplate template = issueService.getCreationRuleTemplates().get(name);
            return Optional.ofNullable(template);
        }
        return Optional.empty();
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
                ksession.setGlobal(EVENT_SERVICE, eventService);
                ksession.setGlobal(LOGGER, LOG);
            } catch (RuntimeException ex) {
                LOG.warning("Unable to set the issue creation service as a global for all rules. This means that no " +
                        "issues will be created! Check that at least one rule contains string 'global com.elster.jupiter." +
                        "issue.share.service.IssueCreationService issueCreationService;' and this rule calls " +
                        "'issueCreationService.createIssue(@{ruleId}, event);' or something like that.");
            }
            events.forEach(ksession::insert);
            ksession.fireAllRules();
            ksession.dispose();
        }
    }

    @Override
    public void processIssueCreationEvent(long ruleId, IssueEvent event) {
        // Sometimes we need to restrict issue creation due to global reasons (common for all type of issues)
        if (event.getEndDevice().isPresent() && restrictIssueCreation(event)) {
            LOG.info("Issue creation for "
                    + event.getEndDevice().map(EndDevice::getName).orElse(event.getUsagePoint().isPresent() ? event.getUsagePoint().get().getName() : "UNKNOWN")
                    + " was restricted");
            return;
        }

        findCreationRuleById(ruleId).ifPresent(firedRule -> {
            CreationRuleTemplate template = firedRule.getTemplate();
            Optional<? extends OpenIssue> existingIssue = event.findExistingIssue();
            if (existingIssue.isPresent()) {
                template.updateIssue(existingIssue.get(), event);
            } else {
                createNewIssue(firedRule, event, template);
            }
        });
    }


    @Override
    public void processAlarmCreationEvent(int ruleId, IssueEvent event, boolean logOnSameAlarm) {
        findCreationRuleById(ruleId).ifPresent(firedRule -> {
                    CreationRuleTemplate template = firedRule.getTemplate();
                    if (logOnSameAlarm) {
                        Optional<? extends OpenIssue> existingIssue = event.findExistingIssue();
                        if (existingIssue.isPresent()) {
                            template.updateIssue(existingIssue.get(), event);
                        } else {
                            createNewIssue(firedRule, event, template);
                        }
                    } else {
                        createNewIssue(firedRule, event, template);
                    }
                }
        );
    }

    private void createNewIssue(CreationRule firedRule, IssueEvent event, CreationRuleTemplate template) {
        try {
            batchUser = userService.findUser("batch executor");
        } catch (FoundUserIsNotActiveException e) {
            batchUser = Optional.empty();
        }
        OpenIssueImpl baseIssue = dataModel.getInstance(OpenIssueImpl.class);
        baseIssue.setReason(firedRule.getReason());
        baseIssue.setStatus(issueService.findStatus(IssueStatus.OPEN).orElse(null));
        long dueInValue = firedRule.getDueInValue();
        Instant dueDate = dueInValue > 0 ? Instant.ofEpochMilli(firedRule.getDueInType().dueValueFor(dueInValue)) : null;
        baseIssue.setDueDate(dueDate);
        baseIssue.setOverdue(false);
        baseIssue.setRule(firedRule);
        baseIssue.setPriority(firedRule.getPriority());
        event.getEndDevice().ifPresent(baseIssue::setDevice);
        event.getUsagePoint().ifPresent(baseIssue::setUsagePoint);
        baseIssue.save();
        baseIssue.addComment(firedRule.getComment(), batchUser.orElse(null));
        OpenIssue newIssue = template.createIssue(baseIssue, event);
        newIssue.autoAssign();
        executeCreationActions(newIssue);
    }

    private boolean restrictIssueCreation(IssueEvent event) {
        for (IssueCreationValidator creationValidator : issueService.getIssueCreationValidators()) {
            if (!creationValidator.isValidCreationEvent(event)) {
                return true;
            }
        }
        return false;
    }


    private boolean logOnSameAlarm(String raiseEventProps) {
        List<String> values = Arrays.asList(raiseEventProps.split(SEPARATOR));
        if (values.size() != 3) {
            throw new LocalizedFieldValidationException(MessageSeeds.ISSUE_CREATION_RULE_PARAMETER_ABSENT, "Log on same alarm indicator");
        }
        return Integer.parseInt(values.get(0)) == 1;
    }

    @Override
    public void processIssueResolutionEvent(long ruleId, IssueEvent event) {
        findCreationRuleById(ruleId).get().getTemplate().resolveIssue(event);
    }

    @Override
    public boolean reReadRules() {
        return createKnowledgeBase();
    }

    private void executeCreationActions(Issue issue) {
        new IssueActionExecutor(issue, CreationRuleActionPhase.CREATE, thesaurus, issueService.getIssueActionService()).run();
    }

    private <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers) {
        QueryExecutor<T> queryExecutor = dataModel.query(clazz, eagers);
        Query<T> query = queryService.wrap(queryExecutor);
        query.setEager();
        return query;
    }

    boolean createKnowledgeBase() {
        try {
            ClassLoader rulesClassloader = getRulesClassloader();
            KnowledgeBuilderConfiguration kbConf = knowledgeBuilderFactoryService.newKnowledgeBuilderConfiguration(null, rulesClassloader);
            KnowledgeBuilder kbuilder = knowledgeBuilderFactoryService.newKnowledgeBuilder(kbConf);

            for (CreationRule rule : getCreationRules()) {
                if (issueService.getCreationRuleTemplates().get(rule.getTemplateImpl()) != null) {
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

    private ClassLoader getRulesClassloader() {
        CompositeClassLoader classLoader = new CompositeClassLoader();
        for (CreationRuleTemplate template : issueService.getCreationRuleTemplates().values()) {
            classLoader.addClassLoader(template.getClass().getClassLoader());
        }
        classLoader.addClassLoader(getClass().getClassLoader());
        classLoader.addClassLoader(ProjectClassLoader.class.getClassLoader());
        return classLoader;
    }

    private List<CreationRule> getCreationRules() {
        List<CreationRule> ruleList;
        try {
            ruleList = getCreationRuleQuery().select(where("active").isEqualTo(true).and(where("obsoleteTime").isNull()));
        } catch (UnderlyingSQLFailedException sqlEx) {
            throw new IllegalStateException("Rule store is not available yet");
        }
        return ruleList;
    }

    private boolean canEvaluateRules() {
        createKnowledgeBase();
        return knowledgeBase != null;
    }
}
