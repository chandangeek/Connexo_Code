package com.elster.jupiter.issue.impl.actions;

import com.elster.jupiter.issue.impl.actions.parameters.IssueCommentParameter;
import com.elster.jupiter.issue.impl.actions.parameters.Parameter;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.cep.AbstractIssueAction;
import com.elster.jupiter.issue.share.cep.IssueActionResult;
import com.elster.jupiter.issue.share.cep.controls.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;

import javax.inject.Inject;
import java.util.Map;

public class CommentIssueAction extends AbstractIssueAction {
    private Thesaurus thesaurus;
    private ThreadPrincipalService threadPrincipalService;

    @Inject
    public CommentIssueAction(Thesaurus thesaurus, ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
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
        return MessageSeeds.ACTION_COMMENT_ISSUE.getTranslated(thesaurus);
    }

    private void initParameterDefinitions() {
        IssueCommentParameter comment = new IssueCommentParameter(thesaurus);
        parameterDefinitions.put(comment.getKey(), comment);
    }
}
