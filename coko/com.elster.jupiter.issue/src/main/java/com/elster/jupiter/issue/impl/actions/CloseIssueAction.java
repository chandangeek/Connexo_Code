package com.elster.jupiter.issue.impl.actions;

import com.elster.jupiter.issue.impl.actions.parameters.CloseStatusParameter;
import com.elster.jupiter.issue.impl.actions.parameters.IssueCommentParameter;
import com.elster.jupiter.issue.impl.actions.parameters.Parameter;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.cep.AbstractIssueAction;
import com.elster.jupiter.issue.share.cep.IssueActionResult;
import com.elster.jupiter.issue.share.cep.controls.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;
import java.util.Map;

public class CloseIssueAction extends AbstractIssueAction {
    private IssueService issueService;
    private Thesaurus thesaurus;

    @Inject
    public CloseIssueAction(Thesaurus thesaurus, IssueService issueService) {
        this.issueService = issueService;
        this.thesaurus = thesaurus;
        initParameterDefinitions();
    }

    @Override
    public IssueActionResult execute(Issue issue, Map<String, String> actionParameters) {
        DefaultActionResult result = new DefaultActionResult();
        if(issue == null || !validate(actionParameters).isEmpty()) {
            result.fail(MessageSeeds.ACTION_INCORRECT_PARAMETERS.getTranslated(thesaurus));
            return result;
        }

        IssueStatus closeStatus = getStatusFromParameters(actionParameters);
        if (closeStatus == null) {
            result.fail(MessageSeeds.ACTION_WRONG_STATUS.getTranslated(thesaurus));
            return result;
        }
        if (isApplicable(issue)){
            ((OpenIssue)issue).close(closeStatus);
            issue.save();
            result.success();
        } else {
            result.fail(MessageSeeds.ACTION_ISSUE_ALREADY_CLOSED.getTranslated(thesaurus));
        }
        return result;
    }

    @Override
    public <T extends Issue> boolean isApplicable(T issue) {
        if (issue != null){
            return !issue.getStatus().isHistorical();
        }
        return false;
    }

    private IssueStatus getStatusFromParameters(Map<String, String> actionParameters){
        String statusKey = actionParameters.get(Parameter.CLOSE_STATUS.getKey());
        return issueService.findStatus(statusKey).orNull();
    }

    @Override
    public String getLocalizedName() {
        return MessageSeeds.ACTION_CLOSE_ISSUE.getTranslated(thesaurus);
    }

    private void initParameterDefinitions() {
        CloseStatusParameter closeStatus = new CloseStatusParameter(issueService, thesaurus);
        parameterDefinitions.put(closeStatus.getKey(), closeStatus);

        IssueCommentParameter comment = new IssueCommentParameter(thesaurus);
        parameterDefinitions.put(comment.getKey(), comment);
    }
}
