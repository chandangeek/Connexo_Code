/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.properties.HasDynamicPropertiesWithValues;
import com.elster.jupiter.users.User;

import java.util.Map;

@ProviderType
public interface IssueAction extends HasDynamicPropertiesWithValues {

    String getDisplayName();

    boolean isApplicable(Issue issue);

    boolean isApplicable(String reasonName);

    boolean isApplicableForUser(User user);

    IssueAction initAndValidate(Map<String, Object> properties);

    IssueActionResult execute(Issue issue);

    default IssueAction setIssue(Issue issue) {
        return this;
    }

    default IssueAction setReasonKey(String reasonKey) {
        return this;
    }

    default IssueAction setIssueType(IssueType issueType) {
        return this;
    }

    default IssueAction setIssueReason(IssueReason issueReason) {
        return this;
    }

    long getActionType();

    default String getFormattedProperties(Map<String, Object> properties) {
        return "";
    }
}
