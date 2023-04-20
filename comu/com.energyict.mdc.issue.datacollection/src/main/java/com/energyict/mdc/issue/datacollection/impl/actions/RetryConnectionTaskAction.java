/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.actions;

import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueActionResult.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.i18n.TranslationKeys;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class RetryConnectionTaskAction extends AbstractIssueAction {

    private IssueService issueService;
    private final ConnectionTaskService connectionTaskService;

    @Inject
    public RetryConnectionTaskAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService, ConnectionTaskService connectionTaskService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
        this.connectionTaskService = connectionTaskService;

    }

    @Override
    public IssueActionResult execute(Issue issue) {
        DefaultActionResult result = new DefaultActionResult();
        if (isApplicable(issue)) {
            issue.setStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
            issue.update();
            ScheduledConnectionTask task = (ScheduledConnectionTask) ((IssueDataCollection) issue).getConnectionTask().get();
            connectionTaskService.findAndLockConnectionTaskById(task.getId()).ifPresent(connectionTask -> ((ScheduledConnectionTask) connectionTask).scheduleNow());
            result.success(getThesaurus().getFormat(TranslationKeys.ACTION_RETRY_CONNECTION_SUCCESS).format());
        }
        return result;
    }

    @Override
    public boolean isApplicable(Issue issue) {
        if (issue != null && !issue.getStatus().isHistorical() && issue instanceof IssueDataCollection) {
            IssueDataCollection dcIssue = (IssueDataCollection) issue;
            if (!dcIssue.getStatus().isHistorical() && dcIssue.getConnectionTask().isPresent() && !dcIssue.getCommunicationTask().isPresent()) {
                ConnectionTask<?, ?> task = dcIssue.getConnectionTask().get();
                return task instanceof ScheduledConnectionTask
                        && task.getConnectionType().getDirection() == ConnectionType.ConnectionTypeDirection.OUTBOUND;
            }
        }
        return false;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_RETRY_NOW).format();
    }
}
