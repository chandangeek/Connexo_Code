package com.energyict.mdc.issue.datavalidation.impl.entity;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.User;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.NotEstimatedBlock;

public class IssueDataValidationImpl implements IssueDataValidation {
    
    public enum Fields {
        BASEISSUE("baseIssue"),
        NOTESTIMATEDBLOCKS("notEstimatedBlocks"),
        ;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private Reference<Issue> baseIssue = ValueReference.absent();

    // Audit fields
    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    private final DataModel dataModel;
    private final IssueDataValidationService issueDataValidationService;    
    
    @Inject
    public IssueDataValidationImpl(DataModel dataModel, IssueDataValidationService issueDataValidationService) {
        this.dataModel = dataModel;
        this.issueDataValidationService = issueDataValidationService;
    }
    
    Issue getBaseIssue() {
        return baseIssue.orNull();
    }

    @Override
    public String getTitle() {
        return getBaseIssue().getTitle();
    }

    @Override
    public IssueReason getReason() {
        return getBaseIssue().getReason();
    }

    @Override
    public IssueStatus getStatus() {
        return getBaseIssue().getStatus();
    }

    @Override
    public IssueAssignee getAssignee() {
        return getBaseIssue().getAssignee();
    }

    @Override
    public EndDevice getDevice() {
        return getBaseIssue().getDevice();
    }

    @Override
    public Optional<UsagePoint> getUsagePoint() {
        return getBaseIssue().getUsagePoint();
    }

    @Override
    public Instant getDueDate() {
        return getBaseIssue().getDueDate();
    }

    @Override
    public boolean isOverdue() {
        return getBaseIssue().isOverdue();
    }

    @Override
    public CreationRule getRule() {
        return getBaseIssue().getRule();
    }

    @Override
    public void setReason(IssueReason reason) {
        getBaseIssue().setReason(reason);
    }

    @Override
    public void setStatus(IssueStatus status) {
        getBaseIssue().setStatus(status);
    }

    @Override
    public void setDevice(EndDevice device) {
        getBaseIssue().setDevice(device);
    }

    @Override
    public void setDueDate(Instant dueDate) {
        getBaseIssue().setDueDate(dueDate);
    }

    @Override
    public void setOverdue(boolean overdue) {
        getBaseIssue().setOverdue(overdue);
    }

    @Override
    public void setRule(CreationRule rule) {
        getBaseIssue().setRule(rule);
    }

    @Override
    public Optional<IssueComment> addComment(String body, User author) {
        return getBaseIssue().addComment(body, author);
    }

    @Override
    public void assignTo(String type, long id) {
        getBaseIssue().assignTo(type, id);
    }

    @Override
    public void assignTo(IssueAssignee assignee) {
        getBaseIssue().assignTo(assignee);
    }

    @Override
    public void autoAssign() {
        getBaseIssue().autoAssign();
    }

    @Override
    public long getId() {
        return getBaseIssue().getId();
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    public void save() {
        getBaseIssue().save();
        if (this.createTime == null) {
            Save.CREATE.save(dataModel, this);
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }

    public void delete(){
        dataModel.remove(this);
    }
    
    @Override
    public List<NotEstimatedBlock> getNotEstimatedBlocks() {
        Optional<? extends IssueDataValidation> issue = null;
        if (getStatus().isHistorical()) {
            issue = issueDataValidationService.findHistoricalIssue(getId());
        } else {
            issue = issueDataValidationService.findIssue(getId());
        }
        return issue.map(IssueDataValidation::getNotEstimatedBlocks).orElse(Collections.emptyList());
    }

    protected DataModel getDataModel() {
        return dataModel;
    }
}
