package com.energyict.mdc.issue.datacollection.impl.actions;


import com.elster.jupiter.issue.share.cep.AbstractIssueAction;
import com.elster.jupiter.issue.share.cep.IssueActionResult;
import com.elster.jupiter.issue.share.cep.controls.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;
import com.energyict.mdc.protocol.api.ConnectionType;

import javax.inject.Inject;
import java.util.Map;

public class RetryCommunicationTaskAction extends AbstractIssueAction {
    private IssueService issueService;
    private Thesaurus thesaurus;

    @Inject
    public RetryCommunicationTaskAction(Thesaurus thesaurus, IssueService issueService) {
        this.issueService = issueService;
        this.thesaurus = thesaurus;
    }

    @Override
    public IssueActionResult execute(Issue issue, Map<String, String> actionParameters) {
        DefaultActionResult result = new DefaultActionResult();
        if (isApplicable(issue)){
            issue.setStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
            issue.save();
            ComTaskExecution comTaskExecution = ((IssueDataCollection) issue).getCommunicationTask().get();
            comTaskExecution.scheduleNow();
            result.success();
        }
        return result;
    }

    @Override
    public <T extends Issue> boolean isApplicable(T issue) {
        if (issue != null && issue instanceof IssueDataCollection){
            IssueDataCollection dcIssue = (IssueDataCollection) issue;
            if (!dcIssue.getStatus().isHistorical() && dcIssue.getConnectionTask().isPresent() && dcIssue.getCommunicationTask().isPresent()){
                ConnectionTask task = dcIssue.getConnectionTask().get();
                return task instanceof ScheduledConnectionTask
                        && task.getConnectionType().getDirection() == ConnectionType.Direction.OUTBOUND
                        && ((ScheduledConnectionTask) task).getConnectionStrategy() == ConnectionStrategy.MINIMIZE_CONNECTIONS;
            }
        }
        return false;
    }

    @Override
    public String getLocalizedName() {
        return MessageSeeds.ACTION_RETRY.getTranslated(thesaurus);
    }
}
