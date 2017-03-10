/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.actions;

import com.elster.jupiter.issue.impl.module.TranslationKeys;
import com.elster.jupiter.issue.security.Privileges;
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

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CommentIssueAction extends AbstractIssueAction {

    private static final String NAME = "CommentIssueAction";
    public static final String ISSUE_COMMENT = NAME + ".comment";

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
        getCommentFromParameters(properties).ifPresent(comment -> issue.addComment(comment, author));
        result.success(getThesaurus().getFormat(TranslationKeys.ACTION_ISSUE_COMMENTED).format());
        return result;
    }

    private Optional<String> getCommentFromParameters(Map<String, Object> properties) {
        Object value = properties.get(ISSUE_COMMENT);
        if (value != null) {
            return this.getPropertySpec(ISSUE_COMMENT)
                    .map(PropertySpec::getValueFactory)
                    .map(valueFactory -> valueFactory.toStringValue(value));
        }
        return Optional.empty();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService().stringSpec().named(ISSUE_COMMENT, ISSUE_COMMENT).describedAs(ISSUE_COMMENT).markRequired().finish());
        return builder.build();
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_COMMENT_ISSUE).format();
    }

    @Override
    public boolean isApplicableForUser(User user) {
        return user.getPrivileges().stream().filter(p -> Privileges.Constants.COMMENT_ISSUE.equals(p.getName())).findAny().isPresent();
    }
}