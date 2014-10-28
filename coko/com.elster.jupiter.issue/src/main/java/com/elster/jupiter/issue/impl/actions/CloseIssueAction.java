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
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;

import java.util.Map;

public class CloseIssueAction extends AbstractIssueAction {
    private IssueService issueService;

    @Inject
    public CloseIssueAction(NlsService nlsService, Thesaurus thesaurus, IssueService issueService) {
        super(nlsService, thesaurus);
        this.issueService = issueService;
        initParameterDefinitions();
    }

    @Override
    public IssueActionResult execute(Issue issue, Map<String, String> actionParameters) {
        validateParametersOrThrowException(actionParameters);
        
        DefaultActionResult result = new DefaultActionResult();

        IssueStatus closeStatus = getStatusFromParameters(actionParameters);
        if (closeStatus == null) {
            result.fail(MessageSeeds.ACTION_WRONG_STATUS.getTranslated(getThesaurus()));
            return result;
        }
        if (isApplicable(issue)){
            ((OpenIssue)issue).close(closeStatus);
            result.success(MessageSeeds.ACTION_ISSUE_WAS_CLOSED.getTranslated(getThesaurus()));
        } else {
            result.fail(MessageSeeds.ACTION_ISSUE_ALREADY_CLOSED.getTranslated(getThesaurus()));
        }
        return result;
    }

    @Override
    public <T extends Issue> boolean isApplicable(T issue) {
        if (issue != null){
            return IssueStatus.OPEN.equals(issue.getStatus().getKey());
        }
        return false;
    }

    private IssueStatus getStatusFromParameters(Map<String, String> actionParameters){
        String statusKey = actionParameters.get(Parameter.CLOSE_STATUS.getKey());
        return issueService.findStatus(statusKey).orElse(null);
    }

    @Override
    public String getLocalizedName() {
        return MessageSeeds.ACTION_CLOSE_ISSUE.getTranslated(getThesaurus());
    }

    private void initParameterDefinitions() {
        CloseStatusParameter closeStatus = new CloseStatusParameter(issueService, getThesaurus());
        parameterDefinitions.put(closeStatus.getKey(), closeStatus);

        IssueCommentParameter comment = new IssueCommentParameter(getThesaurus());
        parameterDefinitions.put(comment.getKey(), comment);
    }
}
