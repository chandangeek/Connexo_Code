package com.elster.jupiter.issue.impl.tasks;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.nls.Thesaurus;

import java.util.Collections;
import java.util.Optional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class IssueActionExecutor implements Runnable {
    public static final Logger LOG = Logger.getLogger(IssueActionExecutor.class.getName());
    private Issue issue;
    private CreationRuleActionPhase phase;
    private Thesaurus thesaurus;
    private IssueActionService issueActionService;

    public IssueActionExecutor(Issue issue, CreationRuleActionPhase phase, Thesaurus thesaurus, IssueActionService issueActionService) {
        if (issue == null){
            throw new IllegalArgumentException("Issue for execution can't be null");
        }
        if (phase == null){
            throw new IllegalArgumentException("Phase for execution can't be null");
        }
        this.issue = issue;
        this.phase = phase;
        this.thesaurus = thesaurus;
        this.issueActionService = issueActionService;
    }

    @Override
    public void run() {
        CreationRule rule = issue.getRule();

        for (CreationRuleAction action : rule.getActions()){
            if (action.getPhase() == phase) {
                executeAction(action);
            }
        }
    }

    private Map<String, Object> getActionProperties(CreationRuleAction ruleAction) {
        Map<String, Object> propertiesMap = new HashMap<>();
        List<CreationRuleActionProperty> properties = ruleAction.getProperties();
        if (properties.isEmpty()){
            return Collections.emptyMap();
        }
        for (CreationRuleActionProperty property : properties) {
            propertiesMap.put(property.getName(), property.getValue());
        }
        return propertiesMap;
    }

    private void executeAction(CreationRuleAction action) {
        Optional<IssueActionType> actionTypeRef = issueActionService.findActionType(action.getAction().getId());
        if (!actionTypeRef.isPresent()) {
            throw new IllegalArgumentException("Rule action type doesn't exist");
        }
        Optional<IssueAction> realAction = actionTypeRef.get().createIssueAction();
        if (realAction.isPresent()) {
            try {
                realAction.get().initAndValidate(getActionProperties(action)).execute(issue);
            } catch (RuntimeException e) {
                MessageSeeds.ISSUE_ACTION_FAIL.log(LOG, thesaurus, e, issue.getTitle());
            }
        }
    }
}
