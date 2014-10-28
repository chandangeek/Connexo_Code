package com.elster.jupiter.issue.impl.actions;

import com.elster.jupiter.issue.impl.actions.parameters.IssueCommentParameter;
import com.elster.jupiter.issue.impl.actions.parameters.Parameter;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.cep.AbstractIssueAction;
import com.elster.jupiter.issue.share.cep.IssueActionResult;
import com.elster.jupiter.issue.share.cep.controls.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;

import javax.inject.Inject;

import java.util.Map;

public class CommentIssueAction extends AbstractIssueAction {
    private ThreadPrincipalService threadPrincipalService;

    @Inject
    public CommentIssueAction(NlsService nlsService, Thesaurus thesaurus, ThreadPrincipalService threadPrincipalService) {
        super(nlsService, thesaurus);
        this.threadPrincipalService = threadPrincipalService;
        initParameterDefinitions();
    }

    @Override
    public IssueActionResult execute(Issue issue, Map<String, String> actionParameters) {
        validateParametersOrThrowException(actionParameters);
        
        DefaultActionResult result = new DefaultActionResult();
        User author = (User) threadPrincipalService.getPrincipal();
        issue.addComment(actionParameters.get(Parameter.COMMENT.getKey()), author);
        result.success();
        return result;
    }

    @Override
    public <T extends Issue> boolean isApplicable(T issue) {
        return issue != null;
    }

    @Override
    public String getLocalizedName() {
        return MessageSeeds.ACTION_COMMENT_ISSUE.getTranslated(getThesaurus());
    }

    private void initParameterDefinitions() {
        IssueCommentParameter comment = new IssueCommentParameter(getThesaurus());
        parameterDefinitions.put(comment.getKey(), comment);
    }
}
