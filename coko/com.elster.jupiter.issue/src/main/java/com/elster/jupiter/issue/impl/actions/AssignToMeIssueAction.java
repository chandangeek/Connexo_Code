package com.elster.jupiter.issue.impl.actions;


import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.module.TranslationKeys;
import com.elster.jupiter.issue.impl.records.IssueAssigneeImpl;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AssignToMeIssueAction extends AbstractIssueAction {

    private static final String NAME = "AssignToMeIssueAction";
    public static final String ASSIGNEE = NAME + ".assignee";

    private IssueService issueService;
    private UserService userService;
    private ThreadPrincipalService threadPrincipalService;

    @Inject
    public AssignToMeIssueAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService, UserService userService, ThreadPrincipalService threadPrincipalService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
        this.userService = userService;
        this.threadPrincipalService = threadPrincipalService;
    }


    @Override
    public IssueActionResult execute(Issue issue) {
        IssueActionResult.DefaultActionResult result = new IssueActionResult.DefaultActionResult();
        IssueAssignee assignee = new IssueAssigneeImpl((User)threadPrincipalService.getPrincipal(),issue.getAssignee().getWorkGroup());
        issue.assignTo(assignee);
        issue.update();
        result.success(getThesaurus().getFormat(MessageSeeds.ACTION_ISSUE_WAS_ASSIGNED).format(issue.getAssignee().getUser().getName()));
        return result;
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_ASSIGN_TO_ME_ISSUE).format();
    }


    @Override
    public boolean isApplicableForUser(User user) {
        return super.isApplicableForUser(user) &&
                user.getPrivileges().stream().filter(p -> Privileges.Constants.ASSIGN_ISSUE.equals(p.getName())).findAny().isPresent();
    }

    @Override
    public boolean isApplicable(Issue issue) {
        return super.isApplicable(issue) && (issue.getAssignee().getUser() == null || issue.getAssignee().getUser().getId() != ((User) this.threadPrincipalService.getPrincipal()).getId());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return new ArrayList<>();
    }

    @Override
    public Optional<PropertySpec> getPropertySpec(String name) {
        return Optional.empty();
    }
}
