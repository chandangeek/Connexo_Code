package com.energyict.mdc.issue.datacollection.impl.actions;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueActionResult.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;
import com.energyict.mdc.protocol.api.ConnectionType;

public class RetryConnectionTaskAction extends AbstractIssueAction {
    
    private IssueService issueService;

    @Inject
    public RetryConnectionTaskAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        DefaultActionResult result = new DefaultActionResult();
        if (isApplicable(issue)){
            issue.setStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
            issue.save();
            ScheduledConnectionTask task = (ScheduledConnectionTask)((IssueDataCollection) issue).getConnectionTask().get();
            task.scheduleNow();
            result.success(MessageSeeds.ACTION_RETRY_CONNECTION_SUCCESS.getTranslated(getThesaurus()));
        }
        return result;
    }

    @Override
    public boolean isApplicable(Issue issue) {
        if (issue != null && !issue.getStatus().isHistorical() && issue instanceof IssueDataCollection){
            IssueDataCollection dcIssue = (IssueDataCollection) issue;
            if (!dcIssue.getStatus().isHistorical() && dcIssue.getConnectionTask().isPresent() && !dcIssue.getCommunicationTask().isPresent()){
                ConnectionTask<?, ?> task = dcIssue.getConnectionTask().get();
                return task instanceof ScheduledConnectionTask
                        && task.getConnectionType().getDirection() == ConnectionType.Direction.OUTBOUND;
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
        return MessageSeeds.ACTION_RETRY_NOW.getTranslated(getThesaurus());
    }
}
