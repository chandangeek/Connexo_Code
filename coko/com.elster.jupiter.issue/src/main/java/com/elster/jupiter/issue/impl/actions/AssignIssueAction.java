package com.elster.jupiter.issue.impl.actions;
//
//import com.elster.jupiter.issue.impl.actions.parameters.AssigneeParameter;
//import com.elster.jupiter.issue.impl.actions.parameters.IssueCommentParameter;
//import com.elster.jupiter.issue.impl.actions.parameters.Parameter;
//import com.elster.jupiter.issue.impl.module.MessageSeeds;
//import com.elster.jupiter.issue.share.DefaultActionResult;
//import com.elster.jupiter.issue.share.IssueActionResult;
//import com.elster.jupiter.issue.share.cep.AbstractIssueAction;
//import com.elster.jupiter.issue.share.cep.ParameterDefinitionContext;
//import com.elster.jupiter.issue.share.cep.ParameterViolation;
//import com.elster.jupiter.issue.share.entity.Issue;
//import com.elster.jupiter.issue.share.entity.IssueAssignee;
//import com.elster.jupiter.issue.share.service.IssueService;
//import com.elster.jupiter.nls.NlsService;
//import com.elster.jupiter.nls.Thesaurus;
//
//import static com.elster.jupiter.util.Checks.*;
//
//import com.elster.jupiter.security.thread.ThreadPrincipalService;
//import com.elster.jupiter.users.User;
//import com.elster.jupiter.users.UserService;
//
//import javax.inject.Inject;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class AssignIssueAction extends AbstractIssueAction {
//    private IssueService issueService;
//    private UserService userService;
//    private ThreadPrincipalService threadPrincipalService;
//
//    @Inject
//    public AssignIssueAction(NlsService nlsService, Thesaurus thesaurus, IssueService issueService, UserService userService, ThreadPrincipalService threadPrincipalService) {
//        super(nlsService, thesaurus);
//        this.issueService = issueService;
//        this.userService = userService;
//        this.threadPrincipalService = threadPrincipalService;
//        initParameterDefinitions();
//    }
//
//    @Override
//    public IssueActionResult execute(Issue issue, Map<String, String> actionParameters) {
//        validateParametersOrThrowException(actionParameters);
//
//        DefaultActionResult result = new DefaultActionResult();
//        String assigneeType = actionParameters.get(Parameter.ASSIGNEE.getKey());
//        if (assigneeType != null) {
//            IssueAssignee assignee = getIssueAssignee(assigneeType, actionParameters);
//            issue.assignTo(assignee);
//            issue.save();
//            User author = (User) threadPrincipalService.getPrincipal();
//            issue.addComment(actionParameters.get(Parameter.COMMENT.getKey()), author);
//            result.success(MessageSeeds.ACTION_ISSUE_WAS_ASSIGNED.getTranslated(getThesaurus(), assignee.getName()));
//        }
//        return result;
//    }
//
//    @Override
//    public String getLocalizedName() {
//        return MessageSeeds.ACTION_ASSIGN_ISSUE.getTranslated(getThesaurus());
//    }
//
//    private void initParameterDefinitions() {
//        AssigneeParameter assigneeParameter = new AssigneeParameter(issueService, userService, getThesaurus());
//        parameterDefinitions.put(assigneeParameter.getKey(), assigneeParameter);
//
//        IssueCommentParameter comment = new IssueCommentParameter(getThesaurus());
//        parameterDefinitions.put(comment.getKey(), comment);
//    }
//
//    @Override
//    protected List<ParameterViolation> validate(Map<String, String> actionParameters, ParameterDefinitionContext context) {
//        List<ParameterViolation> errors = new ArrayList<>();
//        String assigneeType = actionParameters.get(Parameter.ASSIGNEE.getKey());
//        if (!is(assigneeType).emptyOrOnlyWhiteSpace()){
//            IssueAssignee assignee = getIssueAssignee(assigneeType, actionParameters);
//            if (assignee == null){
//                errors.add(new ParameterViolation(context.wrapKey(Parameter.ASSIGNEE.getKey()), MessageSeeds.ACTION_WRONG_ASSIGNEE.getTranslated(getThesaurus())));
//            }
//        } else {
//            errors.add(new ParameterViolation(context.wrapKey(Parameter.ASSIGNEE.getKey()), MessageSeeds.ACTION_WRONG_ASSIGNEE.getTranslated(getThesaurus())));
//        }
//        errors.addAll(getParameterDefinitions().get(Parameter.COMMENT.getKey()).validate(actionParameters.get(Parameter.COMMENT.getKey()), context));
//        return errors;
//    }
//
//    private IssueAssignee getIssueAssignee(String assigneeType, Map<String, String> actionParameters) {
//        IssueAssignee issueAssignee = null;
//        String idValue = actionParameters.get(assigneeType);
//        long id = 0;
//        if (!is(idValue).emptyOrOnlyWhiteSpace()){
//            try {
//                id = Long.parseLong(idValue);
//                issueAssignee = issueService.findIssueAssignee(assigneeType.substring(Parameter.ASSIGNEE.getKey().length()), id);
//            } catch (NumberFormatException ex){}
//        }
//        return issueAssignee;
//    }
//}
