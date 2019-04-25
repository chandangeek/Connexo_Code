/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.OpenIssue;

import java.util.Optional;

public interface TaskIssueService {

    String COMPONENT_NAME = "ITK";
    String TASK_ISSUE = "task";
    String TASK_ISSUE_PREFIX = "TKI";

    Optional<? extends TaskIssue> findIssue(long id);

    Optional<? extends TaskIssue> findAndLockIssueTaskIssueByIdAndVersion(long id, long version);

    Optional<OpenTaskIssue> findOpenIssue(long id);

    Optional<HistoricalTaskIssue> findHistoricalIssue(long id);

    OpenTaskIssue createIssue(OpenIssue baseIssue, IssueEvent issueEvent);

    <T extends Entity> Query<T> query(Class<T> clazz, Class<?>... eagers);

    Finder<? extends TaskIssue> findIssues(TaskIssueFilter filter, Class<?>... eagers);

}