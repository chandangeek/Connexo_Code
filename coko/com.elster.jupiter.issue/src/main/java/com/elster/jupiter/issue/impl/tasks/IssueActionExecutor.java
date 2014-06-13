package com.elster.jupiter.issue.impl.tasks;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.cep.IssueAction;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.nls.Thesaurus;
import com.google.common.base.Optional;

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

    private Map<String, String> getActionParameters (CreationRuleAction ruleAction) {
        Map<String, String> parametersMap = new HashMap<>();
        List<ActionParameter> actionParameters = ruleAction.getParameters();
        if (actionParameters.size() == 0){
            return parametersMap;
        }
        for (ActionParameter actionParameter : actionParameters) {
            parametersMap.put(actionParameter.getKey(), actionParameter.getValue());
        }
        return parametersMap;
    }

    private void executeAction(CreationRuleAction action) {
        Optional<IssueActionType> actionTypeRef = issueActionService.findActionType(action.getType().getId());
        if (!actionTypeRef.isPresent()) {
            throw new IllegalArgumentException("Rule action type doesn't exist");
        }
        IssueAction realAction = actionTypeRef.get().createIssueAction();
        try {
            realAction.execute(issue, getActionParameters(action));
        } catch (RuntimeException e){
            MessageSeeds.ISSUE_ACTION_FAIL.log(LOG, thesaurus, e, action.getId(), issue.getTitle());
        }
    }
}
