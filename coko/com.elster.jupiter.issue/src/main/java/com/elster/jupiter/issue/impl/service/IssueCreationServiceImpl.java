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
import com.elster.jupiter.issue.share.*;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.CreationRuleExclGroup;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.IssueTypes;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.users.FoundUserIsNotActiveException;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

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
import javax.naming.OperationNotSupportedException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    private volatile Clock clock;

    private volatile KnowledgeBase knowledgeBase;
    private volatile KnowledgeBuilderFactoryService knowledgeBuilderFactoryService;
    private volatile KnowledgeBaseFactoryService knowledgeBaseFactoryService;
    private volatile KieResources resourceFactoryService;
    private volatile AtomicReference<RuleRefreshState> ruleRefreshState = new AtomicReference<>(RuleRefreshState.DONE);

    private enum RuleRefreshState {
        REQUIRED,
        IN_PROGRESS,
        READY,
        DONE,
    }

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
            EventService eventService,
            Clock clock) {
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
        this.clock = clock;
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
        // Resetting the ID and Name values (Fix for CXO-12489)
        TemplateUtil templateUtil = new TemplateUtil(null, null);
        return query;
    }

    @Override
    public List<CreationRuleAction> findActionsByMultiValueProperty(List<IssueTypes> issueTypes, String propertyKey,
            List<String> groupIdsList) {
        final List<CreationRuleAction> actionsList;
        if (issueTypes != null && !issueTypes.isEmpty()) {
            final Condition condition = Where.where("type.issueType.key")
                    .in(issueTypes.stream().map(type -> type.getName()).collect(Collectors.toList()));
            actionsList = dataModel.query(CreationRuleAction.class, IssueActionType.class, IssueType.class)
                    .select(condition);
        } else {
            actionsList = dataModel.stream(CreationRuleAction.class).select();
        }
        final List<CreationRuleAction> filteredActions = actionsList.stream().filter(action -> {
            if (action.getProperties().containsKey(propertyKey)) {
                if (groupIdsList != null) {
                    List<HasId> value = (List<HasId>) action.getProperties().get(propertyKey);
                    if (value != null && !value.isEmpty()) {
                        List<String> valuesList = value.stream()
                                .filter(Objects::nonNull)
                                .map(object -> String.valueOf(object.getId()))
                                .collect(Collectors.toList());
                        valuesList.retainAll(groupIdsList);
                        if (!valuesList.isEmpty()) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }).collect(Collectors.toList());
        return filteredActions;
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
        return new ArrayList<>(issueService.getCreationRuleTemplates().values());
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
                LOG.log(Level.WARNING, ex.getMessage(), ex);
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
        LOG.fine("Processing issue creation event: rule id:" + ruleId + " issue event class:" + event.getClass());
        if (event.getEndDevice().isPresent() && restrictIssueCreation(event)) {
            LOG.info("Issue creation for "
                    + event.getEndDevice().map(EndDevice::getName).orElse(event.getUsagePoint().isPresent() ? event.getUsagePoint().get().getName() : "UNKNOWN")
                    + " was restricted");
            return;
        }
        findCreationRuleById(ruleId).ifPresent(firedRule -> {
            if (event.getEndDevice().isPresent() && isEndDeviceExcludedForRule(event.getEndDevice().get(), firedRule)) {
                return;
            }
            if (isIssueCreationRestrictedByComTask(firedRule, event)) {
                final String deviceName = event.getEndDevice().map(dev -> dev.getName()).orElse("");
                LOG.info("Issue creation rule \'" + firedRule.getName()
                        + "\' is restricted by the comunication task(s) of the event, for device \'" + deviceName
                        + "\'");
                return;
            }
            CreationRuleTemplate template = firedRule.getTemplate();
            Optional<? extends OpenIssue> existingIssue = event.findExistingIssue();
            if (existingIssue.isPresent()) {
                OpenIssue openIssue = existingIssue.get();
                LOG.fine("Updating issue:" + openIssue.getIssueId());
                template.updateIssue(openIssue, event);
            } else {
                LOG.fine("Creating new issue with template:" + template.getClass());
                createNewIssue(firedRule, event, template);
            }
        });
    }


    @Override
    public void processAlarmCreationEvent(int ruleId, IssueEvent event, boolean logOnSameAlarm) {
        LOG.fine("Process alarm creation event:" + event + " on ruleId:" + ruleId);
        findCreationRuleById(ruleId).ifPresent(firedRule -> {
                    if (event.getEndDevice().isPresent() && isEndDeviceExcludedForRule(event.getEndDevice().get(), firedRule)) {
                        return;
                    }
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

    private boolean isEndDeviceExcludedForRule(EndDevice endDevice, CreationRule creationRule) {
        if (creationRule.getExcludedGroupMappings() != null) {
            final Instant now = clock.instant();
            for (CreationRuleExclGroup mapping : creationRule.getExcludedGroupMappings()) {
                final EndDeviceGroup group = mapping.getEndDeviceGroup();
                if (group.isMember(endDevice, now)) {
                    LOG.info("Issue creation for device " + endDevice.getName() + " for rule '"
                            + creationRule.getName()
                            + "' is restricted because the device is in the Issue Creation Rule's excluded device group(s)");
                    return true;
                }
            }
        }
        return false;
    }

    private void createNewIssue(CreationRule firedRule, IssueEvent event, CreationRuleTemplate template) {
        LOG.fine("Processing create issue based on event:" + event);
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
        baseIssue.setType(firedRule.getIssueType());
        event.getEndDevice().ifPresent(baseIssue::setDevice);
        event.getUsagePoint().ifPresent(baseIssue::setUsagePoint);
        baseIssue.save();
        baseIssue.addComment(firedRule.getComment(), batchUser.orElse(null));
        OpenIssue newIssue = template.createIssue(baseIssue, event);
        LOG.fine("New issue created:" + newIssue);
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

    private boolean isIssueCreationRestrictedByComTask(CreationRule rule, IssueEvent event) {
        if (event instanceof FiltrableByComTask) {
            if (rule.getTemplate() instanceof AllowsComTaskFiltering) {
                List<HasId> comTasks = (List<HasId>) ((AllowsComTaskFiltering) rule.getTemplate())
                        .getExcludedComTasks(rule.getProperties());
                if (comTasks != null && !comTasks.isEmpty()) {
                    return ((FiltrableByComTask) event).matchesByComTask(comTasks);
                }
            }
        }
        return false;
    }

    @Override
    public void processIssueResolutionEvent(long ruleId, IssueEvent event) {
        LOG.fine("Processing resolution event:" + event);
        findCreationRuleById(ruleId).get().getTemplate().resolveIssue(event);
    }

    @Override
    public void processIssueDiscardPriorityOnResolutionEvent(final long ruleId, final IssueEvent event) {
        final Optional<CreationRule> creationRule = findCreationRuleById(ruleId);
        creationRule.ifPresent(rule -> {
            rule.getTemplate().issueSetPriorityValueToDefault(event, rule.getPriority());
        });
    }

    @Override
    public void closeAllOpenIssuesResolutionEvent(long ruleId, IssueEvent event) throws OperationNotSupportedException {
        LOG.fine("Processing close all:" + event);
        findCreationRuleById(ruleId).get().getTemplate().closeAllOpenIssues(event);
    }

    @Override
    public boolean reReadRules() {
        this.ruleRefreshState.set(RuleRefreshState.REQUIRED);
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

    private KnowledgeBase createRefreshedKnowledgeBase() {
        KnowledgeBase knowledgeBase = null;
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
        }
        return knowledgeBase;
    }


    boolean createKnowledgeBase() {
        if (knowledgeBase == null) {
            knowledgeBase = createRefreshedKnowledgeBase();
        } else if (RuleRefreshState.REQUIRED.equals(ruleRefreshState.get()) && ruleRefreshState.compareAndSet(RuleRefreshState.REQUIRED, RuleRefreshState.IN_PROGRESS)) {
            KnowledgeBase refreshedKnowledgeBase = createRefreshedKnowledgeBase();
            ruleRefreshState.compareAndSet(RuleRefreshState.IN_PROGRESS, RuleRefreshState.READY);
            if (refreshedKnowledgeBase != null && ruleRefreshState.compareAndSet(RuleRefreshState.READY, RuleRefreshState.DONE)) {
                knowledgeBase = refreshedKnowledgeBase;
                return true;
            }
            return false;
        }
        return knowledgeBase != null;
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
