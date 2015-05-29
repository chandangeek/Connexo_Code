package com.elster.jupiter.issue.impl.actions;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.google.common.collect.ImmutableList;

public class CommentIssueAction extends AbstractIssueAction {
    
    public static final String ISSUE_COMMENT = "commentIssueAction.comment";

    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    public CommentIssueAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, ThreadPrincipalService threadPrincipalService) {
        super(dataModel, thesaurus, propertySpecService);
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        IssueActionResult.DefaultActionResult result = new IssueActionResult.DefaultActionResult();
        User author = (User) threadPrincipalService.getPrincipal();
        String comment = getPropertySpec(ISSUE_COMMENT).getValueFactory().toStringValue(properties.get(ISSUE_COMMENT));
        issue.addComment(comment, author);
        result.success();
        return result;
    }

    @Override
    public boolean isApplicable(Issue issue) {
        return issue != null;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService().stringPropertySpec(ISSUE_COMMENT, true, ""));
        return builder.build();
    }

    @Override
    public String getNameDefaultFormat() {
        return "Comment";
    }

    @Override
    public String getPropertyDefaultFormat(String property) {
        return ISSUE_COMMENT.equals(property) ? "Comment" : null;
    }
}
