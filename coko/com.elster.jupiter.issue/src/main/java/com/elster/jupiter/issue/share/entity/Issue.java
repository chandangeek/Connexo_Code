package com.elster.jupiter.issue.share.entity;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.users.User;
import com.google.common.base.Optional;

public interface Issue extends BaseIssue {

    void close(IssueStatus status);

    Optional<IssueComment> addComment(String body, User author);

    void assignTo(String type, long id);

    void assignTo(IssueAssignee assignee);

    void autoAssign();

    Optional<UsagePoint> getUsagePoint();

}
