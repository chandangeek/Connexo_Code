package com.elster.jupiter.issue.impl.tasks;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.cep.IssueAction;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.nls.Thesaurus;

import java.util.List;
import java.util.logging.Logger;

public class IssueActionExecutor implements Runnable {
    public static final Logger LOG = Logger.getLogger(IssueActionExecutor.class.getName());
    private Issue issue;
    private CreationRuleActionPhase phase;
    private Thesaurus thesaurus;

    public IssueActionExecutor(Issue issue, CreationRuleActionPhase phase, Thesaurus thesaurus) {
        this.issue = issue;
        this.phase = phase;
        this.thesaurus = thesaurus;
    }

    @Override
    public void run() {
        if (issue == null){
            throw new IllegalArgumentException("Issue for execution can't be null");
        }
        if (phase == null){
           throw new IllegalArgumentException("Phase for execution can't be null");
        }
        CreationRule rule = issue.getRule();
        ClassLoader currentCl = getClass().getClassLoader();
        for (CreationRuleAction action : rule.getActions()){
            if (action.getPhase() != phase){
                continue;
            }
            try {
                IssueAction realAction = IssueAction.class.cast(currentCl.loadClass(action.getType().getClassName()).newInstance());
                //realAction.setIssue(issue);
                setActionParameters(realAction, action);
                try {
                    realAction.execute(issue, null);
                } catch (RuntimeException e){
                    MessageSeeds.ISSUE_ACTION_FAIL.log(LOG, thesaurus, e, action.getId(), issue.getTitle());
                }
            } catch (ReflectiveOperationException e) {
                MessageSeeds.ISSUE_ACTION_CLASS_LOAD_FAIL.log(LOG, thesaurus, e, action.getId(), issue.getTitle());
            }
        }
    }

    private void setActionParameters(IssueAction action, CreationRuleAction ruleAction){
        List<ActionParameter> actionParameters = ruleAction.getParameters();
        if (actionParameters.size() == 0){
            return;
        }
        for (ActionParameter actionParameter : actionParameters) {
            // TODO set parameters
        }
    }
}
