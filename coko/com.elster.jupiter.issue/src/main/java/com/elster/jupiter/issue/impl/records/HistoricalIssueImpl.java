package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.time.Clock;

public final class HistoricalIssueImpl extends IssueImpl implements HistoricalIssue {

    @Inject
    public HistoricalIssueImpl(DataModel dataModel, IssueService issueService, Clock clock) {
        super(dataModel, issueService, clock);
    }

    void copy(OpenIssue issue) {
        this.setId(issue.getId());
        if (issue.getDueDate() != null) {
            this.setDueDate(issue.getDueDate());
        }
        this.setReason(issue.getReason());
        this.setStatus(issue.getStatus());
        this.setDevice(issue.getDevice());
        this.setRule(issue.getRule());
        this.setPriority(issue.getPriority());
        this.assignTo(issue.getAssignee());
    }

    @Override
    public void delete() {
        this.getIssueService().getIssueProviders().stream().forEach(provider -> provider.getHistoricalIssue(this).ifPresent(Entity::delete));
        this.getDataModel().remove(this);
    }
}
