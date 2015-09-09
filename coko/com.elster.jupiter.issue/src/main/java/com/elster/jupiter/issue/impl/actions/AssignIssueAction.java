package com.elster.jupiter.issue.impl.actions;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.module.TranslationKeys;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueActionResult.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.AssigneeType;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.CanFindByStringKey;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AssignIssueAction extends AbstractIssueAction {

    private static final String NAME = "AssignIssueAction";
    public static final String ASSIGNEE = NAME + ".assignee";
    public static final String COMMENT = NAME + ".comment";

    private final PossibleAssignees assignees = new PossibleAssignees();

    private IssueService issueService;
    private UserService userService;
    private ThreadPrincipalService threadPrincipalService;

    @Inject
    public AssignIssueAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService, UserService userService, ThreadPrincipalService threadPrincipalService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
        this.userService = userService;
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        DefaultActionResult result = new DefaultActionResult();
        IssueAssignee assignee = getAssigneeFromParameters(properties).get();
        issue.assignTo(assignee);
        issue.save();
        getCommentFromParameters(properties).ifPresent(comment -> {
            issue.addComment(comment, (User)threadPrincipalService.getPrincipal());
        });
        result.success(getThesaurus().getFormat(MessageSeeds.ACTION_ISSUE_WAS_ASSIGNED).format(assignee.getName()));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Optional<IssueAssignee> getAssigneeFromParameters(Map<String, Object> properties) {
        Object value = properties.get(ASSIGNEE);
        if (value != null) {
            String assigneeId = getPropertySpec(ASSIGNEE).getValueFactory().toStringValue(value);
            return issueService.findIssueAssignee(AssigneeType.USER, Long.valueOf(assigneeId).longValue());
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

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        builder.add(getPropertySpecService().stringReferencePropertySpec(ASSIGNEE, true, assignees, assignees.getPossibleAssignees()));
        builder.add(getPropertySpecService().stringPropertySpec(COMMENT, false, null));
        return builder.build();
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_ASSIGN_ISSUE).format();
    }

    class PossibleAssignees implements CanFindByStringKey<Assignee> {

        @Override
        public Optional<Assignee> find(String key) {
            return userService.getUser(Long.valueOf(key).longValue()).map(user -> new Assignee(user));
        }

        public Assignee[] getPossibleAssignees() {
            return userService.getUserQuery().select(Condition.TRUE, Order.ascending("authenticationName"))
                                .stream().map(Assignee::new).toArray(Assignee[]::new);
        }

        @Override
        public Class<Assignee> valueDomain() {
            return Assignee.class;
        }
    }

    static class Assignee extends HasIdAndName {

        private User user;

        public Assignee(User user) {
            this.user = user;
        }

        @Override
        public Long getId() {
            return user.getId();
        }

        @Override
        public String getName() {
            return user.getName();
        }
    }
}
