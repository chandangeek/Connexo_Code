package com.elster.jupiter.issue.impl.actions;

import com.elster.jupiter.issue.impl.actions.parameters.AssigneeParameter;
import com.elster.jupiter.issue.impl.actions.parameters.IssueCommentParameter;
import com.elster.jupiter.issue.impl.actions.parameters.Parameter;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.cep.AbstractIssueAction;
import com.elster.jupiter.issue.share.cep.IssueActionResult;
import com.elster.jupiter.issue.share.cep.ParameterDefinitionContext;
import com.elster.jupiter.issue.share.cep.ParameterViolation;
import com.elster.jupiter.issue.share.cep.controls.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import static com.elster.jupiter.util.Checks.*;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssignIssueAction extends AbstractIssueAction {
    private IssueService issueService;
    private UserService userService;
    private Thesaurus thesaurus;
    private ThreadPrincipalService threadPrincipalService;

    @Inject
    public AssignIssueAction(Thesaurus thesaurus, IssueService issueService, UserService userService, ThreadPrincipalService threadPrincipalService) {
        this.issueService = issueService;
        this.userService = userService;
        this.thesaurus = thesaurus;
        this.threadPrincipalService = threadPrincipalService;
        initParameterDefinitions();
    }

    @Override
    public IssueActionResult execute(Issue issue, Map<String, String> actionParameters) {
        DefaultActionResult result = new DefaultActionResult();
        if(issue == null || !validate(actionParameters).isEmpty()) {
            result.fail(MessageSeeds.ACTION_INCORRECT_PARAMETERS.getTranslated(thesaurus));
            return result;
        }

        String assigneeType = actionParameters.get(Parameter.ASSIGNEE.getKey());
        if (assigneeType != null){
            IssueAssignee assignee = getIssueAssignee(assigneeType, actionParameters);
            issue.assignTo(assignee);
            issue.save();
            User author = (User) threadPrincipalService.getPrincipal();
            issue.addComment(actionParameters.get(Parameter.COMMENT.getKey()), author);
            result.success(MessageSeeds.ACTION_ISSUE_WAS_ASSIGNED.getTranslated(thesaurus, assignee.getName()));
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

    @Override
    public String getLocalizedName() {
        return MessageSeeds.ACTION_ASSIGN_ISSUE.getTranslated(thesaurus);
    }

    private void initParameterDefinitions() {
        AssigneeParameter assigneeParameter = new AssigneeParameter(issueService, userService, thesaurus);
        parameterDefinitions.put(assigneeParameter.getKey(), assigneeParameter);

        IssueCommentParameter comment = new IssueCommentParameter(thesaurus);
        parameterDefinitions.put(comment.getKey(), comment);
    }

    @Override
    public List<ParameterViolation> validate(Map<String, String> actionParameters) {
        List<ParameterViolation> errors = new ArrayList<>();
        String assigneeType = actionParameters.get(Parameter.ASSIGNEE.getKey());
        if (!is(assigneeType).emptyOrOnlyWhiteSpace()){
            IssueAssignee assignee = getIssueAssignee(assigneeType, actionParameters);
            if (assignee == null){
                errors.add(new ParameterViolation(Parameter.ASSIGNEE.getKey(), MessageSeeds.ACTION_WRONG_ASSIGNEE.getTranslated(thesaurus)));
            }
        } else {
            errors.add(new ParameterViolation(Parameter.ASSIGNEE.getKey(), MessageSeeds.ACTION_WRONG_ASSIGNEE.getTranslated(thesaurus)));
        }
        errors.addAll(getParameterDefinitions().get(Parameter.COMMENT.getKey()).validate(actionParameters.get(Parameter.COMMENT.getKey()), ParameterDefinitionContext.ACTION));
        return errors;
    }

    private IssueAssignee getIssueAssignee(String assigneeType, Map<String, String> actionParameters) {
        IssueAssignee issueAssignee = null;
        String idValue = actionParameters.get(assigneeType);
        long id = 0;
        if (!is(idValue).emptyOrOnlyWhiteSpace()){
            try {
                id = Long.parseLong(idValue);
                issueAssignee = issueService.findIssueAssignee(assigneeType.substring(Parameter.ASSIGNEE.getKey().length()), id);
            } catch (NumberFormatException ex){}
        }
        return issueAssignee;
    }
}
