package com.energyict.mdc.issue.datacollection.impl.actions;

import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.i18n.TranslationKeys;
import com.energyict.mdc.protocol.api.ConnectionType;

import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueActionResult.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class RetryCommunicationTaskAction extends AbstractIssueAction {

    private final IssueService issueService;

    @Inject
    public RetryCommunicationTaskAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        DefaultActionResult result = new DefaultActionResult();
        if (isApplicable(issue)){
            issue.setStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
            issue.save();
            ComTaskExecution comTaskExecution = ((IssueDataCollection) issue).getCommunicationTask().get();
            comTaskExecution.scheduleNow();
            result.success(getThesaurus().getFormat(TranslationKeys.ACTION_RETRY_COM_TASK_SUCCESS).format());
        }
        return result;
    }

    @Override
    public boolean isApplicable(Issue issue) {
        if (super.isApplicable(issue) && !issue.getStatus().isHistorical() && issue instanceof IssueDataCollection){
            IssueDataCollection dcIssue = (IssueDataCollection) issue;
            if (!dcIssue.getStatus().isHistorical() && dcIssue.getConnectionTask().isPresent() && dcIssue.getCommunicationTask().isPresent()){
                ConnectionTask<?, ?> task = dcIssue.getConnectionTask().get();
                return task instanceof ScheduledConnectionTask
                        && task.getConnectionType().getDirection() == ConnectionType.Direction.OUTBOUND
                        && ((ScheduledConnectionTask) task).getConnectionStrategy() == ConnectionStrategy.MINIMIZE_CONNECTIONS;
            }
        }
        return false;
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_RETRY).format();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }
}
