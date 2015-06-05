package com.elster.jupiter.issue.impl.actions;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueActionResult.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.CanFindByStringKey;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Where;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class CloseIssueAction extends AbstractIssueAction {
    
    private static final String NAME = "CloseIssueAction";
    public static final String CLOSE_STATUS = NAME + ".status";
    public static final String COMMENT = NAME + ".comment";

    private final PossibleStatuses statuses = new PossibleStatuses();
    
    private final IssueService issueService;
    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    protected CloseIssueAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService, ThreadPrincipalService threadPrincipalService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        DefaultActionResult result = new DefaultActionResult();

        Optional<IssueStatus> closeStatus = getStatusFromParameters(properties);
        if (!closeStatus.isPresent()) {
            result.fail(MessageSeeds.ACTION_WRONG_STATUS.getTranslated(getThesaurus()));
            return result;
        }
        if (isApplicable(issue)) {
            ((OpenIssue) issue).close(closeStatus.get());
            getCommentFromParameters(properties).ifPresent(comment -> {
                issue.addComment(comment, (User)threadPrincipalService.getPrincipal());
            });
            result.success(MessageSeeds.ACTION_ISSUE_WAS_CLOSED.getTranslated(getThesaurus()));
        } else {
            result.fail(MessageSeeds.ACTION_ISSUE_ALREADY_CLOSED.getTranslated(getThesaurus()));
        }
        return result;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService().stringReferencePropertySpec(CLOSE_STATUS, true, statuses, statuses.getStatuses()));
        builder.add(getPropertySpecService().stringPropertySpec(COMMENT, false, null));
        return builder.build();
    }
    
    @Override
    public String getDisplayName() {
        return MessageSeeds.ACTION_CLOSE_ISSUE.getTranslated(getThesaurus());
    }

    @Override
    public boolean isApplicable(Issue issue) {
        return super.isApplicable(issue) && IssueStatus.OPEN.equals(issue.getStatus().getKey());
    }

    private Optional<IssueStatus> getStatusFromParameters(Map<String, Object> properties){
        Object value = properties.get(CLOSE_STATUS);
        if (value != null) {
            @SuppressWarnings("unchecked")
            String statusKey = getPropertySpec(CLOSE_STATUS).getValueFactory().toStringValue(value);
            return issueService.findStatus(statusKey);
        }
        return Optional.empty();
    }
    
    private Optional<String> getCommentFromParameters(Map<String, Object> properties) {
        Object value = properties.get(COMMENT);
        if (value != null) {
            @SuppressWarnings("unchecked")
            String comment = getPropertySpec(COMMENT).getValueFactory().toStringValue(value);
            return Optional.ofNullable(comment);
        }
        return Optional.empty();
    }
    
    class PossibleStatuses implements CanFindByStringKey<Status> {
        
        @Override
        public Optional<Status> find(String key) {
            return issueService.findStatus(key).map(status -> new Status(status));
        }
        
        public Status[] getStatuses() {
            List<IssueStatus> statuses = issueService.query(IssueStatus.class).select(Where.where("isHistorical").isEqualTo(Boolean.TRUE));
            return statuses.stream().map(status -> new Status(status)).toArray(Status[]::new);
        }
        
        @Override
        public Class<Status> valueDomain() {
            return Status.class;
        }
    }
    
    static class Status extends HasIdAndName {
        
        private IssueStatus status;
        
        public Status(IssueStatus status) {
            this.status = status;
        }
        
        @Override
        public Object getId() {
            return status.getKey();
        }
        
        @Override
        public String getName() {
            return status.getName();
        }
    }
}
